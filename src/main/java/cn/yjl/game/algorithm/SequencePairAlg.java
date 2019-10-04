package cn.yjl.game.algorithm;

import cn.yjl.game.dto.CardWrapDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SequencePairAlg implements AlgIf {

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public int getType() {
        return 7;
    }

    @Override
    public int getValue(List<CardWrapDto> cards) {
        if (cards.size() % 2 != 0 || cards.size() < 6) {
            return 0;
        }
        Map<Integer, List<CardWrapDto>> pointMap = this.getPointMap(cards);
        if (pointMap.values().stream().anyMatch(list -> list.size() != 2)) {
            return 0;
        }
        boolean isSequence = this.isSequence(pointMap.values().stream().map(list -> list.get(0)).collect(Collectors.toList()));
        if (!isSequence) {
            return 0;
        } else {
            return cards.stream().map(card -> this.getMaxSpecialValue(card.getCardPojo().getPoint())).max(Integer::compareTo).orElse(0);
        }
    }
}
