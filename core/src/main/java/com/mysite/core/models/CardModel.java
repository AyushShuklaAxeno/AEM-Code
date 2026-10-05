package com.mysite.core.models;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
    adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CardModel {

    @ValueMapValue
    private String cardTitle;

    @ValueMapValue
    private String description;

    @ValueMapValue
    private String ctaText;

    @ValueMapValue
    private String ctaLink;

    @ValueMapValue
    @Default(values = "default")
    private String cardStyle;

    private String formattedLink;
    private boolean empty;

    @PostConstruct
    protected void init() {
        if (StringUtils.isNotBlank(ctaLink)) {
            if (ctaLink.startsWith("/content/") && !ctaLink.endsWith(".html")) {
                formattedLink = ctaLink + ".html";
            } else {
                formattedLink = ctaLink;
            }
        }
        empty = StringUtils.isBlank(cardTitle) && StringUtils.isBlank(description);
    }

    public String getCardTitle() {
        return cardTitle;
    }

    public String getDescription() {
        return description;
    }

    public String getCtaText() {
        return ctaText;
    }

    public String getCtaLink() {
        return formattedLink != null ? formattedLink : ctaLink;
    }

    public String getCardStyle() {
        return cardStyle;
    }

    public boolean isEmpty() {
        return empty;
    }
}

