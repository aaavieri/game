package cn.yjl.game.dto.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BaseResponseDto {
    private String userId;
    private int gameId;
}
