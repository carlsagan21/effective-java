package com.sookiwi.effectivejava.item10;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // Broken - violates symmetry!
        CaseInsensitiveString cis = new CaseInsensitiveString("Polish");
        String s = "polish";

        cis.equals(s); // true
        s.equals(cis); // false

        List<CaseInsensitiveString> list = new ArrayList<>();
        list.add(cis);
        list.contains(s); // false or true. cannot sure.

        // Broken - violates transitivity!
        ColorPoint p1 = new ColorPoint(1, 2, Color.RED);
        Point p2 = new Point(1, 2);
        ColorPoint p3 = new ColorPoint(1, 2, Color.BLUE);
    }
}
