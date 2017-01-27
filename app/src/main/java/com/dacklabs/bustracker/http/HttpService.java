package com.dacklabs.bustracker.http;

import android.util.Log;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;

public class HttpService {

    public String get(String urlString) throws IOException {
        URL url = new URL(urlString);
        Log.d("HttpService", "GET " + urlString);
        String response = Resources.asCharSource(url, Charsets.UTF_8).read();
        Log.d("HttpService", "Response: " + response);
        return response;
    }
}
