package com.example.pleasework.controller;

import com.example.pleasework.entity.Post;
import com.example.pleasework.entity.Setup;
import com.example.pleasework.entity.User;
import com.example.pleasework.repository.PostRepository;
import com.example.pleasework.repository.SetupRepository;
import com.example.pleasework.repository.UserRepository;
import com.example.pleasework.service.ExcelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/qualite")
public class QualityController {

    private final SetupRepository setupRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ExcelService excelService;

    @Autowired
    public QualityController(SetupRepository setupRepository,
                             PostRepository postRepository,
                             UserRepository userRepository,
                             ExcelService excelService) {
        this.setupRepository = setupRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.excelService = excelService;
    }

    @GetMapping
    public String showTeamLeaderUploads(Model model) {
        List<Setup> setups = setupRepository.findByIdtlNotNull();

        List<Map<String, Object>> entries = new ArrayList<>();

        for (Setup setup : setups) {
            Post post = postRepository.findById(setup.getPostid()).orElse(null);
            User teamLeader = userRepository.findById(setup.getIdtl()).orElse(null);

            if (post != null && teamLeader != null) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("postName", post.getNom());
                entry.put("matricule", teamLeader.getMatricule());
                entry.put("datetl", setup.getDatetl());
                entry.put("setupId", setup.getSetupid());
                entries.add(entry);
            }
        }

        model.addAttribute("submissions", entries);
        model.addAttribute("posts", postRepository.findAll());
        return "quality-submissions"; // HTML à créer dans src/main/resources/templates
    }

    @GetMapping("/download/{setupId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Integer setupId) throws IOException {
        Setup setup = setupRepository.findById(setupId).orElseThrow();
        byte[] lockedFile = excelService.lockNewlyFilledCells(setup.getFile());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"checklist.xls\"")
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(lockedFile);
    }

    @PostMapping("/upload")
    public String uploadQualityFile(@RequestParam("postId") Integer postId,
                                    @RequestParam("file") MultipartFile file,
                                    HttpSession session) throws IOException {

        if (file == null || file.isEmpty()) {
            return "redirect:/qualite?error=noFile";
        }

        Integer idq = (Integer) session.getAttribute("userid"); // tu dois stocker ça à la connexion
        if (idq == null) {
            return "redirect:/login?error=sessionExpired";
        }

        Setup setup = new Setup();
        setup.setPostid(postId);
        setup.setFile(file.getBytes());
        setup.setDateq(LocalDateTime.now());
        setup.setIdq(idq);

        setupRepository.save(setup);

        return "redirect:/qualite?success";
    }

}
