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
    final static long teamId = 1L;

    @Test
    public void canCreateTeam() {
        running(testServer(3333), new Runnable() {
            public void run() {
                assertThat(getTeam(teamId).getStatus()).isEqualTo(BAD_REQUEST);
                assertThat(createTeam(teamName).getStatus()).isEqualTo(CREATED);
                final WSResponse resp = getTeam(teamId);
                assertThat(resp.getStatus()).isEqualTo(OK);
                assertEquals(Json.toJson(ImmutableMap.of(
                        "id", teamId,
                        "name", teamName,
                        "members", 0
                )), resp.asJson());
            }
        });

    }

    @Test
    public void canAddMemberToTeam() {
        running(testServer(3333), new Runnable() {
            public void run() {
                assertThat(createUser(memberName).getStatus()).isEqualTo(CREATED);
                assertThat(createTeam(teamName).getStatus()).isEqualTo(CREATED);
                final JsonNode body = Json.toJson(ImmutableMap.of("identity", memberName));
                final WSResponse resp =
                        WS.url(teamsEndpoint + "/" + teamId + "/member").post(body).get(timeout);
                assertThat(resp.getStatus()).isEqualTo(CREATED);
                assertEquals(Json.toJson(ImmutableList.of(ImmutableMap.of(
                        "identity", memberName,
                        "email", "john@example.com",
                        "name", "John Doe"
                ))), resp.asJson());
            }
        });

    }

    @Test
    public void canRemoveMemberFromTeam() {
        running(testServer(3333), new Runnable() {
            public void run() {
                assertThat(createUser(memberName).getStatus()).isEqualTo(CREATED);
                final String memberName2 = "somemember";
                assertThat(createUser(memberName2).getStatus()).isEqualTo(CREATED);
                assertThat(createTeam(teamName).getStatus()).isEqualTo(CREATED);
                WS.url(teamsEndpoint + "/" + teamId + "/member")
                    .post(Json.toJson(ImmutableMap.of("identity", memberName))).get(timeout);
                WS.url(teamsEndpoint + "/" + teamId + "/member")
                    .post(Json.toJson(ImmutableMap.of("identity", memberName2))).get(timeout);
                final WSResponse resp =
                        WS.url(teamsEndpoint + "/" + teamId + "/members/" + memberName2).delete().get(timeout);
                assertThat(resp.getStatus()).isEqualTo(OK);
                assertEquals(Json.toJson(ImmutableList.of(ImmutableMap.of(
                        "identity", memberName,
                        "email", "john@example.com",
                        "name", "John Doe"
                ))), resp.asJson());
            }
        });

    }
}
