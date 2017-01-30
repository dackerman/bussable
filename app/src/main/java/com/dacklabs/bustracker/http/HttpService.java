package com.dacklabs.bustracker.http;

import android.util.Log;

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

    public HttpService(OkHttpClient client) {
        this.client = client;
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
                Log.d("HttpService", "GET " + urlString);
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
        log(String.format(Locale.US, "(took %dms) Response: %s",
                response.receivedResponseAtMillis() - response.sentRequestAtMillis(),
                xmlString));
        return xmlString;
    }

    private void log(String message) {
        Log.d("HttpService", message);
    }
}
