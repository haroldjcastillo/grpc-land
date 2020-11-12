package com.github.haroldjcastillo.grpc.client;

import com.github.haroldjcastillo.proto.calculator.SquareRootGrpc;
import com.github.haroldjcastillo.proto.calculator.SquareRootRequest;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

import javax.net.ssl.SSLException;
import java.io.File;

public class SquareRootClient {

  public static void main(String[] args) throws SSLException {
    ManagedChannel channel =
        NettyChannelBuilder.forAddress("localhost", 50052)
            .sslContext(GrpcSslContexts.forClient().trustManager(new File("ssl/ca.crt")).build())
            .build();

    SquareRootGrpc.SquareRootBlockingStub blockingStub = SquareRootGrpc.newBlockingStub(channel);
    try {
      blockingStub.calculate(SquareRootRequest.newBuilder().setValue(-1).build());
    } catch (StatusRuntimeException e) {
      System.out.println("Got an exception for square root!");
      e.printStackTrace();
    }
    System.out.println("Shutting down channel");
    channel.shutdown();
  }
}
