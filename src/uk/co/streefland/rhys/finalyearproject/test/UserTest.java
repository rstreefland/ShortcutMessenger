package uk.co.streefland.rhys.finalyearproject.test;

import uk.co.streefland.rhys.finalyearproject.core.User;

import java.util.Scanner;

/**
 * Created by Rhys on 07/09/2016.
 */
public class UserTest {

    public static void main(String[] args) {

        long start = System.currentTimeMillis();
        User user = new User("titanicsaled", "ilikecheese");
        long end = System.currentTimeMillis();
        long difference = end-start;
        System.out.println("Setting up user took: " + difference + "ms");

        String input = null;
        Scanner sc = new Scanner(System.in);

        while(true) {
            input = sc.nextLine();

            if (user.doPasswordsMatch(input) == true) {
                System.out.println("password is correct");
            } else {
                System.out.println("password is incorrect");
            }
        }
    }
}
