package cn.yjl.game.dto.event;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BaseEventDto {
    private String userId;
    private int gameId;
    private int gameStatus;
    private int userStatus;
}
