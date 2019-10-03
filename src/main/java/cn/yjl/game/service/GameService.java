package cn.yjl.game.service;

import cn.yjl.game.dto.CardWrapDto;
import cn.yjl.game.dto.GameStateDto;
import cn.yjl.game.dto.event.BaseEventDto;
import cn.yjl.game.dto.request.BaseRequestDto;
import cn.yjl.game.event.*;
import cn.yjl.game.exception.ApplicationException;
import cn.yjl.game.mapper.CardMapper;
import cn.yjl.game.pojo.CardPojo;
import cn.yjl.game.util.AppUtil;
import cn.yjl.game.util.ExUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
        this.publishGameStatus(game, requestDto.getUserId());
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
        this.publishGameStatus(game, requestDto.getUserId());
        return game;
    }

    public GameStateDto distributeCard(int gameId) {
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
        return game;
    }

    public GameStateDto skipLord(BaseRequestDto requestDto) {
        GameStateDto game = this.checkLord(requestDto);
        String nextLordUser = game.getUserList().get(game.getUserList().indexOf(requestDto.getUserId()) + 1 % 3);
        game.setLordUser(nextLordUser).getUserInfo().get(requestDto.getUserId()).setStatus(WAITING_OTHER_LORD);
        game.getUserInfo().get(nextLordUser).setStatus(WAITING_SELF_LORD);
        this.publishGameStatus(game, requestDto.getUserId());
        return game;
    }

    public GameStateDto callLord(BaseRequestDto requestDto) {
        GameStateDto game = this.checkLord(requestDto);
        game.getUserInfo().values().forEach(user -> user.setStatus(user.getUserId().equals(requestDto.getUserId())
                ? WAITING_SELF_PLAY : WAITING_OTHER_PLAY));
        this.publishGameStatus(game, requestDto.getUserId());
        return game;
    }

    public GameStateDto skipPlay(BaseRequestDto requestDto) {
        GameStateDto game = this.checkPlay(requestDto);
        String nextPlayUser = game.getUserList().get(game.getUserList().indexOf(requestDto.getUserId()) + 1 % 3);
        game.getUserInfo().get(requestDto.getUserId()).setStatus(WAITING_OTHER_PLAY);
        game.getUserInfo().get(nextPlayUser).setStatus(WAITING_SELF_PLAY);
        this.publishGameStatus(game, requestDto.getUserId());
        return game;
    }

    public <T extends BaseEventDto> List<T> getEventData(Class<T> clazz, BaseGameEvent event) {
        int gameId = event.getGameId();
        return this.gameStateMap.get(gameId).getUserList().stream().map(ExUtil.<String, T>wrapFunc(userId ->
                AppUtil.autoCast(clazz.newInstance().setGameId(gameId).setUserId(userId).setRequestUser(event.getRequestUser())
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

    private void publishGameStatus(GameStateDto game, String requestUser) {
        switch (game.getStatusEnum()) {
            case NOT_ENOUGH_USER:
                if (game.getUserList().size() == 3) {
                    game.setStatusEnum(WAITING_START);
                    this.context.publishEvent(new JoinGameCompleteEvent(this).setUserList(game.getUserList())
                            .setGameId(game.getGameId()).setRequestUser(requestUser));
                }
                break;
            case WAITING_START:
                if (game.getUserInfo().values().stream().allMatch(user -> user.getStatus().equals(WAITING_OTHER_START))) {
                    this.context.publishEvent(new StartGameCompleteEvent(this).setGameId(game.getGameId())
                            .setRequestUser(requestUser));
                }
                break;
            case WAITING_LORD:
                this.context.publishEvent(game.getUserInfo().values().stream().filter(userState -> userState.getStatus()
                        .equals(WAITING_SELF_PLAY))
                        .findAny().<BaseGameEvent>map(userState -> {
                            game.setStatusEnum(PLAYING);
                            return new CallLordGameEvent(this).setLordUserId(game.getLordUser());
                        })
                        .orElse(new SkipLordGameEvent(this).setNextLordUser(game.getLordUser()))
                        .setGameId(game.getGameId()).setRequestUser(requestUser));
                break;
            case PLAYING:
                this.context.publishEvent(game.getUserInfo().values().stream().filter(userState -> userState.getStatus()
                        .equals(WAITING_SELF_PLAY))
                        .findAny().<BaseGameEvent>map(userState -> {
                            game.setStatusEnum(PLAYING);
                            return new CallLordGameEvent(this).setLordUserId(game.getLordUser());
                        })
                        // TODO 拆分掉
                        .orElse(new SkipPlayGameEvent(this).setNextPlayUser(game.getLordUser()))
                        .setGameId(game.getGameId()).setRequestUser(requestUser));
            case COMPLETE:
            default:
        }
    }
}
