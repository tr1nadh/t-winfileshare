package com.example.twinfileshare.repository;

import com.example.twinfileshare.entity.GoogleUserCRED;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GoogleUserCREDRepository extends JpaRepository<GoogleUserCRED, String> {


    GoogleUserCRED findByEmail(String email);

    @Query(value = "SELECT email FROM google_user_cred", nativeQuery = true)
    List<String> getAllEmails();

    @Query(value = "SELECT id FROM google_user_cred", nativeQuery = true)
    List<String> getAllIds();

    @Transactional
    int deleteByEmail(String email);

    @Query(value = "SELECT access_token FROM google_user_cred WHERE email = ?1",
            nativeQuery = true)
    String findAccessTokenByEmail(String email);
}
