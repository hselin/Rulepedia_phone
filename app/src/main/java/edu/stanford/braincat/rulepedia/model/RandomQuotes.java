package edu.stanford.braincat.rulepedia.model;

import java.util.Random;

/**
 * Created by braincat on 5/29/15.
 */
public class RandomQuotes {
    static String[] quotes = {
            "The clear star that is yesterday is omni-present, much like candy.",
            "A token of gratitude shoots pineapples with a machinegun.",
            "A flailing monkey is always a pleasure.",
            "Fashion tests the thesis that your theorem would unleash.",
            "A passionate evening stands upon somebody else's legs.",
            "Nothing of importance likes to have a shower in the morning.",
            "Another day tests the thesis that your theorem would unleash."
    };

    public static String getQuote()
    {
        return quotes[new Random().nextInt(quotes.length)];
    }
}
