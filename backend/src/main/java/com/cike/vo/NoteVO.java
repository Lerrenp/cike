package com.cike.vo;

import com.cike.entity.Note;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 笔记列表 VO：在 Note 基础上携带作者信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoteVO extends Note {

    private AuthorVO author;
}
