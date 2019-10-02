package cn.yjl.game.service;

import cn.yjl.game.dto.CardWrapDto;
import cn.yjl.game.dto.GameStateDto;
import cn.yjl.game.dto.request.BaseRequestDto;
import cn.yjl.game.event.DataInitCompleteEvent;
import cn.yjl.game.event.JoinGameCompleteEvent;
import cn.yjl.game.event.StartGameCompleteEvent;
import cn.yjl.game.exception.ApplicationException;
import cn.yjl.game.mapper.CardMapper;
import cn.yjl.game.pojo.CardPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static cn.yjl.game.enumeration.GameStatusEnum.COMPLETE;
import static cn.yjl.game.enumeration.GameStatusEnum.WAITING_START;
import static cn.yjl.game.enumeration.UserGameStatusEnum.WAITING_OTHER_START;
import static cn.yjl.game.enumeration.UserGameStatusEnum.WAITING_SELF_START;

@Service
public class GameService implements ApplicationListener<DataInitCompleteEvent> {

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private ApplicationContext context;

    private List<CardPojo> cardList;

    private AtomicInteger gameCounter = new AtomicInteger();

    private Map<Integer, GameStateDto> gameStateMap = new HashMap<>();

    private Map<String, SseEmitter> waitingJoinEmitter = new HashMap<>();

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
            this.changeStatus(game);
        } else {
            game = new GameStateDto().setGameId(this.gameCounter.incrementAndGet()).addUser(requestDto.getUserId());
            this.gameStateMap.put(game.getGameId(), game);
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
        this.changeStatus(game);
        return game;
    }
    
    public GameStateDto distributeCard(int gameId) {
        if (this.gameStateMap.containsKey(gameId)) {
            throw new ApplicationException().setMessage("不存在该局游戏，gameId：" + gameId).setErrCode(5);
        }
        GameStateDto game = this.gameStateMap.get(gameId);
        Random random = new Random();
        List<CardWrapDto> cardWrapList = this.cardList.stream().map(cardPojo -> new CardWrapDto().setCardPojo(cardPojo)
            .setSort(random.nextInt(1000)))
            .sorted(Comparator.comparingInt(CardWrapDto::getSort)).collect(Collectors.toList());
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
        return null;
    }

    private void changeStatus(GameStateDto game) {
        switch (game.getStatusEnum()) {
            case NOT_ENOUGH_USER:
                if (game.getUserList().size() == 3) {
                    game.setStatusEnum(WAITING_START);
                    game.getUserInfo().values().forEach(user -> user.setStatus(WAITING_SELF_START));
                    this.context.publishEvent(new JoinGameCompleteEvent(this).setGameId(game.getGameId())
                            .setUserList(game.getUserList()));
                }
                break;
            case WAITING_START:
                if (game.getUserInfo().values().stream().allMatch(user -> user.getStatus().equals(WAITING_OTHER_START))) {
                    this.context.publishEvent(new StartGameCompleteEvent(this).setGameId(game.getGameId()));
                }
            case WAITING_LORD:
            case PLAYING:
            case COMPLETE:
            default:
        }
    }
}
