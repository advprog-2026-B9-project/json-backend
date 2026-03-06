package com.b9.json.jsonplatform.auth.infrastructure.controller;

import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.auth.application.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "Register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        authService.registerUser(user);
        return "redirect:/auth/register?success";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "Login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email, @RequestParam String password, Model model) {
        User loggedInUser = authService.loginUser(email, password);
        if (loggedInUser != null) {
            return "redirect:/auth/profile?email=" + loggedInUser.getEmail();
        }

        model.addAttribute("error", "Email atau password salah!");
        return "Login";
    }

    @GetMapping("/profile")
    public String showProfileForm(@RequestParam String email, Model model) {
        User user = authService.findByEmail(email);
        if (user != null) {
            model.addAttribute("user", user);
            return "Profile";
        }
        return "redirect:/auth/register";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String email, @ModelAttribute User updatedUser) {
        User savedUser = authService.updateProfile(email, updatedUser);

        if (savedUser != null) {
            return "redirect:/auth/profile?email=" + savedUser.getEmail() + "&success";
        }
        return "redirect:/auth/register";
    }

    @GetMapping("/list")
    public String listUsers(Model model) {
        model.addAttribute("users", authService.findAllUsers());
        return "UserList";
    }
}