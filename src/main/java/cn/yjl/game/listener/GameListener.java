package cn.yjl.game.listener;

import cn.yjl.game.dto.GameStateDto;
import cn.yjl.game.dto.event.DistributeCardEventDto;
import cn.yjl.game.dto.event.JoinGameEventDto;
import cn.yjl.game.event.JoinGameCompleteEvent;
import cn.yjl.game.event.StartGameCompleteEvent;
import cn.yjl.game.service.GameService;
import cn.yjl.game.util.AppUtil;
import cn.yjl.game.util.ExUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

import static cn.yjl.game.enumeration.GameStatusEnum.WAITING_LORD;
import static cn.yjl.game.enumeration.GameStatusEnum.WAITING_START;
import static cn.yjl.game.enumeration.UserGameStatusEnum.WAITING_SELF_LORD;
import static cn.yjl.game.enumeration.UserGameStatusEnum.WAITING_SELF_START;

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
        event.getUserList().stream().filter(this.userEventMap::containsKey).forEach(ExUtil.exWrap(user ->
            this.userEventMap.get(user).send(new JoinGameEventDto().setUserList(event.getUserList())
                    .setGameId(event.getGameId()).setUserId(user)
                    .setGameStatus(WAITING_START.getValue()).setUserStatus(WAITING_SELF_START.getValue())),
                (user, e) -> this.userEventMap.get(user).completeWithError(e)));
    }
    
    @EventListener
    @Async
    public void startGameComplete(StartGameCompleteEvent event) {
        GameService service = AppUtil.autoCast(event.getSource());
        GameStateDto game = service.distributeCard(event.getGameId());
        game.getUserList().forEach(ExUtil.exWrap(user ->
            this.userEventMap.get(user).send(new DistributeCardEventDto()
                .setCardList(game.getUserInfo().get(user).getGameCards()).setLordUser(game.getLordUser())
                .setGameId(event.getGameId()).setUserId(user)
                .setGameStatus(WAITING_LORD.getValue()).setUserStatus(WAITING_SELF_LORD.getValue())),
            (user, e) -> this.userEventMap.get(user).completeWithError(e)));
    }
}
