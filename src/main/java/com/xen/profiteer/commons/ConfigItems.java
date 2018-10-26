package com.xen.profiteer.commons;

import io.vertx.core.http.HttpClientOptions;

public class ConfigItems {

    public static HttpClientOptions DEFAULT_CLIENT_OPTIONS = new HttpClientOptions().setSsl(true).setTrustAll(true);

}
