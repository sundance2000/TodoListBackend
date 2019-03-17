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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        todoBase = new TodoBase();
        todoBase.setDescription(this.description);
        todoBase.setDone(this.done);
        todoBase.setDueDate(getOffsetDateTime(this.dueDate));
        todoBase.setTitle(this.title);
    }

    private void remove(int id) throws Exception {
        mockMvc.perform(delete("/todos/" + id))
            .andExpect(status().isNoContent());
    }

    // Create

    @Test
    public void testCreateTodo() throws Exception {
        // 1. Arrange
        String json = this.objectMapper.writeValueAsString(todoBase);

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
        String json = this.objectMapper.writeValueAsString(todoBase);

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
        String json = this.objectMapper.writeValueAsString(todoBase);

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
        String json = this.objectMapper.writeValueAsString(todoBase);

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
        String json = this.objectMapper.writeValueAsString(todoBase);

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
        String json = this.objectMapper.writeValueAsString(todoBase);

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
        String json = this.objectMapper.writeValueAsString(todoBase);

        // 2. Action
        mockMvc.perform(post("/todos/")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

}
