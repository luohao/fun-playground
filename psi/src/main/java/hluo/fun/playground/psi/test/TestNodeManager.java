package hluo.fun.playground.psi.test;

import io.airlift.discovery.client.ServiceSelector;

import javax.inject.Inject;

public class TestNodeManager
{
    private final ServiceSelector serviceSelector;

    @Inject
    public TestNodeManager(ServiceSelector serviceSelector) {this.serviceSelector = serviceSelector;}

    public void listServiceSelector() {
        serviceSelector.selectAllServices().stream()
                .forEach(x -> System.out.println(x.getType()));
    }
}
