package com.github.haroldjcastillo.grpc.server.impl;

import com.github.haroldjcastillo.proto.calculator.SquareRootGrpc;
import com.github.haroldjcastillo.proto.calculator.SquareRootRequest;
import com.github.haroldjcastillo.proto.calculator.SquareRootResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class SquareRootServiceImpl extends SquareRootGrpc.SquareRootImplBase {

  @Override
  public void calculate(
      SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {
    int number = request.getValue();
    if (number >= 0) {
      double sqrt = Math.sqrt(number);
      responseObserver.onNext(SquareRootResponse.newBuilder().setResult(sqrt).build());
      responseObserver.onCompleted();
    } else {
      responseObserver.onError(
          Status.INVALID_ARGUMENT
              .withDescription("The number being sent is not positive")
              .augmentDescription("Number sent " + number)
              .asRuntimeException());
    }
  }
}
