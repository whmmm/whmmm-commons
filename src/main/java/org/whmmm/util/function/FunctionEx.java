package org.whmmm.util.function;

import java.io.Serializable;

@FunctionalInterface
public interface FunctionEx<T, R> extends Serializable {
    R apply(T t) throws Exception;
}
