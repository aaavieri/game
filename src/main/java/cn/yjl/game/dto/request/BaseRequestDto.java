package cn.yjl.game.dto.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BaseRequestDto {
    private String userId;
    private int gameId;
}
