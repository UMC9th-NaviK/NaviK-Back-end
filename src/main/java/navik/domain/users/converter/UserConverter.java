package navik.domain.users.converter;

import navik.domain.users.dto.UserResponseDTO;
import navik.domain.users.entity.User;

public class UserConverter {

	public static UserResponseDTO.UserInfoDTO toUserInfoDTO(User user) {
		return UserResponseDTO.UserInfoDTO.builder()
			.id(user.getId())
			.name(user.getName())
			.email(user.getEmail())
			.role(user.getRole())
			.socialType(user.getSocialType())
			.build();
	}
}
