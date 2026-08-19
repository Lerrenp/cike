package com.cike.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记图片表 t_note_image
 */
@Data
@TableName("t_note_image")
public class NoteImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long noteId;

    private String imageUrl;

    private Integer sort;

    @TableLogic
    @JsonIgnore
    private Integer isDeleted;

    private LocalDateTime createTime;
}
