package cn.yjl.game.algorithm;

import cn.yjl.game.dto.CardWrapDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
        boolean isSequence = this.isSequencePoint(pointMap.keySet());
        if (!isSequence) {
            return 0;
        } else {
            return this.getMaxValuePoint(pointMap.keySet());
        }
    }
}
