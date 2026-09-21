package com.yesh.apartmentledger.master.referencetype.repository;

import com.yesh.apartmentledger.master.referencetype.entity.ReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferenceTypeRepository extends JpaRepository<ReferenceType, Long> {
}