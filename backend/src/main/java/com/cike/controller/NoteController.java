package com.cike.controller;

import com.cike.common.Result;
import com.cike.common.UserContext;
import com.cike.dto.NotePublishRequest;
import com.cike.service.NoteService;
import com.cike.vo.NoteDetailVO;
import com.cike.vo.NoteVO;
import com.cike.vo.PageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 笔记模块 /api/v1/notes
 */
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    /** 11. 首页瀑布流笔记列表（分页/分类） */
    @GetMapping
    public Result<PageVO<NoteVO>> feed(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(required = false) String category) {
        return Result.ok(noteService.feed(page, size, category));
    }

    /** 12. 笔记详情 */
    @GetMapping("/{id}")
    public Result<NoteDetailVO> detail(@PathVariable Long id) {
        return Result.ok(noteService.detail(id));
    }

    /** 13. 发布笔记 */
    @PostMapping
    public Result<Map<String, Object>> publish(@RequestBody NotePublishRequest request) {
        Long id = noteService.publish(request, UserContext.requireUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.ok("发布成功", data);
    }

    /** 14. 删除笔记（作者） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noteService.delete(id, UserContext.requireUserId());
        return Result.ok("删除成功");
    }
}
