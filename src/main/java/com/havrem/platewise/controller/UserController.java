package com.havrem.platewise.controller;

import com.havrem.platewise.config.CurrentUser;
import com.havrem.platewise.dto.user.ChangePasswordRequest;
import com.havrem.platewise.dto.user.UserDto;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserDto getProfile(@CurrentUser User user) {
        return userService.getProfile(user);
    }

    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@CurrentUser User user, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(user, request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@CurrentUser User user) {
        userService.deleteAccount(user);
    }
}
