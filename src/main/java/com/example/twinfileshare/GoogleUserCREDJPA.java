package com.example.twinfileshare;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GoogleUserCREDJPA extends JpaRepository<GoogleUserCRED, Long> {

    @Query(value = "SELECT email  FROM google_user_cred", nativeQuery = true)
    List<String> getAllEmails();

    @Transactional
    int deleteByEmail(String email);

    GoogleUserCRED findByEmail(String email);
}
