package io.github.whmmm.springboottest.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestForm implements Serializable {
    private String name;
    private String age;
}
