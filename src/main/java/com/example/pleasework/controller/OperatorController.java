package com.example.pleasework.controller;

import com.example.pleasework.entity.Post;
import com.example.pleasework.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class OperatorController {

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/operator")
    public String showPostsForOperator(Model model) {
        List<Post> posts = postRepository.findAll();
        model.addAttribute("posts", posts);
        return "operator-posts"; // 🔁 correspond au nom du fichier .html dans templates
    }
}
