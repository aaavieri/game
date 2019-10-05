package cn.yjl.game.algorithm;

import cn.yjl.game.dto.CardWrapDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KingBombAlg implements AlgIf {

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public int getType() {
        return 11;
    }

    @Override
    public int getValue(List<CardWrapDto> cards) {
        if (cards.size() != 2) {
            return 0;
        } else if (cards.stream().map(card -> card.getCardPojo().getPoint()).reduce(Integer::sum).orElse(0) != 50) {
            return 0;
        } else {
            return 50;
        }
    }
}
