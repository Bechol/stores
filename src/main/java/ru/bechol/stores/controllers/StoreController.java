package ru.bechol.stores.controllers;

import jdk.jfr.Frequency;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bechol.stores.models.Product;
import ru.bechol.stores.models.ResultAggObject;
import ru.bechol.stores.models.Store;
import ru.bechol.stores.services.StoreService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/store")
@Slf4j
public class StoreController {

    private final static String BASE_URL = "http://localhost:8080/api/v1/store/";

    @Autowired
    private StoreService storeService;

    @GetMapping("/all")
    public ResponseEntity<List<Store>> findAll() {
        return ResponseEntity.ok(storeService.findAllStores());
    }

    @GetMapping
    public ResponseEntity<Store> findByName(@RequestParam(value = "storeName") String storeName) {
        Store result = storeService.findStoreByName(storeName.trim());
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        log.warn("Store {} not found by name.", storeName.trim());
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<Store> findById(@PathVariable String storeId) {
        Store result = storeService.findStoreById(storeId.trim());
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        log.warn("Store {} not found by Id.", storeId.trim());
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public ResponseEntity<URI> createNewStore(@RequestBody Store newStore) throws URISyntaxException {
        if (storeService.createNewStore(newStore)) {
            return ResponseEntity.created(
                    new URI(BASE_URL + storeService.findStoreByName(newStore.getName()).getId())).build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/update")
    public ResponseEntity<URI> updateStore(@RequestParam(value = "storeName") String existStoreName,
                                           @RequestBody Store newStore)
            throws URISyntaxException {
        if (storeService.updateStoreProperties(existStoreName, newStore)) {
            return ResponseEntity.created(
                    new URI(BASE_URL + storeService.findStoreByName(newStore.getName()).getId())).build();
        }
        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteStore(@RequestParam(value = "storeName") String existStoreName) {
        if (storeService.deleteStoreByName(existStoreName)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, ResultAggObject>> getAggregationResult() {
            return ResponseEntity.ok(storeService.getStatiscticsMap());
    }

    /*
    todo: СТАТИСТИКА_ТОВАРОВ
    Которая должна выводить для каждого магазина:

    — Общее количество товаров

    — Среднюю цену товара

    — Самый дорогой и самый дешевый товар

    — Количество товаров, дешевле 100 рублей.
     */

}
