package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.Customer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepo extends JpaRepository<Customer, Long> {

    List<Customer> findByOwnerId(Long ownerId);
    boolean existsBySdt(String sdt);
    boolean existsBySdtAndIdNot(String sdt, Long id);

    @Query("""
           select c from Customer c
           where lower(c.ten) like lower(concat('%', :kw, '%'))
              or lower(c.sdt) like lower(concat('%', :kw, '%'))
           """)
    List<Customer> searchAll(@Param("kw") String kw);

    @Query("""
           select c from Customer c
           where c.ownerId = :ownerId
             and ( lower(c.ten) like lower(concat('%', :kw, '%'))
                   or lower(c.sdt) like lower(concat('%', :kw, '%')) )
           """)
    List<Customer> searchMine(@Param("ownerId") Long ownerId, @Param("kw") String kw);


    @Query("""
        SELECT c FROM Customer c ORDER BY c.createdAt DESC
    """)
    List<Customer> findLatestCustomers(Pageable pageable);


    @Query("""
    select count(c) 
    from Customer c 
    where c.ownerId in (
        select u.id from User u where u.dealer.id = :dealerId
    )
    """)
    Integer countByDealer(@Param("dealerId") Long dealerId);

}
