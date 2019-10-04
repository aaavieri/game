package cn.yjl.game.algorithm;

import cn.yjl.game.dto.CardWrapDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FourWithTowAlg implements AlgIf {
    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public int getType() {
        return 5;
    }

    @Override
    public int getValue(List<CardWrapDto> cards) {
        if (cards.size() < 6 || cards.size() > 8) {
            return 0;
        }
        Map<Integer, List<CardWrapDto>> pointMap = this.getPointMap(cards);
        if (pointMap.size() > 3) {
            return 0;
        }
        int fourPoint = pointMap.entrySet().stream().filter(entry -> entry.getValue().size() == 4)
                .findFirst().map(Map.Entry::getKey).orElse(0);
        return this.getMaxSpecialValue(fourPoint);
    }
}
