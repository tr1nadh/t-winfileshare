package com.example.twinfileshare.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
public class HistoryFile {

    @Id
    private String id;
    private String filename;
    private String sharableLink;
    private String email;

    @Override
    public String toString() {
        return filename;
    }
}
