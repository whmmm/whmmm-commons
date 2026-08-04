package io.github.whmmm.commons.function;

import java.io.Serializable;

@FunctionalInterface
public interface Action extends Serializable {
    void perform() throws Exception;
}
