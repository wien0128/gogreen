package com.backend.gogreen.api.member.repository;

import com.backend.gogreen.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {

    Optional<Member> findByUserId(String userId);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByRefreshToken(String refreshToken);

//    @Query("select m from Member m" +
//            " where m.id=:memberId and m.role=:role")
//    Optional<Member> findByMemberIdAndRole(@Param("memberId")Long memberId, @Param("role") Role role );
}
