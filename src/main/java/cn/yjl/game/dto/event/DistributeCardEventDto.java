package cn.yjl.game.dto.event;

import cn.yjl.game.dto.CardWrapDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DistributeCardEventDto extends BaseEventDto {
	private String lordUser;
	private List<CardWrapDto> cardList;
}
