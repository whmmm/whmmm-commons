package io.github.whmmm.commons.linq;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
class UserScore {

    private String name;
    private String sex;
    private Integer age;
    private Double score;


    public UserScore(String name, String sex, Integer age, Double score) {
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.score = score;
    }
}
