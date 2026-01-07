package navik.auth.repository;

import navik.auth.entity.Member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    /**
 * Finds a member by their email address.
 *
 * @param email the member's email address to search for
 * @return an Optional containing the Member with the matching email if found, or empty if none exists
 */
Optional<Member> findByEmail(String email);
}