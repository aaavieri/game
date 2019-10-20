package cn.yjl.game.listener;

import cn.yjl.game.dto.event.BaseEventDto;
import cn.yjl.game.dto.event.GameEventWrapDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class GameListener {

    @Value("${application.sse.timeout:0}")
    private long sseTimeout;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    private Map<String, Object> socketHeaderMap = new HashMap<>();
    private Map<String, SseEmitter> userEventMap = new HashMap<>();

    @PostConstruct
    public void init() {
        this.socketHeaderMap.put(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);
    }

    public SseEmitter registerEvent(String userId) {
        SseEmitter sseEmitter = new SseEmitter(this.sseTimeout);
        this.userEventMap.put(userId, sseEmitter);
        return sseEmitter;
    }

    @EventListener
    @Async
    public <T extends BaseEventDto> void sseEvent(GameEventWrapDto<T> gameEvent) {
        gameEvent.getEventData()
                .forEach(eventData -> {
                    System.out.println(eventData.getUserId());
                    this.messagingTemplate.convertAndSend("/topic/gameEvent/" + eventData.getUserId(), eventData);
                });
    }
}
