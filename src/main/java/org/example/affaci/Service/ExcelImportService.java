package org.example.affaci.Service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.affaci.Models.Entity.*;
import org.example.affaci.Models.Enum.Language;
import org.example.affaci.Models.Enum.Mineral;
import org.example.affaci.Models.Enum.Unit;
import org.example.affaci.Repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final ProductsRepository productsRepository;
    private final RegionsRepository regionsRepository;
    private final CategoriesRepository categoriesRepository;
    private final ProductTranslateRepo productTranslateRepo;
    private final photoRepository photoRepository;
    private final MinioService minioService;


    @Transactional
    public void importExcel(MultipartFile file) {
        try (InputStream fis = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(fis)) {
        log.info("Начинаем импорт файла: {}", file.getOriginalFilename());
            for (Sheet sheet : workbook) {
                String regionName = sheet.getSheetName();
                Regions region =
                        regionsRepository.findByNameIgnoreCase(regionName).orElseThrow(()-> new EntityNotFoundException(
                                "Регион не найден" + regionName));

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

                    Optional<Categories> catOpt = categoriesRepository.findByNameAndRegion(categoryName, region);
                    if (catOpt.isEmpty()) {
                        log.warn("Категория \"{}\" не найдена для региона \"{}\" — пропуск строки",
                                categoryName, region.getName());
                        continue;
                    }
                    Categories categories = catOpt.get();
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
                                log.warn("Не удалось получить корректное значение для хим. состава, продукт: " + productName);
                            }
                        }catch (NumberFormatException e){
                            log.warn("Не удалось преобразовать \"" + cellValue + "\" в число (хим. состав), продукт: " + productName);
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
                                log.warn("Не удалось получить корректное значение для аминокислотного состава, " +
                                        "продукт: " + productName);
                            }
                        }catch (NumberFormatException e){
                            log.warn("Не удалось преобразовать \"" + cellValue + "\" в число (аминокислотный состав), продукт: " + productName);
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
                            log.warn("Минерал \"" + meneralNameStr + "\" не найден в enum Mineral, пропускаем.");
                        }

                        try{
                            Double[] parsed = parseQuantityAndError(cellValue);
                            if(parsed[0] != null){
                                mineralComposition.setQuantity(parsed[0]);
                                mineralComposition.setError(parsed[1]);
                            } else {
                                log.warn("Не удалось получить корректное значение для минерального состава, продукт: " + productName);
                            }
                        }catch (NumberFormatException e){
                            log.warn("Не удалось преобразовать \"" + cellValue + "\" в число (минеральный состав), продукт: " + productName);
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
                                log.warn("Не удалось получить корректное значение для жирнокислотного состава, " +
                                        "продукт: " + productName);
                            }

                        }catch (NumberFormatException e){
                            log.warn("Не удалось преобразовать \"" + cellValue + "\" в число (жирнокислотный состав), продукт: " + productName);
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
    private static final int PHOTO_NAME_COL = 29; // AD




    //Импорт национального продукта
    @Transactional
    public void importExcelNationalFood(MultipartFile file,
                                        MultipartFile photosZip) throws Exception {

        // Распаковка ZIP
        Path tmpDir = Files.createTempDirectory("import-photos-");
        try(ZipInputStream zis = new ZipInputStream(photosZip.getInputStream())){
            ZipEntry ze;
            while((ze = zis.getNextEntry()) != null){
                Path out = tmpDir.resolve(ze.getName());
                if(ze.isDirectory()){
                    Files.createDirectories(out);
                }else{
                    Files.createDirectories(out.getParent());
                    Files.copy(zis, out, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }



        //Читаем Excel

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
                    for(int i = 0; i < chemNames.size(); i++){
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





                    //имя фото
                    Cell photoCell = row.getCell(PHOTO_NAME_COL);
                    if (photoCell != null) {
                        String allNames = photoCell.getStringCellValue().trim();
                        if (!allNames.isEmpty()) {
                            // разбиваем по ; или ,
                            String[] rawNames = allNames.split("\\s*[;,]\\s*");
                            for (String rawName : rawNames) {
                                Path photoPath = resolvePhoto(tmpDir, rawName);
                                if (photoPath != null) {
                                    String uploadedName = minioService.uploadPhoto(photoPath);

                                    photo photo = new photo();
                                    photo.setFilename(uploadedName);
                                    photo.setProduct(product);
                                    product.getPhotos().add(photo);
                                    log.info("✅ Фотo найдено: " + photoPath);
                                } else {
                                    log.warn("🔥 Фото не найдено: " + rawName);
                                }
                            }
                        }
                    }
                    /*if(photoCell!=null){
                        String photoName = photoCell.getStringCellValue().trim();
                        Path photoPath;
                        if(photoName .contains(".")) {
                            photoPath = tmpDir.resolve(photoName);
                        }else {
                            photoPath = findPhotoFile(tmpDir, photoName);
                        }

                        if(Files.exists(photoPath)){
                            photo photo = new photo();
                            photo.setFilename(photoName);
                            photo.setProduct(product);
                            product.getPhotos().add(photo);
                        }else{
                            System.out.println("Фото не найдено: "+photoName);
                        }
                    }*/

                    //-----Сохраняем продукт и перевод
                    productsRepository.save(product);


                    //Название на англ
                    String kgName = row.getCell(3).getStringCellValue().trim();
                    Products_translate tr = new Products_translate();
                    tr.setProduct(product);
                    tr.setLanguage(Language.EN);
                    tr.setProduct_name(kgName);
                    productTranslateRepo.save(tr);
                }
            }
        }finally {
            FileSystemUtils.deleteRecursively(tmpDir);
        }

    }

    /**
     * Ищет файл с именем rawName (с учётом расширения или без)
     * в любом месте внутри tmpDir.
     */
    private Path resolvePhoto(Path tmpDir, String rawName) throws IOException {
        boolean hasExt = rawName.contains(".");
        try (Stream<Path> stream = Files.walk(tmpDir)) {
            Optional<Path> found = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String fn = p.getFileName().toString();
                        if (hasExt) {
                            // ищем точное совпадение имени, игнорируя регистр
                            return fn.equalsIgnoreCase(rawName);
                        } else {
                            // ищем любую версию rawName.* (jpg/png/...),
                            // игнорируя регистр
                            return fn.toLowerCase().startsWith(rawName.toLowerCase() + ".");
                        }
                    })
                    .findFirst();
            return found.orElse(null);
        }
    }



    //Поиск фото, если оно без расширения в Excel
    private Path findPhotoFile(Path dir, String baseName) throws IOException {
        try(DirectoryStream<Path> ds = Files.newDirectoryStream(dir, baseName + ".")){
            for(Path p : ds){
                String ext = FilenameUtils.getExtension(p.getFileName().toString().toLowerCase());
                if(List.of("png", "jpg", "jpeg").contains(ext)){
                    return p;
                }
            }
        }
        return null;
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



    private static final int HEADER_ROW_COUNT2 = 6;
    private static final int CHEM_START_COL2 = 4; // E
    private static final int CHEM_END_COL2   = 9; // J
    private static final int MIN_START_COL2  = 10; // K
    private static final int MIN_END_COL2    = 14; // О



    //Импорт национального продукта
    @Transactional
    public void importExcelFinal(MultipartFile file) throws Exception {
        try(InputStream is = file.getInputStream();
            Workbook wb = new XSSFWorkbook(is);){


            //Считываем загаловки химии/минералов ровно один раз (из первого листа)
            Sheet firstSheet = wb.getSheetAt(0);
            Row nameRow = firstSheet.getRow(2);
            Row unitRow = firstSheet.getRow(3);
            List<String> chemNames = readRowCells(nameRow, CHEM_START_COL2, CHEM_END_COL2);
            List<String> chemUnits = readRowCells(unitRow, CHEM_START_COL2, CHEM_END_COL2);
            List<String> minNames  = readRowCells(nameRow, MIN_START_COL2, MIN_END_COL2);
            List<String> minUnits  = readRowCells(unitRow, MIN_START_COL2, MIN_END_COL2);


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
                for(int r = HEADER_ROW_COUNT2; r<= sheet.getLastRowNum(); r++){
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
                    product.setNational(false);


                    //Химический состав
                    for(int i = 0; i < chemNames.size(); i++){
                        Cell cell = row.getCell(CHEM_START_COL2 + i);
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
                        Cell cell = row.getCell(MIN_START_COL2 + i);
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
                    tr.setProduct(product);
                    tr.setLanguage(Language.KG);
                    tr.setProduct_name(kgName);
                    productTranslateRepo.save(tr);
                }
            }
        }

    }
}