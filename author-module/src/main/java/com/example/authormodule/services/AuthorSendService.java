package com.example.authormodule.services;


import com.example.authormodule.entities.Author;
import com.example.book.module.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.NoSuchElementException;
import java.util.Optional;

@GrpcService
@Slf4j
public class AuthorSendService extends AuthorSendServiceGrpc.AuthorSendServiceImplBase {

    @Autowired
    AuthorRepository authorRepository;

    @Override
    public void checkAuthor(AuthorCreateRequest request,
                            StreamObserver<AuthorCreateResponse> observer) {
        observer.onNext(AuthorCreateResponse.newBuilder().setId(1).build());
        observer.onCompleted();
    }

    @Override
    public void getAuthor(AuthorRequest request,
                          StreamObserver<AuthorResponse> observer) {
        Optional<Author> a = authorRepository.findById(request.getId());
        log.info("found author for grpc request " + a.toString());
        if (a.isPresent()) {
            Author author = a.get();
            AuthorResponse authorResponse = AuthorResponse.newBuilder()
                    .setId(author.getId())
                    .setFirstname(author.getFirstname())
                    .setLastname(author.getLastname()).build();
            observer.onNext(authorResponse);
        } else observer.onError(new NoSuchElementException());

        observer.onCompleted();
    }
}

