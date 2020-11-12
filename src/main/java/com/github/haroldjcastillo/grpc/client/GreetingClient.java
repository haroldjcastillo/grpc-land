package com.github.haroldjcastillo.grpc.client;

import com.github.haroldjcastillo.proto.greet.GreetRequest;
import com.github.haroldjcastillo.proto.greet.GreetResponse;
import com.github.haroldjcastillo.proto.greet.GreetServiceGrpc;
import com.github.haroldjcastillo.proto.greet.Greeting;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

  public static void main(String[] args) {
    GreetingClient client = new GreetingClient();
    client.run();
  }

  private void run() {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();
    // doUnaryCall(channel);
    // doServerStreamingCall(channel);
    // doClientStreamingCall(channel);
    // doBiDiStreamingCall(channel);
    doUnaryCallWithDeadline(channel);
    System.out.println("Shutting down channel");
    channel.shutdown();
  }

  private void doUnaryCallWithDeadline(ManagedChannel channel) {
    GreetServiceGrpc.GreetServiceBlockingStub blockingStub =
        GreetServiceGrpc.newBlockingStub(channel);
    // first call (500 ms deadline)
    try {
      System.out.println("Sending a request with a deadline of 500 ms.");
      GreetResponse response =
          blockingStub
              .withDeadline(Deadline.after(500, TimeUnit.MILLISECONDS))
              .greetWithDeadline(
                  GreetRequest.newBuilder()
                      .setGreeting(Greeting.newBuilder().setFirstName("Harold").build())
                      .build());
      System.out.println(response.getResult());
    } catch (StatusRuntimeException e) {
      if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
        System.out.println("Deadline has been exceeded, we don't won't the response");
      } else {
        e.printStackTrace();
      }
    }
  }

  public void doUnaryCall(ManagedChannel channel) {
    // Created a greet service client
    GreetServiceGrpc.GreetServiceBlockingStub greetClient =
        GreetServiceGrpc.newBlockingStub(channel);

    // Created a protocol buffer message
    Greeting greeting =
        Greeting.newBuilder().setFirstName("Harold").setLastName("Castillo").build();
    GreetRequest request = GreetRequest.newBuilder().setGreeting(greeting).build();

    // Unary Call RPC and get response
    GreetResponse response = greetClient.greet(request);
    System.out.println(response.getResult());
  }

  private void doServerStreamingCall(ManagedChannel channel) {
    // Created a greet service client
    GreetServiceGrpc.GreetServiceBlockingStub greetClient =
        GreetServiceGrpc.newBlockingStub(channel);

    // Created a protocol buffer message
    Greeting greeting =
        Greeting.newBuilder().setFirstName("Harold").setLastName("Castillo").build();
    GreetRequest request = GreetRequest.newBuilder().setGreeting(greeting).build();

    greetClient
        .greetManyTimes(request)
        .forEachRemaining(response -> System.out.println(response.getResult()));
  }

  private void doClientStreamingCall(ManagedChannel channel) {
    GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);
    CountDownLatch latch = new CountDownLatch(1);

    StreamObserver<GreetRequest> requestObserver =
        asyncClient.longGreet(
            new StreamObserver<GreetResponse>() {
              @Override
              public void onNext(GreetResponse value) {
                // we get a response from the server
                System.out.println("Received a response from the server");
                System.out.println(value.getResult());
                // onNext will be call only once
              }

              @Override
              public void onError(Throwable t) {
                // we get an error from the server
              }

              @Override
              public void onCompleted() {
                // the server is done sending us data
                System.out.println("Server has completed sending us something");
                latch.countDown();
                // onCompleted will be called right after onNext()
              }
            });

    System.out.println("Sending message 1");
    Greeting greeting =
        Greeting.newBuilder().setFirstName("Harold").setLastName("Castillo").build();
    GreetRequest request = GreetRequest.newBuilder().setGreeting(greeting).build();
    requestObserver.onNext(request);

    System.out.println("Sending message 2");
    greeting = Greeting.newBuilder().setFirstName("Johan").setLastName("Castillo").build();
    request = GreetRequest.newBuilder().setGreeting(greeting).build();
    requestObserver.onNext(request);

    System.out.println("Sending message 3");
    greeting = Greeting.newBuilder().setFirstName("John").setLastName("Doe").build();
    request = GreetRequest.newBuilder().setGreeting(greeting).build();
    requestObserver.onNext(request);
    requestObserver.onCompleted();

    try {
      latch.await(3L, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void doBiDiStreamingCall(ManagedChannel channel) {
    GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);
    CountDownLatch latch = new CountDownLatch(1);

    StreamObserver<GreetRequest> requestObserver =
        asyncClient.greetEveryone(
            new StreamObserver<GreetResponse>() {
              @Override
              public void onNext(GreetResponse value) {
                System.out.println("Response from server: " + value.getResult());
              }

              @Override
              public void onError(Throwable t) {
                latch.countDown();
              }

              @Override
              public void onCompleted() {
                System.out.println("Serve is done sending data");
                latch.countDown();
              }
            });
    Arrays.asList("Harold", "Foo", "Bar", "John", "Doe")
        .forEach(
            name -> {
              System.out.println("Sending " + name);
              requestObserver.onNext(
                  GreetRequest.newBuilder()
                      .setGreeting(Greeting.newBuilder().setFirstName(name).build())
                      .build());
              try {
                Thread.sleep(100);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            });
    requestObserver.onCompleted();
    try {
      latch.await(3, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
