package hluo.fun.playground.pica.compiler;

import java.io.Serializable;

public class ClassInfo
        implements Serializable
{
    private final String classsName;
    private final byte[] bytes;

    public ClassInfo(String classsName, byte[] bytes) {
        this.classsName = classsName;
        this.bytes = bytes;
    }
    public String getClasssName()
    {
        return classsName;
    }

    public byte[] getBytes()
    {
        return bytes;
    }
}
