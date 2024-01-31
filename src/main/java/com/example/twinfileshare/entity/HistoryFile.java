package com.example.twinfileshare.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
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
