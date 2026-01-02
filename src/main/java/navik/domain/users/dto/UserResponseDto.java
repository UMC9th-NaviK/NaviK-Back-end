package navik.domain.users.dto;

import navik.auth.entity.Role;
import navik.auth.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

	private Long id;
	private String name;
	private String email;
	private Role role;
	private String socialType;

	public static UserResponseDto from(Member member) {
		return UserResponseDto.builder()
			.id(member.getId())
			.name(member.getName())
			.email(member.getEmail())
			.role(member.getRole())
			.socialType(member.getSocialType())
			.build();
	}
}
