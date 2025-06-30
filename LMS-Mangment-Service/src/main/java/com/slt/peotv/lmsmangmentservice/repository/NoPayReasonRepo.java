package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayReasonEntity;
import java.util.Optional;

@Repository
public interface NoPayReasonRepo extends JpaRepository<NoPayReasonEntity, Long> {
    Optional<NoPayReasonEntity> findNoPayReasonEntitiesByNoPay(NoPayEntity noPay);
}
