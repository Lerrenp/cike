package com.cike.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户点赞笔记表 t_user_like_note（唯一索引 uk_user_note 兜底去重）
 */
@Data
@TableName("t_user_like_note")
public class UserLikeNote {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long noteId;

    private LocalDateTime createTime;
}
