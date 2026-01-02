package navik.domain.users.service;

import navik.domain.users.dto.UserResponseDto;
import navik.auth.entity.Member;
import navik.auth.repository.MemberRepository;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final MemberRepository memberRepository;

	public UserResponseDto getUser(Long userId) {
		Member member = memberRepository.findById(userId)
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));
		return UserResponseDto.from(member);
	}

	public UserResponseDto getMyInfo(String email) {
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));
		return UserResponseDto.from(member);
	}
}
