package com.astrahealth.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.astrahealth.backend.entity.AccountStatus;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

	Optional<UserAccount> findByEmail(String email);

	Optional<UserAccount> findByAstronautProfileId(Long astronautId);

	boolean existsByEmail(String email);

	boolean existsByUsername(String username);

	boolean existsByUsernameAndIdNot(String username, Long id);

	@Query("""
			select account from UserAccount account
			where (:query is null
				or lower(account.email) like lower(concat('%', :query, '%'))
				or lower(coalesce(account.username, '')) like lower(concat('%', :query, '%'))
				or lower(coalesce(account.displayName, '')) like lower(concat('%', :query, '%')))
				and (:status is null or account.status = :status)
			""")
	Page<UserAccount> search(
			@Param("query") String query,
			@Param("status") AccountStatus status,
			Pageable pageable);

	@Query("select count(account) > 0 from UserAccount account "
			+ "where account.astronautProfile.id = :astronautId and account.id <> :userId")
	boolean isAstronautProfileLinkedToAnotherUser(
			@Param("astronautId") Long astronautId,
			@Param("userId") Long userId);

	@Query("select count(account) > 0 from UserAccount account "
			+ "where account.astronautProfile.id = :astronautId")
	boolean isAstronautProfileLinked(@Param("astronautId") Long astronautId);

	@Query("select count(distinct account.id) from UserAccount account join account.roles role "
			+ "where account.status = :status and role = :role")
	long countAccountsByStatusAndRole(
			@Param("status") AccountStatus status,
			@Param("role") UserRole role);
}
