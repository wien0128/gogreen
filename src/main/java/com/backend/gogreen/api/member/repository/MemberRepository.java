package com.backend.gogreen.api.member.repository;

import com.backend.gogreen.api.member.entity.Member;
import com.backend.gogreen.api.member.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {

    Optional<Member> findById(Long userId);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByRefreshToken(String refreshToken);

//    @Query("select m from Member m" +
//            " where m.id=:memberId and m.role=:role")
//    Optional<Member> findByMemberIdAndRole(@Param("memberId")Long memberId, @Param("role") Role role );
}
