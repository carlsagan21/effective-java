package com.sookiwi.effectivejava.item03;

// Enum singleton - the preferred approach
public enum ElvisEnum {
    INSTANCE;
}

// Singleton with static factory
public class Elvis {

    private static final transient Elvis INSTANCE = new Elvis();

    private Elvis() {
    }

    public static Elvis getInstance() {
        return INSTANCE;
    }

    // readResolve method to preserve singleton property
    private Object readResolve() {
        // Return the one true Elvis and let the garbage collector
        // take care of the Elvis impersonator.
        return INSTANCE;
    }

}
