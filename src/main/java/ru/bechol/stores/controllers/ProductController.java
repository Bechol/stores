package ru.bechol.stores.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bechol.stores.models.Product;
import ru.bechol.stores.services.ProductService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    private final static String BASE_URL = "http://localhost:8080/api/v1/product/";

    @Autowired
    private ProductService productService;

    @GetMapping("/all")
    public ResponseEntity<List<Product>> findAll() {
        return ResponseEntity.ok(productService.findAllProducts());
    }

    @GetMapping("/name/{productName}")
    public ResponseEntity<Product> findByName(@PathVariable String productName) {
        Product product = productService.findProductByName(productName);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> findById(@PathVariable String productId) {
        Product product = productService.findProductById(productId);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @PostMapping("/create")
    public ResponseEntity<URI> create(@RequestBody Product newProduct, @RequestParam(value = "storeName", required = false) String storeName)
            throws URISyntaxException {
        if (productService.createNewProduct(newProduct, storeName)) {
            return ResponseEntity.created(new URI(
                    BASE_URL + productService.findProductByName(newProduct.getName()).getId())).build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/tostore")
    public ResponseEntity<?> addToStore(@RequestParam(value = "productName") String existProductName,
                                        @RequestParam(value = "store") String store) {
        if (productService.addProductToStore(existProductName, store)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/update")
    public ResponseEntity<URI> updateProduct(@RequestParam(value = "productName") String existProductName,
                                             @RequestBody Product newProduct)
            throws URISyntaxException {
        if (productService.updateProduct(existProductName, newProduct)) {
            return ResponseEntity.created(
                    new URI(BASE_URL + productService.findProductByName(newProduct.getName()).getId())).build();
        }
        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteStore(@RequestParam(value = "productName") String existProductName) {
        if (productService.deleteProduct(existProductName)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
