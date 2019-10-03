package cn.yjl.game.dto;

import cn.yjl.game.pojo.CardPojo;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CardWrapDto {
    private CardPojo cardPojo;
    private int gameIndex;
}
