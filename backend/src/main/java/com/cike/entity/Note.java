package com.cike.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记表 t_note
 */
@Data
@TableName("t_note")
public class Note {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    private String coverUrl;

    private Integer viewCount;

    private Integer likeCount;

    private Integer collectCount;

    private Integer commentCount;

    /** 可见性：1公开，2仅自己可见 */
    private Integer visible;

    /** 状态：1正常，0审核中，-1违规下架 */
    private Integer status;

    @TableLogic
    @JsonIgnore
    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
