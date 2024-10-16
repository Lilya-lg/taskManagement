package com.example.taskManagement.repository;

import com.example.taskManagement.entity.ChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Long> {
}
