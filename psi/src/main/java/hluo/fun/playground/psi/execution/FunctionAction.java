package hluo.fun.playground.psi.execution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionAction
{
    private final Tuple data;
    private final StreamFunction streamFunction;

    public FunctionAction(Tuple data, StreamFunction streamFunction)
    {
        this.data = data;
        this.streamFunction = streamFunction;
    }

    public StreamFunction getStreamFunction()
    {
        return streamFunction;
    }

    public Tuple getData()
    {
        return data;
    }

    public List<FunctionAction> proc()
    {
        return streamFunction.proc(data).stream()
                .map(x -> new FunctionAction(x, streamFunction))
                .collect(Collectors.toList());
    }

    public void sink()
    {
        streamFunction.sink(ImmutableList.of(data));
    }
}
