package com.example.pleasework.controller;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import com.example.pleasework.entity.Post;
import com.example.pleasework.entity.Setup;
import com.example.pleasework.entity.User;
import com.example.pleasework.repository.PostRepository;
import com.example.pleasework.repository.SetupRepository;
import com.example.pleasework.repository.UserRepository;
import com.example.pleasework.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/teamleader")
public class TeamLeaderController {

    private final SetupRepository setupRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ExcelService excelService;

    public TeamLeaderController(SetupRepository setupRepository,
                                PostRepository postRepository,
                                UserRepository userRepository,
                                ExcelService excelService) {
        this.setupRepository = setupRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.excelService = excelService;
    }

    @GetMapping
    public String showSubmissions(Model model) {
        // Récupère les setups soumis par les opérateurs (idop non null)
        List<Setup> operatorSetups = setupRepository.findByIdopNotNull();

        // Construction d’un objet DTO pour afficher les données
        List<Map<String, Object>> entries = new ArrayList<>();

        for (Setup setup : operatorSetups) {
            Map<String, Object> entry = new HashMap<>();

            Post post = postRepository.findById(setup.getPostid()).orElse(null);
            User user = userRepository.findById(setup.getIdop()).orElse(null);

            if (post != null && user != null) {
                entry.put("postName", post.getNom());
                entry.put("matricule", user.getMatricule());
                entry.put("dateop", setup.getDateop());
                entry.put("setupId", setup.getSetupid()); // pour le lien de téléchargement
                entry.put("postId", post.getPostid());
                entries.add(entry);
            }
        }

        model.addAttribute("submissions", entries);
        return "teamleader-submissions"; // ➜ page HTML à créer
    }
    private boolean isXLS(byte[] data) {
        if (data == null || data.length < 8) return false;
        return data[0] == (byte) 0xD0
                && data[1] == (byte) 0xCF
                && data[2] == (byte) 0x11
                && data[3] == (byte) 0xE0;
    }
    // Téléchargement du fichier avec verrouillage
    @GetMapping("/download/{setupId}")
    public ResponseEntity<byte[]> downloadSetup(@PathVariable Integer setupId) throws IOException {
        Setup setup = setupRepository.findById(setupId).orElseThrow();

        if (setup.getFile() == null) {
            throw new IllegalStateException("Le fichier à télécharger est vide.");
        }

        // ✅ Détection du format d'origine
        boolean originalIsXLS = isXLS(setup.getFile());

        // ✅ Traitement (le fichier modifié sera TOUJOURS en format XSSF)
        byte[] lockedFile = excelService.lockNewlyFilledCells(setup.getFile());


        // ✅ Nommer et typer correctement
        String extension = originalIsXLS ? "xls" : "xlsx";
        String contentType = originalIsXLS
                ? "application/vnd.ms-excel"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"submission." + extension + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(lockedFile);
    }

    @PostMapping("/upload/{postId}")
    public String uploadTeamLeaderFile(@PathVariable Integer postId,
                                       @RequestParam("file") MultipartFile file,
                                       HttpSession session) throws IOException {

        Integer matricule = (Integer) session.getAttribute("matricule");

        // 🟢 Récupère le User complet à partir du matricule
        User teamLeader = userRepository.findByMatricule(matricule)
                .orElseThrow(() -> new IllegalArgumentException("Matricule inconnu"));

        // 🟢 Crée et remplit le setup avec le bon user ID
        Setup setup = new Setup();
        setup.setFile(file.getBytes());
        setup.setDatetl(LocalDateTime.now());
        setup.setPostid(postId);
        setup.setIdtl(teamLeader.getUserid());  // ✅ C’est bien le userid ici

        setupRepository.save(setup);

        return "redirect:/teamleader";
    }



}
