package cn.yjl.game.listener;

import cn.yjl.game.dto.GameStateDto;
import cn.yjl.game.dto.event.DistributeCardEventDto;
import cn.yjl.game.dto.event.JoinGameEventDto;
import cn.yjl.game.dto.event.SkipLordEventDto;
import cn.yjl.game.event.JoinGameCompleteEvent;
import cn.yjl.game.event.SkipLordGameEvent;
import cn.yjl.game.event.StartGameCompleteEvent;
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
//        event.getUserList().stream().filter(this.userEventMap::containsKey).forEach(ExUtil.wrapCon(user ->
//            this.userEventMap.get(user).send(new JoinGameEventDto().setUserList(event.getUserList())
//                    .setGameId(event.getGameId()).setUserId(user)
//                    .setGameStatusValue(WAITING_START.getValue()).setUserStatusValue(WAITING_SELF_START.getValue())),
//                (user, e) -> this.userEventMap.get(user).completeWithError(e)));
        event.getSource().getEventData(JoinGameEventDto.class, event).stream()
                .filter(eventData -> this.userEventMap.containsKey(eventData.getUserId()))
                .forEach(ExUtil.wrapCon(eventData ->
                                this.userEventMap.get(eventData.getUserId()).send(eventData.setUserList(event.getUserList())),
                        (eventData, e) -> this.userEventMap.get(eventData.getUserId()).completeWithError(e)));
    }

    @EventListener
    @Async
    public void startGameComplete(StartGameCompleteEvent event) {
        GameStateDto game = event.getSource().distributeCard(event.getGameId());
//        game.getUserList().forEach(ExUtil.wrapCon(user ->
//            this.userEventMap.get(user).send(new DistributeCardEventDto()
//                .setCardList(game.getUserInfo().get(user).getGameCards()).setLordUser(game.getLordUser())
//                .setGameId(event.getGameId()).setUserId(user)
//                .setGameStatusValue(WAITING_LORD.getValue()).setUserStatusValue(WAITING_SELF_LORD.getValue())),
//            (user, e) -> this.userEventMap.get(user).completeWithError(e)));
        event.getSource().getEventData(DistributeCardEventDto.class, event)
                .forEach(ExUtil.wrapCon(eventData ->
                                this.userEventMap.get(eventData.getUserId()).send(
                                        eventData.setCardList(game.getUserInfo().get(eventData.getUserId()).getGameCards())
                                                .setLordUser(game.getLordUser())),
                        (eventData, e) -> this.userEventMap.get(eventData.getUserId()).completeWithError(e)));
    }

    @EventListener
    @Async
    public void skipLordComplete(SkipLordGameEvent event) {
        event.getSource().getEventData(SkipLordEventDto.class, event)
                .forEach(ExUtil.wrapCon(eventData ->
                                this.userEventMap.get(eventData.getUserId()).send(eventData.setNextLordUser(event.getNextLordUser())),
                        (eventData, e) -> this.userEventMap.get(eventData.getUserId()).completeWithError(e)));
    }
}
