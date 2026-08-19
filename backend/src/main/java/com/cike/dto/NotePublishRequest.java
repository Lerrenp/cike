package com.cike.dto;

import lombok.Data;

import java.util.List;

/**
 * 发布笔记请求
 */
@Data
public class NotePublishRequest {

    private String title;
    private String content;
    /** 图片 url 列表 */
    private List<String> images;
    /** 话题列表，如 ["#美食"] */
    private List<String> topics;
    /** 可见性：1公开，2仅自己可见 */
    private Integer visible;
}
