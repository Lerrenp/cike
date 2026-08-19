package com.cike.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户草稿表 t_draft（每用户仅一条，唯一索引 uk_user_id）
 */
@Data
@TableName("t_draft")
public class Draft {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    @JsonIgnore
    private String content;

    /** 草稿图片列表 json 数组 */
    @JsonIgnore
    private String imageJson;

    /** 话题 id 逗号分隔 */
    @JsonIgnore
    private String topicIds;

    private Integer visible;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
