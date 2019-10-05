package cn.yjl.game.algorithm;

import cn.yjl.game.dto.CardWrapDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SequenceThreeOnlyAlg implements AlgIf {
    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public int getType() {
        return 8;
    }

    @Override
    public int getValue(List<CardWrapDto> cards) {
        if (cards.size() < 6 || cards.size() % 3 != 0) {
            return 0;
        }
        Map<Integer, List<CardWrapDto>> pointMap = this.getPointMap(cards);
        if (pointMap.values().stream().anyMatch(list -> list.size() != 3)) {
            return 0;
        } else if (!this.isSequencePoint(pointMap.keySet())) {
            return 0;
        } else {
            return this.getMaxValuePoint(pointMap.keySet());
        }
    }
}
