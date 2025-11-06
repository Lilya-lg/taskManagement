package com.example.taskManagement.repository;

import com.example.taskManagement.entity.RequestType;
import com.example.taskManagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  User findByEmailIgnoreCase(String email);
  @Query("SELECT u FROM User u WHERE u.role = :role OR u.role = :role2")
  List<User> findUsersByRoleAndRequestType(@Param("role") User.Role role, @Param("role2") User.Role role2);
  boolean existsByEmailIgnoreCase(String email);

}
