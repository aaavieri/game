package cn.yjl.game.algorithm;

import cn.yjl.game.dto.CardWrapDto;
import cn.yjl.game.dto.OnceSendCardDto;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface AlgIf {

    int getPriority();

    int getType();

    int getValue(List<CardWrapDto> cards);

    default OnceSendCardDto generate(List<CardWrapDto> cards, String userId) {
        cards.sort(Comparator.comparingInt(card -> this.getMaxSpecialValue(card.getCardPojo().getPoint())));
        int value = this.getValue(cards);
        if (value <= 0) {
            return null;
        } else {
            return new OnceSendCardDto().setPriority(this.getPriority()).setSentCards(cards)
                    .setType(this.getType()).setUserId(userId).setValue(value);
        }
    }

    default int getMaxSpecialValue(int point) {
        switch (point) {
            case 1:
                return 14;
            case 2:
                return 16;
            default:
                return point;
        }
    }

    default Map<Integer, List<CardWrapDto>> getPointMap(List<CardWrapDto> cards) {
        return cards.stream().collect(Collectors.groupingBy(card -> card.getCardPojo().getPoint()));
    }

    default boolean isSequenceCard(Collection<CardWrapDto> cards) {
        List<Integer> points = this.getPointsByCards(cards);
        return this.isSequencePoint(points);
    }

    default boolean isSequencePoint(Collection<Integer> points) {
        List<Integer> valueList = points.stream().map(this::getMaxSpecialValue).distinct().sorted()
                .collect(Collectors.toList());
        return (valueList.size() == points.size()
                && (valueList.get(valueList.size() - 1) - valueList.get(0) == points.size() - 1));
    }

    default List<Integer> getPointsByCards(Collection<CardWrapDto> cards) {
        return cards.stream().map(card -> card.getCardPojo().getPoint()).collect(Collectors.toList());
    }

    default int getMaxValueCard(Collection<CardWrapDto> cards) {
        List<Integer> points = this.getPointsByCards(cards);
        return this.getMaxValuePoint(points);
    }

    default int getMaxValuePoint(Collection<Integer> points) {
        return points.stream().map(this::getMaxSpecialValue).max(Integer::compareTo).orElse(0);
    }
}
