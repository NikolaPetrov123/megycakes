package com.megycakes.web;

import com.megycakes.catalog.CategoryRepository;
import com.megycakes.catalog.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CatalogController {
    private final CategoryRepository categories;
    private final ProductRepository products;

    public CatalogController(CategoryRepository categories, ProductRepository products) {
        this.categories = categories;
        this.products = products;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categories.findAllByOrderBySortOrderAscNameAsc());
        model.addAttribute("products", products.findByActiveTrueOrderByNameAsc());
        return "index";
    }

    @GetMapping("/c/{slug}")
    public String byCategory(@PathVariable("slug") String slug, Model model) {
        model.addAttribute("categories", categories.findAllByOrderBySortOrderAscNameAsc());
        model.addAttribute("products", products.findByCategory_SlugAndActiveTrueOrderByNameAsc(slug));
        model.addAttribute("currentCategory", slug);
        return "index";
    }
}