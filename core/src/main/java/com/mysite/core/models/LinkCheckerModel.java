package com.mysite.core.models;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;

/**
 * LinkCheckerModel validates authored links, formatting internal /content URLs.
 * Used in HTL templates with:
 * data-sly-use.link="${'com.mysite.core.models.LinkCheckerModel' @ url=properties.link}"
 */
@Model(
    adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class LinkCheckerModel {

    @RequestAttribute(name = "url")
    private String url;

    public String getUrl() {
        if (StringUtils.isBlank(url)) {
            return "";
        }
        if (url.startsWith("/content/") && !url.endsWith(".html") && !url.contains(".")) {
            return url + ".html";
        }
        return url;
    }
}
