package navik.domain.users.controller;

import navik.domain.users.dto.UserResponseDto;
import navik.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "User", description = "유저 관련 API")
public interface UserControllerDocs {
    /**
     * Retrieve the authenticated user's profile information.
     *
     * @param userDetails the currently authenticated user's principal
     * @return an ApiResponse containing the authenticated user's UserResponseDto
     */
    @Operation(summary = "내 정보", description = "로그인한 사용자의 정보를 가져옵니다")
    ApiResponse<UserResponseDto> getMyInfo(@AuthenticationPrincipal UserDetails userDetails);

    /**
     * Retrieve information for a specific user.
     *
     * @param userId the ID of the user to retrieve
     * @return an ApiResponse containing the user's UserResponseDto
     */
    @Operation(summary = "사용자 조회", description = "특정 사용자의 정보를 가져옵니다")
    ApiResponse<UserResponseDto> getUser(@PathVariable Long userId);
}