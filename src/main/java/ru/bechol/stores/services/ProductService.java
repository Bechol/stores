package ru.bechol.stores.services;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.bechol.stores.models.Product;
import ru.bechol.stores.models.Store;
import ru.bechol.stores.repositories.ProductRepository;
import ru.bechol.stores.repositories.StoreRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    public Product findProductByName(String productName) {
        return productRepository.findByName(productName).orElse(null);
    }

    public Product findProductById(String productId) {
        return productRepository.findById(productId).orElse(null);
    }

    public boolean createNewProduct(Product newProduct, String storeName) {
        if (productRepository.findByName(newProduct.getName().trim()).isPresent()) {
            log.warn("Product {} already exists.", newProduct.getName());
            return false;
        }
        if (!Strings.isNullOrEmpty(storeName) && storeRepository.findByName(storeName).isPresent()) {
            Store store = storeRepository.findByName(storeName).get();
            Set<Product> storeProducts = store.getProducts();
            if (storeProducts == null) {
                storeProducts = new HashSet<>();
            }
            storeProducts.add(productRepository.save(
                    new Product(newProduct.getName(), newProduct.getBalance(), newProduct.getPrice())));
            store.setProducts(storeProducts);
            storeRepository.save(store);
            log.info("Product [{}] created and added to store {}.", newProduct.getName(), store.getName());
            return true;
        }
        productRepository.save(newProduct);
        log.info("Product [{}] created.", newProduct.getName());
        return true;
    }

    public boolean addProductToStore(String productName, String storeName) {
        if (Strings.isNullOrEmpty(productName) || Strings.isNullOrEmpty(storeName)) {
            log.warn("Some params are incorrect. Product name: {}. Store: {}", productName, storeName);
            return false;
        }
        Optional<Product> product = productRepository.findByName(productName);
        Optional<Store> store = storeRepository.findByName(storeName);
        if (store.isEmpty() || product.isEmpty()) {
            log.warn("Product or store not found in database. Product: {}. Store: {}", productName, storeName);
            return false;
        }
        if (store.get().getProducts().stream().anyMatch(p -> product.get().getId().equals(p.getId()))) {
            log.warn("Product [{}] exists in store {}.", productName, storeName);
            return false;
        }
        Set<Product> storeProducts = store.get().getProducts();
        storeProducts.add(product.get());
        Store result = store.get();
        result.setProducts(storeProducts);
        storeRepository.save(result);
        log.info("Product [{}] added to store {}", product.get().getName(), store.get().getName());
        return true;
    }

    public boolean updateProduct(String existsProductName, Product newProduct) {
        Optional<Product> existsProduct = productRepository.findByName(existsProductName.trim());
        if (existsProduct.isEmpty()) {
            log.warn("Product {} not found for updating.", existsProductName.trim());
            return false;
        }
        if (!existsProductName.equals(newProduct.getName()) &&
                productRepository.findByName(newProduct.getName().trim()).isPresent()) {
            log.warn("Product {} already exists.", newProduct.getName().trim());
            return false;
        }
        newProduct.setId(existsProduct.get().getId());
        updateProductInStores(newProduct, existsProduct.get());
        productRepository.save(newProduct);
        log.info("Product [{}] updated.", newProduct.getName());
        return true;
    }

    private void updateProductInStores(Product newProduct, Product existsProduct) {
        List<Store> storeList = storeRepository.findAll();
        Map<String, Set<Product>> prMap = storeList.stream()
                .filter(s -> s.getProducts().stream().anyMatch(p -> p.getId().equals(existsProduct.getId())))
                .collect(Collectors.toMap(Store::getId, Store::getProducts));
        prMap.forEach((key, value) -> value.removeIf(p -> p.getId().equals(existsProduct.getId())));
        prMap.forEach((key, value) -> value.add(newProduct));

        storeList.forEach(store -> prMap.forEach((storeId, productSet) -> {
            if (storeId.equals(store.getId())) {
                store.setProducts(productSet);
                storeRepository.save(store);
            }
        }));
    }

    public boolean deleteProduct(String productName) {
        Optional<Product> existsProduct = productRepository.findByName(productName.trim());
        if (existsProduct.isEmpty()) {
            log.warn("Product [{}] not found for deleting.", productName.trim());
            return false;
        }
        if (storeRepository.findAll().stream()
                .anyMatch(store ->
                        store.getProducts().parallelStream().anyMatch(p ->
                                p.getId().equals(existsProduct.get().getId())))) {
            log.warn("There are product leftovers in stores. Removal of product [{}] is not possible.",
                    existsProduct.get().getName());
            return false;
        }
        productRepository.deleteById(existsProduct.get().getId());
        log.info("Product [{}] deleted.", productName);
        return true;
    }
}
