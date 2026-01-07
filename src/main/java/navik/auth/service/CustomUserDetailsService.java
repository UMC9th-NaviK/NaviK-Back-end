package navik.auth.service;

import navik.auth.entity.Member;
import navik.auth.repository.MemberRepository;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * Load UserDetails for the member identified by the given string ID.
     *
     * Converts the member found by the parsed numeric ID into a Spring Security UserDetails.
     *
     * @param userId the member's primary key as a string
     * @return the UserDetails representing the member with the given ID
     * @throws GeneralExceptionHandler if no member exists for the provided ID
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // username은 Member의 pk(userid)
        return memberRepository.findById(Long.parseLong(userId))
                .map(this::createUserDetails)
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));
    }

    /**
     * Converts a persisted Member into a Spring Security UserDetails instance.
     *
     * <p>The resulting UserDetails uses the member's id (as a decimal string) as the username,
     * an empty password, and a single authority derived from the member's role.</p>
     *
     * @param member the member entity to convert
     * @return a UserDetails whose username is the member's id string, whose password is empty,
     *         and whose authorities contain the member's role
     */
    private UserDetails createUserDetails(Member member) {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(member.getRole().toString());

        return new User(
                String.valueOf(member.getId()),
                "", // 소셜로그인만 구현 -> 임의값
                Collections.singleton(grantedAuthority)
        );
    }
}