package cn.yjl.game.dto.event;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class GameEventWrapDto {
    private int gameId;
    private List<BaseEventDto> eventData;
}
