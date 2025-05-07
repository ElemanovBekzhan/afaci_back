package org.example.affaci.Controller;


import lombok.RequiredArgsConstructor;
import org.example.affaci.Repo.ProductsRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final ProductsRepository prRepo;


    @GetMapping
    public Map<String, Long> getStats(){
        long products = prRepo.count();
        long national = prRepo.countByNationalTrue();
        return Map.of(
                "products", products,
                "national", national
        );
    }
}
