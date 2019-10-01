package cn.yjl.game.dto.event;

import cn.yjl.game.enumeration.GameStatusEnum;
import cn.yjl.game.enumeration.UserGameStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BaseEventDto {
    private String userId;
    private int gameId;
    private GameStatusEnum gameStatus;
    private UserGameStatusEnum userStatus;
}
