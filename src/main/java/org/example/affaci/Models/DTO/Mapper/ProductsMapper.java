package org.example.affaci.Models.DTO.Mapper;


import org.example.affaci.Models.DTO.ProductsDTO;
import org.example.affaci.Models.Entity.Products;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductsMapper {


    public List<ProductsDTO> toDtoProducts(List<Products> products) {
        return products.stream().map(this::toDto).collect(Collectors.toList());
    }


    public ProductsDTO toDto(Products product) {
        ProductsDTO dto = new ProductsDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setCategories(product.getCategories().getName());
        dto.setRegion(product.getRegion().getName());
        dto.setDate(product.getCreated_at());
        return dto;
    }

}
