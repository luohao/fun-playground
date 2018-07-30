package hluo.fun.playground.pica;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.net.MediaType;
import io.airlift.json.JsonCodec;

import java.util.Objects;

import static io.airlift.json.JsonCodec.jsonCodec;
import static java.util.Objects.requireNonNull;

public final class FunctionId
{
    public static final String FUNCTION_ID = "application/X-function-id";
    public static final MediaType FUNCTION_ID_TYPE = MediaType.create("application", "X-function-id");

    public static final JsonCodec<FunctionId> functionIdJsonCodec = jsonCodec(FunctionId.class);

    private final String id;

    public FunctionId(String id)
    {
        this.id = requireNonNull(id);
    }

    @JsonCreator
    public static FunctionId valueOf(String functionId)
    {
        return new FunctionId(functionId);
    }

    public String getId() { return id; }

    @Override
    @JsonValue
    public String toString() { return id; }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        FunctionId other = (FunctionId) obj;
        return Objects.equals(this.id, other.id);
    }
}
