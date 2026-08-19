package com.cike.controller;

import com.cike.common.Result;
import com.cike.entity.User;
import com.cike.service.UserService;
import com.cike.vo.NoteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户模块 /api/v1/users
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 5. 用户列表 */
    @GetMapping
    public Result<List<User>> list() {
        return Result.ok(userService.listUsers());
    }

    /** 6. 用户详情 */
    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        return Result.ok(userService.getUser(id));
    }

    /** 7. 修改用户信息（传哪个字段改哪个） */
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody User update) {
        return Result.ok("修改成功", userService.updateUser(id, update));
    }

    /** 8. 我的笔记列表 */
    @GetMapping("/{id}/notes")
    public Result<List<NoteVO>> myNotes(@PathVariable Long id) {
        return Result.ok(userService.myNotes(id));
    }

    /** 9. 我赞过的笔记 */
    @GetMapping("/{id}/likes")
    public Result<List<NoteVO>> myLikes(@PathVariable Long id) {
        return Result.ok(userService.myLikes(id));
    }

    /** 10. 我收藏的笔记 */
    @GetMapping("/{id}/collects")
    public Result<List<NoteVO>> myCollects(@PathVariable Long id) {
        return Result.ok(userService.myCollects(id));
    }
}
