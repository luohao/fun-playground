package hluo.fun.playground.psi.utils;

import java.util.Random;

// Utils class to generate random String at given length
public class RandomString
{
    private final char[] symbols;

    private final Random random = new Random();

    private final char[] buf;

    public RandomString(int length)
    {
        // Construct the symbol set
        StringBuilder tmp = new StringBuilder();
        for (char ch = '0'; ch <= '9'; ++ch) {
            tmp.append(ch);
        }

        for (char ch = 'a'; ch <= 'z'; ++ch) {
            tmp.append(ch);
        }

        symbols = tmp.toString().toCharArray();
        if (length < 1) {
            throw new IllegalArgumentException("length < 1: " + length);
        }

        buf = new char[length];
    }

    public String nextString()
    {
        for (int idx = 0; idx < buf.length; ++idx) {
            buf[idx] = symbols[random.nextInt(symbols.length)];
        }

        return new String(buf);
    }
}
