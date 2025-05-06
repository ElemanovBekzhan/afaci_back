package org.example.affaci.Service;


import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.affaci.Models.Entity.*;
import org.example.affaci.Models.Enum.Language;
import org.example.affaci.Models.Enum.Mineral;
import org.example.affaci.Models.Enum.Unit;
import org.example.affaci.Repo.CategoriesRepository;
import org.example.affaci.Repo.ProductTranslateRepo;
import org.example.affaci.Repo.ProductsRepository;
import org.example.affaci.Repo.RegionsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final ProductsRepository productsRepository;
    private final RegionsRepository regionsRepository;
    private final CategoriesRepository categoriesRepository;
    private final ProductTranslateRepo productTranslateRepo;




    @Transactional
    public void importExcel(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String regionName = sheet.getSheetName();

                Regions region =
                        regionsRepository.findByNameIgnoreCase(regionName).orElseThrow(()-> new IllegalArgumentException(
                                "Региона не найден" + regionName));
                /*if (region == null) {
                    System.out.println("Region " + regionName + " not found");
                    continue;
                }*/

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




    private static final int HEADER_ROW_COUNT = 6;
    private static final int CHEM_START_COL = 4; // E
    private static final int CHEM_END_COL   = 9; // J
    private static final int MIN_START_COL  = 10; // K
    private static final int MIN_END_COL    = 28; // AC


    @Transactional
    public void importExcelFinal(MultipartFile file) throws Exception {
        try(InputStream is = file.getInputStream();
            Workbook wb = new XSSFWorkbook(is);){


            //Считываем загаловки химии/минералов ровно один раз (из первого листа)
            Sheet firstSheet = wb.getSheetAt(0);
            Row nameRow = firstSheet.getRow(2);
            Row unitRow = firstSheet.getRow(3);
            List<String> chemNames = readRowCells(nameRow, CHEM_START_COL, CHEM_END_COL);
            List<String> chemUnits = readRowCells(unitRow, CHEM_START_COL, CHEM_END_COL);
            List<String> minNames  = readRowCells(nameRow, MIN_START_COL, MIN_END_COL);
            List<String> minUnits  = readRowCells(unitRow, MIN_START_COL, MIN_END_COL);


            //Перебираем все листы - каждый лист соответствует новому региону
            for(Sheet sheet : wb){
                String sheetName = sheet.getSheetName().trim();
                Regions region =
                        regionsRepository.findByNameIgnoreCase(sheetName).orElseThrow(() -> new IllegalArgumentException(
                                "Региона не найден" + sheetName));
                /*if (region == null) {
                    System.out.println("Region " + sheetName + " not found");
                    continue;
                }*/
                //Обход строк с данными начиная с 7-й
                for(int r = HEADER_ROW_COUNT; r<= sheet.getLastRowNum(); r++){
                    Row row = sheet.getRow(r);
                    if(row == null || row.getCell(2) == null) continue;

                    // --- категория точно так же ищем через categoryRepository ---
                    String excelCat = row.getCell(1).getStringCellValue().trim();
                    String dbCatName = mapCategoryName(excelCat);
                    Categories categories =
                            categoriesRepository.findByNameAndRegion(dbCatName, region).orElseThrow(() -> new IllegalArgumentException("Категория не найдена " + dbCatName + " Для региона " + region.getName()));
                    /*if(categories == null){
                        System.out.println("Category " + dbCatName + " not found");
                        continue;
                    }*/

                    // --- создаём продукт и устанавливаем регион из имени листа ---
                    Products product = new Products();
                    product.setName(row.getCell(2).getStringCellValue().trim());
                    product.setCategories(categories);
                    product.setRegion(region);
                    product.setNational(true);


                    //Химический состав
                    for(int i = 0; i < minNames.size(); i++){
                        Cell cell = row.getCell(CHEM_START_COL + i);
                        if(cell != null && cell.getCellType() == CellType.NUMERIC){
                            Chemical_composition chem = new Chemical_composition();
                            chem.setProduct(product);
                            chem.setCompound_name(chemNames.get(i));
                            chem.setQuantity(cell.getNumericCellValue());
                            chem.setUnit(Unit.valueOf(chemUnits.get(i)));
                            product.getChemicalCompositions().add(chem);

                        }
                    }

                    //---------Минеральный состав ------
                    for(int i = 0; i < minNames.size(); i++){
                        Cell cell = row.getCell(MIN_START_COL + i);
                        if(cell != null && cell.getCellType() == CellType.NUMERIC){
                            String nameMin = minNames.get(i);
                            Mineral enumMin = findMineralByName(nameMin);

                            Mineral_composition mineral = new Mineral_composition();
                            mineral.setProduct(product);
                            mineral.setMineral_name(enumMin);
                            mineral.setQuantity(cell.getNumericCellValue());
                            mineral.setUnit(Unit.valueOf(minUnits.get(i)));
                            product.getMineralCompositions().add(mineral);
                        }
                    }

                    //-----Сохраняем продукт и перевод

                    productsRepository.save(product);

                    String kgName = row.getCell(3).getStringCellValue().trim();
                    Products_translate tr = new Products_translate();
                    tr.setProducts(product);
                    tr.setLanguage(Language.EN);
                    tr.setProduct_name(kgName);
                    productTranslateRepo.save(tr);
                }
            }
        }

    }

    // Вспомогательный метод для считывания строк ячеек в List<String>
    private List<String> readRowCells(Row row, int startCol, int endCol) {
        List<String> result = new ArrayList<>();
        for (int c = startCol; c <= endCol; c++) {
            Cell cell = row.getCell(c);
            result.add(cell == null
                    ? ""
                    : cell.getStringCellValue().trim());
        }
        return result;
    }
    private String mapCategoryName(String excel) {
        switch (excel) {
            case "Мясо и мясные продукты":   return "Мясной";
            case "Крахмалосодержащие продукты": return "Крахмалосодержащие";
            case "Ореховые":                   return "Орехи";
            case "Масло и жировые продукт":    return "Масло";
            case "Зерно и Зерновые продукты":  return "Зерновые";
            case "Молоко и молочные продукты": return "Молочный";
            case "Сахаросодержащий продукт":  return "Сахаросодержащий";
            case "Масло и жировые продукты":  return "Масло";
            default: return excel;
        }
    }

    private Mineral findMineralByName(String name){
        return Arrays.stream(Mineral.values())
                .filter(m -> m.getName().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Минерал не найден в Enum: " + name));
    }
}