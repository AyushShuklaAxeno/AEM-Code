package com.mysite.core.services.impl;

import com.mysite.core.services.WeatherService;
import com.mysite.core.services.config.WeatherConfig;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Implementation of WeatherService that fetches weather data server-side
 * using credentials securely stored in OSGi configurations.
 */
@Component(
    service = WeatherService.class,
    immediate = true
)
@Designate(ocd = WeatherConfig.class)
public class WeatherServiceImpl implements WeatherService {

    private static final Logger LOG = LoggerFactory.getLogger(WeatherServiceImpl.class);

    private String apiEndpoint;
    private String apiKey;
    private int timeoutMs;

    @Activate
    @Modified
    protected void activate(final WeatherConfig config) {
        this.apiEndpoint = config.apiEndpoint();
        this.apiKey = config.apiKey();
        this.timeoutMs = config.timeoutMs();
        LOG.info("WeatherService activated. Endpoint: [{}], Timeout: [{}ms]", apiEndpoint, timeoutMs);
    }

    @Override
    public String getWeatherData(final String city, final String units) {
        if (StringUtils.isBlank(city)) {
            return null;
        }

        // Try primary provider (OpenWeatherMap) using the configured secret key
        if (StringUtils.isNotBlank(apiKey)) {
            try {
                String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.name());
                String encodedUnits = URLEncoder.encode(StringUtils.defaultIfBlank(units, "metric"), StandardCharsets.UTF_8.name());
                String urlString = String.format("%s?q=%s&units=%s&appid=%s", apiEndpoint, encodedCity, encodedUnits, apiKey);

                String response = executeHttpGet(urlString);
                if (response != null) {
                    return response;
                }
            } catch (Exception e) {
                LOG.warn("Error calling OpenWeatherMap for city '{}': {}. Attempting fallback.", city, e.getMessage());
            }
        }

        // Automatic fallback: wttr.in (keyless provider for resiliency)
        LOG.info("Fetching keyless fallback data for city: {}", city);
        return fetchKeylessFallback(city);
    }

    private String executeHttpGet(final String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(timeoutMs);
            connection.setReadTimeout(timeoutMs);
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                return readStream(connection.getInputStream());
            } else {
                LOG.warn("HTTP call to [{}] returned status code: {}", url.getHost(), responseCode);
                return null;
            }
        } catch (Exception e) {
            LOG.error("Failed to execute HTTP GET to: {}", urlString, e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String fetchKeylessFallback(final String city) {
        try {
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.name());
            String fallbackUrl = "https://wttr.in/" + encodedCity + "?format=j1";
            return executeHttpGet(fallbackUrl);
        } catch (Exception e) {
            LOG.error("Keyless fallback failed for city: {}", city, e);
            return null;
        }
    }

    private String readStream(final InputStream inputStream) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}

