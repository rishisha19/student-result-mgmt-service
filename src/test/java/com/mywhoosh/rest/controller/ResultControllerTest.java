package com.mywhoosh.rest.controller;

import com.mywhoosh.common.AppConstants;
import com.mywhoosh.common.Remarks;
import com.mywhoosh.exception.ErrorMsgs;
import com.mywhoosh.rest.model.ResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@AutoConfigureDataMongo
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Slf4j
public class ResultControllerTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private WebTestClient client;

    private WebSocketStompClient stompClient;

    private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

    @BeforeAll
    public void setup() {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        transports.add(new RestTemplateXhrTransport(new RestTemplate()));
        SockJsClient sockJsClient = new SockJsClient(transports);

        this.stompClient = new WebSocketStompClient(sockJsClient);
        this.stompClient.setTaskScheduler(new ConcurrentTaskScheduler());
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    }

    @AfterAll
    public void tearDown() {

        this.stompClient.stop();
    }

    private <T> StompSessionHandler getStompSessionHandler(CountDownLatch latch,
                                                           AtomicReference<Throwable> failure,
                                                           final String destination,
                                                           Class<T> payloadClassType,
                                                           final ResultDTO resultPayload,
                                                           final Consumer<Object> assertionHandler) {
        return new TestSessionHandler(failure) {

            @Override
            public void afterConnected(final StompSession session, StompHeaders connectedHeaders) {
                session.subscribe(destination, new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) { return payloadClassType; }
                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        try {
                            assertionHandler.accept(payload);
                        } catch (Throwable t) {
                            failure.set(t);
                        } finally {
                            session.disconnect();
                            latch.countDown();
                        }
                    }

                });

                try {
                    session.send("/app/results", resultPayload);
                } catch (Throwable t) {
                    failure.set(t);
                    latch.countDown();
                }
            }
        };
    }

    private void connectToWSEndpoint(WebSocketStompClient stompClient, int wsPort, CountDownLatch latch,
                                     AtomicReference<Throwable> failure, StompSessionHandler handler)
            throws InterruptedException {
        stompClient.connect("ws://localhost:{port}"+ AppConstants.WS_ENDPOINT, this.headers, handler, wsPort);

        if (latch.await(5, TimeUnit.SECONDS)) {
            if (failure.get() != null) {
                throw new AssertionError("", failure.get());
            }
        } else {

            fail("Message not received");
        }
    }


    @Test
    public void addResultSuccess() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Throwable> failure = new AtomicReference<>();

        StompSessionHandler handler = getStompSessionHandler(latch, failure, "/topic/results",
                ResultDTO.class, ResultDTO.builder().rollNumber(1).totalMarks(100).obtainedMarks(55).build(),
                (response) -> {
                    ResultDTO publishedResult = (ResultDTO) response;
                    assertEquals(1, publishedResult.getPositionInClass());
                    assertEquals(Remarks.PASSED, publishedResult.getRemarks());
                    assertEquals(1, publishedResult.getRollNumber());
                });

        connectToWSEndpoint(this.stompClient, this.port, latch, failure, handler);

    }

    @Test
    public void addResult_InvalidStudentRollNumber() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Throwable> failure = new AtomicReference<>();

        StompSessionHandler handler = getStompSessionHandler(latch, failure, "/queue/errors",
                String.class, ResultDTO.builder().rollNumber(122).totalMarks(100).obtainedMarks(55).build(),
                (errorMsg) -> {
                    String message = (String) errorMsg;
                    assertEquals("Student with roll number 122 does not exist", message);
                });

        connectToWSEndpoint(this.stompClient, this.port, latch, failure, handler);

    }


    @Test
    @Order(2)
    public void getResultApiTestSuccess() throws InterruptedException {
        insteringResult(ResultDTO.builder().rollNumber(1).totalMarks(100).obtainedMarks(55).build());
        this.client.get().uri("/students/result/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResultDTO.class)
                .value(resultDTO -> {
                    assertEquals(1, resultDTO.getRollNumber());
                    assertEquals(100, resultDTO.getTotalMarks());
                    assertEquals(55, resultDTO.getObtainedMarks());
                    assertEquals(Remarks.PASSED, resultDTO.getRemarks());
                    assertEquals(1, resultDTO.getPositionInClass());
                });
    }

    @Test
    @Order(2)
    public void getResultApiTestFailed_whenStudentNotFound(){
        this.client.get().uri("/students/result/122")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo("failed")
                .jsonPath("$.message").isEqualTo(MessageFormatter.format(ErrorMsgs.STUDENT_NOT_FOUND, 122).getMessage());
    }

    @Test
    @Order(2)
    public void getResultApiTestFailed_whenNoResultFoundForStudent(){
        this.client.get().uri("/students/result/2")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo("failed")
                .jsonPath("$.message").isEqualTo(MessageFormatter.format(ErrorMsgs.NO_RESULT_FOUND_FOR_STUDENT, 2).getMessage());
    }

    @Test
    @Order(4)
    public void getAllResultApiTestSuccess() throws InterruptedException {
        insteringResult(ResultDTO.builder().rollNumber(1).totalMarks(100).obtainedMarks(55).build());
        this.client.get().uri("/students/result/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResultDTO.class)
                .value(resultDTO -> {
                    assertEquals(1, resultDTO.getRollNumber());
                    assertEquals(100, resultDTO.getTotalMarks());
                    assertEquals(55, resultDTO.getObtainedMarks());
                    assertEquals(Remarks.PASSED, resultDTO.getRemarks());
                    assertEquals(1, resultDTO.getPositionInClass());
                });
    }


    public void insteringResult(ResultDTO resultDTO) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Throwable> failure = new AtomicReference<>();
        connectToWSEndpoint(this.stompClient, this.port, latch, failure,  getStompSessionHandler(latch, failure, "/topic/results",
                ResultDTO.class, resultDTO,
                (response) -> {
                    ResultDTO publishedResult = (ResultDTO) response;
                    assertEquals(1, publishedResult.getPositionInClass());
                    assertEquals(Remarks.PASSED, publishedResult.getRemarks());
                    assertEquals(1, publishedResult.getRollNumber());
                }));
    }

    private static class TestSessionHandler extends StompSessionHandlerAdapter {

        private final AtomicReference<Throwable> failure;

        public TestSessionHandler(AtomicReference<Throwable> failure) {
            this.failure = failure;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            this.failure.set(new Exception(headers.toString()));
        }

        @Override
        public void handleException(StompSession s, StompCommand c, StompHeaders h, byte[] p, Throwable ex) {
            this.failure.set(ex);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable ex) {
            this.failure.set(ex);
        }
    }

}
