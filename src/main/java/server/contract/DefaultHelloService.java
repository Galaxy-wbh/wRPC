package server.contract;

import contract.HelloService;
import org.springframework.stereotype.Service;

import java.net.InetAddress;

@Service
public class DefaultHelloService implements HelloService {
    @Override
    public String sayHello(String name) {
        return String.format("%s say hello!", name);
    }
}
