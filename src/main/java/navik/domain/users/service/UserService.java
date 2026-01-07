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

    /**
     * Retrieve user information for the given user ID.
     *
     * @param userId the ID of the user to retrieve
     * @return the UserResponseDto representing the user
     * @throws GeneralExceptionHandler if no user exists for the given ID (GeneralErrorCode.USER_NOT_FOUND)
     */
    public UserResponseDto getUser(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));
        return UserResponseDto.from(member);
    }

    /**
     * Retrieves the user's information and returns it as a UserResponseDto.
     *
     * @param userId the identifier of the user to retrieve
     * @return a UserResponseDto containing the user's data
     * @throws GeneralExceptionHandler if no user with the given id exists (GeneralErrorCode.USER_NOT_FOUND)
     */
    public UserResponseDto getMyInfo(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));
        return UserResponseDto.from(member);
    }
}