package cn.yjl.game.algorithm;

import cn.yjl.game.dto.CardWrapDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BombAlg implements AlgIf {

    @Override
    public int getPriority() {
        {
        }
        return 2;
    }

    @Override
    public int getType() {
        return 10;
    }

    @Override
    public int getValue(List<CardWrapDto> cards) {
        if (cards.size() != 4) {
            return 0;
        } else if (cards.stream().map(card -> card.getCardPojo().getPoint()).distinct().count() > 1) {
            return 0;
        } else {
            return this.getMaxSpecialValue(cards.get(0).getCardPojo().getPoint());
        }
    }
}
