package cn.yjl.game.lambda;

@FunctionalInterface
public interface FunctionWithEx<T, R> {
    R accept(T t) throws Exception;
}
