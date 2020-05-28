package ru.bechol.stores.services;

import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import ru.bechol.stores.models.ResultAggObject;
import ru.bechol.stores.models.Store;
import ru.bechol.stores.repositories.StoreRepository;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Slf4j
@Service
public class StoreService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StoreRepository storeRepository;

    public List<Store> findAllStores() {
        return storeRepository.findAll();
    }

    public Store findStoreByName(String storeName) {
        Aggregation agg = newAggregation(
                match(new Criteria("name").is(storeName)
        ));
        AggregationResults<Store> output = mongoTemplate.aggregate(agg, "store", Store.class);
//        return storeRepository.findByName(storeName).orElse(null);
        return output.getUniqueMappedResult();
    }

    public Store findStoreById(String storeId) {
        return storeRepository.findById(storeId).orElse(null);
    }

    public boolean createNewStore(Store newStore) {
        if (storeRepository.findByName(newStore.getName()).isPresent()) {
            log.warn("Creating of new store aborted. Store {} is exists now.", newStore.getName());
            return false;
        }
        storeRepository.save(newStore);
        return true;
    }

    public boolean updateStoreProperties(String existStoreName, Store newStore) {
        Store existStore = findStoreByName(existStoreName);
        if (existStore == null) {
            log.warn("Store {} not found.", existStoreName);
        }
        if (!existStoreName.equals(newStore.getName()) && storeRepository.findByName(newStore.getName()).isPresent()) {
            log.warn("Store {} already exists.", newStore.getName());
            return false;
        }
        newStore.setId(existStore.getId());
        storeRepository.save(newStore);
        log.info("Store {} updated.", newStore.getName());
        return true;
    }

    public boolean deleteStoreByName(String existStoreName) {
        Optional<Store> store = storeRepository.findByName(existStoreName);
        if (store.isEmpty()) {
            log.warn("Store {} not found for deleting.", existStoreName);
            return false;
        }
        storeRepository.deleteById(store.get().getId());
        log.info("Store {} deleted.", existStoreName);
        return true;
    }

    public Map<String, ResultAggObject> getStatiscticsMap() {
        Map<String, ResultAggObject> resultMap = new HashMap<>();
        storeRepository.findAll().forEach(store -> {
            resultMap.put(store.getName(), getStoreStatistics(store.getName()));
        });
        return resultMap;
    }
    private ResultAggObject getStoreStatistics(String storeName) {

        ResultAggObject resultAggObject = new ResultAggObject();

        Aggregation countAggregation = newAggregation(
                match(new Criteria("name").is(storeName)),
                unwind("products", false),
                count().as("total_count")
        );
        AggregationResults<Document> countAggregationResult = mongoTemplate.aggregate(countAggregation, "store", Document.class);
        resultAggObject.setCountResult(String.valueOf(countAggregationResult.getUniqueMappedResult().getInteger("total_count",0)));

        Aggregation avgAggregation = newAggregation(
                match(new Criteria("name").is(storeName)),
                unwind("products", false),
                group("_id").avg("products.price").as("avg_Price")
        );
        AggregationResults<Document> avgAggregationResult = mongoTemplate.aggregate(avgAggregation, "store", Document.class);
        resultAggObject.setAvgResult(String.valueOf(avgAggregationResult.getUniqueMappedResult().getDouble("avg_Price")));

        Aggregation maxAggregation = newAggregation(
                match(new Criteria("name").is(storeName)),
                unwind("products", false),
                group("_id").max("products.price").as("max_Price")
        );
        AggregationResults<Document> maxAggregationResult = mongoTemplate.aggregate(maxAggregation, "store", Document.class);
        resultAggObject.setMaxResult(String.valueOf(maxAggregationResult.getUniqueMappedResult().getDouble("max_Price")));

        Aggregation minAggregation = newAggregation(
                match(new Criteria("name").is(storeName)),
                unwind("products", false),
                group("_id").min("products.price").as("min_Price")
        );
        AggregationResults<Document> minAggregationResult = mongoTemplate.aggregate(minAggregation, "store", Document.class);
        resultAggObject.setMinResult(String.valueOf(minAggregationResult.getUniqueMappedResult().getDouble("min_Price")));


        Aggregation ltAggregation = newAggregation(
                match(new Criteria("name").is(storeName)),
                unwind("products", false),
                match(new Criteria("products.price").lt(100.0)),
                count().as("prod_count")
        );
        AggregationResults<Document> ltAggregationResult = mongoTemplate.aggregate(ltAggregation, "store", Document.class);
        resultAggObject.setLtSumResult(String.valueOf(ltAggregationResult.getUniqueMappedResult().getInteger("prod_count",0)));

        return resultAggObject;
    }
}
