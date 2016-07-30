package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import static api.AbstractApiTest.*;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.CREATED;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

public class TeamApiTest {
    final static String teamName = java.util.UUID.randomUUID().toString();
    final static String memberName = java.util.UUID.randomUUID().toString();
    final static String memberName2 = java.util.UUID.randomUUID().toString();
    final static long teamId = 1L;

    final static String memberUrl = teamsEndpoint + "/" + teamId + "/member";

    @Test
    public void canCreateTeam() {
        running(testServer(3333), new Runnable() {
            public void run() {
                assertBAD_REQUEST(getTeam(teamId));
                assertCREATED(createTeam(teamName));
                final WSResponse resp = getTeam(teamId);
                assertOK(resp);
                assertEquals(Json.toJson(ImmutableMap.of(
                        "id", teamId,
                        "name", teamName,
                        "members", 0
                )), resp.asJson());
            }
        });
    }

    @Test
    public void canCreateOnlyUniqueTeam() {
        running(testServer(3333), new Runnable() {
            public void run() {
                assertCREATED(createTeam(teamName));
                final WSResponse resp = createTeam(teamName);
                assertBAD_REQUEST(resp);
                assertEquals(Json.toJson(ImmutableMap.of(
                        "name", "Team name " + teamName + " is already taken"
                )), resp.asJson());
            }
        });
    }

    @Test
    public void canAddMemberToTeam() {
        running(testServer(3333), new Runnable() {
            public void run() {
                assertCREATED(createUser(memberName));
                assertCREATED(createTeam(teamName));
                final JsonNode body = memberIdentity(memberName);
                final WSResponse resp = WS.url(memberUrl).post(body).get(timeout);
                assertCREATED(resp);
                assertEquals(Json.toJson(ImmutableList.of(memberDetails(memberName))), resp.asJson());
            }
        });
    }

    @Test
    public void canAddOnlyUniqueMemberToTeam() {
        running(testServer(3333), new Runnable() {
            public void run() {
                assertCREATED(createUser(memberName));
                assertCREATED(createTeam(teamName));
                final JsonNode body = memberIdentity(memberName);
                assertCREATED(WS.url(memberUrl).post(body).get(timeout));

                final WSResponse resp = WS.url(memberUrl).post(body).get(timeout);
                assertOK(resp);
                assertEquals(Json.toJson(ImmutableList.of(memberDetails(memberName))), resp.asJson());

                assertCREATED(createUser(memberName2));
                final JsonNode body2 = memberIdentity(memberName2);
                final WSResponse resp2 = WS.url(memberUrl).post(body2).get(timeout);
                assertCREATED(resp2);
                assertEquals(Json.toJson(ImmutableList.of(
                        memberDetails(memberName2),
                        memberDetails(memberName)
                        )), resp2.asJson());
            }
        });
    }

    @Test
    public void canRemoveMemberFromTeam() {
        running(testServer(3333), new Runnable() {
            public void run() {
                assertCREATED(createUser(memberName));
                assertCREATED(createUser(memberName2));
                assertCREATED(createTeam(teamName));
                assertCREATED(WS.url(memberUrl).post(memberIdentity(memberName)).get(timeout));
                assertCREATED(WS.url(memberUrl).post(memberIdentity(memberName2)).get(timeout));
                final WSResponse resp =
                        WS.url(teamsEndpoint + "/" + teamId + "/members/" + memberName2).delete().get(timeout);
                assertOK(resp);
                assertEquals(Json.toJson(ImmutableList.of(memberDetails(memberName))), resp.asJson());
            }
        });
    }

    @Test
    public void canRemoveOnlyActualMemberFromTeam() {
        running(testServer(3333), new Runnable() {
            public void run() {
                assertCREATED(createUser(memberName));
                assertCREATED(createTeam(teamName));
                assertCREATED(WS.url(memberUrl).post(memberIdentity(memberName)).get(timeout));

                final WSResponse resp =
                        WS.url(teamsEndpoint + "/" + teamId + "/members/" + memberName2).delete().get(timeout);
                assertOK(resp);
                assertEquals(Json.toJson(ImmutableList.of(memberDetails(memberName))), resp.asJson());

                final WSResponse resp2 =
                        WS.url(teamsEndpoint + "/" + teamId + "/members/" + memberName).delete().get(timeout);
                assertOK(resp2);
                assertEquals(Json.toJson(ImmutableList.of()), resp2.asJson());
            }
        });
    }

    void assertStatus(WSResponse wsResponse, int expectedStatus) {
        assertThat(wsResponse.getStatus()).isEqualTo(expectedStatus);
    }

    void assertOK(WSResponse wsResponse) {
        assertStatus(wsResponse, OK);
    }

    void assertCREATED(WSResponse wsResponse) {
        assertStatus(wsResponse, CREATED);
    }

    void assertBAD_REQUEST(WSResponse wsResponse) {
        assertStatus(wsResponse, BAD_REQUEST);
    }

    JsonNode memberIdentity(String name) {
        return Json.toJson(ImmutableMap.of("identity", name));
    }

    ImmutableMap<String, String> memberDetails(String name) {
        return ImmutableMap.of(
                "identity", name,
                "email", "john@example.com",
                "name", "John Doe");
    }
}
