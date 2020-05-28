package ru.bechol.stores.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.bechol.stores.models.Store;

import java.util.Optional;

@Repository
public interface StoreRepository extends MongoRepository<Store, String> {
    public Optional<Store> findByName(@Param("name") String name);
}
