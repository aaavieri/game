package cn.yjl.game.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DoPlayRequestDto extends BaseRequestDto {
	private List<Integer> cardList;
}
