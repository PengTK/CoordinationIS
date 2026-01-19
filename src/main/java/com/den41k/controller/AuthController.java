package com.den41k.controller;

import com.den41k.model.User;
import com.den41k.service.UserService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.session.Session;
import io.micronaut.views.ModelAndView;
import io.micronaut.views.View;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Controller("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Get
    @View("auth")
    public ModelAndView auth(@Nullable @QueryValue String error) {
        return new ModelAndView("auth", Collections.singletonMap("error", error));
    }

    @Post(consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<?> login(@Body Map<String, String> formData, Session session) {
        String email = formData.get("email");
        String plainPassword = formData.get("password");
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent()) {
            String storedHash = userOpt.get().getPassword();
            if (BCrypt.checkpw(plainPassword, storedHash)) {
                session.put("userId", userOpt.get().getId());
                session.put("email", email);
                return HttpResponse.redirect(URI.create("home"));
            }
        }
        return HttpResponse.redirect(URI.create("/auth?error=invalid"));
    }

    @Post(value = "/logout", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<?> logout(Session session) {
        session.clear();
        Cookie expiredSessionCookie = Cookie.of("SESSION", "")
                .maxAge(0)
                .path("/")
                .httpOnly(true)
                .secure(false);
        return HttpResponse.redirect(URI.create("/home"))
                .cookie(expiredSessionCookie);
    }
}
