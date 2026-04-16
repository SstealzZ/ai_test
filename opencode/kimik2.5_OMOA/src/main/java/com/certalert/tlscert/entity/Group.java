package com.certalert.tlscert.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "groups_table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;
}
