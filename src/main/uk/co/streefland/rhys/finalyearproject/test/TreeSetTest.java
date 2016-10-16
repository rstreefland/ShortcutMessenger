package uk.co.streefland.rhys.finalyearproject.test;

import java.util.TreeSet;

/**
 * Created by Rhys on 16/10/2016.
 */
public class TreeSetTest {

    public static void main(String[] args) {
        TreeSet<String> tree = new TreeSet<String>();


        String test1 = "hello world";
        String test2 = "hello rhys";

        tree.add(test2);
        tree.add(test1);

        test1 = "bleh";

        System.out.println(tree.last());
    }
}
