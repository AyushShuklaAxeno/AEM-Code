package com.mysite.core.services;

/**
 * Service interface for fetching third-party weather data securely on the server side.
 */
public interface WeatherService {

    /**
     * Retrieves weather JSON payload for a specified city and unit.
     *
     * @param city  City name (e.g. "London", "New York")
     * @param units Unit format ("metric" or "imperial")
     * @return Raw JSON response from the weather provider, or fallback JSON
     */
    String getWeatherData(String city, String units);
}

