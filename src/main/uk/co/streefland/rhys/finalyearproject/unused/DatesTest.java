package uk.co.streefland.rhys.finalyearproject.unused;

import java.util.Date;

/**
 * Created by Rhys on 08/10/2016.
 */
class DatesTest {

    public static void main(String[] args) {
        long date = new Date().getTime() / 1000;

        System.out.println(date);

        date = date + 172800;

        System.out.println(date);
    }
}
