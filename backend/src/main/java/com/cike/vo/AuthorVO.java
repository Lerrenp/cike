package com.cike.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 笔记作者信息
 */
@Data
@AllArgsConstructor
public class AuthorVO {

    private Long id;
    private String nickname;
    private String avatar;
}
