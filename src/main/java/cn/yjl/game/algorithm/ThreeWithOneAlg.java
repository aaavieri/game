package cn.yjl.game.algorithm;

import cn.yjl.game.dto.CardWrapDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ThreeWithOneAlg implements AlgIf {
    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public int getType() {
        return 3;
    }

    @Override
    public int getValue(List<CardWrapDto> cards) {
        if (cards.size() != 4) {
            return 0;
        }
        int threePoint = this.getPointMap(cards).entrySet().stream().filter(entry -> entry.getValue().size() == 3)
                .findFirst().map(Map.Entry::getKey).orElse(0);
        return this.getMaxSpecialValue(threePoint);
    }
}
