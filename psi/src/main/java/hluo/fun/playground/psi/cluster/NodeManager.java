package hluo.fun.playground.psi.cluster;

import hluo.fun.playground.psi.server.NodeState;

import java.util.Set;

public interface NodeManager
{
    // get all nodes
    AllNodes getAllNodes();

    // get all nodes in specific state
    Set<Node> getNodes(NodeState state);

    // get current ndoe
    Node getCurrentNode();

    // refresh nodes
    void refreshNodes();
}
