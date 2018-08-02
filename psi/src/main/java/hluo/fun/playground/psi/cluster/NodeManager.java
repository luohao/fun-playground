package hluo.fun.playground.psi.cluster;

import hluo.fun.playground.psi.server.NodeState;

import java.util.Set;

public interface NodeManager
{
    // get all nodes
    AllNodes getAllNodes();

    // get all nodes in specific state
    Set<Node> getNodes(NodeState state);

    // get current node
    Node getCurrentNode();

    // get master nodes
    Set<Node> getMasters();

    // refresh nodes
    void refreshNodes();
}
