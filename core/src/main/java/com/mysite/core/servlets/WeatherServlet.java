package com.mysite.core.servlets;

import com.mysite.core.services.WeatherService;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Weather Proxy Servlet:
 * Mounts at /bin/mysite/weather.
 * Accepts GET requests with query parameters (?city=London&units=metric).
 * Calls WeatherService server-side and writes the JSON response to the client.
 * 
 * The client browser never sees the third-party API key.
 */
@Component(
    service = { Servlet.class },
    property = {
        "sling.servlet.paths=/bin/mysite/weather",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
    }
)
@ServiceDescription("My Site - Weather Proxy Servlet")
public class WeatherServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(WeatherServlet.class);

    @Reference
    private transient WeatherService weatherService;

    @Override
    protected void doGet(final SlingHttpServletRequest request,
                         final SlingHttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String city = request.getParameter("city");
        String units = request.getParameter("units");

        if (StringUtils.isBlank(city)) {
            response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Missing mandatory query parameter: 'city'\"}");
            return;
        }

        if (StringUtils.isBlank(units)) {
            units = "metric";
        }

        LOG.debug("WeatherServlet processing request for city: [{}], units: [{}]", city, units);

        String weatherJson = weatherService.getWeatherData(city.trim(), units.trim());

        if (StringUtils.isNotBlank(weatherJson)) {
            response.setStatus(SlingHttpServletResponse.SC_OK);
            response.getWriter().write(weatherJson);
        } else {
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Failed to retrieve weather data for '" + city + "'.\"}");
        }
    }
}

