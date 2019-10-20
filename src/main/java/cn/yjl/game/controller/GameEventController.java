package cn.yjl.game.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class GameEventController {

    @SubscribeMapping("/gameEvent/{userId}")
    public void gameEvent(@DestinationVariable("userId") String userId) {
        log.info("gameEvent init complete: {}", userId);
    }
}
