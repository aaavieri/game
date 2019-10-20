package cn.yjl.game.util;

import cn.yjl.game.exception.ApplicationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Array;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppUtil {

    @SuppressWarnings("unchecked")
    public static <T> T autoCast(Object obj) {
        return (T) obj;
    }

    public static <T> List<List<T>> combineList(List<T> src, int quantity) {
        return combine(src, quantity, autoCast(Collectors.toList()));
    }

    public static <T> List<T[]> combineArray(List<T> src, int quantity) {
        final Function<Object[], Integer> findNullIndex = array -> IntStream.range(0, array.length)
                .filter(index -> array[index] == null)
                .findFirst().orElseThrow(() -> new ApplicationException().setErrCode(999).setMessage("理论上不会出现的异常"));
        return combine(src, quantity, Collector.of(() -> AppUtil.autoCast(Array.newInstance(src.get(0).getClass(), quantity)),
                (array, ele) -> array[findNullIndex.apply(array)] = ele,
                (array1, array2) -> {
                    System.arraycopy(array2, 0, array1, findNullIndex.apply(array1), findNullIndex.apply(array2));
                    return array1;
                }));
    }

    public static <T, R> List<R> combine(List<T> src, int quantity, Collector<T, R, R> collector) {
        if (quantity <= 0 || quantity > src.size()) {
            throw new ApplicationException().setErrCode(101)
                    .setMessage(String.format("不能形成组合：组合元素数量%d，总数量%d", quantity, src.size()));
        }
        if (quantity == 1) {
            return src.stream().map(ele -> Stream.of(ele).collect(collector)).collect(Collectors.toList());
        } else if (quantity == src.size()) {
            return Stream.of(src.stream().collect(collector)).collect(Collectors.toList());
        } else {
            return IntStream.range(0, src.size() - quantity + 1).boxed().flatMap(startIndex -> {
                List<R> subResult = combine(src.subList(startIndex + 1, src.size()), quantity - 1, collector);
                return subResult.stream().map(list -> {
                    R headList = collector.supplier().get();
                    collector.accumulator().accept(headList, src.get(startIndex));
                    return collector.combiner().apply(headList, list);
                });
            }).collect(Collectors.toList());
        }
    }

    public static <T> void clearList(List<T> src) {
        if (src != null) {
            src.clear();
        }
    }
}
