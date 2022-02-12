package com.web.doitcommit.domain.files;

import lombok.*;

import javax.persistence.Entity;

import lombok.*;
import com.web.doitcommit.domain.BaseEntity;
import javax.persistence.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Files {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileNm;

    @Column(columnDefinition = "TEXT")
    private String filePath;

}
