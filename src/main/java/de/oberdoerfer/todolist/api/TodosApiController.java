package de.oberdoerfer.todolist.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import de.oberdoerfer.todolist.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-03-15T19:21:55.765Z")

@Controller
public class TodosApiController implements TodosApi {

    /**
     * Enum class to define the state of a todo
     */
    enum TodoState {

        unfinished("unfinished"),
        all("all");

        /**
         * Static lookup map to find the corresponding enum for the given name
         */
        private static final Map<String, TodoState> LOOKUP = Maps.uniqueIndex(Arrays.asList(TodoState.values()), TodoState::getName);

        private final String name;

        /**
         * Internal constructor for enum
         *
         * @param name The name of the todo state
         */
        @NotNull TodoState(String name) {
            this.name = name;
        }

        /**
         * Gets the corresponding enum for the given name
         *
         * @param name The name of the state to get
         * @return The corresponding enum
         */
        @Nullable
        static TodoState getEnum(@NotNull String name) {
            return LOOKUP.get(name);
        }

        @NotNull String getName() {
            return this.name;
        }

    }

    @Autowired
    private TodoRepository todoRepository;

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
        return new ResponseEntity<TodoFull>(this.todoRepository.save(new TodoFull(body)), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteTodo(@PathVariable("todo-id") Integer todoId) {
        TodoFull todo = this.todoRepository.findOne(todoId);
        if (todo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.todoRepository.delete(todoId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<TodoFull> getTodo(@PathVariable("todo-id") Integer todoId) {
        TodoFull todo = this.todoRepository.findOne(todoId);
        if (todo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<TodoFull>(todo, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<TodoList>> getTodos(@Valid @RequestParam(value = "state", required = false, defaultValue="unfinished") String state, @Valid @RequestParam(value = "limit", required = false, defaultValue="5") Integer limit, @Valid @RequestParam(value = "offset", required = false) Integer offset) {
        // Handle limit and offset
        if (offset == null) {
            offset = 0;
        }
        Pageable pageable = new OffsetPageRequest(limit, offset);

        // Get todoList with desired state
        Page<TodoFull> todoListPage;
        TodoState todoState = TodoState.getEnum(state);
        if (todoState == TodoState.unfinished) {
            todoListPage = this.todoRepository.findAllByDone(false, pageable);
        } else {
            todoListPage = this.todoRepository.findAll(pageable);
        }

        // Convert result list of TodoFull items to list of TodoList items
        List<TodoList> resultList = todoListPage.getContent().stream().map(TodoList::new).collect(Collectors.toList());

        // Return 204 if empty
        if (resultList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        // Return 200 if end of list reached
        if (todoListPage.isLast()) {
            return new ResponseEntity<List<TodoList>>(resultList, HttpStatus.OK);
        }

        // Return 206 if there are more elements
        return new ResponseEntity<List<TodoList>>(resultList, HttpStatus.PARTIAL_CONTENT);
    }

    @Override
    public ResponseEntity<Void> updateTodo(@PathVariable("todo-id") Integer todoId, @Valid @RequestBody TodoBase body) {
        TodoFull todo = this.todoRepository.findOne(todoId);
        if (todo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        todo.setDone(body.isDone());
        todo.setDueDate(body.getDueDate());
        todo.setDescription(body.getDescription());
        todo.setTitle(body.getTitle());
        this.todoRepository.save(todo);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
