package com.example.twinfileshare;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GoogleUserCREDJPA extends JpaRepository<GoogleUserCRED, Long> {

    @Query(nativeQuery = true, value = "SELECT email FROM google_user_cred")
    List<String> findAllEmail();
}
