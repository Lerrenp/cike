package com.cike.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户收藏笔记表 t_user_collect_note（唯一索引 uk_user_note 兜底去重）
 */
@Data
@TableName("t_user_collect_note")
public class UserCollectNote {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long noteId;

    private LocalDateTime createTime;
}
