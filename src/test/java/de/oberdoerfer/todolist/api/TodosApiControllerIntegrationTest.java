package de.oberdoerfer.todolist.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.JsonPath;
import de.oberdoerfer.todolist.RFC3339DateFormat;
import de.oberdoerfer.todolist.model.TodoBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TodosApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;
    private RFC3339DateFormat rfc3339DateFormat = new RFC3339DateFormat();

    private String description = "Test Description";
    private Boolean done = true;
    private String dueDate = "2019-03-17T16:06:38.445Z";
    private String title = "Test Title";

    private TodoBase todoBase;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(this.rfc3339DateFormat);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.setUpTodoBase();
    }

    private OffsetDateTime getOffsetDateTime(String inputDate) {
        Date date = null;
        try {
            date = this.rfc3339DateFormat.parse(inputDate);
        } catch (ParseException ignored) {
        }
        OffsetDateTime offsetDateTime = null;
        if (date != null) {
            offsetDateTime = date.toInstant().atOffset(ZoneOffset.UTC);
        }
        return offsetDateTime;
    }

    private void setUpTodoBase() {
        this.todoBase = new TodoBase();
        this.todoBase.setDescription(this.description);
        this.todoBase.setDone(this.done);
        this.todoBase.setDueDate(getOffsetDateTime(this.dueDate));
        this.todoBase.setTitle(this.title);
    }

    private int create(Boolean done) throws Exception {
        TodoBase todoBase = new TodoBase();
        todoBase.setDone(done);
        todoBase.setDescription(this.description);
        todoBase.setDueDate(getOffsetDateTime(this.dueDate));
        todoBase.setTitle(this.title);

        String json = this.objectMapper.writeValueAsString(todoBase);
        MvcResult result = mockMvc.perform(post("/todos/")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
        String response = result.getResponse().getContentAsString();
        return JsonPath.parse(response).read("id");
    }

    private void remove(int id) throws Exception {
        mockMvc.perform(delete("/todos/" + id))
            .andExpect(status().isNoContent());
    }

    // Create

    @Test
    public void testCreateTodo() throws Exception {
        // 1. Arrange
        String json = this.objectMapper.writeValueAsString(this.todoBase);

        // 2. Action
        MvcResult result = mockMvc.perform(post("/todos/")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("description", is(this.description)))
            .andExpect(jsonPath("done", is(this.done)))
            .andExpect(jsonPath("dueDate", is(this.dueDate)))
            .andExpect(jsonPath("title", is(this.title)))
            .andReturn();

        // 4. Annihilate
        String response = result.getResponse().getContentAsString();
        Integer id = JsonPath.parse(response).read("id");
        this.remove(id);
    }

    @Test
    public void testCreateTodoMissingDescription() throws Exception {
        // 1. Arrange
        this.todoBase.setDescription(null);
        String json = this.objectMapper.writeValueAsString(this.todoBase);

        // 2. Action
        MvcResult result = mockMvc.perform(post("/todos/")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("description").doesNotExist())
            .andExpect(jsonPath("done", is(this.done)))
            .andExpect(jsonPath("dueDate", is(this.dueDate)))
            .andExpect(jsonPath("title", is(this.title)))
            .andReturn();

        // 4. Annihilate
        String response = result.getResponse().getContentAsString();
        Integer id = JsonPath.parse(response).read("id");
        this.remove(id);
    }

    @Test
    public void testCreateTodoMissingDone() throws Exception {
        // 1. Arrange
        this.todoBase.setDone(null);
        String json = this.objectMapper.writeValueAsString(this.todoBase);

        // 2. Action
        mockMvc.perform(post("/todos/")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateTodoMissingDueDate() throws Exception {
        // 1. Arrange
        this.todoBase.setDueDate(null);
        String json = this.objectMapper.writeValueAsString(this.todoBase);

        // 2. Action
        mockMvc.perform(post("/todos/")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateTodoInvalidDate() throws Exception {
        // 1. Arrange
        String json = "{  \"id\" : 1,  \"title\" : \"" + this.title + "\",  \"description\" : \"" + this.description + "\",  \"dueDate\" : \"test\",  \"done\" : " + this.done + "}";

        // 2. Action
        mockMvc.perform(post("/todos/")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateTodoMissingTitle() throws Exception {
        // 1. Arrange
        this.todoBase.setTitle(null);
        String json = this.objectMapper.writeValueAsString(this.todoBase);

        // 2. Action
        mockMvc.perform(post("/todos/")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateTodoTitleTooShort() throws Exception {
        // 1. Arrange
        this.todoBase.setTitle("");
        String json = this.objectMapper.writeValueAsString(this.todoBase);

        // 2. Action
        mockMvc.perform(post("/todos/")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateTodoTitleTooLong() throws Exception {
        // 1. Arrange
        this.todoBase.setTitle("1234567890123456789012345678901");
        String json = this.objectMapper.writeValueAsString(this.todoBase);

        // 2. Action
        mockMvc.perform(post("/todos/")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    // Delete

    @Test
    public void testDeleteTodo() throws Exception {
        // 1. Arrange
        int id = this.create(true);

        // 2. Action
        mockMvc.perform(delete("/todos/" + id))
            .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteTodoNoId() throws Exception {
        // 2. Action
        mockMvc.perform(delete("/todos/"))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testDeleteTodoNotExisting() throws Exception {
        // 2. Action
        mockMvc.perform(delete("/todos/999"))
            .andExpect(status().isNotFound());
    }

    // Get

    @Test
    public void testGetTodo() throws Exception {
        // 1. Arrange
        int id = this.create(true);

        // 2. Action
        mockMvc.perform(get("/todos/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("id", is(id)))
            .andExpect(jsonPath("description", is(this.description)))
            .andExpect(jsonPath("done", is(this.done)))
            .andExpect(jsonPath("dueDate", is(this.dueDate)))
            .andExpect(jsonPath("title", is(this.title)));

        // 4. Annihilate
        this.remove(id);
    }

    @Test
    public void testGetTodoNotExisting() throws Exception {
        // 2. Action
        mockMvc.perform(get("/todos/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetTodosEmptyList() throws Exception {
        // 2. Action
        mockMvc.perform(get("/todos/"))
            .andExpect(status().isNoContent());
    }

    @Test
    public void testGetTodosUnfinished() throws Exception {
        // 1. Arrange
        int id1 = this.create(false);
        int id2 = this.create(true);
        int id3 = this.create(false);
        int id4 = this.create(true);
        int id5 = this.create(false);

        // 2. Action
        mockMvc.perform(get("/todos/?state=unfinished"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].id", is(id1)))
            .andExpect(jsonPath("$[0].done", is(false)))
            .andExpect(jsonPath("$[0].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[0].title", is(this.title)))
            .andExpect(jsonPath("$[1].id", is(id3)))
            .andExpect(jsonPath("$[1].done", is(false)))
            .andExpect(jsonPath("$[1].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[1].title", is(this.title)))
            .andExpect(jsonPath("$[2].id", is(id5)))
            .andExpect(jsonPath("$[2].done", is(false)))
            .andExpect(jsonPath("$[2].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[2].title", is(this.title)));

        // 4. Annihilate
        this.remove(id1);
        this.remove(id2);
        this.remove(id3);
        this.remove(id4);
        this.remove(id5);
    }

    @Test
    public void testGetTodosAll() throws Exception {
        // 1. Arrange
        int id1 = this.create(false);
        int id2 = this.create(true);
        int id3 = this.create(false);

        // 2. Action
        mockMvc.perform(get("/todos/?state=all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].id", is(id1)))
            .andExpect(jsonPath("$[0].done", is(false)))
            .andExpect(jsonPath("$[0].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[0].title", is(this.title)))
            .andExpect(jsonPath("$[1].id", is(id2)))
            .andExpect(jsonPath("$[1].done", is(true)))
            .andExpect(jsonPath("$[1].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[1].title", is(this.title)))
            .andExpect(jsonPath("$[2].id", is(id3)))
            .andExpect(jsonPath("$[2].done", is(false)))
            .andExpect(jsonPath("$[2].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[2].title", is(this.title)));

        // 4. Annihilate
        this.remove(id1);
        this.remove(id2);
        this.remove(id3);
    }

    @Test
    public void testGetTodosNoState() throws Exception {
        // 1. Arrange
        int id1 = this.create(false);
        int id2 = this.create(true);
        int id3 = this.create(false);
        int id4 = this.create(true);
        int id5 = this.create(false);

        // 2. Action
        mockMvc.perform(get("/todos/"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].id", is(id1)))
            .andExpect(jsonPath("$[0].done", is(false)))
            .andExpect(jsonPath("$[0].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[0].title", is(this.title)))
            .andExpect(jsonPath("$[1].id", is(id3)))
            .andExpect(jsonPath("$[1].done", is(false)))
            .andExpect(jsonPath("$[1].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[1].title", is(this.title)))
            .andExpect(jsonPath("$[2].id", is(id5)))
            .andExpect(jsonPath("$[2].done", is(false)))
            .andExpect(jsonPath("$[2].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[2].title", is(this.title)));

        // 4. Annihilate
        this.remove(id1);
        this.remove(id2);
        this.remove(id3);
        this.remove(id4);
        this.remove(id5);
    }

    @Test
    public void testGetTodosLimit() throws Exception {
        // 1. Arrange
        int id1 = this.create(false);
        int id2 = this.create(false);
        int id3 = this.create(false);
        int id4 = this.create(false);
        int id5 = this.create(false);

        // 2. Action
        mockMvc.perform(get("/todos/?limit=2"))
            .andExpect(status().isPartialContent())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(id1)))
            .andExpect(jsonPath("$[0].done", is(false)))
            .andExpect(jsonPath("$[0].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[0].title", is(this.title)))
            .andExpect(jsonPath("$[1].id", is(id2)))
            .andExpect(jsonPath("$[1].done", is(false)))
            .andExpect(jsonPath("$[1].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[1].title", is(this.title)));

        // 4. Annihilate
        this.remove(id1);
        this.remove(id2);
        this.remove(id3);
        this.remove(id4);
        this.remove(id5);
    }

    @Test
    public void testGetTodosOffset() throws Exception {
        // 1. Arrange
        int id1 = this.create(false);
        int id2 = this.create(false);
        int id3 = this.create(false);

        // 2. Action
        mockMvc.perform(get("/todos/?offset=1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(id2)))
            .andExpect(jsonPath("$[0].done", is(false)))
            .andExpect(jsonPath("$[0].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[0].title", is(this.title)))
            .andExpect(jsonPath("$[1].id", is(id3)))
            .andExpect(jsonPath("$[1].done", is(false)))
            .andExpect(jsonPath("$[1].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[1].title", is(this.title)));

        // 4. Annihilate
        this.remove(id1);
        this.remove(id2);
        this.remove(id3);
    }

    @Test
    public void testGetTodosRemaining() throws Exception {
        // 1. Arrange
        int id1 = this.create(false);
        int id2 = this.create(false);
        int id3 = this.create(false);

        // 2. Action
        mockMvc.perform(get("/todos/?limit=2"))
            .andExpect(status().isPartialContent())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(id1)))
            .andExpect(jsonPath("$[0].done", is(false)))
            .andExpect(jsonPath("$[0].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[0].title", is(this.title)))
            .andExpect(jsonPath("$[1].id", is(id2)))
            .andExpect(jsonPath("$[1].done", is(false)))
            .andExpect(jsonPath("$[1].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[1].title", is(this.title)));

        // 4. Annihilate
        this.remove(id1);
        this.remove(id2);
        this.remove(id3);
    }

    // Update

    @Test
    public void testUpdateTodo() throws Exception {
        // 1. Arrange
        int id = this.create(false);
        String json = this.objectMapper.writeValueAsString(this.todoBase);

        // 2. Action
        mockMvc.perform(put("/todos/" + id)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // 4. Annihilate
        this.remove(id);
    }

    @Test
    public void testUpdateTodoNoId() throws Exception {
        // 1. Arrange
        String json = this.objectMapper.writeValueAsString(this.todoBase);

        // 2. Action
        mockMvc.perform(put("/todos/")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testUpdateTodoNotExisting() throws Exception {
        // 1. Arrange
        String json = this.objectMapper.writeValueAsString(this.todoBase);

        // 2. Action
        mockMvc.perform(put("/todos/999")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateTodoMissingDescription() throws Exception {
        // 1. Arrange
        int id = this.create(false);
        this.todoBase.setDescription(null);
        String json = this.objectMapper.writeValueAsString(this.todoBase);

        // 2. Action
        mockMvc.perform(put("/todos/" + id)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // 4. Annihilate
        this.remove(id);
    }

    @Test
    public void testUpdateTodoMissingDone() throws Exception {
        // 1. Arrange
        int id = this.create(false);
        this.todoBase.setDone(null);
        String json = this.objectMapper.writeValueAsString(this.todoBase);

        // 2. Action
        mockMvc.perform(put("/todos/" + id)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        // 4. Annihilate
        this.remove(id);
    }

    @Test
    public void testUpdateTodoMissingDueDate() throws Exception {
        // 1. Arrange
        int id = this.create(false);
        this.todoBase.setDueDate(null);
        String json = this.objectMapper.writeValueAsString(this.todoBase);

        // 2. Action
        mockMvc.perform(put("/todos/" + id)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        // 4. Annihilate
        this.remove(id);
    }

    @Test
    public void testUpdateTodoInvalidDate() throws Exception {
        // 1. Arrange
        int id = this.create(false);
        String json = "{  \"id\" : 1,  \"title\" : \"" + this.title + "\",  \"description\" : \"" + this.description + "\",  \"dueDate\" : \"test\",  \"done\" : " + this.done + "}";

        // 2. Action
        mockMvc.perform(put("/todos/" + id)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        // 4. Annihilate
        this.remove(id);
    }

    @Test
    public void testUpdateTodoMissingTitle() throws Exception {
        // 1. Arrange
        int id = this.create(false);
        this.todoBase.setTitle(null);
        String json = this.objectMapper.writeValueAsString(this.todoBase);

        // 2. Action
        mockMvc.perform(put("/todos/" + id)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        // 4. Annihilate
        this.remove(id);
    }

    @Test
    public void testUpdateTodoTitleTooShort() throws Exception {
        // 1. Arrange
        int id = this.create(false);
        this.todoBase.setTitle("");
        String json = this.objectMapper.writeValueAsString(this.todoBase);

        // 2. Action
        mockMvc.perform(put("/todos/" + id)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        // 4. Annihilate
        this.remove(id);
    }

    @Test
    public void testUpdateTodoTitleTooLong() throws Exception {
        // 1. Arrange
        int id = this.create(false);
        this.todoBase.setTitle("1234567890123456789012345678901");
        String json = this.objectMapper.writeValueAsString(this.todoBase);

        // 2. Action
        mockMvc.perform(put("/todos/" + id)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        // 4. Annihilate
        this.remove(id);
    }

}
