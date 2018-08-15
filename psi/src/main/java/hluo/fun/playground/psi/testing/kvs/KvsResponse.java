package hluo.fun.playground.psi.testing.kvs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

public class KvsResponse
{
    private final boolean successful;
    private final Optional<byte[]> returnValue;
    private final Optional<String> errMessage;

    @JsonCreator
    public KvsResponse(
            @JsonProperty("successful") boolean successful,
            @JsonProperty("returnValue") Optional<byte[]> returnValue,
            @JsonProperty("errMessage") Optional<String> errMessage)
    {
        this.successful = requireNonNull(successful, "successful is null");
        this.returnValue = requireNonNull(returnValue, "returnValue is null");
        this.errMessage = requireNonNull(errMessage, "errMessage is null");
    }

    @JsonProperty
    public boolean isSuccessful() { return successful; }

    @JsonProperty
    public Optional<byte[]> getReturnValue() { return returnValue; }

    @JsonProperty
    public Optional<String> getErrMessage()
    {
        return errMessage;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("successful", successful)
                .add("errMessage", errMessage.isPresent() ? errMessage.get() : null)
                .omitNullValues()
                .toString();
    }

    public static KvsResponse succeed()
    {
        return new KvsResponse(true, Optional.empty(), Optional.empty());
    }

    public static KvsResponse succeedWithValue(byte[] value)
    {
        if (value != null) {
            return new KvsResponse(true, Optional.of(value), Optional.empty());
        }
        return new KvsResponse(true, Optional.empty(), Optional.empty());

    }

    public static KvsResponse failWith(String errorMessage)
    {
        return new KvsResponse(false, Optional.empty(), Optional.of(errorMessage));
    }
}
