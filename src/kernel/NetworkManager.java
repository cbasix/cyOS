package kernel;

import network.NetworkStack;
import network.Nic;

public class NetworkManager {
    public Nic nic;
    public NetworkStack stack = new NetworkStack();
}
