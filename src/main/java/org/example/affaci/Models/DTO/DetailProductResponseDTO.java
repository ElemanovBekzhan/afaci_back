package org.example.affaci.Models.DTO;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetailProductResponseDTO {

    UUID id;
    String name;
    String regionName;
    String categoryName;
    List<ChemicalDTO> chemicalComposition;
    List<MineralDTO> mineralComposition;


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ChemicalDTO{
        UUID id;
        private String compoundName;
        private String compoundCategory;
        private Double quantity;
    }
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MineralDTO{
        private UUID id;
        private String mineral;
        private Double quantity;
    }


}
