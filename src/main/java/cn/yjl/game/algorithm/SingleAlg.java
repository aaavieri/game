package cn.yjl.game.algorithm;

import cn.yjl.game.dto.CardWrapDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SingleAlg implements AlgIf {

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public int getValue(List<CardWrapDto> cards) {
        if (cards.size() != 1) {
            return 0;
        } else {
            return this.getMaxSpecialValue(cards.get(0).getCardPojo().getPoint());
        }
    }
}
