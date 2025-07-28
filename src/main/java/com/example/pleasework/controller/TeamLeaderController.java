package com.example.pleasework.controller;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
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

                entries.add(entry);
            }
        }

        model.addAttribute("submissions", entries);
        return "teamleader-submissions"; // ➜ page HTML à créer
    }

    // Téléchargement du fichier avec verrouillage
    @GetMapping("/download/{setupId}")
    public ResponseEntity<byte[]> downloadSetup(@PathVariable Integer setupId) throws IOException {
        Setup setup = setupRepository.findById(setupId).orElseThrow();

        // 🔒 Verrouille les cellules pleines
        byte[] fileData = excelService.lockFilledCells(setup.getFile());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"submission.xls\"") // ✅ extension correcte
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel")) // ✅ type MIME pour .xls
                .body(fileData);
    }


}
