package com.forum.search;
import com.forum.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class UserTest{

    private String id;

    private String name;

    private Integer sex;

    private Integer age;

    private String creator;
}
