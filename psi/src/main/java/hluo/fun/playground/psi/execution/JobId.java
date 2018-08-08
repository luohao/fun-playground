package hluo.fun.playground.psi.execution;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public class JobId
{

    @JsonCreator
    public static JobId valueOf(String id)
    {
        return new JobId(id);
    }

    private final String id;

    public JobId(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    @Override
    @JsonValue
    public String toString()
    {
        return id;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        JobId other = (JobId) obj;
        return Objects.equals(this.id, other.id);
    }
}
