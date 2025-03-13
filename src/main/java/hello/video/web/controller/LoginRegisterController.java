package hello.video.web.controller;

import hello.video.domain.User;
import hello.video.domain.dto.UserRegisterRequestDTO;
import hello.video.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginRegisterController {

    private final UserService userService;

    @GetMapping("/register")
    public String getRegisterForm(Model model){
        model.addAttribute("user", new UserRegisterRequestDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(UserRegisterRequestDTO user, Model model){
        try{
            userService.registerUser_ROLE_USER(user);
            return "redirect:/login";
        } catch (Exception e){
            e.getStackTrace();
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String getLoginForm(Model model){
        model.addAttribute("user", new User());
        return "login";
    }
}
