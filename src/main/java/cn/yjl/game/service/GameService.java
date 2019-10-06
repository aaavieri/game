package cn.yjl.game.service;

import cn.yjl.game.algorithm.AlgIf;
import cn.yjl.game.dto.CardWrapDto;
import cn.yjl.game.dto.GameStateDto;
import cn.yjl.game.dto.OnceSendCardDto;
import cn.yjl.game.dto.UserGameStateDto;
import cn.yjl.game.dto.event.*;
import cn.yjl.game.dto.request.BaseRequestDto;
import cn.yjl.game.dto.request.DoPlayRequestDto;
import cn.yjl.game.event.DataInitCompleteEvent;
import cn.yjl.game.exception.ApplicationException;
import cn.yjl.game.mapper.CardMapper;
import cn.yjl.game.pojo.CardPojo;
import cn.yjl.game.util.AppUtil;
import cn.yjl.game.util.Const;
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

import static cn.yjl.game.enumeration.GameStatusEnum.*;
import static cn.yjl.game.enumeration.UserGameStatusEnum.*;

@Service
public class GameService implements ApplicationListener<DataInitCompleteEvent> {

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private List<AlgIf> algList;

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
                        && state.getStatus().getValue() < COMPLETE.getValue())) {
            throw new ApplicationException().setMessage("用户尚有未完成的游戏").setErrCode(1);
        }
        GameStateDto game = this.findGameToJoin();
        if (game != null && game.getUserList().size() < 3) {
            game.addUser(requestDto.getUserId());
        } else {
            game = new GameStateDto().setGameId(this.gameCounter.incrementAndGet()).addUser(requestDto.getUserId());
            this.gameStateMap.put(game.getGameId(), game);
        }
        game.getUserInfo().values().forEach(user -> user.setStatus(WAITING_SELF_START));
        requestDto.setGameId(game.getGameId());
        if (game.getUserList().size() == 3) {
            game.setStatus(WAITING_START);
        }
        final List<String> userList = game.getUserList();
        this.context.publishEvent(this.getEventData(JoinGameEventDto.class, requestDto, event -> event.setUserList(userList)));
        return game;
    }

    public GameStateDto startGame(BaseRequestDto requestDto) {
        GameStateDto game = this.getGameWithCheck(requestDto);
        this.checkFirstStartGame(game);
        this.processStart(game, requestDto);
        return game;
    }

    public GameStateDto skipLord(BaseRequestDto requestDto) {
        GameStateDto game = this.checkLord(requestDto);
        String nextLordUser = this.getNextUser(requestDto.getUserId(), game);
        game.setLordUser(nextLordUser).getUserInfo().get(requestDto.getUserId()).setStatus(WAITING_OTHER_LORD);
        game.getUserInfo().get(nextLordUser).setStatus(WAITING_SELF_LORD);
        this.context.publishEvent(this.getEventData(SkipLordEventDto.class, requestDto,
                event -> event.setNextLordUser(game.getLordUser())));
        return game;
    }

    public GameStateDto callLord(BaseRequestDto requestDto) {
        GameStateDto game = this.checkLord(requestDto);
        game.setStatus(PLAYING).getUserInfo().values().forEach(user ->
                user.setStatus(user.getUserId().equals(requestDto.getUserId()) ? WAITING_SELF_PLAY : WAITING_OTHER_PLAY));
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
        OnceSendCardDto onceSendCardDto = this.oncePlay(requestDto.getCardList(), game, requestDto.getUserId());
        String nextPlayUser = this.getNextUser(requestDto.getUserId(), game);
        UserGameStateDto userState = game.onceSend(onceSendCardDto).getUserInfo().get(requestDto.getUserId())
                .setStatus(WAITING_OTHER_PLAY);
        game.getUserInfo().get(nextPlayUser).setStatus(WAITING_SELF_PLAY);
        onceSendCardDto.getSentCards().forEach(FuncUtil.andCons(userState.getSentCards()::add, userState.getUnsentCards()::remove));
        if (userState.getUnsentCards().size() == 0) {
            game.setStatus(COMPLETE);
            List<String> winners = game.getUserInfo().values().stream().peek(everyOneState ->
                    everyOneState.setStatus(everyOneState.getUserId().equals(requestDto.getUserId())
                            || (!everyOneState.getUserId().equals(game.getLordUser()) && !userState.getUserId().equals(game.getLordUser()))
                            ? WIN : LOST)).filter(user -> user.getStatus().equals(WIN))
                    .map(UserGameStateDto::getUserId).collect(Collectors.toList());
            this.context.publishEvent(this.getEventData(CompleteGameEventDto.class, requestDto,
                    event -> event.setWinner(winners),
                    event -> event.setSentCard(onceSendCardDto)));
        } else {
            this.context.publishEvent(this.getEventData(DoPlayEventDto.class, requestDto,
                    event -> event.setNextPlayUser(nextPlayUser),
                    event -> event.setSentCard(onceSendCardDto)));
        }
        return game;
    }

    public GameStateDto restart(BaseRequestDto requestDto) {
        GameStateDto game = this.checkRestart(requestDto);
        if (game.getStatus().equals(COMPLETE)) {
            game.reset();
        }
        this.processStart(game, requestDto);
        return game;
    }

    public GameStateDto quitGame(BaseRequestDto requestDto) {
        GameStateDto game = this.checkQuit(requestDto);
        game.removeUser(requestDto.getUserId()).setStatus(NOT_ENOUGH_USER).getUserInfo()
                .forEach((userId, userState) -> userState.setStatus(WAITING_OTHER_JOIN));
        this.context.publishEvent(this.getEventData(QuitGameEventDto.class, requestDto,
                event -> event.setQuitUser(requestDto.getUserId())));
        return game;
    }

    private void checkFirstStartGame(GameStateDto game) {
        if (game.getUserList().size() != 3) {
            throw new ApplicationException().setMessage("当前游戏人员不齐").setErrCode(2);
        }
        if (game.getStatus().equals(WAITING_START)) {
            throw new ApplicationException().setMessage("游戏状态错误").setErrCode(3);
        }
    }

    private void processStart(GameStateDto game, BaseRequestDto request) {
        game.getUserInfo().get(request.getUserId()).setStatus(WAITING_OTHER_START);
        if (game.getUserInfo().values().stream().allMatch(user -> user.getStatus().equals(WAITING_OTHER_START))) {
            this.distributeCard(request);
            game.setStatus(WAITING_LORD);
        }
        this.context.publishEvent(this.getEventData(StartGameEventDto.class, request,
                event -> event.setCardList(game.getUserInfo().get(event.getUserId()).getGameCards()),
                event -> event.setLordUser(game.getLordUser())));
    }

    private GameStateDto findGameToJoin() {
        return this.gameStateMap.values().stream().filter(game -> game.getUserList().size() < 3
                && game.getStatus().equals(NOT_ENOUGH_USER)).findFirst().orElse(null);
    }

    private void distributeCard(BaseRequestDto request) {
        GameStateDto game = this.getGameWithCheck(request);
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

    private GameStateDto getGameWithCheck(BaseRequestDto request) {
        GameStateDto game = this.gameStateMap.get(request.getGameId());
        if (game == null) {
            throw new ApplicationException().setMessage("不存在该局游戏，gameId：" + request.getGameId()).setErrCode(5);
        }
        if (!game.getUserInfo().containsKey(request.getUserId())) {
            throw new ApplicationException().setMessage("用户不在该局游戏中").setErrCode(4);
        }
        return game;
    }

    private GameStateDto checkLord(BaseRequestDto requestDto) {
        GameStateDto game = this.getGameWithCheck(requestDto);
        if (!game.getLordUser().equals(requestDto.getUserId())) {
            throw new ApplicationException().setMessage("当前叫地主顺序还未轮到").setErrCode(6);
        }
        return game;
    }

    private GameStateDto checkPlay(BaseRequestDto requestDto) {
        GameStateDto game = this.getGameWithCheck(requestDto);
        if (game.getUserInfo().values().stream().noneMatch(userState -> userState.getStatus().equals(WAITING_SELF_PLAY)
                && userState.getUserId().equals(requestDto.getUserId()))) {
            throw new ApplicationException().setMessage("当前出牌顺序还未轮到").setErrCode(7);
        }
        return game;
    }

    private GameStateDto checkRestart(BaseRequestDto request) {
        GameStateDto game = this.getGameWithCheck(request);
        if (!game.getStatus().equals(COMPLETE)
                && !game.getStatus().equals(WAITING_START)
                && !game.getStatus().equals(NOT_ENOUGH_USER)) {
            throw new ApplicationException().setMessage("当前游戏还不能重新开始").setErrCode(8);
        }
        return game;
    }

    private GameStateDto checkQuit(BaseRequestDto request) {
        GameStateDto game = this.getGameWithCheck(request);
        if (!game.getStatus().equals(COMPLETE)
                && !game.getStatus().equals(WAITING_START)
                && !game.getStatus().equals(NOT_ENOUGH_USER)) {
            throw new ApplicationException().setMessage("当前游戏还不能退出").setErrCode(8);
        }
        return game;
    }

    private String getNextUser(String userId, GameStateDto game) {
        return game.getUserList().get(game.getUserList().indexOf(userId) + 1 % 3);
    }

    @SafeVarargs
    private final <T extends BaseEventDto> List<T> getEventData(Class<T> clazz, BaseRequestDto requestDto, Function<T, T>... setters) {
        Function<T, T> setter = FuncUtil.andFunc(setters);
        int gameId = requestDto.getGameId();
        return this.gameStateMap.get(gameId).getUserList().stream().map(FuncUtil.<String, T>wrapFunc(userId ->
                AppUtil.autoCast(setter.apply(clazz.newInstance()).setGameId(gameId).setUserId(userId)
                        .setRequestUser(requestDto.getUserId())
                        .setGameStatus(this.gameStateMap.get(gameId).getStatus())
                        .setUserStatus(this.gameStateMap.get(gameId).getUserInfo().get(userId).getStatus())
                        .setGameStatusValue(this.gameStateMap.get(gameId).getStatus().getValue())
                        .setUserStatusValue(this.gameStateMap.get(gameId).getUserInfo().get(userId).getStatus().getValue()))))
                .collect(Collectors.toList());
    }

    private OnceSendCardDto oncePlay(List<Integer> cardIndexList, GameStateDto game, String requestUser) {
        UserGameStateDto userState = game.getUserInfo().get(requestUser);
        List<CardWrapDto> cardList = cardIndexList.stream().map(id -> userState.getUnsentCards().stream()
                .filter(card -> card.getGameIndex() == id).findFirst().orElseThrow(() ->
                        new ApplicationException().setErrCode(8).setMessage("有牌不在未出牌堆中，id:" + id)))
                .collect(Collectors.toList());
        OnceSendCardDto lastOne = game.getSentHistory().isEmpty()
                ? null : game.getSentHistory().get(game.getSentHistory().size() - 1);
        OnceSendCardDto nowOne = this.algList.stream().filter(alg -> lastOne == null || lastOne.getUserId().equals(requestUser)
                || alg.getType() == lastOne.getType() || alg.getPriority() > lastOne.getPriority())
                .map(alg -> alg.generate(cardList, requestUser)).max(Comparator.comparingInt(OnceSendCardDto::getValue))
                .orElseThrow(() -> new ApplicationException().setErrCode(9).setMessage("牌型不正确"));
        if (lastOne != null && lastOne.getUserId().equals(requestUser)) {
            int compareResult = nowOne.compare(lastOne);
            if (compareResult == Const.COMPARE_CARD_CANNOT) {
                throw new ApplicationException().setErrCode(10).setMessage("虽然牌型相同，但无法于上一手出牌比较大小");
            } else if (compareResult < 1) {
                throw new ApplicationException().setErrCode(11).setMessage("不比上一手出牌大，无法出牌");
            }
        }
        return nowOne;
    }
}
