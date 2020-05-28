package ru.bechol.stores.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.bechol.stores.models.Product;

import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    public Optional<Product> findByName(@Param("name") String name);
}
