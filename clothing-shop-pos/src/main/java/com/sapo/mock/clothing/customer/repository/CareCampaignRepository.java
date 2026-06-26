package com.sapo.mock.clothing.customer.repository;

import com.sapo.mock.clothing.entity.CareCampaign; // <-- Đảm bảo đã import đúng Entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CareCampaignRepository extends JpaRepository<CareCampaign, Integer> {

}