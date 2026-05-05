package edu.cit.caones.splitshare.features.groups.repository;

import edu.cit.caones.splitshare.features.groups.entity.BalanceSettlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BalanceSettlementRepository extends JpaRepository<BalanceSettlement, Long> {
    Optional<BalanceSettlement> findFirstByGroup_IdAndUserA_IdAndUserB_IdAndSettledAtIsNull(
            Long groupId,
            Long userAId,
            Long userBId
    );

    List<BalanceSettlement> findByGroup_IdAndSettledAtIsNotNull(Long groupId);

    List<BalanceSettlement> findByGroup_IdAndSettledAtIsNull(Long groupId);
}
