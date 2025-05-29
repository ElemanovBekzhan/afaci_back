package org.example.affaci.Models.DTO.Mapper;


import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.example.affaci.Models.DTO.DetailProductResponseDTO;
import org.example.affaci.Models.Entity.Chemical_composition;
import org.example.affaci.Models.Entity.Mineral_composition;
import org.example.affaci.Models.Entity.Products;
import org.example.affaci.Models.Enum.Mineral;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ProductDetailsMapper {


    public DetailProductResponseDTO toDto(Products p) {
        DetailProductResponseDTO dto = new DetailProductResponseDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setRegionName(p.getRegion().getName());
        dto.setCategoryName(p.getCategories().getName());


        dto.setChemicalComposition(
                p.getChemicalCompositions().stream()
                        .map(this::toChemicalDto)
                        .collect(Collectors.toList())
        );
        dto.setMineralComposition(
                p.getMineralCompositions().stream()
                        .map(this::toMineralDto)
                        .collect(Collectors.toList())
        );
        return dto;
    }


    private DetailProductResponseDTO.ChemicalDTO toChemicalDto(Chemical_composition chem){
        DetailProductResponseDTO.ChemicalDTO dto = new DetailProductResponseDTO.ChemicalDTO();
        dto.setId(chem.getId());
        dto.setCompoundName(chem.getCompound_name());
        dto.setCompoundCategory(chem.getCompound_category());
        dto.setQuantity(chem.getQuantity());
        dto.setUnit(chem.getUnit());
        return dto;
    }

    private DetailProductResponseDTO.MineralDTO toMineralDto(Mineral_composition m){
        DetailProductResponseDTO.MineralDTO dto = new DetailProductResponseDTO.MineralDTO();
        dto.setId(m.getId());
        dto.setMineral(m.getMineral_name().getName());
        dto.setQuantity(m.getQuantity());
        dto.setUnit(m.getUnit());
        return dto;
    }
}
