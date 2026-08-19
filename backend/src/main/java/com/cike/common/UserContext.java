package com.cike.common;

/**
 * 当前登录用户上下文（ThreadLocal，由 AuthInterceptor 填充）
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    /**
     * 获取当前登录用户 id，未登录抛出 401
     */
    public static Long requireUserId() {
        Long id = USER_ID.get();
        if (id == null) {
            throw new BusinessException(401, "未登录或登录已失效");
        }
        return id;
    }

    public static void clear() {
        USER_ID.remove();
    }
}
