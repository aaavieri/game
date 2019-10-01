package cn.yjl.game.listener;

import cn.yjl.game.dto.response.JoinGameResponseDto;
import cn.yjl.game.event.JoinGameCompleteEvent;
import cn.yjl.game.util.ExUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class GameListener {

    @Value("${application.sse.timeout:0}")
    private long sseTimeout;

    private Map<String, SseEmitter> userEventMap = new HashMap<>();

    public SseEmitter registerEvent(String userId) {
        SseEmitter sseEmitter = new SseEmitter(this.sseTimeout);
        this.userEventMap.put(userId, sseEmitter);
        return sseEmitter;
    }

    @EventListener
    @Async
    public void joinGameComplete(JoinGameCompleteEvent event) {
        event.getUserList().forEach(ExUtil.exWrap(user ->
            this.userEventMap.get(user).send(new JoinGameResponseDto().setUserList(event.getUserList())
                    .setGameId(event.getGameId()).setUserId(user)),
                (user, e) -> this.userEventMap.get(user).completeWithError(e)));
    }
}
