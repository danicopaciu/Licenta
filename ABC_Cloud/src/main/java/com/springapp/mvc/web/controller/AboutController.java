package com.springapp.mvc.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Daniel on 5/14/2015.
 * AboutController
 */
@Controller
@RequestMapping("/")
public class AboutController {

    @RequestMapping(value = "about", method = RequestMethod.GET)
    public String renderAbout() {
        return "about";
    }
}
