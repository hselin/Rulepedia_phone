package edu.stanford.braincat.rulepedia.omletUI;

import java.util.Random;

/**
 * Created by braincat on 5/29/15.
 */
public class RandomQuotes {
    private static final String[] quotes = {
            "The clear star that is yesterday is omni-present, much like candy.",
            "A token of gratitude shoots pineapples with a machinegun.",
            "A flailing monkey is always a pleasure.",
            "Fashion tests the thesis that your theorem would unleash.",
            "A passionate evening stands upon somebody else's legs.",
            "Nothing of importance likes to have a shower in the morning.",
            "Another day tests the thesis that your theorem would unleash."
    };
    private static final Random random = new Random();

    public static String getQuote()
    {
        return quotes[random.nextInt(quotes.length)];
    }
}
