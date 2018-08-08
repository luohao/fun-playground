package hluo.fun.playground.psi.execution;

import java.util.List;

public interface StreamFunction
{
    List<Tuple> source();
    List<Tuple> proc(Tuple tuple);
    void sink(List<Tuple> tuples);
}
