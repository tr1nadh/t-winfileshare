package com.example.twinfileshare.repository;

import com.example.twinfileshare.entity.HistoryFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends JpaRepository<HistoryFile, String> {
}
