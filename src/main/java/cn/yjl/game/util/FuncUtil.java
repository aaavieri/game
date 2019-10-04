package cn.yjl.game.util;

import cn.yjl.game.lambda.ConsumerWithEx;
import cn.yjl.game.lambda.ExConsumer;
import cn.yjl.game.lambda.FunctionWithEx;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FuncUtil {

    public static <T> Consumer<T> wrapCon(ConsumerWithEx<T> consumer) {
        return wrapCon(consumer, ExConsumer.getDefault());
    }

    public static <T> Consumer<T> wrapCon(ConsumerWithEx<T> consumer, Consumer<Exception> exceptionConsumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception e) {
                exceptionConsumer.accept(e);
            }
        };
    }

    public static <T> Consumer<T> wrapCon(ConsumerWithEx<T> consumer, BiConsumer<T, Exception> exceptionConsumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception e) {
                exceptionConsumer.accept(t, e);
            }
        };
    }

    public static <T, R> Function<T, R> wrapFunc(FunctionWithEx<T, R> function) {
        return wrapFunc(function, ExConsumer.getDefault());
    }


    public static <T, R> Function<T, R> wrapFunc(FunctionWithEx<T, R> function, Consumer<Exception> exceptionConsumer) {
        return t -> {
            try {
                return function.accept(t);
            } catch (Exception e) {
                exceptionConsumer.accept(e);
            }
            return null;
        };
    }

    public static <T, R> Function<T, R> wrapFunc(FunctionWithEx<T, R> function, BiConsumer<T, Exception> exceptionConsumer) {
        return t -> {
            try {
                return function.accept(t);
            } catch (Exception e) {
                exceptionConsumer.accept(t, e);
            }
            return null;
        };
    }

    @SafeVarargs
    public static <T> Consumer<T> andCons(Consumer<T>... consumers) {
        return Stream.of(consumers).reduce(Consumer::andThen).orElse(t -> {
        });
    }

    @SafeVarargs
    public static <T> Function<T, T> andFunc(Function<T, T>... functions) {
        return Stream.of(functions).reduce(Function::andThen).orElse(Function.identity());
    }
}
