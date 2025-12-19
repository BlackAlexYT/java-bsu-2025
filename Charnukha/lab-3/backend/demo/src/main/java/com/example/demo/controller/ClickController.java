package com.example.demo.controller;

import com.example.demo.model.Click;
import com.example.demo.repository.ClickRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clicks")
@CrossOrigin(origins = "http://localhost")
public class ClickController {

    private final ClickRepository repository;

    public ClickController(ClickRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Long getClicks() {
        return repository.findById(1L).map(Click::getCount).orElse(0L);
    }

    @PostMapping("/increment")
    public Long increment() {
        Click click = repository.findById(1L).orElse(new Click());
        click.setCount(click.getCount() + 1);
        repository.save(click);
        return click.getCount();
    }
}