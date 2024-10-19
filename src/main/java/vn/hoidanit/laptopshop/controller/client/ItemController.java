package vn.hoidanit.laptopshop.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
public class ItemController {
    @GetMapping("/product/{id}")
    public String getDetailProductPage(Model model, @PathVariable long id) {
        return "client/product/detail";
    }
}
