package navik.auth.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@AllArgsConstructor
@Builder
@RedisHash(value = "refreshToken", timeToLive = 60 * 60 * 24 * 14) // 14일
public class RefreshToken {

    @Id
    private String id; // User ID (PK)

    @Indexed
    private String token;

    /**
     * Update the stored refresh token for this entity.
     *
     * @param token the new refresh token string to store
     */
    public void updateToken(String token) {
        this.token = token;
    }
}