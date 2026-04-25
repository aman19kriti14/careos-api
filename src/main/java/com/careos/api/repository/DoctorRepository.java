package com.careos.api.repository;

import com.careos.api.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    Optional<Doctor> findByPhone(String phone);

    Optional<Doctor> findByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);
}