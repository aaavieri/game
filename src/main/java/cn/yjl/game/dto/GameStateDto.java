package cn.yjl.game.dto;

import cn.yjl.game.enumeration.GameStatusEnum;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

import static cn.yjl.game.enumeration.GameStatusEnum.*;
import static cn.yjl.game.enumeration.UserGameStatusEnum.*;

@Data
@Accessors(chain = true)
public class GameStateDto {

    private int gameId;

    @Getter
    private Map<String, UserGameStateDto> users = new HashMap<>();

    private GameStatusEnum statusEnum = NOT_ENOUGH_USER;

    public GameStateDto addUser(String userId) {
        this.users.put(userId, new UserGameStateDto().setUserId(userId).setStatus(WAITING_OTHER_JOIN));
        return this;
    }
}
