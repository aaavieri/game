package cn.yjl.game.service;

import cn.yjl.game.dto.CardWrapDto;
import cn.yjl.game.dto.GameStateDto;
import cn.yjl.game.dto.OnceSendCardDto;
import cn.yjl.game.dto.UserGameStateDto;
import cn.yjl.game.dto.event.*;
import cn.yjl.game.dto.request.BaseRequestDto;
import cn.yjl.game.dto.request.DoPlayRequestDto;
import cn.yjl.game.event.*;
import cn.yjl.game.exception.ApplicationException;
import cn.yjl.game.mapper.CardMapper;
import cn.yjl.game.pojo.CardPojo;
import cn.yjl.game.util.AppUtil;
import cn.yjl.game.util.FuncUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static cn.yjl.game.enumeration.GameStatusEnum.*;
import static cn.yjl.game.enumeration.UserGameStatusEnum.*;

@Service
public class GameService implements ApplicationListener<DataInitCompleteEvent> {

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private ApplicationContext context;

    private List<CardPojo> cardList;

    private AtomicInteger gameCounter = new AtomicInteger();

    private Map<Integer, GameStateDto> gameStateMap = new HashMap<>();

    @Override
    public void onApplicationEvent(DataInitCompleteEvent dataInitCompleteEvent) {
        this.cardList = this.cardMapper.getAllCards();
    }

    public GameStateDto joinGame(BaseRequestDto requestDto) throws ApplicationException {
        if (this.gameStateMap.values().stream().anyMatch(
                state -> state.getUserInfo().containsKey(requestDto.getUserId())
                        && state.getStatusEnum().getValue() < COMPLETE.getValue())) {
            throw new ApplicationException().setMessage("用户尚有未完成的游戏").setErrCode(1);
        }
        GameStateDto game = this.gameStateMap.get(gameCounter.get());
        if (game != null && game.getUserList().size() < 3) {
            game.addUser(requestDto.getUserId());
        } else {
            game = new GameStateDto().setGameId(this.gameCounter.incrementAndGet()).addUser(requestDto.getUserId());
            this.gameStateMap.put(game.getGameId(), game);
        }
        game.getUserInfo().values().forEach(user -> user.setStatus(WAITING_SELF_START));
        requestDto.setGameId(game.getGameId());
        if (game.getUserList().size() == 3) {
            game.setStatusEnum(WAITING_START);
            final List<String> userList = game.getUserList();
            this.context.publishEvent(this.getEventData(JoinGameEventDto.class, requestDto, event -> event.setUserList(userList)));
        }
        return game;
    }

    public GameStateDto startGame(BaseRequestDto requestDto) {
        GameStateDto game = this.gameStateMap.get(requestDto.getGameId());
        if (game.getUserList().size() != 3) {
            throw new ApplicationException().setMessage("当前游戏人员不齐").setErrCode(2);
        }
        if (game.getStatusEnum().equals(WAITING_START)) {
            throw new ApplicationException().setMessage("游戏状态错误").setErrCode(3);
        }
        if (!game.getUserInfo().containsKey(requestDto.getUserId())) {
            throw new ApplicationException().setMessage("用户不在该局游戏中").setErrCode(4);
        }
        game.getUserInfo().get(requestDto.getUserId()).setStatus(WAITING_OTHER_START);
        this.distributeCard(game.getGameId());
        if (game.getUserInfo().values().stream().allMatch(user -> user.getStatus().equals(WAITING_OTHER_START))) {
            game.setStatusEnum(WAITING_LORD);
            this.context.publishEvent(this.getEventData(StartGameEventDto.class, requestDto,
                event -> event.setCardList(game.getUserInfo().get(event.getUserId()).getGameCards()),
                event -> event.setLordUser(game.getLordUser())));
        }
//        this.publishGameStatus(game, requestDto.getUserId());
        return game;
    }

    public GameStateDto skipLord(BaseRequestDto requestDto) {
        GameStateDto game = this.checkLord(requestDto);
        String nextLordUser = this.getNextUser(requestDto.getUserId(), game);
        game.setLordUser(nextLordUser).getUserInfo().get(requestDto.getUserId()).setStatus(WAITING_OTHER_LORD);
        game.getUserInfo().get(nextLordUser).setStatus(WAITING_SELF_LORD);
        this.context.publishEvent(this.getEventData(SkipLordEventDto.class, requestDto,
            event -> event.setNextLordUser(game.getLordUser())));
//        this.publishGameStatus(game, requestDto.getUserId());
        return game;
    }

    public GameStateDto callLord(BaseRequestDto requestDto) {
        GameStateDto game = this.checkLord(requestDto);
        game.getUserInfo().values().forEach(user -> user.setStatus(user.getUserId().equals(requestDto.getUserId())
                ? WAITING_SELF_PLAY : WAITING_OTHER_PLAY));
//        this.publishGameStatus(game, requestDto.getUserId());
        this.context.publishEvent(this.getEventData(CallLordEventDto.class, requestDto,
            event -> event.setLordUser(game.getLordUser())));
        return game;
    }

    public GameStateDto skipPlay(BaseRequestDto requestDto) {
        GameStateDto game = this.checkPlay(requestDto);
        String nextPlayUser = this.getNextUser(requestDto.getUserId(), game);
        game.getUserInfo().get(requestDto.getUserId()).setStatus(WAITING_OTHER_PLAY);
        game.getUserInfo().get(nextPlayUser).setStatus(WAITING_SELF_PLAY);
        this.context.publishEvent(this.getEventData(SkipPlayEventDto.class, requestDto,
            event -> event.setNextPlayUser(nextPlayUser)));
        return game;
    }
    
    public GameStateDto doPlay(DoPlayRequestDto requestDto) {
        GameStateDto game = this.checkPlay(requestDto);
        OnceSendCardDto onceSendCardDto = this.oncePlay(requestDto.getCardList(), game);
        String nextPlayUser = this.getNextUser(requestDto.getUserId(), game);
        UserGameStateDto userState = game.getUserInfo().get(requestDto.getUserId());
        onceSendCardDto.getSentCards().forEach(FuncUtil.andCons(userState.getSentCards()::add, userState.getUnsentCards()::remove));
        game.onceSend(onceSendCardDto).getUserInfo().get(requestDto.getUserId()).setStatus(WAITING_OTHER_PLAY);
        game.getUserInfo().get(nextPlayUser).setStatus(WAITING_SELF_PLAY);
        this.context.publishEvent(this.getEventData(DoPlayEventDto.class, requestDto,
            event -> event.setNextPlayUser(nextPlayUser),
            event -> event.setSentCard(onceSendCardDto)));
        return game;
    }
    
    private void distributeCard(int gameId) {
        GameStateDto game = this.getGameWithCheck(gameId);
        Random random = new Random();
        List<CardWrapDto> cardWrapList = this.cardList.stream().map(cardPojo -> new CardWrapDto().setCardPojo(cardPojo)
            .setGameIndex(random.nextInt(1000)))
            .sorted(Comparator.comparingInt(CardWrapDto::getGameIndex)).collect(Collectors.toList());
        game.setLordUser(game.getUserList().get(random.nextInt(3)))
            .setLordCardList(cardWrapList.subList(cardWrapList.size() - 3, cardWrapList.size()));
        IntStream.range(0, cardWrapList.size() - 3).boxed()
            .collect(Collectors.groupingBy(index -> game.getUserList().get(index % 3)))
            .forEach((userId, cardIndexList) -> {
                List<CardWrapDto> userCards = cardIndexList.stream().map(cardWrapList::get).collect(Collectors.toList());
                game.getUserInfo().get(userId).setGameCards(new ArrayList<>(userCards));
                game.getUserInfo().get(userId).setUnsentCards(new ArrayList<>(userCards));
            });
    }
    
    @SafeVarargs
    private final <T extends BaseEventDto> List<T> getEventData(Class<T> clazz, BaseRequestDto requestDto, Function<T, T>... setters) {
        Function<T, T> setter = FuncUtil.andFunc(setters);
        int gameId = requestDto.getGameId();
        return this.gameStateMap.get(gameId).getUserList().stream().map(FuncUtil.<String, T>wrapFunc(userId ->
                AppUtil.autoCast(setter.apply(clazz.newInstance()).setGameId(gameId).setUserId(userId)
                    .setRequestUser(requestDto.getUserId())
                    .setGameStatus(this.gameStateMap.get(gameId).getStatusEnum())
                    .setUserStatus(this.gameStateMap.get(gameId).getUserInfo().get(userId).getStatus())
                    .setGameStatusValue(this.gameStateMap.get(gameId).getStatusEnum().getValue())
                    .setUserStatusValue(this.gameStateMap.get(gameId).getUserInfo().get(userId).getStatus().getValue()))))
                .collect(Collectors.toList());
    }

    private GameStateDto getGameWithCheck(int gameId) {
        if (this.gameStateMap.containsKey(gameId)) {
            throw new ApplicationException().setMessage("不存在该局游戏，gameId：" + gameId).setErrCode(5);
        }
        return this.gameStateMap.get(gameId);
    }

    private GameStateDto checkLord(BaseRequestDto requestDto) {
        GameStateDto game = this.getGameWithCheck(requestDto.getGameId());
        if (!game.getLordUser().equals(requestDto.getUserId())) {
            throw new ApplicationException().setMessage("当前叫地主顺序还未轮到").setErrCode(6);
        }
        return game;
    }

    private GameStateDto checkPlay(BaseRequestDto requestDto) {
        GameStateDto game = this.getGameWithCheck(requestDto.getGameId());
        if (game.getUserInfo().values().stream().noneMatch(userState -> userState.getStatus().equals(WAITING_SELF_PLAY)
                && userState.getUserId().equals(requestDto.getUserId()))) {
            throw new ApplicationException().setMessage("当前出牌顺序还未轮到").setErrCode(7);
        }
        return game;
    }
    
    private String getNextUser(String userId, GameStateDto game) {
        return game.getUserList().get(game.getUserList().indexOf(userId) + 1 % 3);
    }
    
    private OnceSendCardDto oncePlay(List<Integer> cardList, GameStateDto game) {
        // TODO
        return new OnceSendCardDto();
    }
}
