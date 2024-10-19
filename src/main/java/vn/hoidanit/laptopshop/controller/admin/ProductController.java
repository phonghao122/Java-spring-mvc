package vn.hoidanit.laptopshop.controller.admin;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.service.ProductService;
import vn.hoidanit.laptopshop.service.UploadService;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

@Controller
public class ProductController {
    private final ProductService productService;
    private final UploadService uploadService;

    public ProductController(ProductService productService, UploadService uploadService) {
        this.productService = productService;
        this.uploadService = uploadService;
    }

    @GetMapping("/admin/product")
    public String getProductPage(Model model) {
        List<Product> products = this.productService.getAllProducts();
        model.addAttribute("products", products);
        return "admin/product/show";
    }

    @GetMapping("/admin/product/create")
    public String getProductCreatePage(Model model) {
        model.addAttribute("newProduct", new Product());
        return "admin/product/create";
    }

    @PostMapping("/admin/product/create")
    public String postProductCreate(Model model,
            @ModelAttribute("newProduct") @Valid Product product,
            BindingResult newProductBindingResult,
            @RequestParam("uploadFile") MultipartFile file) {
        // validate
        List<FieldError> errors = newProductBindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(error.getField() + " - " + error.getDefaultMessage());
        }

        if (newProductBindingResult.hasErrors()) {
            return "admin/product/create";
        }

        String imageProduct = this.uploadService.handleSaveUploadFile(file, "product");
        product.setImage(imageProduct);
        this.productService.handleSaveProduct(product);
        return "redirect:/admin/product";
    }

    @GetMapping("/admin/product/update/{id}")
    public String getProductUpdatePage(Model model, @PathVariable long id) {
        Product product = this.productService.getProductById(id);
        model.addAttribute("newProduct", product);
        return "admin/product/update";
    }

    @PostMapping("/admin/product/update")
    public String postProductUpdate(Model model, @ModelAttribute("newProduct") @Valid Product product,
            BindingResult newProductBindingResult) {
        // validate
        List<FieldError> errors = newProductBindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(error.getField() + " - " + error.getDefaultMessage());
        }

        if (newProductBindingResult.hasErrors()) {
            return "admin/product/update";
        }
        Product crrProduct = this.productService.getProductById(product.getId());
        crrProduct.setName(product.getName());
        crrProduct.setDetailDesc(product.getDetailDesc());
        crrProduct.setFactory(product.getFactory());
        crrProduct.setPrice(product.getPrice());
        crrProduct.setQuantity(product.getQuantity());
        crrProduct.setShortDesc(product.getShortDesc());
        crrProduct.setTarget(product.getTarget());
        this.productService.handleSaveProduct(crrProduct);
        return "redirect:/admin/product";
    }

    @GetMapping("/admin/product/delete/{id}")
    public String getDeleteProductPage(Model model, @PathVariable long id) {
        model.addAttribute("newProduct", new Product());
        model.addAttribute("id", id);
        return "admin/product/delete";
    }

    @PostMapping("/admin/product/delete")
    public String postDeleteProduct(Model model, @ModelAttribute("newProduct") Product product) {
        Product currentProduct = this.productService.getProductById(product.getId());
        if (currentProduct != null) {
            this.productService.handleDeleteProduct(currentProduct);
        }
        return "redirect:/admin/product";
    }

}
