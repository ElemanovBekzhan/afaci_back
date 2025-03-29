package org.example.affaci.Service;


import org.apache.poi.ss.usermodel.*;
import org.example.affaci.Models.Entity.*;
import org.example.affaci.Models.Enum.Mineral;
import org.example.affaci.Models.Enum.Unit;
import org.example.affaci.Repo.CategoriesRepository;
import org.example.affaci.Repo.ProductsRepository;
import org.example.affaci.Repo.RegionsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
public class ExcelImportService {

    private final ProductsRepository productsRepository;
    private final RegionsRepository regionsRepository;
    private final CategoriesRepository categoriesRepository;

    public ExcelImportService(ProductsRepository productsRepository, RegionsRepository regionsRepository, CategoriesRepository categoriesRepository) {
        this.productsRepository = productsRepository;
        this.regionsRepository = regionsRepository;
        this.categoriesRepository = categoriesRepository;
    }


    @Transactional
    public void importExcel(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String regionName = sheet.getSheetName();

                Regions region = regionsRepository.findByName(regionName);
                if (region == null) {
                    System.out.println("Region " + regionName + " not found");
                    continue;
                }

                Iterator<Row> rowIterator = sheet.iterator();
                if(!rowIterator.hasNext()) {
                    continue;
                }

                Row headerRow = rowIterator.next();
                Map<Integer, String> headerMap = new HashMap<>();
                for(Cell cell : headerRow) {
                    headerMap.put(cell.getColumnIndex(), cell.getStringCellValue().trim());
                }

                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    if(row == null){
                        continue;
                    }

                    Cell categoryCell = row.getCell(0);
                    if(categoryCell == null){
                        continue;
                    }
                    String categoryName = categoryCell.getStringCellValue().trim();
                    if(categoryName == null){
                        continue;
                    }

                    Categories categories = categoriesRepository.findByName(categoryName);
                    if (categories == null) {
                        System.out.println("Категория \"" + categoryName + "\" не найдена в БД. Пропускаем продукт.");
                        continue;
                    }

                    Cell productNameCell = row.getCell(1);
                    if(productNameCell == null){
                        continue;
                    }

                    String productName = productNameCell.getStringCellValue().trim();
                    if(productName == null){
                        continue;
                    }

                    Products product = new Products();
                    product.setName(productName);
                    product.setCategories(categories);
                    product.setRegion(region);

                    // ================ Химический состав ============================
                    for(int col = 2; col <=8; col++){
                        Cell cell = row.getCell(col);
                        if(cell == null){
                            continue;
                        }
                        String cellValue = cell.toString().trim();
                        if(cellValue.isEmpty() || cellValue.equals("-")){
                            continue;
                        }

                        Chemical_composition chemical_composition = new Chemical_composition();
                        String compoundName = headerMap.get(col);
                        chemical_composition.setCompound_name(compoundName);

                        try{
                            Double[] parsed = parseQuantityAndError(cellValue);
                            if(parsed[0] != null){
                                chemical_composition.setQuantity(parsed[0]);
                                chemical_composition.setError(parsed[1]);
                            } else {
                                System.out.println("Не удалось получить корректное значение для хим. состава, продукт: " + productName);
                            }
                        }catch (NumberFormatException e){
                            System.out.println("Не удалось преобразовать \"" + cellValue + "\" в число (хим. состав), продукт: " + productName);
                        }

                        chemical_composition.setProduct(product);
                        chemical_composition.setUnit(Unit.g);
                        product.getChemicalCompositions().add(chemical_composition);
                    }


                    // ================ Аминокислотный состав =========================
                    for(int col = 10; col <= 27; col++){
                        Cell cell = row.getCell(col);
                        if(cell == null){
                            continue;
                        }
                        String cellValue = cell.toString().trim();
                        if(cellValue.isEmpty() || cellValue.equals("-")){
                            continue;
                        }

                        Amino_acid_composition aminoAcidComposition = new Amino_acid_composition();
                        String aminoName = headerMap.get(col);
                        aminoAcidComposition.setAmino_acid_name(aminoName);

                        try{
                            Double[] parsed = parseQuantityAndError(cellValue);
                            if(parsed[0] != null){
                                aminoAcidComposition.setQuantity(parsed[0]);
                                aminoAcidComposition.setError(parsed[1]);
                            } else {
                                System.out.println("Не удалось получить корректное значение для аминокислотного состава, " +
                                        "продукт: " + productName);
                            }
                        }catch (NumberFormatException e){
                            System.out.println("Не удалось преобразовать \"" + cellValue + "\" в число (аминокислотный состав), продукт: " + productName);
                        }

                        aminoAcidComposition.setProduct(product);
                        aminoAcidComposition.setUnit(Unit.g);
                        product.getAminoAcidCompositions().add(aminoAcidComposition);
                    }

                    // ================= Минеральный состав ===================
                    for(int col = 29; col <= 47; col++){
                        Cell cell = row.getCell(col);
                        if(cell == null){
                            continue;
                        }
                        String cellValue = cell.toString().trim();
                        if(cellValue.isEmpty() || cellValue.equals("-")){
                            continue;
                        }

                        Mineral_composition mineralComposition = new Mineral_composition();
                        String meneralNameStr = headerMap.get(col);
                        try{
                            Mineral mineralName = Mineral.valueOf(meneralNameStr);
                            mineralComposition.setMineral_name(mineralName);
                        }catch (Exception e){
                            System.out.println("Минерал \"" + meneralNameStr + "\" не найден в enum Mineral, пропускаем.");
                        }

                        try{
                            Double[] parsed = parseQuantityAndError(cellValue);
                            if(parsed[0] != null){
                                mineralComposition.setQuantity(parsed[0]);
                                mineralComposition.setError(parsed[1]);
                            } else {
                                System.out.println("Не удалось получить корректное значение для минерального состава, продукт: " + productName);
                            }
                        }catch (NumberFormatException e){
                            System.out.println("Не удалось преобразовать \"" + cellValue + "\" в число (минеральный состав), продукт: " + productName);
                        }

                        mineralComposition.setProduct(product);
                        mineralComposition.setUnit(Unit.g);
                        product.getMineralCompositions().add(mineralComposition);
                    }


                    // ======================== Жирнокислотный состав ==========================


                    for(int col = 49; col <= 61; col++){
                        Cell cell = row.getCell(col);
                        if(cell == null){
                            continue;
                        }
                        String cellValue = cell.toString().trim();
                        if(cellValue.isEmpty() || cellValue.equals("-")){
                            continue;
                        }

                        Fatty_acid_composition fattyAcidComposition = new Fatty_acid_composition();
                        String fattyName = headerMap.get(col);
                        fattyAcidComposition.setFatty_acid_name(fattyName);

                        try{
                            Double[] parsed = parseQuantityAndError(cellValue);
                            if(parsed[0] != null){
                                fattyAcidComposition.setQuantity(parsed[0]);
                                fattyAcidComposition.setError(parsed[1]);
                            } else {
                                System.out.println("Не удалось получить корректное значение для жирнокислотного состава, " +
                                        "продукт: " + productName);
                            }

                        }catch (NumberFormatException e){
                            System.out.println("Не удалось преобразовать \"" + cellValue + "\" в число (жирнокислотный состав), продукт: " + productName);
                        }

                        fattyAcidComposition.setProduct(product);
                        fattyAcidComposition.setUnit(Unit.g);
                        product.getFattyAcidCompositions().add(fattyAcidComposition);
                    }

                    productsRepository.save(product);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    private Double[] parseQuantityAndError(String cellValue) {
        String numericStr = cellValue.replace(",", ".").trim();
        Double quantity = null;
        Double error = null;
        try {
            if (numericStr.contains("±")) {
                String[] parts = numericStr.split("±");
                quantity = Double.parseDouble(parts[0].trim());
                error = Double.parseDouble(parts[1].trim());
            } else {
                quantity = Double.parseDouble(numericStr);
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка преобразования числа: " + cellValue);
        }
        return new Double[]{quantity, error};
    }
}