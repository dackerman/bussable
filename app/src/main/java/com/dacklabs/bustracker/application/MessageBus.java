package com.dacklabs.bustracker.application;

import android.util.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;

public final class MessageBus<T> {

    public interface ExceptionToMessage<T> {
        T toMessage(Throwable e);
    }

    public interface MessageHandler<A, T> {
        Set<T> handle(A message);
    }

    private final ExceptionToMessage<T> exceptionToMessage;
    private final Executor executor;
    private Multimap<Class, MessageHandler> dispatch = ArrayListMultimap.create();
    private BlockingDeque<T> deque = new LinkedBlockingDeque<>();
    private volatile boolean shouldStop;

    public MessageBus(ExceptionToMessage<T> exceptionToMessage, Executor executor) {
        this.exceptionToMessage = exceptionToMessage;
        this.executor = executor;
    }

    public void fire(T value) {
        log("adding message " + value + ", current queue length: " + deque.size());
        deque.offer(value);
    }

    public void startProcessingMessages() {
        log("starting to process messages...");
        shouldStop = false;
        while (true) {
            final T message;
            try {
                log("Waiting for next message... current queue length: " + deque.size());
                message = deque.take();
            } catch (InterruptedException e) {
                log("Interrupted! " + e.getMessage());
                deque.addLast(exceptionToMessage.toMessage(e));
                continue;
            }
            log("Processing message " + message);

            boolean didProcess = false;
            for (Class handlerClass : dispatch.keySet()) {
                if (handlerClass.isAssignableFrom(message.getClass())) {
                    for (MessageHandler handler : dispatch.get(handlerClass)) {
                        executor.execute(() -> {
                            log("Processing message in another thread");
                            Set<T> resultingMessages = handler.handle(message);
                            for (T resultingMessage : resultingMessages) {
                                deque.offer(resultingMessage);
                            }
                        });
                        didProcess = true;
                    }
                }
            }

            if (!didProcess) {
                log("No consumers for event " + message.getClass().getCanonicalName());
            }

            if (shouldStop) {
                log("Stopping processing messages");
                break;
            }
        }
    }

    public void stopProcessingMessages() {
        log("stopProcessingMessages called! Stopping");
        shouldStop = true;
    }

    public <A extends T> void register(Class<A> cls, MessageHandler<A, T> value) {
        dispatch.put(cls, value);
    }

    private void log(String message) {
        Log.d("DACK:MessageBus", message);
    }
}
