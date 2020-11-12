package com.github.haroldjcastillo.grpc.server;

import com.github.haroldjcastillo.grpc.server.impl.GreetServiceImpl;
import com.github.haroldjcastillo.grpc.server.impl.SquareRootServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.File;
import java.io.IOException;

public class GreetingServer {

  public static void main(String[] args) throws IOException, InterruptedException {
    System.out.println("> Running server on 50051 port ...");

    // Plaintext server
    Server server =
        ServerBuilder.forPort(50051)
            .addService(new GreetServiceImpl())
            .addService(new SquareRootServiceImpl())
            .addService(ProtoReflectionService.newInstance())
            .build();
    server.start();

    System.out.println("> Running secure server on 50052 port ...");

    // Secure server
    Server secureServer =
        ServerBuilder.forPort(50052)
            .addService(new SquareRootServiceImpl())
            .useTransportSecurity(new File("ssl/server.crt"), new File("ssl/server.pem"))
            .build();
    secureServer.start();

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Received Shutdown Request");
                  server.shutdown();
                  secureServer.shutdown();
                  System.out.println("Successfully stopped the server");
                }));

    server.awaitTermination();
    secureServer.awaitTermination();
  }
}
