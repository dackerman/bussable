package com.dacklabs.bustracker.application;

import android.util.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

public final class MessageBus<T> {

    public interface ExceptionToMessage<T> {
        T toMessage(Throwable e);
    }

    public interface MessageHandler<A, T> {
        Set<T> handle(A message);
    }

    private final ExceptionToMessage<T> exceptionToMessage;
    private Multimap<Class, MessageHandler> dispatch = ArrayListMultimap.create();
    private LinkedBlockingDeque<T> deque = new LinkedBlockingDeque<>();
    private volatile boolean shouldStop;

    public MessageBus(ExceptionToMessage<T> exceptionToMessage) {
        this.exceptionToMessage = exceptionToMessage;
    }

    public void fire(T value) {
        deque.add(value);
    }

    public void startProcessingMessages() {
        log("starting to process messages...");
        shouldStop = false;
        while (true) {
            final T message;
            try {
                log("Waiting for next message...");
                message = deque.takeFirst();
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
                        deque.addAll(handler.handle(message));
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
        Log.d("MessageBus", message);
    }
}
