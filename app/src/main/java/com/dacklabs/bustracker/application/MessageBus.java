package com.dacklabs.bustracker.application;

import android.util.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Function;

public final class MessageBus<T> {

    private final Function<Throwable, T> exceptionToMessage;
    private Multimap<Class, Function> dispatch = ArrayListMultimap.create();
    private LinkedBlockingDeque<T> deque = new LinkedBlockingDeque<>();
    private volatile boolean shouldStop;

    public MessageBus(Function<Throwable, T> exceptionToMessage) {
        this.exceptionToMessage = exceptionToMessage;
    }

    public void handle(T value) {
        deque.add(value);
    }

    public void startProcessingMessages() {
        shouldStop = false;
        while (true) {
            final T message;
            try {
                message = deque.takeFirst();
            } catch (InterruptedException e) {
                log("Interrupted! " + e.getMessage());
                deque.addLast(exceptionToMessage.apply(e));
                continue;
            }
            log("Processing message " + message.getClass().getCanonicalName());
            Collection<Function> functions = dispatch.get(message.getClass());
            for (Function<T, Set<T>> handler : functions) {
                deque.addAll(handler.apply(message));
            }
            if (functions.isEmpty()) {
                log("No consumers for event " + message.getClass().getCanonicalName());
            }
            if (shouldStop) {
                log("Stopping processing messages");
                break;
            }
        }
    }

    public void stopProcessingMessages() {
        shouldStop = true;
    }

    public <A extends T> void register(Class<A> cls, Function<A, Set<T>> value) {
        dispatch.put(cls, value);
    }

    private void log(String message) {
        Log.d("MessageBus", message);
    }
}
