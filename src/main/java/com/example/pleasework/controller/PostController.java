package com.example.pleasework.controller;

import com.example.pleasework.entity.Post;
import com.example.pleasework.entity.Template;
import com.example.pleasework.repository.PostRepository;
import com.example.pleasework.repository.TemplateRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
}
