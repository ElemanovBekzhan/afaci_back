package org.example.affaci.Controller;


import org.example.affaci.Service.ExcelImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final ExcelImportService excelImportService;

    @Autowired
    public ImportController(ExcelImportService excelImportService) {
        this.excelImportService = excelImportService;
    }


    @GetMapping("")
    public ResponseEntity<?> importExcel(@RequestParam("filePath") String filePath) {
        try{
            excelImportService.importExcel(filePath);
            return ResponseEntity.ok("Import successful");
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Import failed" + e.getMessage());
        }
    }
}
