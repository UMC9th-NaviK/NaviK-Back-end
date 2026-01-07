package navik.auth.service;

import navik.auth.entity.Role;
import navik.auth.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final String name;
    private final String email;
    private final String socialType;
    private final String socialId;

    /**
     * Creates an OAuthAttributes instance holding provider-supplied user information.
     *
     * @param attributes        the original attribute map returned by the OAuth provider
     * @param nameAttributeKey  the attribute key used to identify the user's name in the provider data
     * @param name              the user's display name
     * @param email             the user's email address
     * @param socialType        the provider identifier (e.g., "google", "naver", "kakao")
     * @param socialId          the provider-specific user id
     */
    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email,
                           String socialType, String socialId) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.socialType = socialType;
        this.socialId = socialId;
    }

    /**
     * Create an OAuthAttributes instance mapped from the provider identified by registrationId.
     *
     * @param registrationId the OAuth provider identifier (e.g., "google", "naver", "kakao")
     * @param userNameAttributeName the attribute key used to identify the user's name in the provider response
     * @param attributes the full attribute map returned by the provider
     * @return an OAuthAttributes populated from the provider attributes, or `null` if the provider is unsupported
     */
    public static OAuthAttributes of(String registrationId, String userNameAttributeName,
                                     Map<String, Object> attributes) {

        return switch (registrationId) {
            case "google" -> ofGoogle(userNameAttributeName, attributes);
            case "naver" -> ofNaver(userNameAttributeName, attributes);
            case "kakao" -> ofKakao(userNameAttributeName, attributes);
            default -> null;
        };
    }

    /**
     * Create an OAuthAttributes instance populated from Google OAuth2 user information.
     *
     * @param userNameAttributeName the attribute key used by the OAuth2 provider to identify the user's name
     * @param attributes the original attribute map returned by Google's userinfo endpoint
     * @return an OAuthAttributes populated with name, email, socialType='google', socialId (from the `sub` attribute), the original attributes, and the provided nameAttributeKey
     */
    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .socialType("google")
                .socialId((String) attributes.get("sub"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    /**
     * Create an OAuthAttributes instance populated from a Naver provider response.
     *
     * @param userNameAttributeName the attribute name to use as the principal key
     * @param attributes the raw attributes map returned by Naver (expects a nested "response" map)
     * @return an OAuthAttributes populated with name, email, socialType set to "naver", socialId, and the provider response as attributes
     */
    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .socialType("naver")
                .socialId((String) response.get("id"))
                .attributes(response)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    /**
     * Create OAuthAttributes populated from a Kakao OAuth2 provider response.
     *
     * @param userNameAttributeName the attribute key used as the name identifier
     * @param attributes            the raw attribute map returned by Kakao's OAuth response
     * @return an OAuthAttributes instance with `name` set from `profile.nickname`, `email` from `kakao_account.email`,
     *         `socialType` set to "kakao", `socialId` set from `id`, and `attributes` set to the original map
     */
    public static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                .name((String) profile.get("nickname")) // todo: 카카오 디벨로퍼스 이름 권한 설정 ?
                .email((String) kakaoAccount.get("email"))
                .socialType("kakao")
                .socialId(String.valueOf(attributes.get("id")))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    /**
     * Convert this OAuthAttributes instance into a Member entity.
     *
     * The created Member has name, email, socialType, and socialId populated, and its role set to {@code Role.USER}.
     *
     * @return the Member populated with name, email, socialType, socialId, and role USER
     */
    public Member toEntity() {
        return Member.builder()
                .name(name)
                .email(email)
                .role(Role.USER)
                .socialType(socialType)
                .socialId(socialId)
                .build();
    }
}