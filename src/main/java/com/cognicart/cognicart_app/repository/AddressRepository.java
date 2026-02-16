package com.cognicart.cognicart_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cognicart.cognicart_app.model.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

}