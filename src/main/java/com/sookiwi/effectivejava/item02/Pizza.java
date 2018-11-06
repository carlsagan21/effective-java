package com.sookiwi.effectivejava.item02;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public abstract class Pizza {

    final Set<Topping> toppings;

    Pizza(Builder<?> builder) {
        toppings = builder.toppings.clone();
    }

    public enum Topping {
        HAM, MUSHROOM, ONION, PEEPER, SAUSAGE
    }

    abstract static class Builder<T extends Builder<T>> { // `재귀적인 타입 매개변수`
        EnumSet<Topping> toppings = EnumSet.noneOf(Topping.class);

        public T addTopping(Topping topping) {
            toppings.add(Objects.requireNonNull(topping));
            return self();
        }

        abstract Pizza build(); // `Convariant 리턴 타입`을 위한 준비작업

        protected abstract T self(); // `self-type` 개념을 사용해서 메소드 체이닝이 가능케 함
    }

}
