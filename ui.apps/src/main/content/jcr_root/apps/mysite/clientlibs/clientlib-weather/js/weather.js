/**
 * City Weather Component Client-Side Controller
 * Location: /apps/mysite/clientlibs/clientlib-weather/js/weather.js
 *
 * Secure Architecture:
 * The browser never talks to third-party APIs directly, and never knows the API key.
 * It queries our local AEM Proxy Servlet at `/bin/mysite/weather`.
 */
(function() {
    'use strict';

    /**
     * Renders normalized weather data into the component DOM.
     */
    function renderWeather(widget, data, units, showDetails) {
        var statusEl = widget.querySelector('.cmp-weather__status');
        var mainEl = widget.querySelector('.cmp-weather__main');
        var detailsEl = widget.querySelector('.cmp-weather__details');
        var cityNameEl = widget.querySelector('.cmp-weather__city');
        var countryEl = widget.querySelector('.cmp-weather__country');
        var tempEl = widget.querySelector('.cmp-weather__temp');
        var descEl = widget.querySelector('.cmp-weather__description');
        var iconWrapper = widget.querySelector('.cmp-weather__icon-wrapper');

        var unitSymbol = units === 'imperial' ? '°F' : '°C';

        // Hide loading/error status
        if (statusEl) {
            statusEl.style.display = 'none';
        }

        // Header
        if (cityNameEl && data.city) {
            cityNameEl.textContent = data.city;
        }
        if (countryEl && data.country) {
            countryEl.textContent = data.country;
        }

        // Temperature & description
        if (tempEl && data.temp !== undefined) {
            tempEl.textContent = Math.round(data.temp) + unitSymbol;
        }
        if (descEl && data.description) {
            descEl.textContent = data.description;
        }

        // Icon
        if (iconWrapper && data.iconUrl) {
            iconWrapper.innerHTML = '';
            var iconImg = document.createElement('img');
            iconImg.src = data.iconUrl;
            iconImg.alt = data.description || 'Weather condition';
            iconImg.loading = 'lazy';
            iconWrapper.appendChild(iconImg);
        }

        if (mainEl) {
            mainEl.style.display = 'flex';
        }

        // Additional Details (Humidity, Wind, Feels like)
        if (showDetails && detailsEl) {
            var feelsLikeEl = widget.querySelector('.cmp-weather__feels-like');
            var humidityEl = widget.querySelector('.cmp-weather__humidity');
            var windEl = widget.querySelector('.cmp-weather__wind');

            if (feelsLikeEl && data.feelsLike !== undefined) {
                feelsLikeEl.textContent = Math.round(data.feelsLike) + unitSymbol;
            }
            if (humidityEl && data.humidity !== undefined) {
                humidityEl.textContent = data.humidity + '%';
            }
            if (windEl && data.windSpeed !== undefined) {
                windEl.textContent = data.windSpeed;
            }
            detailsEl.style.display = 'grid';
        }
    }

    /**
     * Normalizes either OpenWeatherMap JSON or wttr.in JSON format into a unified structure.
     */
    function parseWeatherData(data, city, units) {
        var isImperial = units === 'imperial';
        var speedUnit = isImperial ? 'mph' : 'm/s';

        // OpenWeatherMap format
        if (data.main && data.weather) {
            var weatherInfo = data.weather[0] || {};
            var iconUrl = weatherInfo.icon ?
                ('https://openweathermap.org/img/wn/' + weatherInfo.icon + '@2x.png') : '';
            var windSpeed = data.wind && data.wind.speed !== undefined ?
                (Math.round(data.wind.speed) + ' ' + speedUnit) : '';

            return {
                city: data.name || city,
                country: (data.sys && data.sys.country) || '',
                temp: data.main.temp,
                feelsLike: data.main.feels_like,
                description: weatherInfo.description || '',
                iconUrl: iconUrl,
                humidity: data.main.humidity,
                windSpeed: windSpeed
            };
        }

        // wttr.in format (fallback provider)
        if (data.current_condition && data.current_condition[0]) {
            var current = data.current_condition[0];
            var area = (data.nearest_area && data.nearest_area[0]) || {};
            var areaName = (area.areaName && area.areaName[0] && area.areaName[0].value) || city;
            var country = (area.country && area.country[0] && area.country[0].value) || '';
            var temp = isImperial ? parseFloat(current.temp_F) : parseFloat(current.temp_C);
            var feelsLike = isImperial ? parseFloat(current.FeelsLikeF) : parseFloat(current.FeelsLikeC);
            var desc = (current.weatherDesc && current.weatherDesc[0] && current.weatherDesc[0].value) || '';
            var wttrIconUrl = (current.weatherIconUrl && current.weatherIconUrl[0] && current.weatherIconUrl[0].value) || '';
            var speed = isImperial ? (current.windspeedMiles + ' mph') : (current.windspeedKmph + ' km/h');

            return {
                city: areaName,
                country: country,
                temp: temp,
                feelsLike: feelsLike,
                description: desc.trim(),
                iconUrl: wttrIconUrl,
                humidity: current.humidity,
                windSpeed: speed
            };
        }

        throw new Error('Unsupported weather response format');
    }

    /**
     * Initializes all weather widgets on the page by querying the AEM proxy servlet.
     */
    function initWeatherComponents() {
        var widgets = document.querySelectorAll('.cmp-weather[data-city]');

        widgets.forEach(function(widget) {
            if (widget.dataset.initialized === 'true') {
                return;
            }
            widget.dataset.initialized = 'true';

            var city = widget.getAttribute('data-city');
            var units = widget.getAttribute('data-units') || 'metric';
            var showDetails = widget.getAttribute('data-show-details') === 'true';

            if (!city) {
                return;
            }

            // Query our secure AEM server-side proxy servlet
            var proxyUrl = '/bin/mysite/weather?city=' + encodeURIComponent(city) +
                           '&units=' + encodeURIComponent(units);

            fetch(proxyUrl)
                .then(function(res) {
                    if (!res.ok) {
                        return res.json().then(function(errJson) {
                            throw new Error(errJson.error || 'Server error ' + res.status);
                        }).catch(function() {
                            throw new Error('Weather service unavailable (HTTP ' + res.status + ')');
                        });
                    }
                    return res.json();
                })
                .then(function(data) {
                    var parsed = parseWeatherData(data, city, units);
                    renderWeather(widget, parsed, units, showDetails);
                })
                .catch(function(err) {
                    var statusEl = widget.querySelector('.cmp-weather__status');
                    if (statusEl) {
                        statusEl.textContent = err.message || 'Unable to load weather.';
                        statusEl.classList.add('cmp-weather__status--error');
                        statusEl.style.display = 'block';
                    }
                });
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initWeatherComponents);
    } else {
        initWeatherComponents();
    }

    // Support live updates in AEM Sites Editor when dialog changes
    if (window.Granite && window.Granite.author) {
        document.addEventListener('cq-contentframe-layout', initWeatherComponents);
    }
})();

