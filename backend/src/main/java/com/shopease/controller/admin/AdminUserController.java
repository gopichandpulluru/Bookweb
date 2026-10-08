package com.shopease.controller.admin;

import com.shopease.dto.response.PageResponse;
import com.shopease.dto.response.UserResponse;
import com.shopease.service.UserService;
import com.shopease.util.Constants;
import com.shopease.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public PageResponse<UserResponse> list(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size) {
        return userService.getAll(PageUtil.of(page, size, "createdAt", "desc", Set.of("createdAt"), "createdAt"));
    }

    // PATCH /api/v1/admin/users/5/enabled?value=false  -> blocks the user from logging in
    @PatchMapping("/{id}/enabled")
    public UserResponse setEnabled(@PathVariable Long id, @RequestParam boolean value) {
        return userService.setEnabled(id, value);
    }
}
