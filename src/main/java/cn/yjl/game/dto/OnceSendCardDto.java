package cn.yjl.game.dto;

import cn.yjl.game.util.Const;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class OnceSendCardDto {
    private String userId;
    private int type;
    private int priority;
    private int value;
    private List<CardWrapDto> sentCards;

    public int compare(OnceSendCardDto another) {
        if (this.priority != another.priority) {
            return Integer.compare(this.priority, another.priority);
        }
        if (this.type == another.type && (this.sentCards.size() == another.sentCards.size() || this.type == 4)) {
            return Integer.compare(this.value, another.value);
        }
        return Const.COMPARE_CARD_CANNOT;
    }
}
