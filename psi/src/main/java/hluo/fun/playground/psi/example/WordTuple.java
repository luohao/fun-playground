package hluo.fun.playground.psi.example;

import hluo.fun.playground.psi.execution.Tuple;

import java.nio.ByteBuffer;

public class WordTuple
        implements Tuple
{
    private final String word;

    public WordTuple(String word)
    {
        this.word = word;
    }

    public String getWord()
    {
        return word;
    }

    public static WordTuple fromByteBuffer(ByteBuffer buffer)
    {
        byte[] payload = new byte[buffer.remaining()];
        buffer.get(payload);
        return new WordTuple(new String(payload));
    }
}
