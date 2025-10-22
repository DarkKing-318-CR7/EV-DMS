package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepo extends JpaRepository<OrderHdr, Long> {

    // Dealer Staff – “Đơn của tôi”
    List<OrderHdr> findBySalesStaffIdOrderByIdDesc(Long staffId);

    // Dealer Manager – tất cả đơn theo đại lý
    @Query("select o from OrderHdr o where o.dealerId = :dealerId order by o.id desc")
    List<OrderHdr> findAllForDealer(@Param("dealerId") Long dealerId);

    List<OrderHdr> findByDealerIdOrderByIdDesc(Long dealerId);

    // EVM – tất cả đơn theo trạng thái (không theo region)
    List<OrderHdr> findByStatusOrderByIdDesc(OrderStatus status);

    // EVM – tất cả đơn theo region
    @Query("""
        select o from OrderHdr o
        where o.dealerId in (
            select d.id from Dealer d
            where d.regionId = :regionId
        )
        order by o.id desc
    """)
    List<OrderHdr> findAllByDealerRegion(@Param("regionId") Long regionId);

    // EVM – lọc theo trạng thái + region
    @Query("""
        select o from OrderHdr o
        where o.status = :status
          and o.dealerId in (
              select d.id from Dealer d
              where d.regionId = :regionId
          )
        order by o.id desc
    """)
    List<OrderHdr> findByStatusAndDealerRegion(@Param("status") OrderStatus status,
                                               @Param("regionId") Long regionId);
}
