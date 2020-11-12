package com.github.haroldjcastillo.grpc.server.impl;

import com.github.haroldjcastillo.proto.greet.GreetRequest;
import com.github.haroldjcastillo.proto.greet.GreetResponse;
import com.github.haroldjcastillo.proto.greet.GreetServiceGrpc;
import com.github.haroldjcastillo.proto.greet.Greeting;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {

  @Override
  public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
    Greeting greeting = request.getGreeting();

    // Create the response
    String result = "Hello " + greeting.getFirstName();
    GreetResponse response = GreetResponse.newBuilder().setResult(result).build();

    // Send the response
    responseObserver.onNext(response);

    // Complete the RPC call
    responseObserver.onCompleted();
  }

  @Override
  public void greetManyTimes(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
    try {
      for (int i = 0; i < 10; i++) {
        GreetResponse response =
            GreetResponse.newBuilder()
                .setResult(
                    "Hello " + request.getGreeting().getFirstName() + ", response number " + i)
                .build();
        responseObserver.onNext(response);
        Thread.sleep(1000L);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      responseObserver.onCompleted();
    }
  }

  @Override
  public StreamObserver<GreetRequest> longGreet(StreamObserver<GreetResponse> responseObserver) {

    return new StreamObserver<GreetRequest>() {
      String result = null;

      @Override
      public void onNext(GreetRequest value) {
        // Client sends a message
        result = "Hello " + value.getGreeting().getFirstName() + "! ";
      }

      @Override
      public void onError(Throwable t) {
        // Client sends an error
      }

      @Override
      public void onCompleted() {
        // Client is done
        responseObserver.onNext(GreetResponse.newBuilder().setResult(result).build());
        responseObserver.onCompleted();
        // this is when we want to return a response
      }
    };
  }

  @Override
  public StreamObserver<GreetRequest> greetEveryone(
      StreamObserver<GreetResponse> responseObserver) {

    return new StreamObserver<GreetRequest>() {
      @Override
      public void onNext(GreetRequest value) {
        String response = "Hello " + value.getGreeting().getFirstName();
        GreetResponse greetResponse = GreetResponse.newBuilder().setResult(response).build();
        responseObserver.onNext(greetResponse);
      }

      @Override
      public void onError(Throwable t) {
        // do nothing
      }

      @Override
      public void onCompleted() {
        responseObserver.onCompleted();
      }
    };
  }

  @Override
  public void greetWithDeadline(
      GreetRequest request, StreamObserver<GreetResponse> responseObserver) {

    Context context = Context.current();

    try {
      for (int i = 0; i < 3; i++) {
        if (!context.isCancelled()) {
          System.out.println("Sleep for 100 ms");
          Thread.sleep(100);
        } else return;
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    System.out.println("Send response");
    responseObserver.onNext(
        GreetResponse.newBuilder()
            .setResult("Hello " + request.getGreeting().getFirstName())
            .build());
    responseObserver.onCompleted();
  }
}
