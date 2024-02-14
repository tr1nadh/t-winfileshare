package com.example.twinfileshare.repository;

import com.example.twinfileshare.entity.SharedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends JpaRepository<SharedFile, String> {
}
