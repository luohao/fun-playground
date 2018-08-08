package hluo.fun.playground.psi.compiler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jdk.nashorn.internal.ir.annotations.Immutable;

import java.io.Serializable;

@Immutable
public class ClassInfo
        implements Serializable
{
    private final String classsName;
    private final byte[] byteCode;

    @JsonCreator
    public ClassInfo(@JsonProperty("classsName") String classsName,
            @JsonProperty("byteCode")byte[] byteCode) {
        this.classsName = classsName;
        this.byteCode = byteCode;
    }

    @JsonProperty
    public String getClasssName()
    {
        return classsName;
    }

    @JsonProperty
    public byte[] getByteCode()
    {
        return byteCode;
    }
}
