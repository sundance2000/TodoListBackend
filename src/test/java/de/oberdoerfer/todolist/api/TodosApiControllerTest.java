package de.oberdoerfer.todolist.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.oberdoerfer.todolist.RFC3339DateFormat;
import de.oberdoerfer.todolist.model.OffsetPageRequest;
import de.oberdoerfer.todolist.model.TodoBase;
import de.oberdoerfer.todolist.model.TodoFull;
import de.oberdoerfer.todolist.model.TodoRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(TodosApiController.class)
public class TodosApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoRepository todoRepository;

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

    private TodoFull generateTodoFull(int id, Boolean done) {
        todoFull = new TodoFull();
        todoFull.setId(id);
        todoFull.setDescription(this.description);
        todoFull.setDone(done);
        todoFull.setDueDate(getOffsetDateTime(this.dueDate));
        todoFull.setTitle(this.title);
        return todoFull;
    }

    // Create

    @Test
    public void testCreateTodo() throws Exception {
        // 1. Arrange
        given(todoRepository.save(new TodoFull(todoBase))).willReturn(todoFull);
        String json = this.objectMapper.writeValueAsString(todoBase);

        // 2. Action
        mockMvc.perform(post("/todos/")
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
        mockMvc.perform(post("/todos/")
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
    public void testDeleteTodoNoId() throws Exception {
        // 2. Action
        mockMvc.perform(delete("/todos/"))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testDeleteTodoNotExisting() throws Exception {
        // 1. Arrange
        given(todoRepository.findOne(1)).willReturn(null);

        // 2. Action
        mockMvc.perform(delete("/todos/1"))
            .andExpect(status().isNotFound());
    }

    // Get

    @Test
    public void testGetTodo() throws Exception {
        // 1. Arrange
        given(todoRepository.findOne(1)).willReturn(todoFull);

        // 2. Action
        mockMvc.perform(get("/todos/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("id", is(this.id)))
            .andExpect(jsonPath("description", is(this.description)))
            .andExpect(jsonPath("done", is(this.done)))
            .andExpect(jsonPath("dueDate", is(this.dueDate)))
            .andExpect(jsonPath("title", is(this.title)));
    }

    @Test
    public void testGetTodoNotExisting() throws Exception {
        // 1. Arrange
        given(todoRepository.findOne(1)).willReturn(null);

        // 2. Action
        mockMvc.perform(get("/todos/1"))
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetTodosEmptyList() throws Exception {
        // 1. Arrange
        Pageable pageable = new OffsetPageRequest(5, 0);
        PageImpl<TodoFull> page = new PageImpl<>(new ArrayList<>());
        given(todoRepository.findAllByDone(false, pageable)).willReturn(page);

        // 2. Action
        mockMvc.perform(get("/todos/"))
            .andExpect(status().isNoContent());
    }

    @Test
    public void testGetTodosUnfinished() throws Exception {
        // 1. Arrange
        List<TodoFull> todoFullList = new ArrayList<>();
        todoFullList.add(this.generateTodoFull(1, false));
        todoFullList.add(this.generateTodoFull(2, false));
        todoFullList.add(this.generateTodoFull(3, false));
        Pageable pageable = new OffsetPageRequest(5, 0);
        PageImpl<TodoFull> page = new PageImpl<>(todoFullList);
        given(todoRepository.findAllByDone(false, pageable)).willReturn(page);

        // 2. Action
        mockMvc.perform(get("/todos/?state=unfinished"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].done", is(false)))
            .andExpect(jsonPath("$[0].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[0].title", is(this.title)))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].done", is(false)))
            .andExpect(jsonPath("$[1].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[1].title", is(this.title)))
            .andExpect(jsonPath("$[2].id", is(3)))
            .andExpect(jsonPath("$[2].done", is(false)))
            .andExpect(jsonPath("$[2].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[2].title", is(this.title)));
    }

    @Test
    public void testGetTodosAll() throws Exception {
        // 1. Arrange
        List<TodoFull> todoFullList = new ArrayList<>();
        todoFullList.add(this.generateTodoFull(1, false));
        todoFullList.add(this.generateTodoFull(2, true));
        todoFullList.add(this.generateTodoFull(3, false));
        Pageable pageable = new OffsetPageRequest(5, 0);
        PageImpl<TodoFull> page = new PageImpl<>(todoFullList);
        given(todoRepository.findAll(pageable)).willReturn(page);

        // 2. Action
        mockMvc.perform(get("/todos/?state=all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].done", is(false)))
            .andExpect(jsonPath("$[0].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[0].title", is(this.title)))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].done", is(true)))
            .andExpect(jsonPath("$[1].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[1].title", is(this.title)))
            .andExpect(jsonPath("$[2].id", is(3)))
            .andExpect(jsonPath("$[2].done", is(false)))
            .andExpect(jsonPath("$[2].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[2].title", is(this.title)));
    }

    @Test
    public void testGetTodosNoState() throws Exception {
        // 1. Arrange
        List<TodoFull> todoFullList = new ArrayList<>();
        todoFullList.add(this.generateTodoFull(1, false));
        todoFullList.add(this.generateTodoFull(2, true));
        todoFullList.add(this.generateTodoFull(3, false));
        Pageable pageable = new OffsetPageRequest(5, 0);
        PageImpl<TodoFull> page = new PageImpl<>(todoFullList);
        given(todoRepository.findAllByDone(false, pageable)).willReturn(page);

        // 2. Action
        mockMvc.perform(get("/todos/"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].done", is(false)))
            .andExpect(jsonPath("$[0].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[0].title", is(this.title)))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].done", is(true)))
            .andExpect(jsonPath("$[1].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[1].title", is(this.title)))
            .andExpect(jsonPath("$[2].id", is(3)))
            .andExpect(jsonPath("$[2].done", is(false)))
            .andExpect(jsonPath("$[2].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[2].title", is(this.title)));
    }

    @Test
    public void testGetTodosLimit() throws Exception {
        // 1. Arrange
        List<TodoFull> todoFullList = new ArrayList<>();
        todoFullList.add(this.generateTodoFull(1, false));
        todoFullList.add(this.generateTodoFull(2, true));
        Pageable pageable = new OffsetPageRequest(2, 0);
        PageImpl<TodoFull> page = new PageImpl<>(todoFullList);
        given(todoRepository.findAllByDone(false, pageable)).willReturn(page);

        // 2. Action
        mockMvc.perform(get("/todos/?limit=2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].done", is(false)))
            .andExpect(jsonPath("$[0].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[0].title", is(this.title)))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].done", is(true)))
            .andExpect(jsonPath("$[1].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[1].title", is(this.title)));

    }

    @Test
    public void testGetTodosOffset() throws Exception {
        // 1. Arrange
        List<TodoFull> todoFullList = new ArrayList<>();
        todoFullList.add(this.generateTodoFull(2, true));
        todoFullList.add(this.generateTodoFull(3, false));
        Pageable pageable = new OffsetPageRequest(5, 1);
        PageImpl<TodoFull> page = new PageImpl<>(todoFullList);
        given(todoRepository.findAllByDone(false, pageable)).willReturn(page);

        // 2. Action
        mockMvc.perform(get("/todos/?offset=1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(2)))
            .andExpect(jsonPath("$[0].done", is(true)))
            .andExpect(jsonPath("$[0].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[0].title", is(this.title)))
            .andExpect(jsonPath("$[1].id", is(3)))
            .andExpect(jsonPath("$[1].done", is(false)))
            .andExpect(jsonPath("$[1].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[1].title", is(this.title)));

    }

    @Test
    public void testGetTodosRemaining() throws Exception {
        // 1. Arrange
        List<TodoFull> todoFullList = new ArrayList<>();
        todoFullList.add(this.generateTodoFull(1, false));
        todoFullList.add(this.generateTodoFull(2, true));
        Pageable pageable = new OffsetPageRequest(2, 0);
        PageImpl<TodoFull> page = new PageImpl<>(todoFullList, pageable, 3);
        given(todoRepository.findAllByDone(false, pageable)).willReturn(page);

        // 2. Action
        mockMvc.perform(get("/todos/?limit=2"))
            .andExpect(status().isPartialContent())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].done", is(false)))
            .andExpect(jsonPath("$[0].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[0].title", is(this.title)))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].done", is(true)))
            .andExpect(jsonPath("$[1].dueDate", is(this.dueDate)))
            .andExpect(jsonPath("$[1].title", is(this.title)));
    }

    // Update

    @Test
    public void testUpdateTodo() throws Exception {
        // 1. Arrange
        given(todoRepository.findOne(1)).willReturn(todoFull);
        given(todoRepository.save(new TodoFull(todoBase))).willReturn(todoFull);
        String json = this.objectMapper.writeValueAsString(todoBase);

        // 2. Action
        mockMvc.perform(put("/todos/1")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    @Test
    public void testUpdateTodoNoId() throws Exception {
        // 1. Arrange
        String json = this.objectMapper.writeValueAsString(todoBase);

        // 2. Action
        mockMvc.perform(put("/todos/")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testUpdateTodoNotExisting() throws Exception {
        // 1. Arrange
        given(todoRepository.findOne(1)).willReturn(null);
        String json = this.objectMapper.writeValueAsString(todoBase);

        // 2. Action
        mockMvc.perform(put("/todos/1")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateTodoMissingDescription() throws Exception {
        // 1. Arrange
        this.todoBase.setDescription(null);
        this.setUpTodoFull();
        given(todoRepository.findOne(1)).willReturn(todoFull);
        given(todoRepository.save(new TodoFull(todoBase))).willReturn(todoFull);
        String json = this.objectMapper.writeValueAsString(todoBase);

        // 2. Action
        mockMvc.perform(put("/todos/1")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    @Test
    public void testUpdateTodoMissingDone() throws Exception {
        // 1. Arrange
        this.todoBase.setDone(null);
        String json = this.objectMapper.writeValueAsString(todoBase);

        // 2. Action
        mockMvc.perform(put("/todos/1")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateTodoMissingDueDate() throws Exception {
        // 1. Arrange
        this.todoBase.setDueDate(null);
        String json = this.objectMapper.writeValueAsString(todoBase);

        // 2. Action
        mockMvc.perform(put("/todos/1")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateTodoInvalidDate() throws Exception {
        // 1. Arrange
        String json = "{  \"id\" : 1,  \"title\" : \"" + this.title + "\",  \"description\" : \"" + this.description + "\",  \"dueDate\" : \"test\",  \"done\" : " + this.done + "}";

        // 2. Action
        mockMvc.perform(put("/todos/1")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateTodoMissingTitle() throws Exception {
        // 1. Arrange
        this.todoBase.setTitle(null);
        String json = this.objectMapper.writeValueAsString(todoBase);

        // 2. Action
        mockMvc.perform(put("/todos/1")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateTodoTitleTooShort() throws Exception {
        // 1. Arrange
        this.todoBase.setTitle("");
        String json = this.objectMapper.writeValueAsString(todoBase);

        // 2. Action
        mockMvc.perform(put("/todos/1")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateTodoTitleTooLong() throws Exception {
        // 1. Arrange
        this.todoBase.setTitle("1234567890123456789012345678901");
        String json = this.objectMapper.writeValueAsString(todoBase);

        // 2. Action
        mockMvc.perform(put("/todos/1")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
    
}
