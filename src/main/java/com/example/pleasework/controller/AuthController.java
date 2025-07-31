package com.example.pleasework.controller;

import com.example.pleasework.entity.User;
import com.example.pleasework.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public String login(@RequestParam int matricule, HttpSession session) {
        Optional<User> userOpt = userRepository.findByMatricule(matricule);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String role = user.getRole().toLowerCase();

            // 🟡 Stocke le matricule dans la session
            session.setAttribute("matricule", matricule);
            session.setAttribute("userid", user.getUserid());
            switch (role) {
                case "admin":
                    return "redirect:/post";
                case "teamleader":
                    return "redirect:/teamleader";
                case "respo qualité":
                    return "redirect:/qualite";
                case "operateur":
                    return "redirect:/operator";
                default:
                    return "redirect:/error";
            }
        } else {
            return "redirect:/login?error=true";
        }
    }

}
