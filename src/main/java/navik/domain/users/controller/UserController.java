package navik.domain.users.controller;

import navik.auth.annotation.AuthUser;
import navik.domain.users.dto.UserResponseDto;
import navik.domain.users.service.UserService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserController implements UserControllerDocs{

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponseDto> getMyInfo(@AuthUser UserDetails userDetails) {
        UserResponseDto myInfo = userService.getMyInfo(userDetails.getUsername());
        return ApiResponse.onSuccess(GeneralSuccessCode._OK, myInfo);
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserResponseDto> getUser(@PathVariable Long userId) {
        UserResponseDto userInfo = userService.getUser(userId);
        return ApiResponse.onSuccess(GeneralSuccessCode._OK, userInfo);
    }
}
