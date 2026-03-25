package com.example.freetableapp.util;

import android.text.TextUtils;

public final class UrlResolver {
    private static final String STORAGE_BASE_URL = "http://10.0.2.2:8081/laravel-freetable-api/public";

    private UrlResolver() {
    }

    public static String resolveStorageUrl(String pathOrUrl) {
        if (TextUtils.isEmpty(pathOrUrl)) {
            return null;
        }
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            return pathOrUrl
                    .replace("http://localhost", "http://10.0.2.2")
                    .replace("https://localhost", "https://10.0.2.2")
                    .replace("http://127.0.0.1", "http://10.0.2.2")
                    .replace("https://127.0.0.1", "https://10.0.2.2");
        }
        if (pathOrUrl.startsWith("/")) {
            return STORAGE_BASE_URL + pathOrUrl;
        }
        return STORAGE_BASE_URL + "/" + pathOrUrl;
    }
}

