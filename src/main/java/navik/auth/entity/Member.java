package navik.auth.entity;

import navik.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Column(nullable = false)
	private String socialId; // 소셜 로그인 제공자에서 주는 ID

	@Column(nullable = false)
	private String socialType; // google, kakao, naver

	public Member update(String name) {
		this.name = name;
		return this;
	}

	public String getRoleKey() {
		return this.role.getKey();
	}
}
