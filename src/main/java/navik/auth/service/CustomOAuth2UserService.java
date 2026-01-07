package navik.auth.service;

import navik.auth.entity.Member;
import navik.auth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    @Setter
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();

    /**
     * Load or create a Member from provider attributes and produce an OAuth2User whose principal is the member's ID.
     *
     * @param userRequest the OAuth2 user request containing client registration and access token
     * @return an OAuth2User with authorities derived from the Member's role and attributes that include a "memberId" principal
     * @throws OAuth2AuthenticationException if the underlying OAuth2UserService fails to load the provider user
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName,
                oAuth2User.getAttributes());

        Member member = saveOrUpdate(attributes);

        // Member ID를 Principal Name으로 사용하기 위해 attributes에 memberId 추가
        Map<String, Object> newAttributes = new java.util.HashMap<>(attributes.getAttributes());
        newAttributes.put("memberId", String.valueOf(member.getId()));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRoleKey())),
                newAttributes,
                "memberId");
    }

    /**
     * Creates or updates a Member record based on the given OAuth attributes and persists the result.
     *
     * @param attributes OAuthAttributes containing the provider's user information (email, name, etc.)
     * @return the persisted Member reflecting any applied updates or the newly created entity
     */
    private Member saveOrUpdate(OAuthAttributes attributes) {
        Member member = memberRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName()))
                .orElse(attributes.toEntity());

        return memberRepository.save(member);
    }
}