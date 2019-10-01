package cn.yjl.game.lambda;

import java.util.function.Consumer;

@FunctionalInterface
public interface ExConsumer extends Consumer<Exception> {

    static ExConsumer getDefault() {
        return e -> {
            throw new RuntimeException(e);
        };
    }
}
