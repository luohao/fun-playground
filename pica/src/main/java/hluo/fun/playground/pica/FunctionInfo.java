package hluo.fun.playground.pica;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import hluo.fun.playground.pica.compiler.ClassInfo;
import io.airlift.json.JsonCodec;
import jdk.nashorn.internal.ir.annotations.Immutable;

import static com.google.common.base.MoreObjects.toStringHelper;
import static io.airlift.json.JsonCodec.jsonCodec;
import static java.util.Objects.requireNonNull;

@Immutable
public class FunctionInfo
{
    public static final JsonCodec<FunctionInfo> functionInfoJsonCodec = jsonCodec(FunctionInfo.class);

    private final FunctionId functionId;
    private final ClassInfo classInfo;

    @JsonCreator
    public FunctionInfo(@JsonProperty("functionId") FunctionId functionId,
            @JsonProperty("classInfo") ClassInfo classInfo)
    {
        this.functionId = requireNonNull(functionId, "Function Id is null");
        this.classInfo = requireNonNull(classInfo, "Function Name is null");
    }

    @JsonProperty
    public FunctionId getFunctionId() { return functionId; }

    @JsonProperty
    public ClassInfo getClassInfo() {return classInfo; }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("functionId", functionId)
                .add("className", classInfo.getClasssName())
                .toString();
    }
}
