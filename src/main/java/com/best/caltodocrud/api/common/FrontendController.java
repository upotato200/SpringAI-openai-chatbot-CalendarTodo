package com.best.caltodocrud.api.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }
}

