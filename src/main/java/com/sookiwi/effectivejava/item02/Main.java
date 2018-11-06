package com.sookiwi.effectivejava.item02;

import static com.sookiwi.effectivejava.item02.NyPizza.Size.SMALL;

public class Main {

    public static void main(String[] args) {
        NyPizza nyPizza = new NyPizza.Builder(SMALL)
                .addTopping(Pizza.Topping.SAUSAGE)
                .addTopping(Pizza.Topping.ONION)
                .build();

        Calzone calzone = new Calzone.Builder()
                .addTopping(Pizza.Topping.HAM)
                .sauceInde()
                .build();
    }

}
