package de.oberdoerfer.todolist.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.oberdoerfer.todolist.RFC3339DateFormat;
import de.oberdoerfer.todolist.model.TodoBase;
import de.oberdoerfer.todolist.model.TodoFull;
import de.oberdoerfer.todolist.model.TodoRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(TodosApiController.class)
public class TodosApiControllerTest {

    private ObjectMapper objectMapper;
    private RFC3339DateFormat rfc3339DateFormat = new RFC3339DateFormat();

    private Integer id = 1;
    private String description = "Test Description";
    private Boolean done = true;
    private String dueDate = "2019-03-17T16:06:38.445Z";
    private String title = "Test Title";

    private TodoBase todoBase;
    private TodoFull todoFull;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(this.rfc3339DateFormat);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.setUpTodoBase();
        this.setUpTodoFull();
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

    private void setUpTodoFull() {
        todoFull = new TodoFull(todoBase);
        todoFull.setId(id);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoRepository todoRepository;

    // Create

    @Test
    public void testCreateTodo() throws Exception {
        // 1. Arrange
        given(todoRepository.save(new TodoFull(todoBase))).willReturn(todoFull);
        String json = this.objectMapper.writeValueAsString(todoBase);

        // 2. Action
        mockMvc.perform(post("/todos")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id", is(this.id)))
                .andExpect(jsonPath("description", is(this.description)))
                .andExpect(jsonPath("done", is(this.done)))
                .andExpect(jsonPath("dueDate", is(this.dueDate)))
                .andExpect(jsonPath("title", is(this.title)));
    }

    @Test
    public void testCreateTodoMissingDescription() throws Exception {
        // 1. Arrange
        this.todoBase.setDescription(null);
        this.setUpTodoFull();
        given(todoRepository.save(new TodoFull(todoBase))).willReturn(todoFull);
        String json = this.objectMapper.writeValueAsString(todoBase);

        // 2. Action
        mockMvc.perform(post("/todos")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("id", is(this.id)))
            .andExpect(jsonPath("description").doesNotExist())
            .andExpect(jsonPath("done", is(this.done)))
            .andExpect(jsonPath("dueDate", is(this.dueDate)))
            .andExpect(jsonPath("title", is(this.title)));
    }

    @Test
    public void testCreateTodoMissingDone() throws Exception {
        // 1. Arrange
        this.todoBase.setDone(null);
        this.setUpTodoFull();
        given(todoRepository.save(new TodoFull(todoBase))).willReturn(todoFull);
        String json = this.objectMapper.writeValueAsString(todoBase);

        // 2. Action
        mockMvc.perform(post("/todos")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateTodoMissingDueDate() throws Exception {
        // 1. Arrange
        this.todoBase.setDueDate(null);
        this.setUpTodoFull();
        given(todoRepository.save(new TodoFull(todoBase))).willReturn(todoFull);
        String json = this.objectMapper.writeValueAsString(todoBase);

        // 2. Action
        mockMvc.perform(post("/todos")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateTodoInvalidDate() throws Exception {
        // 1. Arrange
        String json = "{  \"title\" : \"clean fridge\",  \"description\" : \"It's a mess\",  \"dueDate\" : \"test\",  \"done\" : false}";

        // 2. Action
        mockMvc.perform(post("/todos")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateTodoMissingTitle() throws Exception {
        // 1. Arrange
        this.todoBase.setTitle(null);
        this.setUpTodoFull();
        given(todoRepository.save(new TodoFull(todoBase))).willReturn(todoFull);
        String json = this.objectMapper.writeValueAsString(todoBase);

        // 2. Action
        mockMvc.perform(post("/todos")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateTodoTitleTooShort() throws Exception {
        // 1. Arrange
        this.todoBase.setTitle("");
        this.setUpTodoFull();
        given(todoRepository.save(new TodoFull(todoBase))).willReturn(todoFull);
        String json = this.objectMapper.writeValueAsString(todoBase);

        // 2. Action
        mockMvc.perform(post("/todos")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateTodoTitleTooLong() throws Exception {
        // 1. Arrange
        this.todoBase.setTitle("1234567890123456789012345678901");
        this.setUpTodoFull();
        given(todoRepository.save(new TodoFull(todoBase))).willReturn(todoFull);
        String json = this.objectMapper.writeValueAsString(todoBase);

        // 2. Action
        mockMvc.perform(post("/todos")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    // Delete

    @Test
    public void testDeleteTodo() throws Exception {
        // 1. Arrange
        given(todoRepository.findOne(1)).willReturn(todoFull);

        // 2. Action
        mockMvc.perform(delete("/todos/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteTodoNotExisting() throws Exception {
        // 1. Arrange
        given(todoRepository.findOne(1)).willReturn(null);

        // 2. Action
        mockMvc.perform(delete("/todos/1"))
            .andExpect(status().isNotFound());
    }

}
