package vn.hoidanit.laptopshop.controller.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetail;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.service.ProductService;

import org.springframework.ui.Model;

@Controller
public class ItemController {
    private final ProductService productService;

    public ItemController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/product/{id}")
    public String getDetailProductPage(Model model, @PathVariable long id) {
        Product product = this.productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("id", id);
        return "client/product/detail";
    }

    @PostMapping("/add-product-to-cart/{id}")
    public String addProductToCart(@PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        long productId = id;
        String email = (String) session.getAttribute("email");

        this.productService.handleAddProductToCart(email, productId, session, 1);

        return "redirect:/";
    }

    @GetMapping("/cart")
    public String getCartPage(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User user = new User();
        user.setId((long) session.getAttribute("id"));
        Cart cart = this.productService.getCartByUSer(user);
        List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() : cart.getCartDetails();
        double total = 0;
        for (CartDetail cartDetail : cartDetails) {
            total = total + (cartDetail.getPrice() * cartDetail.getQuantity());
        }
        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("total", total);

        model.addAttribute("cart", cart);
        return "client/cart/show";
    }

    @PostMapping("/delete-cart-product/{id}")
    public String postDeleleCartProduct(Model model, @PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        CartDetail cartDetail = this.productService.getCartDetailById(id);
        if (cartDetail != null) {
            this.productService.handleDeleteCartDetail(cartDetail, session);
        }
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String getCheckOutPage(Model model, HttpServletRequest request) {
        User currentUser = new User();// null
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        Cart cart = this.productService.getCartByUSer(currentUser);

        List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() : cart.getCartDetails();

        double totalPrice = 0;
        for (CartDetail cd : cartDetails) {
            totalPrice += cd.getPrice() * cd.getQuantity();
        }

        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("totalPrice", totalPrice);

        return "client/cart/checkout";
    }

    @PostMapping("/confirm-checkout")
    public String getCheckOutPage(@ModelAttribute("cart") Cart cart) {
        List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() : cart.getCartDetails();
        this.productService.handleUpdateCartBeforeCheckout(cartDetails);
        return "redirect:/checkout";
    }

    @PostMapping("/place-order")
    public String handlePlaceOrder(
            HttpServletRequest request,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverAddress") String receiverAddress,
            @RequestParam("receiverPhone") String receiverPhone) {
        User currentUser = new User();// null
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        this.productService.handlePlaceOrder(currentUser, session, receiverName, receiverAddress, receiverPhone);
        return "client/cart/thanks";
    }

    @PostMapping("/add-product-from-view-detail")
    public String handleAddProductFromViewDetail(
            @RequestParam("id") long id,
            @RequestParam("quantity") long quantity,
            HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        String email = (String) session.getAttribute("email");
        this.productService.handleAddProductToCart(email, id, session, quantity);
        return "redirect:/product/" + id;
    }

    @GetMapping("/products")
    public String getProductPage(Model model,
            @RequestParam("page") Optional<String> pageOptional,
            @RequestParam("name") Optional<String> nameOptional,
            @RequestParam("min-price") Optional<String> minPriceOptional,
            @RequestParam("max-price") Optional<String> maxPriceOptional,
            @RequestParam("price") Optional<String> priceOptional,
            @RequestParam("factory") Optional<String> factoryOptional) {
        int page = 1;
        try {
            if (pageOptional.isPresent()) {
                // convert from String to int
                page = Integer.parseInt(pageOptional.get());
            } else {
                // page = 1
            }
        } catch (Exception e) {
            // page = 1
            // TODO: handle exception
        }

        // String name = nameOptional.get();

        Pageable pageable = PageRequest.of(page - 1, 60);
        // Page<Product> prs = this.productService.getAllProductsWithSpec(pageable,
        // name);

        // case 1
        // double min = minPriceOptional.isPresent() ?
        // Double.parseDouble(minPriceOptional.get()) : 0;
        // Page<Product> prs = this.productService.getAllProductsWithSpec(pageable,
        // min);

        // case 2
        // double max = maxPriceOptional.isPresent() ?
        // Double.parseDouble(maxPriceOptional.get()) : 0;
        // Page<Product> prs = this.productService.getAllProductsWithSpec(pageable,
        // max);

        // case 3
        // String factory = factoryOptional.get();
        // Page<Product> prs = this.productService.getAllProductsWithSpec(pageable, factory);

        //case 4
        // List<String> factory = Arrays.asList(factoryOptional.get().split(","));
        // Page<Product> prs = this.productService.getAllProductsWithSpec(pageable, factory);

        //case 5
        // String price = priceOptional.isPresent() ? priceOptional.get() : "";
        // Page<Product> prs = this.productService.getAllProductsWithSpec(pageable, price);

        //case 6
        List<String> price = Arrays.asList(priceOptional.get().split(","));
        Page<Product> prs = this.productService.getAllProductsWithSpec(pageable, price);
        
        List<Product> products = prs.getContent();

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", prs.getTotalPages());
        return "client/product/show";
    }

}
