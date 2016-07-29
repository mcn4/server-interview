package api;

import org.junit.*;
import play.test.*;
import play.libs.Json;
import play.libs.ws.*;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.*;

public class ProjectApiTest extends AbstractApiTest {

	@Test
	public void canGetACreatedProject() {
		running(testServer(3333), new Runnable() {
			public void run() {
				assertThat(createTeam("Team Foo").getStatus()).isEqualTo(CREATED);
				final WSResponse createResponse = createProject("Yet Another To-Do App", 1L);
				assertThat(createResponse.getStatus()).isEqualTo(CREATED);
				checkJsonBody(createResponse.asJson());
				final WSResponse getResponse = getProject(1L);
				assertThat(getResponse.getStatus()).isEqualTo(OK);
				checkJsonBody(getResponse.asJson());
			}
		});
	}

	@Test
	public void anUpdatedDescriptionIsRepresentedInTheResource() {
		running(testServer(3333), new Runnable() {
			public void run() {
				assertThat(createTeam("Team Foo").getStatus()).isEqualTo(CREATED);
				assertThat(createProject("Yet Another To-Do App", 1L)).isEqualTo(CREATED);
				final WSResponse updateResponse = changeDescription(1L, "new description");
				assertThat(updateResponse.getStatus()).isEqualTo(OK);
				final WSResponse getResponse = getProject(1L);
				assertThat(getResponse.getStatus()).isEqualTo(OK);
				checkJsonBody(getResponse.asJson());
			}
		});
	}

	private static void checkJsonBody(JsonNode json) {
		checkJsonBody(json, "The best project in the world");
	}
	private static void checkJsonBody(JsonNode json, String description) {
		assertThat(json.path("id").longValue()).isEqualTo(1L);
		assertThat(json.path("name").textValue()).isEqualTo("Yet Another To-Do App");
		assertThat(json.path("description").textValue()).isEqualTo(description);
		assertThat(json.path("teamId").longValue()).isEqualTo(1L);
		assertTrue(json.path("githubUrl").isTextual());
		assertTrue(json.path("gitUrl").isTextual());
		assertTrue(json.path("githubWatchers").isInt());
		assertTrue(json.path("githubForks").isInt());
	}

	private static WSResponse changeDescription(Long projectId, String newDescription) {
		final JsonNode json = Json.toJson(ImmutableMap.of("description", newDescription));
		return WS.url(projectsEndpoint + "/" + projectId + "/description").put(json).get(timeout);
	}

}