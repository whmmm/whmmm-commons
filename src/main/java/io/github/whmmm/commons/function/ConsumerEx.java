package io.github.whmmm.commons.function;

import java.io.Serializable;

@FunctionalInterface
public interface ConsumerEx<T> extends Serializable {
    void accept(T t) throws Exception;
}
