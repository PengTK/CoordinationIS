package com.den41k.controller;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.views.ModelAndView;
import io.micronaut.views.View;

import java.util.Collections;

@Controller
public class IndexController {

    @Get
    @View("home")
    public void auth(@Nullable @QueryValue String error) {
    }
}
