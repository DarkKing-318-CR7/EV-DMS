package com.uth.ev_dms.fix.controllers.api;

import com.uth.ev_dms.fix.service.dto.UserDto;
import com.uth.ev_dms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;

    @GetMapping("/{username}")
    public UserDto getUser(@PathVariable String username) {
        return userService.getUserDto(username);
    }
}
