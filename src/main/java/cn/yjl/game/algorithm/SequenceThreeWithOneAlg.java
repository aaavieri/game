package cn.yjl.game.algorithm;

import cn.yjl.game.dto.CardWrapDto;
import cn.yjl.game.util.AppUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SequenceThreeWithOneAlg implements AlgIf {
    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public int getType() {
        return 9;
    }

    @Override
    public int getValue(List<CardWrapDto> cards) {
        if (cards.size() < 8 || cards.size() % 4 != 0) {
            return 0;
        }
        List<Integer> geThree = this.getPointMap(cards).entrySet().stream().filter(entry -> entry.getValue().size() >= 3)
                .map(Map.Entry::getKey).collect(Collectors.toList());
        return AppUtil.combineList(geThree, cards.size() / 4).stream().filter(this::isSequencePoint)
                .map(this::getMaxValuePoint).max(Integer::compareTo).orElse(0);
    }
}
