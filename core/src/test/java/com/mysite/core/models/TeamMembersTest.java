package com.mysite.core.models;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import com.mysite.core.testcontext.AppAemContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(AemContextExtension.class)
class TeamMembersTest {

    private final AemContext context = AppAemContext.newAemContext();

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(TeamMembers.class, TeamMember.class);
    }

    @Test
    void testTeamMembersWithData() {
        Map<String, Object> props = new HashMap<>();
        props.put("sling:resourceType", TeamMembers.RESOURCE_TYPE);
        props.put("title", "Our Leadership Team");

        Resource rootResource = context.create().resource("/content/mysite/team", props);

        Map<String, Object> member1Props = new HashMap<>();
        member1Props.put("name", "Jane Doe");
        member1Props.put("role", "Chief Executive Officer");
        member1Props.put("photo", "/content/dam/mysite/jane.jpg");
        context.create().resource(rootResource, "members/item0", member1Props);

        Map<String, Object> member2Props = new HashMap<>();
        member2Props.put("name", "John Smith");
        member2Props.put("role", "Chief Technology Officer");
        member2Props.put("photo", "/content/dam/mysite/john.jpg");
        context.create().resource(rootResource, "members/item1", member2Props);

        context.currentResource(rootResource);

        TeamMembers model = context.request().adaptTo(TeamMembers.class);
        assertNotNull(model);
        assertEquals("Our Leadership Team", model.getTitle());
        assertFalse(model.isEmpty());
        assertEquals(2, model.getMembers().size());

        TeamMember first = model.getMembers().get(0);
        assertEquals("Jane Doe", first.getName());
        assertEquals("Chief Executive Officer", first.getRole());
        assertEquals("/content/dam/mysite/jane.jpg", first.getPhoto());

        TeamMember second = model.getMembers().get(1);
        assertEquals("John Smith", second.getName());
        assertEquals("Chief Technology Officer", second.getRole());
        assertEquals("/content/dam/mysite/john.jpg", second.getPhoto());
    }

    @Test
    void testTeamMembersEmpty() {
        Resource emptyResource = context.create().resource("/content/mysite/empty-team",
            "sling:resourceType", TeamMembers.RESOURCE_TYPE);
        context.currentResource(emptyResource);

        TeamMembers model = context.request().adaptTo(TeamMembers.class);
        assertNotNull(model);
        assertTrue(model.isEmpty());
        assertTrue(model.getMembers().isEmpty());
    }
}

