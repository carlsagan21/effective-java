package com.sookiwi.effectivejava.item04;

// Noninstantiable utility class
public class UtilityClass {
    // Suppress default constructor for noninstantiability
    private UtilityClass() {
        throw new AssertionError();
    }
}

// or make abstract class
public abstract class UtilityClassAbstract {

}
