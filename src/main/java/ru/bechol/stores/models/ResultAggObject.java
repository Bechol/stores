package ru.bechol.stores.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResultAggObject {
    private String countResult;
    private String avgResult;
    private String maxResult;
    private String minResult;
    private String ltSumResult;

    @Override
    public String toString() {
        return "Общее количество товаров: " + countResult + "\n"
                + "Средняя цена товара: " + avgResult + "\n"
                + "Самый дорогой товар: " + maxResult + "\n"
                + "Самый дешевый товар: " + minResult + "\n"
                + "Количество товаров, дешевле 100 рублей: " + ltSumResult;
    }
}
