package com.den41k.controller;

import com.den41k.model.Role;
import com.den41k.model.RolePermission;
import com.den41k.model.User;
import com.den41k.repository.RoleRepository;
import com.den41k.service.RoleService;
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
    private final RoleService roleService;

    public RegistrationController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @Get
    @View("reg")
    public ModelAndView reg(@Nullable @QueryValue String error) {
        return new ModelAndView("reg", Collections.singletonMap("error", error));
    }


    @Post(consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<?> register(@Body Map<String, String> formData) {
        if (roleService.findByName("GUEST")!=null) {
            Role roleOpt = roleService.findByName("GUEST");
            User user = new User(formData.get("name"), formData.get("surname"), formData.get("patronymic"), formData.get("email"), formData.get("password"), roleOpt);
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12)));
            userService.registerUser(user);
        }
        return HttpResponse.redirect(URI.create("auth"));
    }

}
