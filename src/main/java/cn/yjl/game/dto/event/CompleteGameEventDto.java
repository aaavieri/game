package cn.yjl.game.dto.event;

import cn.yjl.game.dto.OnceSendCardDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class CompleteGameEventDto extends BaseEventDto {
    private List<String> winner;
    private OnceSendCardDto sentCard;
}
