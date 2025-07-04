package org.example.affaci.Controller;


import lombok.RequiredArgsConstructor;
import org.example.affaci.Models.DTO.DetailProductResponseDTO;
import org.example.affaci.Models.DTO.Mapper.ProductsMapper;
import org.example.affaci.Models.DTO.ProductsDTO;
import org.example.affaci.Repo.CategoriesRepository;
import org.example.affaci.Repo.ProductsRepository;
import org.example.affaci.Repo.RegionsRepository;
import org.example.affaci.Service.ProductsService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductsController {


    private final ProductsService productsService;
    private final CategoriesRepository categoriesRepository;
    private final RegionsRepository regionsRepository;
    private final ProductsRepository productsRepository;
    private final ProductsMapper productsMapper;


    @GetMapping
    public ResponseEntity<Page<ProductsDTO>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required=false)      String search,
            @RequestParam(required=false)      String category,
            @RequestParam(required=false)      String region,
            @RequestParam(defaultValue = "ru") String lng
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<ProductsDTO> result = productsService.findFiltered(search, category, region, pageable, lng);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/details/{id}")
    public ResponseEntity<DetailProductResponseDTO> getProduct(@PathVariable UUID id,
                                                               @RequestParam(defaultValue = "ru")String lng) {
        return ResponseEntity.ok(productsService.getProductById(id, lng));
    }



    @GetMapping("/national")
    public ResponseEntity<?> getNatioanl(@RequestParam(defaultValue = "ru")String lng){
        return ResponseEntity.ok(productsService.getNatioanlProducts(lng));
    }

    @GetMapping("/getCategoryList")
    public ResponseEntity<?> getCategoryList(){
        return ResponseEntity.ok(categoriesRepository.findAllIdAndName());
    }

    @GetMapping("/getRegionsList")
    public ResponseEntity<?> getRegionsList(){
        return ResponseEntity.ok(regionsRepository.findAllIdAndName());
    }



    @GetMapping("/search")
    public List<ProductsDTO> searchProducts(@RequestParam String name,
                                            @RequestParam(defaultValue = "ru") String lng){
        return productsRepository.findByNameContainingIgnoreCase(name).stream()
                .map(product -> productsMapper.toDto(product, lng))
                .toList();
    }

    @GetMapping("/searchcategory")
    public List<ProductsDTO> searchProductsByCategory(@RequestParam UUID id,
                                                      @RequestParam(defaultValue = "ru") String lng){
        return productsRepository.findAllByCategoriesId(id).stream()
                .map(product -> productsMapper.toDto(product, lng))
                .toList();
    }

    @GetMapping("/searchregion")
    public List<ProductsDTO> searchProductsByRegion(@RequestParam UUID id,
                                                    @RequestParam(defaultValue = "ru") String lng){
        return productsRepository.findAllByRegionId(id).stream()
                .map(product -> productsMapper.toDto(product, lng))
                .toList();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteNationalProducts(){
        productsService.deleteAllNationalProducts();
        return ResponseEntity.ok().build();
    }


}
