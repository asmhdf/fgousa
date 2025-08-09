package com.example.pleasework.controller;

import com.example.pleasework.entity.Post;
import com.example.pleasework.entity.Setup;
import com.example.pleasework.entity.Template;
import com.example.pleasework.entity.User;
import com.example.pleasework.repository.PostRepository;
import com.example.pleasework.repository.TemplateRepository;
import com.example.pleasework.repository.SetupRepository;
import com.example.pleasework.repository.UserRepository;
import com.example.pleasework.service.ExcelService;


import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import java.util.Optional;

import java.io.IOException;

@Controller
@RequestMapping("/operator")
public class OperatorPageController {

    @Autowired
    private PostRepository postRepository;
    @GetMapping("/operator")
    public String showPostsForOperator(Model model) {
        List<Post> posts = postRepository.findAll();
        model.addAttribute("posts", posts);
        return "operator-posts"; // le nom du fichier HTML
    }

    // ➤ Page d'accueil avec la liste des postes
    @GetMapping("/posts")
    public String showPostList(Model model) {
        List<Post> posts = postRepository.findAll();
        model.addAttribute("posts", posts);
        return "operator-posts";  // 👉 page HTML à créer
    }

    // ➤ Lien pour télécharger le template associé au poste
    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private SetupRepository setupRepository;

    @Autowired
    private UserRepository userRepository;


    @GetMapping("/post/{postId}")
    public String showPostDetails(@PathVariable Integer postId, Model model) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            return "error/404"; // ou une autre page d'erreur
        }

        Post post = optionalPost.get();
        model.addAttribute("post", post);
        return "operator-post-download"; // 👉 nouvelle page HTML
    }

    @GetMapping("/download/{templateId}")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable Integer templateId) throws IOException {
        Template template = templateRepository.findById(templateId).orElseThrow();
        byte[] lockedExcel = excelService.lockNewlyFilledCells(template.getFile());


        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + template.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(lockedExcel);
    }

    @PostMapping("/post/{postId}/upload")
    public String uploadFilledFile(@PathVariable Integer postId,
                                   @RequestParam("file") MultipartFile file,
                                   HttpSession session) throws IOException {

        // Vérifie si un fichier a été envoyé
        if (file == null || file.isEmpty()) {
            return "redirect:/operator/post/" + postId + "?error=noFile";
        }

        // Récupère le matricule de session
        Integer matricule = (Integer) session.getAttribute("matricule");
        if (matricule == null) {
            return "redirect:/login?error=sessionExpired";
        }

        // 🔍 Cherche l'utilisateur en base avec ce matricule
        Optional<User> userOpt = userRepository.findByMatricule(matricule);
        if (userOpt.isEmpty()) {
            return "redirect:/operator/post/" + postId + "?error=userNotFound";
        }

        Integer idop = userOpt.get().getUserid(); // ⚠️ on utilise le userid ici

        Setup setup = new Setup();
        setup.setFile(file.getBytes());
        setup.setPostid(postId);
        setup.setDateop(LocalDateTime.now());
        setup.setIdop(idop);

        setupRepository.save(setup);

        return "redirect:/operator/post/" + postId + "?uploadSuccess";
    }



}