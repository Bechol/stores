package ru.bechol.stores.models;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.UUID;

/**
 * Класс Product.
 * Товар в магазине.
 *
 * @author Oleg Bech.
 * @email oleg071984@gmail.com
 */
@Data
public class Product {
    @Id
    private String id;
    private String name; //наименование
    private int balance; //остаток
    private double price; //цена

    public Product() {
        this.id = UUID.randomUUID().toString();
    }

    public Product(String name, int balance, double price) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.balance = balance;
        this.price = price;
    }
}
