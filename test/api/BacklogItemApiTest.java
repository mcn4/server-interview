package api;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.*;
import play.test.*;
import play.libs.Json;
import play.libs.ws.*;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;
import static org.junit.Assert.*;

import com.google.common.collect.*;

import domain.model.Priority;

public class BacklogItemApiTest extends AbstractApiTest {

	@Test
	public void canGetACreatedBacklogItem() {
		running(testServer(3333), new Runnable() {
			public void run() {
				assertThat(createTeam("Team Foo").getStatus()).isEqualTo(CREATED);
				assertThat(createProject("Todo App 3.0", 1L).getStatus()).isEqualTo(CREATED);
				final WSResponse createResponse = createBacklogItem("some stuff", 1L);
				assertThat(createResponse.getStatus()).isEqualTo(CREATED);
				final JsonNode json = Json.toJson(ImmutableMap.builder()
					.put("id", 1L)
					.put("name", "some stuff")
					.put("summary", "As a user I want to have a shiny UI")
					.put("itemType", "FEATURE")
					.put("storyPoints", 5)
					.put("priority", "URGENT")
					.put("status", "ESTIMATED")
					.put("projectId", 1L).build());
				assertEquals(json, createResponse.asJson());
				final WSResponse getResponse = getBacklogItem(1L);
				assertThat(getResponse.getStatus()).isEqualTo(OK);
				assertEquals(json, getResponse.asJson());
			}
		});
	}

	@Test
	public void updatedSummaryIsRepresentedInResource() {
		running(testServer(3333), new Runnable() {
			public void run() {	
				assertThat(createTeam("Team Foo").getStatus()).isEqualTo(CREATED);
				assertThat(createProject("Todo App 3.0", 1L).getStatus()).isEqualTo(CREATED);
				assertThat(createBacklogItem("some stuff", 1L).getStatus()).isEqualTo(CREATED);
				final WSResponse updateResponse = changeSummary(1L, "modified user story");
				assertThat(updateResponse.getStatus()).isEqualTo(OK);
				final JsonNode json = Json.toJson(ImmutableMap.builder()
					.put("id", 1L)
					.put("name", "some stuff")
					.put("summary", "modified user story")
					.put("itemType", "FEATURE")
					.put("storyPoints", 5)
					.put("priority", "URGENT")
					.put("status", "ESTIMATED")
					.put("projectId", 1L).build());
				final WSResponse getResponse = getBacklogItem(1L);
				assertThat(getResponse.getStatus()).isEqualTo(OK);
				assertEquals(json, getResponse.asJson());
			}
		});
	}

	@Test
	public void updatedPriorityIsRepresentedInResource() {
		running(testServer(3333), new Runnable() {
			public void run() {	
				assertThat(createTeam("Team Foo").getStatus()).isEqualTo(CREATED);
				assertThat(createProject("Todo App 3.0", 1L).getStatus()).isEqualTo(CREATED);
				assertThat(createBacklogItem("some stuff", 1L).getStatus()).isEqualTo(CREATED);
				final WSResponse updateResponse = changeSummary(1L, "modified user story");
				assertThat(updateResponse.getStatus()).isEqualTo(OK);
				final JsonNode json = Json.toJson(ImmutableMap.builder()
					.put("id", 1L)
					.put("name", "some stuff")
					.put("summary", "modified user story")
					.put("itemType", "FEATURE")
					.put("storyPoints", 5)
					.put("priority", "URGENT")
					.put("status", "ESTIMATED")
					.put("projectId", 1L).build());
				final WSResponse getResponse = getBacklogItem(1L);
				assertThat(getResponse.getStatus()).isEqualTo(OK);
				assertEquals(json, getResponse.asJson());
			}
		});
	}

	@Test
	public void createdBacklogItemsCanBeFoundByProject() {
		running(testServer(3333), new Runnable() {
			public void run() {
				assertThat(createTeam("Team Foo").getStatus()).isEqualTo(CREATED);
				assertThat(createProject("Todo App 3.0", 1L).getStatus()).isEqualTo(CREATED);
				assertThat(createBacklogItem("some stuff", 1L).getStatus()).isEqualTo(CREATED);
				final WSResponse response = getBacklogItemsForProject(1L);
				assertThat(response.getStatus()).isEqualTo(OK);
				final JsonNode json = Json.toJson(ImmutableList.of(
					ImmutableMap.builder()
						.put("id", 1L)
						.put("name", "some stuff")
						.put("summary", "As a user I want to have a shiny UI")
						.put("itemType", "FEATURE")
						.put("storyPoints", 5)
						.put("priority", "URGENT")
						.put("status", "ESTIMATED")
						.put("tasks", 0)
						.put("projectId", 1L).build()));
				assertEquals(json, response.asJson());
			}
		});
	}

	@Test
	public void canGetACreatedTask() {
		running(testServer(3333), new Runnable() {
			public void run() {
				assertThat(createTeam("Team Foo").getStatus()).isEqualTo(CREATED);
				assertThat(createProject("Todo App 3.0", 1L).getStatus()).isEqualTo(CREATED);
				assertThat(createBacklogItem("some stuff", 1L).getStatus()).isEqualTo(CREATED);
				final WSResponse createResponse = createTask(1L, "set up project");
				assertThat(createResponse.getStatus()).isEqualTo(CREATED);
				checkTaskJson(createResponse.asJson());
				final WSResponse getResponse = getTask(1L, 1L);
				assertThat(getResponse.getStatus()).isEqualTo(OK);
				checkTaskJson(getResponse.asJson());
			}
		});
	}

	@Test
	public void createdTaskAppearsInListOfTasksForBacklogItem() {
		running(testServer(3333), new Runnable() {
			public void run() {
				assertThat(createTeam("Team Foo").getStatus()).isEqualTo(CREATED);
				assertThat(createProject("Todo App 3.0", 1L).getStatus()).isEqualTo(CREATED);
				assertThat(createBacklogItem("some stuff", 1L).getStatus()).isEqualTo(CREATED);
				assertThat(createTask(1L, "set up project").getStatus()).isEqualTo(CREATED);
				final WSResponse response = getTasks(1L);
				assertThat(response.getStatus()).isEqualTo(OK);
				final JsonNode json = response.asJson();
				assertTrue(json.isArray());
				assertTrue(json.path(0).isObject());
				checkTaskJson(json.path(0));
			}
		});
	}

	private static void checkTaskJson(JsonNode json) {
		assertThat(json.path("id").intValue()).isEqualTo(1);
		assertThat(json.path("name").textValue()).isEqualTo("set up project");
		assertThat(json.path("description").textValue()).isEqualTo("What needs to be done");
		assertThat(json.path("backlogItemId").longValue()).isEqualTo(1L);
		assertThat(json.path("githubStatus").textValue()).isEqualTo("OPEN");
		assertTrue(json.path("githubUrl").isTextual());
		assertTrue(json.path("githubComments").isArray());
		assertTrue(json.path("githubComments").path(0).path("login").isTextual());
		assertTrue(json.path("githubComments").path(0).path("body").isTextual());
		assertTrue(json.path("githubComments").path(0).path("url").isTextual());
	}

	private static WSResponse changeSummary(Long backlogItemId, String newSummary) {
		final JsonNode body = Json.toJson(ImmutableMap.of("summary", newSummary));
		return WS.url(backlogItemsEndpoint + "/" + backlogItemId + "/summary").put(body).get(timeout);
	}

	private static WSResponse prioritize(Long backlogItemId, Priority priority) {
		final JsonNode body = Json.toJson(ImmutableMap.of("priority", priority));
		return WS.url(backlogItemsEndpoint + "/" + backlogItemId + "/priority").put(body).get(timeout);
	}

}