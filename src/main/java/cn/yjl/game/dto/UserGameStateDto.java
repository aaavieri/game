package cn.yjl.game.dto;

import cn.yjl.game.enumeration.UserGameStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserGameStateDto {
    private String userId;
    private UserGameStatusEnum status;
}
