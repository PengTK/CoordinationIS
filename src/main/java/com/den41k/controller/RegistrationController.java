package com.den41k.controller;

import com.den41k.model.User;
import com.den41k.service.UserService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.ModelAndView;
import io.micronaut.views.View;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

@Controller("/reg")
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @Get
    @View("reg")
    public ModelAndView reg(@Nullable @QueryValue String error) {
        return new ModelAndView("reg", Collections.singletonMap("error", error));
    }


    @Post(consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<?> register(@Body Map<String, String> formData) {
        User user = new User(formData.get("name"), formData.get("email"), formData.get("password"));
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(15)));
        userService.registerUser(user);
        return HttpResponse.redirect(URI.create("auth"));
    }

}
