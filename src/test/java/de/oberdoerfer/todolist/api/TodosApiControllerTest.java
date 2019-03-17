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

}