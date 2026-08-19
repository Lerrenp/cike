package com.cike.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表 t_user
 */
@Data
@TableName("t_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    @JsonIgnore
    private String password;

    private String nickname;

    private String avatar;

    private String bio;

    private Integer noteCount;

    private Integer likeTotal;

    @TableLogic
    @JsonIgnore
    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
