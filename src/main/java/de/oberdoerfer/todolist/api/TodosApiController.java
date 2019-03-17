package de.oberdoerfer.todolist.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.oberdoerfer.todolist.model.TodoBase;
import de.oberdoerfer.todolist.model.TodoFull;
import de.oberdoerfer.todolist.model.TodoList;
import de.oberdoerfer.todolist.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-03-15T19:21:55.765Z")

@Controller
public class TodosApiController implements TodosApi {

    @Autowired
    private TodoService todoService;

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public TodosApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    @Override
    public Optional<ObjectMapper> getObjectMapper() {
        return Optional.ofNullable(objectMapper);
    }

    @Override
    public Optional<HttpServletRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    public ResponseEntity<TodoFull> createTodo(@Valid @RequestBody TodoBase body) {
        return new ResponseEntity<TodoFull>(this.todoService.create(body), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteTodo(@PathVariable("todo-id") Integer todoId) {
        if (!this.todoService.delete(todoId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<TodoFull> getTodo(@PathVariable("todo-id") Integer todoId) {
        TodoFull todo = this.todoService.get(todoId);
        if (todo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<TodoFull>(this.todoService.get(todoId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<TodoList>> getTodos(@Valid @RequestParam(value = "state", required = false, defaultValue="unfinished") String state, @Valid @RequestParam(value = "limit", required = false, defaultValue="5") Integer limit, @Valid @RequestParam(value = "offset", required = false) Integer offset) {
        List<TodoList> todos = this.todoService.get(state, limit, offset);
        if (todos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<List<TodoList>>(todos, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateTodo(@PathVariable("todo-id") Integer todoId, @Valid @RequestBody TodoBase body) {
        if (!this.todoService.update(todoId, body)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
