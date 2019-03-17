package de.oberdoerfer.todolist.service;

import com.google.common.collect.Maps;
import de.oberdoerfer.todolist.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main service class to handle todo operations
 */
@Service
public class TodoService {

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
    TodoRepository todoRepository;

    /**
     * Creates a new TodoFull entity for a given TodoBase object
     * and stores it in the database.
     *
     * @param todo The TodoBase object containing the data of the todo to store
     * @return The created TodoFull object
     */
    @NotNull
    public TodoFull create(TodoBase todo) {
        return todoRepository.save(new TodoFull(todo));
    }

    /**
     * Delete a todo in the database
     *
     * @param id The ID of the todo to delete
     * @return True if todo was found and deleted
     */
    public boolean delete(@NotNull Integer id) {
        TodoFull todo = todoRepository.findOne(id);
        if (todo == null) {
            return false;
        }
        todoRepository.delete(id);
        return true;
    }

    /**
     * Gets a todo form the database
     *
     * @param id The ID of the todo to get
     * @return The todo, null if todo was not found
     */
    @Nullable
    public TodoFull get(@NotNull Integer id) {
        return todoRepository.findOne(id);
    }


    /**
     * Gets a list of todos from the database
     *
     * @param state The state of the todos
     * @param limit The maximum number of elements in the returned list
     * @param offset The starting point of the database elements to return.
     *               For a database with n elements, the list contains {@code limit} elements
     *               starting with {@code offset}+1.
     * @return The list of todos
     */
    @NotNull
    public List<TodoList> get(@NotNull String state, @NotNull Integer limit, @Nullable Integer offset) {
        // Handle limit and offset
        if (offset == null) {
            offset = 0;
        }
        Pageable pageable = new OffsetPageRequest(limit, offset);

        // Get todoList with desired state
        TodoState todoState = TodoState.getEnum(state);
        List<TodoFull> todoList = new ArrayList<>();
        if (todoState == TodoState.unfinished) {
            todoList = todoRepository.findAllByDone(false, pageable);
        } else if (todoState == TodoState.all) {
            todoList = todoRepository.findAll(pageable).getContent();
        }

        // Convert result list of TodoFull items to list of TodoList items
        return todoList.stream().map(TodoList::new).collect(Collectors.toList());
    }

    /**
     * Updates a todo
     *
     * @param id The ID of the todo to update
     * @param todoBase The TodoBase object containing the data of the todo to store
     * @return True if todo was found and updated
     */
    public boolean update(@NotNull Integer id, @NotNull TodoBase todoBase) {
        TodoFull todo = todoRepository.findOne(id);
        if (todo == null) {
            return false;
        }
        todo.setDone(todoBase.isDone());
        todo.setDueDate(todoBase.getDueDate());
        todo.setDescription(todoBase.getDescription());
        todo.setTitle(todoBase.getTitle());
        todoRepository.save(todo);
        return true;
    }

}
