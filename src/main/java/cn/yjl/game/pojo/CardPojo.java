package cn.yjl.game.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class CardPojo {
    private int cardId;
    private int colorId;
    private int point;
    private int number;
    private String label;
}
