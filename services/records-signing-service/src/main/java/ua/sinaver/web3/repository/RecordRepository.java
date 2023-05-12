package ua.sinaver.web3.repository;

import ua.sinaver.web3.data.Record;

import java.util.List;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface RecordRepository extends CrudRepository<Record, Integer> {
    // JPA: UPGRADE_SKIPLOCKED - PESSIMISTIC_WRITE with a
    // javax.persistence.lock.timeout setting of -2
    // https://docs.jboss.org/hibernate/orm/5.0/userguide/html_single/chapters/locking/Locking.html
    @QueryHints(@QueryHint(name = AvailableSettings.JAKARTA_LOCK_TIMEOUT, value = "-2"))
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Record> findBySignedFalse(Pageable pageable);
}
