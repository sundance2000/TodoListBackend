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

@Service
public class TodoService {

    enum TodoState {

        unfinished("unfinished"),
        all("all");

        private static final Map<String, TodoState> LOOKUP = Maps.uniqueIndex(Arrays.asList(TodoState.values()), TodoState::getName);

        private final String name;

        @NotNull TodoState(String name) {
            this.name = name;
        }

        @Nullable static TodoState getEnum(@NotNull String name) {
            return LOOKUP.get(name);
        }

        @NotNull String getName() {
            return this.name;
        }

    }

    @Autowired
    TodoRepository todoRepository;

    @NotNull public TodoFull create(TodoBase todo) throws ConstraintViolationException {
        return todoRepository.save(new TodoFull(todo));
    }

    public boolean delete(@NotNull Integer id) {
        TodoFull todo = todoRepository.findOne(id);
        if (todo == null) {
            return false;
        }
        todoRepository.delete(id);
        return true;
    }

    @Nullable public TodoFull get(@NotNull Integer id) {
        return todoRepository.findOne(id);
    }

    @NotNull public List<TodoList> get(@NotNull String state, @NotNull Integer limit, @Nullable Integer offset) {
        // Handle paging
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
