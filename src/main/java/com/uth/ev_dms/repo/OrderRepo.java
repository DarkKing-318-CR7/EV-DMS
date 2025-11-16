package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepo extends JpaRepository<OrderHdr, Long> {

    // =======================================================
    // ===============  DEALER STAFF (MY ORDERS)  ============
    // =======================================================

    // Cũ: chỉ lọc theo salesStaffId + dealerId (vẫn giữ lại để chỗ khác dùng)
    List<OrderHdr> findBySalesStaffIdAndDealerIdOrderByIdDesc(Long salesStaffId, Long dealerId);

    // Cũ: chỉ lọc theo salesStaffId (không có dealer)
    List<OrderHdr> findBySalesStaffIdOrderByIdDesc(Long salesStaffId);

    // Mới: “Đơn của tôi” = salesStaffId == userId OR createdBy == userId
    @Query("""
           select o from OrderHdr o
           where (o.salesStaffId = :userId or o.createdBy = :userId)
           order by o.id desc
           """)
    List<OrderHdr> findMyOrdersForStaff(@Param("userId") Long userId);

    // Mới: “Đơn của tôi” + đúng dealer
    @Query("""
           select o from OrderHdr o
           where o.dealerId = :dealerId
             and (o.salesStaffId = :userId or o.createdBy = :userId)
           order by o.id desc
           """)
    List<OrderHdr> findMyOrdersForStaff(@Param("dealerId") Long dealerId,
                                        @Param("userId") Long userId);

    // =======================================================
    // ===============  DEALER MANAGER VIEW  =================
    // =======================================================

    // Manager – tất cả đơn của 1 dealer (dùng cho /dealer/orders, /dealer/orders/pending,...)
    @Query("select o from OrderHdr o where o.dealerId = :dealerId order by o.id desc")
    List<OrderHdr> findAllForDealer(@Param("dealerId") Long dealerId);

    // Có thể dùng để thống kê đơn theo dealer
    List<OrderHdr> findByDealerIdOrderByIdDesc(Long dealerId);

    // =======================================================
    // ====================  EVM VIEW  =======================
    // =======================================================

    // EVM – tất cả đơn theo trạng thái (không filter region)
    List<OrderHdr> findByStatusOrderByIdDesc(OrderStatus status);

    // EVM – tất cả đơn theo region
    @Query("""
      select o from OrderHdr o
      where o.dealerId in (
          select d.id from Dealer d
          where d.region = :region
      )
      order by o.id desc
      """)
    List<OrderHdr> findAllByDealerRegion(@Param("region") String region);

    // EVM – lọc theo trạng thái + region
    @Query("""
      select o from OrderHdr o
      where o.status = :status
        and o.dealerId in (
            select d.id from Dealer d
            where d.region = :region
        )
      order by o.id desc
      """)
    List<OrderHdr> findByStatusAndDealerRegion(@Param("status") OrderStatus status,
                                               @Param("region") String region);
}
