package org.example.affaci.Models.DTO.Mapper;


import org.example.affaci.Models.DTO.ProductsDTO;
import org.example.affaci.Models.Entity.Products;
import org.example.affaci.Models.Enum.Language;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductsMapper {


//    public List<ProductsDTO> toDtoProducts(List<Products> products) {
//        return products.stream().map(this::toDto).collect(Collectors.toList());
//    }


    public ProductsDTO toDto(Products product, String lng) {
        ProductsDTO dto = new ProductsDTO();
        dto.setId(product.getId());
//        dto.setName(product.getName());
        if ("en".equalsIgnoreCase(lng)) {
            if (product.getTranslates() != null) {
                product.getTranslates().stream()
                        .filter(t -> t.getLanguage() == Language.EN)
                        .findFirst()
                        .ifPresentOrElse(
                                t -> dto.setName(t.getProduct_name()),
                                () -> dto.setName(product.getName()) // fallback на оригинальное имя
                        );
            } else {
                dto.setName(product.getName());
            }
        } else {
            dto.setName(product.getName());
        }
        dto.setCategories(product.getCategories().getName());
        dto.setRegion(product.getRegion().getName());
        dto.setDate(product.getCreated_at());
        return dto;
    }

}
