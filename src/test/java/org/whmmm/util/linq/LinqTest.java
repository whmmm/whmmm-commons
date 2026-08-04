package org.whmmm.util.linq;

import org.junit.Test;

import java.util.List;

public class LinqTest {

    @Test
    public void test() {
        UserScore v1 = new UserScore("张三", "男", 20, 90.0);
        UserScore v2 = new UserScore("李四", "男", 21, 80.0);
        UserScore v3 = new UserScore("王五", "女", 22, 70.0);
        UserScore v4 = new UserScore("赵六", "女", 23, 60.0);
        UserScore v5 = new UserScore("田七", "男", 24, 50.0);
        UserScore v6 = new UserScore("钱八", "女", 25, 40.0);
        UserScore v7 = new UserScore("孙九", "男", 26, 30.0);
        UserScore v8 = new UserScore("周十", "女", 27, 20.0);
        UserScore v9 = new UserScore("吴十一", "男", 28, 10.0);
        UserScore v10 = new UserScore("郑十二", "女", 29, 80.0);

        LinqList<UserScore> list = LinqList.of(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10);

        List<UserScore> sort = list.orderByDescending(x -> x.getScore())
                .thenByDescending(x -> x.getAge())
                .toList();


        System.out.println(sort);

    }
}
