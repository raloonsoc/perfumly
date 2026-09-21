package com.ralonsoc.backend.perfume;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "accords")
@Getter
@Setter
public class Accord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Name can't be empty")
    @Size(max = 100)
    @Column(nullable = false, length = 100, unique = true)
    private String name;
}
