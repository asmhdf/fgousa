package com.example.pleasework.controller;

import com.example.pleasework.entity.Post;
import com.example.pleasework.entity.Setup;
import com.example.pleasework.entity.Template;
import com.example.pleasework.repository.PostRepository;
import com.example.pleasework.repository.SetupRepository;
import com.example.pleasework.repository.TemplateRepository;
import com.example.pleasework.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.List;

@Controller
public class PostController {

    private final PostRepository postRepository;
    private final TemplateRepository templateRepository;

    public PostController(PostRepository postRepository, TemplateRepository templateRepository) {
        this.postRepository = postRepository;
        this.templateRepository = templateRepository;
    }

    @GetMapping("/post")
    public String getAllPosts(Model model) {
        List<Post> post = postRepository.findAll();
        System.out.println("Nombre de posts : " + post.size());
        model.addAttribute("post", post);
        return "post";  // retourne post.html (Thymeleaf)
    }

    @GetMapping("/post/{id}")
    public String viewTemplate(@PathVariable Integer id, Model model) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return "redirect:/post"; // ou une page d’erreur
        }

        Template template = templateRepository.findById(post.getTemplateid()).orElse(null);

        model.addAttribute("post", post);
        model.addAttribute("template", template);
        return "detailsPost";
    }
    @Autowired
    private SetupRepository setupRepository;
    @GetMapping("/post/{id}/archive")
    public String viewPostArchive(@PathVariable Integer id, Model model) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return "redirect:/post";
        }

        List<Setup> archives = setupRepository.findByPostid(id);
        model.addAttribute("post", post);
        model.addAttribute("archives", archives);

        return "post-archives";
    }

    @Autowired
    private ExcelService excelService;

    @GetMapping("/post/archive/download/{setupId}")
    public ResponseEntity<byte[]> downloadArchive(@PathVariable Integer setupId) {
        Setup s = setupRepository.findById(setupId).orElseThrow();

        // On ne sert que les enregistrements Qualité (PDF)
        if (s.getIdq() == null || s.getFile() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"archive.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(s.getFile());
    }




}
