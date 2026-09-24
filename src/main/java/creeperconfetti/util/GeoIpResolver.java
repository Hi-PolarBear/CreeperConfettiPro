package creeperconfetti.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves the server region (country code) from several public GeoIP services.
 *
 * <p>All providers are queried in parallel and the first successful answer wins,
 * so a single failing / slow / rate-limited API can never break detection.
 * Values are extracted with a plain regex instead of a YAML/JSON parser, which
 * avoids parser incompatibilities (some providers return JSON that SnakeYAML
 * cannot read).
 */
public class GeoIpResolver {

    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 3000;
    private static final long TOTAL_TIMEOUT_MS = 6000L;

    private static final Endpoint[] ENDPOINTS = {
            new Endpoint("https://ipwho.is/", "country_code"),
            new Endpoint("https://api.country.is/", "country"),
            new Endpoint("https://ipinfo.io/json", "country"),
            new Endpoint("https://ipapi.co/json/", "country_code"),
            new Endpoint("http://ip-api.com/json/?fields=countryCode", "countryCode"),
            new Endpoint("https://ip.zxinc.org/api.php?type=json", "country")
    };

    private final Logger logger;
    private final String userAgent;
    private final boolean debug;

    public GeoIpResolver(Logger logger, String userAgent, boolean debug) {
        this.logger = logger;
        this.userAgent = userAgent;
        this.debug = debug;
    }

    /**
     * @return an upper-case 2-letter country code, or {@code null} when every
     * provider failed.
     */
    public String resolve() {
        ExecutorService pool = Executors.newFixedThreadPool(ENDPOINTS.length);
        try {
            ExecutorCompletionService<String> completion = new ExecutorCompletionService<>(pool);
            for (Endpoint endpoint : ENDPOINTS) {
                completion.submit(() -> query(endpoint));
            }

            long deadline = System.currentTimeMillis() + TOTAL_TIMEOUT_MS;
            for (int index = 0; index < ENDPOINTS.length; index++) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0L) {
                    break;
                }
                Future<String> future = completion.poll(remaining, TimeUnit.MILLISECONDS);
                if (future == null) {
                    break;
                }
                try {
                    String code = future.get();
                    if (code != null) {
                        return code;
                    }
                } catch (Exception ignored) {
                    // provider failed, wait for the next finished one
                }
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } finally {
            pool.shutdownNow();
        }
        return null;
    }

    private String query(Endpoint endpoint) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(endpoint.url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setRequestProperty("Accept", "application/json");

            StringBuilder body = new StringBuilder();
            try (InputStream inputStream = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line).append('\n');
                }
            }

            String code = normalize(extract(body.toString(), endpoint.key));
            if (code != null && debug) {
                logger.info("[GeoIP] Country detected by " + endpoint.url + " -> " + code);
            }
            return code;
        } catch (Exception exception) {
            if (debug) {
                logger.info("[GeoIP] Provider unavailable: " + endpoint.url + " (" + exception.getMessage() + ")");
            }
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Extracts a JSON string value by key without pulling in a JSON parser.
     */
    private static String extract(String json, String key) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Normalizes a raw provider value into a 2-letter country code. Providers
     * that answer with localized names (e.g. Chinese) are mapped too.
     */
    private static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.matches("[A-Za-z]{2}")) {
            return value.toUpperCase();
        }
        if (value.contains("中国") || value.contains("中國") || value.equalsIgnoreCase("China")) {
            return "CN";
        }
        if (value.contains("香港") || value.equalsIgnoreCase("Hong Kong")) {
            return "HK";
        }
        if (value.contains("台湾") || value.contains("台灣") || value.equalsIgnoreCase("Taiwan")) {
            return "TW";
        }
        if (value.contains("澳门") || value.contains("澳門")) {
            return "MO";
        }
        if (value.contains("日本") || value.equalsIgnoreCase("Japan")) {
            return "JP";
        }
        if (value.contains("韩国") || value.contains("韓國") || value.equalsIgnoreCase("Korea")) {
            return "KR";
        }
        return null;
    }

    private static final class Endpoint {
        private final String url;
        private final String key;

        private Endpoint(String url, String key) {
            this.url = url;
            this.key = key;
        }
    }
}
