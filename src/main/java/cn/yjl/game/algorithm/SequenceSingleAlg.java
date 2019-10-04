package cn.yjl.game.algorithm;

import cn.yjl.game.dto.CardWrapDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SequenceSingleAlg implements AlgIf {

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public int getType() {
        return 6;
    }

    @Override
    public int getValue(List<CardWrapDto> cards) {
        if (cards.size() < 5) {
            return 0;
        } else {
            List<Integer> valueList = cards.stream().map(card ->
                    this.getMaxSpecialValue(card.getCardPojo().getPoint())).distinct().sorted()
                    .collect(Collectors.toList());
            if (valueList.size() != cards.size()
                    || valueList.get(valueList.size() - 1) - valueList.get(0) != cards.size() - 1) {
                return 0;
            }
            return valueList.get(valueList.size() - 1);
        }
    }
}
