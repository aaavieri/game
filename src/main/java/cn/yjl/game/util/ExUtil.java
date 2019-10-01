package cn.yjl.game.util;

import cn.yjl.game.lambda.ConsumerWithEx;
import cn.yjl.game.lambda.ExConsumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExUtil {

    public static <T> Consumer<T> exWrap(ConsumerWithEx<T> consumer) {
        return exWrap(consumer, ExConsumer.getDefault());
    }

    public static <T> Consumer<T> exWrap(ConsumerWithEx<T> consumer, Consumer<Exception> exceptionConsumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception e) {
                exceptionConsumer.accept(e);
            }
        };
    }

    public static <T> Consumer<T> exWrap(ConsumerWithEx<T> consumer, BiConsumer<T, Exception> exceptionConsumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception e) {
                exceptionConsumer.accept(t, e);
            }
        };
    }
}
