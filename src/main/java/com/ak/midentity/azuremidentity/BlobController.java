package com.ak.midentity.azuremidentity;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.Model;

@RestController
@RequestMapping("/")
public class BlobController {

    @Autowired
    private final AzureBlobAdapter azureBlobAdapter;

    public BlobController(AzureBlobAdapter azureBlobAdapter) {
        this.azureBlobAdapter = azureBlobAdapter;
    }

    /**
     * Upload file to Azure Blob Storage
     * 
     * @param file
     * @param model
     * @return
     */
    @PostMapping("/blob/upload")
    public ModelAndView uploadFileToBlob(@RequestParam("file") MultipartFile file, Model model) {
        String containerName = "uploads";
        String fileUrl = azureBlobAdapter.uploadFile(containerName, file);
        if (fileUrl != null) {
            model.addAttribute("message", "File uploaded successfully: " + file.getOriginalFilename());
            model.addAttribute("fileUrl", fileUrl);
            return new ModelAndView("success");
        } else {
            model.addAttribute("message", "File upload failed: ");
            return new ModelAndView("upload");
        }
    }

    /**
     * Upload file to Azure Blob Storage
     * 
     * @return
     */
    @GetMapping("/blob/upload")
    public ModelAndView uploadFile() {
        return new ModelAndView("upload");
    }

    /**
     * Display the home page.
     * 
     * @param model
     * @return
     */
    @GetMapping("/")
    public ModelAndView home(Model model) {
        model.addAttribute("message", "Azure Blob Storage demo application");
        return new ModelAndView("index");
    }

    /**
     * Display view for all files in Azure storage container
     * 
     * @param model
     * @return
     */
    @GetMapping("blob/files")
    public ModelAndView listUploadedFiles(Model model) {
        try {
            List<String> fileUrls = azureBlobAdapter.listFiles();
            model.addAttribute("files", fileUrls);
            return new ModelAndView("files");
        } catch (Exception e) {
            model.addAttribute("message", "Failed to retrieve files: " + e.getMessage());
            return new ModelAndView("files");
        }
    }

}
