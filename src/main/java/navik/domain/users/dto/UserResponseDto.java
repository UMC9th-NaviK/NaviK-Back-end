package navik.domain.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.domain.users.enums.Role;

public class UserResponseDTO {

	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	public static class UserInfoDTO{
		private Long id;
		private String name;
		String email;
		Role role;
		String socialType;
	}
}
