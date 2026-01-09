package navik.domain.users.controller;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.users.dto.UserRequestDTO;
import navik.domain.users.dto.UserResponseDTO;
import navik.domain.users.service.UserCommandService;
import navik.domain.users.service.UserQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserController implements UserControllerDocs {

	private final UserQueryService userQueryService;
	private final UserCommandService userCommandService;

	@GetMapping("/me")
	public ApiResponse<UserResponseDTO.UserInfoDTO> getMyInfo(@AuthUser UserDetails userDetails) {
		Long userId = Long.parseLong(userDetails.getUsername());
		UserResponseDTO.UserInfoDTO myInfo = userQueryService.getMyInfo(userId);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, myInfo);
	}

	@GetMapping("/{userId}")
	public ApiResponse<UserResponseDTO.UserInfoDTO> getUser(@PathVariable Long userId) {
		UserResponseDTO.UserInfoDTO userInfo = userQueryService.getUserInfo(userId);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, userInfo);
	}

	@PostMapping("/me/basic-info")
	public ApiResponse<UserResponseDTO.BasicInfoDto> register(@AuthUser UserDetails userDetails,
		@RequestBody @Valid UserRequestDTO.BasicInfoDto req) {
		Long userId = Long.parseLong(userDetails.getUsername());
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, userCommandService.updateBasicInfo(userId,req));
	}
}
