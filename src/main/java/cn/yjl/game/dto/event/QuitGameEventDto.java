package cn.yjl.game.dto.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class QuitGameEventDto extends BaseEventDto {
    private String quitUser;
}
