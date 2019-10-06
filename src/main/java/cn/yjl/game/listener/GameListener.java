package cn.yjl.game.listener;

import cn.yjl.game.dto.event.BaseEventDto;
import cn.yjl.game.util.FuncUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
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
    public <T extends BaseEventDto> void sseEvent(List<T> eventList) {
        eventList.stream().filter(eventData -> this.userEventMap.containsKey(eventData.getUserId()))
                .forEach(FuncUtil.wrapCon(eventData ->
                                this.userEventMap.get(eventData.getUserId()).send(eventData),
                        (eventData, e) -> this.userEventMap.get(eventData.getUserId()).completeWithError(e)));
    }
}
