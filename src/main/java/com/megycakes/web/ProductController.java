package com.megycakes.web;

import com.megycakes.catalog.Product;
import com.megycakes.catalog.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProductController {
    private final ProductRepository products;

    public ProductController(ProductRepository products) { this.products = products; }

    @GetMapping("/p/{slug}")
    public String details(@PathVariable("slug") String slug, Model model) {
        Product p = products.findBySlugAndActiveTrue(slug).orElseThrow();
        model.addAttribute("product", p);
        return "product";
    }
}