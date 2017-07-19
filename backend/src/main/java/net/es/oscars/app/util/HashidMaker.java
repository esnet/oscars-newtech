package net.es.oscars.app.util;

import org.hashids.Hashids;

import java.util.Random;

public class HashidMaker {


    public static String randomHashid() {
        Random rand = new Random();
        Integer randInt = rand.nextInt();
        randInt = randInt < 0 ? -1 * randInt : randInt;

        Hashids hashids = new Hashids("oscars");
        return hashids.encode(randInt);
    }

}
