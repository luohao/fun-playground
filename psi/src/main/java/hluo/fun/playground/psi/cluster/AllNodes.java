package hluo.fun.playground.psi.cluster;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class AllNodes
{
    private final Set<Node> activeNodes;
    private final Set<Node> inactiveNodes;

    public AllNodes(Set<Node> activeNodes, Set<Node> inactiveNodes)
    {
        this.activeNodes = ImmutableSet.copyOf(requireNonNull(activeNodes, "activeNodes is null"));
        this.inactiveNodes = ImmutableSet.copyOf(requireNonNull(inactiveNodes, "inactiveNodes is null"));
    }

    public Set<Node> getActiveNodes()
    {
        return activeNodes;
    }

    public Set<Node> getInactiveNodes()
    {
        return inactiveNodes;
    }
}
