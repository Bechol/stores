package ru.bechol.stores.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.HashSet;
import java.util.Set;

/**
 * Класс Store.
 * Магазин.
 *
 * @author Oleg Bech
 * @email oleg071984@gmail.com
 */
@Data
@NoArgsConstructor
public class Store {

    @Id
    private String id;
    private String name;
    private Set<Product> products;

}
