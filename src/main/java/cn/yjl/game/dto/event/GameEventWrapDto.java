package cn.yjl.game.dto.event;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class GameEventWrapDto<T extends BaseEventDto> {
    private int gameId;
    private List<T> eventData;
}
