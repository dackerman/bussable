package com.dacklabs.bustracker.http;

import com.dacklabs.bustracker.application.AppLogger;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpService {
    private final Collection<Call> inFlightRequests = new LinkedList<>();
    private final OkHttpClient client;
    private volatile boolean isCanceled;
    private AppLogger log;

    public HttpService(OkHttpClient client, AppLogger log) {
        this.client = client;
        this.log = log;
    }

    public void cancelInFlightRequests() {
        synchronized (inFlightRequests) {
            isCanceled = true;
            for (Call request : inFlightRequests) {
                request.cancel();
            }
        }
    }

    public String get(String urlString) throws IOException {
        Call call;
        synchronized (inFlightRequests) {
            if (isCanceled) {
                log("GET " + urlString);
                throw new IOException("Requests canceled");
            }

            call = client.newCall(new Request.Builder()
                    .url(urlString)
                    .build());

            inFlightRequests.add(call);
        }

        log("GET " + urlString);
        Response response = call.execute();
        String xmlString = response.body().string();
        log(String.format(Locale.US, "Response received: took %dms for response of %s",
                response.receivedResponseAtMillis() - response.sentRequestAtMillis(), urlString));
        logv(String.format(Locale.US, "Response: %s", xmlString));
        return xmlString;
    }

    private void logv(String message) {
        log.verbose(this, message);
    }

    private void log(String message) {
        log.info(this, message);
    }
}
