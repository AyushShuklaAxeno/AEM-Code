package com.mysite.core.models;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

/**
 * Sling Model for the Team Members component.
 * Adapts SlingHttpServletRequest and Resource to expose team member details.
 */
@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    resourceType = TeamMembers.RESOURCE_TYPE,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TeamMembers {

    public static final String RESOURCE_TYPE = "mysite/components/team-members";

    @ValueMapValue
    private String title;

    @ChildResource(name = "members")
    private List<TeamMember> members;

    private boolean empty;

    @PostConstruct
    protected void init() {
        empty = CollectionUtils.isEmpty(members);
    }

    public String getTitle() {
        return title;
    }

    public List<TeamMember> getMembers() {
        return members != null ? Collections.unmodifiableList(members) : Collections.emptyList();
    }

    public boolean isEmpty() {
        return empty;
    }
}

