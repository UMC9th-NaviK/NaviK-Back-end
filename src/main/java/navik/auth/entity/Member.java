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
    private String socialType; /**
     * Update the member's name and return the same instance.
     *
     * @param name the new name for the member
     * @return the updated Member instance with the name set to the provided value
     */

    public Member update(String name) {
        this.name = name;
        return this;
    }

    /**
     * Retrieve the key associated with this member's role.
     *
     * @return the role's key as a String
     */
    public String getRoleKey() {
        return this.role.getKey();
    }
}