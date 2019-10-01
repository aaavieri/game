package cn.yjl.game.listener;

import cn.yjl.game.dto.response.JoinGameResponseDto;
import cn.yjl.game.event.JoinGameCompleteEvent;
import cn.yjl.game.util.ExUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class GameListener {

    private Map<String, SseEmitter> joinGameMap = new HashMap<>();

    private Map<String, SseEmitter> startGameMap = new HashMap<>();

    private Map<String, SseEmitter> lordGameMap = new HashMap<>();

    private Map<String, SseEmitter> playGameMap = new HashMap<>();

    public SseEmitter joinGame(String userId) {
        SseEmitter sseEmitter = new SseEmitter();
        this.joinGameMap.put(userId, sseEmitter);
        return sseEmitter;
    }

    public SseEmitter startGame(String userId) {
        SseEmitter sseEmitter = new SseEmitter();
        this.startGameMap.put(userId, sseEmitter);
        return sseEmitter;
    }

    public SseEmitter lordGame(String userId) {
        SseEmitter sseEmitter = new SseEmitter();
        this.lordGameMap.put(userId, sseEmitter);
        return sseEmitter;
    }

    public SseEmitter playGame(String userId) {
        SseEmitter sseEmitter = new SseEmitter();
        this.playGameMap.put(userId, sseEmitter);
        return sseEmitter;
    }

    @EventListener
    @Async
    public void joinGameComplete(JoinGameCompleteEvent event) {
        event.getUserList().forEach(ExUtil.exWrap(user -> {
            this.joinGameMap.get(user).send(new JoinGameResponseDto().setUserList(event.getUserList())
                    .setGameId(event.getGameId()).setUserId(user));
            this.joinGameMap.get(user).complete();
        }, (user, e) -> this.joinGameMap.get(user).completeWithError(e)));
    }
}
