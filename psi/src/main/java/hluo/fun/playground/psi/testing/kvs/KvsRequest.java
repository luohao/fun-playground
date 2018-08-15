package hluo.fun.playground.psi.testing.kvs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;

import java.util.Optional;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static hluo.fun.playground.psi.testing.kvs.KvsOperation.PUT;
import static java.util.Objects.requireNonNull;

@Immutable
public class KvsRequest
{
    private final KvsOperation operation;
    private final byte[] key;
    private final Optional<byte[]> value;

    @JsonCreator
    public KvsRequest(
            @JsonProperty("operation") KvsOperation operation,
            @JsonProperty("key") byte[] key,
            @JsonProperty("value") Optional<byte[]> value
    )
    {
        checkArgument(operation != PUT || value.isPresent());
        this.operation = requireNonNull(operation, "operation is null");
        this.key = requireNonNull(key, "key is null");
        this.value = requireNonNull(value, "value is null");
    }

    @JsonProperty
    public KvsOperation getOperation()
    {
        return operation;
    }

    @JsonProperty
    public byte[] getKey()
    {
        return key;
    }

    @JsonProperty
    public Optional<byte[]> getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("operation", operation)
                .add("key", key)
                .add("value", value.isPresent() ? value.get() : null)
                .omitNullValues()
                .toString();
    }
}
