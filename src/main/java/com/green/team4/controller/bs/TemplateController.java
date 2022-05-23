package com.green.team4.controller.bs;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("*/documentation/*")
public class TemplateController {

    @GetMapping("index")
    public void templateTest() {

    }

}