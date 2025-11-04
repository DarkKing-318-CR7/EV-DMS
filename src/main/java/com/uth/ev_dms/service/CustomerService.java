package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.Customer;
import org.springframework.data.domain.Page;

public interface CustomerService {
    Page<Customer> myList(String username, int page, int size);
    Customer createForOwner(String username, String name, String email, String phone, String address);
    Customer updateForOwner(String username, Long id, String name, String email, String phone, String address);
}
