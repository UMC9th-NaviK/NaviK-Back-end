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
public class UserController implements UserControllerDocs {

    private final UserService userService;

    /**
     * Retrieve the authenticated user's profile information.
     *
     * <p>Parses the authenticated principal's username as a numeric user ID and returns the corresponding
     * user information wrapped in a successful ApiResponse.</p>
     *
     * @param userDetails the authenticated user's details; the method parses {@code userDetails.getUsername()}
     *                    as the numeric user ID
     * @return an ApiResponse containing the authenticated user's UserResponseDto and the success code GeneralSuccessCode._OK
     */
    @GetMapping("/me")
    public ApiResponse<UserResponseDto> getMyInfo(@AuthUser UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserResponseDto myInfo = userService.getMyInfo(userId);
        return ApiResponse.onSuccess(GeneralSuccessCode._OK, myInfo);
    }

    /**
     * Retrieves public information for the user identified by the given ID.
     *
     * @param userId the identifier of the user to retrieve
     * @return an ApiResponse containing the user's information as a UserResponseDto
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserResponseDto> getUser(@PathVariable Long userId) {
        UserResponseDto userInfo = userService.getUser(userId);
        return ApiResponse.onSuccess(GeneralSuccessCode._OK, userInfo);
    }
}