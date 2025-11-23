package com.uth.ev_dms.domain;

import com.uth.ev_dms.domain.Customer;

import java.util.List;

public interface CustomerService {

    Customer create(Customer c);
    Customer update(Customer c);
    Customer findById(Long id);

    List<Customer> findAll();
    List<Customer> findMine(Long ownerId);

    List<Customer> searchAll(String kw);
    List<Customer> searchMine(Long ownerId, String kw);

    void delete(Long id);
}
