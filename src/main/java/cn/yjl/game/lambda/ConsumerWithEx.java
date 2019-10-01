package cn.yjl.game.lambda;

@FunctionalInterface
public interface ConsumerWithEx<T> {
    void accept(T t) throws Exception;
}
