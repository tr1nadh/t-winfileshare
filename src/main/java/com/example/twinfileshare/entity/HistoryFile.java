package com.example.twinfileshare.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Getter
@Setter
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
