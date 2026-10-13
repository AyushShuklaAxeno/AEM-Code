package com.mysite.core.services.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * OSGi Configuration for the Weather Service.
 * Allows administrators to configure API endpoints and secure credentials
 * via AEM OSGi configurations (ui.config) or Cloud Manager secrets.
 */
@ObjectClassDefinition(
    name = "My Site - Weather Service Configuration",
    description = "Configures OpenWeatherMap API endpoints, timeouts, and secret credentials"
)
public @interface WeatherConfig {

    @AttributeDefinition(
        name = "API Endpoint",
        description = "URL for the OpenWeatherMap API",
        type = AttributeType.STRING
    )
    String apiEndpoint() default "https://api.openweathermap.org/data/2.5/weather";

    @AttributeDefinition(
        name = "API Key",
        description = "Secret API key for OpenWeatherMap (Stored securely via OSGi / Cloud Manager secrets)",
        type = AttributeType.PASSWORD
    )
    String apiKey() default "080ffde6b027dfedace9b1c0312965c9";

    @AttributeDefinition(
        name = "Connection Timeout (ms)",
        description = "HTTP connection and read timeout in milliseconds",
        type = AttributeType.INTEGER
    )
    int timeoutMs() default 5000;
}

