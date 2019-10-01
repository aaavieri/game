package cn.yjl.game.service;

import cn.yjl.game.dto.GameStateDto;
import cn.yjl.game.dto.request.BaseRequestDto;
import cn.yjl.game.dto.response.JoinGameResponseDto;
import cn.yjl.game.event.DataInitCompleteEvent;
import cn.yjl.game.event.JoinGameCompleteEvent;
import cn.yjl.game.exception.ApplicationException;
import cn.yjl.game.mapper.CardMapper;
import cn.yjl.game.pojo.CardPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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

    private Map<String, SseEmitter> waitingJoinEmitter = new HashMap<>();

    @Override
    public void onApplicationEvent(DataInitCompleteEvent dataInitCompleteEvent) {
        this.cardList = this.cardMapper.getAllCards();
    }

    public void joinGame(BaseRequestDto requestDto) throws ApplicationException {
        if (this.gameStateMap.values().stream().anyMatch(
                state -> state.getUsers().containsKey(requestDto.getUserId())
                        && state.getStatusEnum().getValue() < COMPLETE.getValue())) {
            throw new ApplicationException().setMessage("用户尚有未完成的游戏").setErrCode(1);
        }
        GameStateDto game = this.gameStateMap.get(gameCounter.get());
        if (game != null && game.getUsers().size() < 3) {
            game.addUser(requestDto.getUserId());
            this.changeStatus(game);
        } else {
            game = new GameStateDto().setGameId(this.gameCounter.incrementAndGet()).addUser(requestDto.getUserId());
            this.gameStateMap.put(game.getGameId(), game);
        }
    }

    public SseEmitter startGame(BaseRequestDto requestDto) {
        GameStateDto game = this.gameStateMap.get(requestDto.getGameId());
        if (game.getUsers().size() != 3) {
            throw new ApplicationException().setMessage("当前游戏人员不齐").setErrCode(2);
        }
        if (game.getStatusEnum().equals(WAITING_START)) {
            throw new ApplicationException().setMessage("游戏状态错误").setErrCode(3);
        }
        if (!game.getUsers().containsKey(requestDto.getUserId())) {
            throw new ApplicationException().setMessage("用户不在该局游戏中").setErrCode(4);
        }
        SseEmitter sseEmitter = new SseEmitter();
        this.waitingJoinEmitter.put(requestDto.getUserId(), sseEmitter);
        return sseEmitter;
    }

    private void changeStatus(GameStateDto gameState) {
        switch (gameState.getStatusEnum()) {
            case NOT_ENOUGH_USER:
                if (gameState.getUsers().size() == 3) {
                    gameState.setStatusEnum(WAITING_START);
                    gameState.getUsers().values().forEach(user -> user.setStatus(WAITING_SELF_START));
                    this.context.publishEvent(new JoinGameCompleteEvent(this).setGameId(gameState.getGameId())
                            .setUserList(new ArrayList<>(gameState.getUsers().keySet())));
                }
                break;
            case WAITING_START:
            case WAITING_LORD:
            case PLAYING:
            case COMPLETE:
            default:
        }
    }
}
