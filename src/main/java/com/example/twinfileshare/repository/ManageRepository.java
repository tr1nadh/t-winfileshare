package com.example.twinfileshare.repository;

import com.example.twinfileshare.entity.SharedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManageRepository extends JpaRepository<SharedFile, String> {

    @Query("select id from SharedFile")
    List<String> findAllIds();
}
