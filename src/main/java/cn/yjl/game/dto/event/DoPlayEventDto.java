package cn.yjl.game.dto.event;

import cn.yjl.game.dto.OnceSendCardDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DoPlayEventDto extends BaseEventDto {
    private String nextPlayUser;
    private OnceSendCardDto sentCard;
}
