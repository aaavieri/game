package cn.yjl.game.algorithm;

import cn.yjl.game.dto.CardWrapDto;
import cn.yjl.game.dto.OnceSendCardDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface AlgIf {

    int getPriority();

    int getType();

    int getValue(List<CardWrapDto> cards);

    default OnceSendCardDto generate(List<CardWrapDto> cards, String userId) {
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

    default boolean isSequence(List<CardWrapDto> cards) {
        List<Integer> valueList = cards.stream().map(card ->
                this.getMaxSpecialValue(card.getCardPojo().getPoint())).distinct().sorted()
                .collect(Collectors.toList());
        return (valueList.size() == cards.size()
                && (valueList.get(valueList.size() - 1) - valueList.get(0) == cards.size() - 1));
    }
}
