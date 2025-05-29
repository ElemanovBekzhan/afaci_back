package org.example.affaci.Service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import org.example.affaci.Models.DTO.DetailProductResponseDTO;
import org.example.affaci.Models.DTO.Mapper.ProductDetailsMapper;
import org.example.affaci.Models.DTO.Mapper.ProductsMapper;
import org.example.affaci.Models.DTO.ProductResponseDTO;
import org.example.affaci.Models.DTO.ProductsDTO;
import org.example.affaci.Models.Entity.Categories;
import org.example.affaci.Models.Entity.Products;
import org.example.affaci.Models.Entity.Regions;
import org.example.affaci.Repo.ProductsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductsService {

    private final ProductsRepository productsRepository;
    private final ProductsMapper productsMapper;
    private final ProductDetailsMapper productDetailsMapper;
    private final MinioService minioService;





    @Transactional(readOnly = true)
    public Page<ProductsDTO> getProducts(Pageable pageable) {
        return productsRepository
                .findAll(pageable)
                .map(productsMapper::toDto);
    }


    public DetailProductResponseDTO getProductById(UUID id) {
        Products products =
                productsRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Продукт с ID: " + id + " не " +
                        "найден"));
        return productDetailsMapper.toDto(products);
    }


    public List<ProductResponseDTO> getNatioanlProducts(){
        List<Products> products = productsRepository.findAllByNationalIsTrue();
        if(products.isEmpty()){
            throw new EntityNotFoundException("Националные продукты не найдены");
        }

        return products.stream()
                .map(p -> {
                    ProductResponseDTO dto = new ProductResponseDTO();
                    dto.setId(p.getId());
                    dto.setName(p.getName());
                    dto.setCategories(p.getCategories().getName());
                    dto.setDatе(p.getCreated_at());
                    dto.setRegion(p.getRegion().getName());

                    List<String> urls = p.getPhotos().stream()
                            .map(photo -> minioService.getObjectUrl(photo.getFilename()))
                            .collect(Collectors.toList());
                    dto.setPhotoUrls(urls);
                    return dto;
                }).collect(Collectors.toList());

    }



    @Transactional
    public void deleteAllNationalProducts() {
        productsRepository.deleteAllByNationalTrue();
    }

    public Page<ProductsDTO> findFiltered(String search, String category, String region, Pageable pageable, String lng) {


        Specification<Products> spec = Specification.where(null);

        if (StringUtils.hasText(search)) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("name")),
                            "%" + search.trim().toLowerCase() + "%"
                    )
            );
        }

        // Фильтрация по связанной сущности Categories
        if (StringUtils.hasText(category)) {
            spec = spec.and((root, query, cb) -> {
                Join<Products, Categories> joinCat = root.join("categories");
                return cb.equal(
                        cb.lower(joinCat.get("name")),
                        category.toLowerCase()
                );
            });
        }

        // Фильтрация по связанной сущности Regions
        if (StringUtils.hasText(region)) {
            spec = spec.and((root, query, cb) -> {
                Join<Products, Regions> joinReg = root.join("region");
                return cb.equal(
                        cb.lower(joinReg.get("name")),
                        region.toLowerCase()
                );
            });
        }

        return productsRepository.findAll(spec, pageable)
                .map(productsMapper::toDto);
    }

}
