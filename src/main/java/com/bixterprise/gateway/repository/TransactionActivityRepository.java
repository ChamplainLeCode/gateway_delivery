package com.bixterprise.gateway.repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bixterprise.gateway.domain.TransactionActivity;

@Repository
public interface TransactionActivityRepository extends JpaRepository<TransactionActivity, Long>{

	@Query("from TransactionActivity ta where ta.transactionId.id = :reference")
	Set<TransactionActivity> findByTransactionsId(
			@Param("reference") String reference);

	@Query("from TransactionActivity ta where ta.createdAt between :debut and :fin")
	Page<TransactionActivity> findAllByCreatedAt(Pageable page, @Param("debut") Date ddebut, @Param("fin") Date dfin);

	@Query("from TransactionActivity ta where ta.createdAt between :debut and :fin")
	List<TransactionActivity> findAllByCreatedAt(@Param("debut") Date ddebut, @Param("fin") Date dfin);

	@Query("from TransactionActivity ta where ta.createdAt >= :debut")
	Page<TransactionActivity> findAllAfterDate(Pageable page, @Param("debut") Date ddebut);

	@Query("from TransactionActivity ta where ta.createdAt <= :fin")
	Page<TransactionActivity> findAllBeforeDate(Pageable page, @Param("fin") Date time);

}
