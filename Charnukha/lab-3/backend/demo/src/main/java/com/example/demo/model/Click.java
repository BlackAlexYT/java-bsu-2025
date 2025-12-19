package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "clicks")
public class Click {
    @Id
    private Long id = 1L;
    private Long count = 0L;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
}