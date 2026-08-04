package org.whmmm.util.function;

import java.io.Serializable;

@FunctionalInterface
public interface Action extends Serializable {
    void perform() throws Exception;
}
