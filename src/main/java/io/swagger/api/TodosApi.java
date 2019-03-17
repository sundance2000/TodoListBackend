/**
 * NOTE: This class is auto generated by the swagger code generator program (2.4.2).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package io.swagger.api;

import io.swagger.model.ErrorResponse;
import io.swagger.model.TodoBase;
import io.swagger.model.TodoFull;
import io.swagger.model.TodoList;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-03-15T19:21:55.765Z")

@Api(value = "todos", description = "the todos API")
public interface TodosApi {

    Logger log = LoggerFactory.getLogger(TodosApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @ApiOperation(value = "Create Todo", nickname = "createTodo", notes = "Create a new todo.", response = TodoFull.class, tags={ "Todos", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Todo created.", response = TodoFull.class),
        @ApiResponse(code = 400, message = "Invalid new todo.", response = ErrorResponse.class, responseContainer = "List") })
    @RequestMapping(value = "/todos",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    default ResponseEntity<TodoFull> createTodo(@ApiParam(value = "The new todo."  )  @Valid @RequestBody TodoBase body) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"id\" : 1,  \"title\" : \"clean fridge\",  \"description\" : \"It's a mess\",  \"dueDate\" : \"2018-08-27T12:34:56.789Z\",  \"done\" : false}", TodoFull.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default TodosApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "Delete Todo", nickname = "deleteTodo", notes = "Delete an existing todo.", tags={ "Todos", })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Todo deleted."),
        @ApiResponse(code = 404, message = "Todo not found.") })
    @RequestMapping(value = "/todos/{todo-id}",
        produces = { "application/json" }, 
        method = RequestMethod.DELETE)
    default ResponseEntity<Void> deleteTodo(@ApiParam(value = "The todo identifier.",required=true) @PathVariable("todo-id") Integer todoId) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default TodosApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "Get Todo", nickname = "getTodo", notes = "Request an existing todo.", response = TodoFull.class, tags={ "Todos", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Todo found.", response = TodoFull.class),
        @ApiResponse(code = 404, message = "Todo not found.") })
    @RequestMapping(value = "/todos/{todo-id}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    default ResponseEntity<TodoFull> getTodo(@ApiParam(value = "The todo identifier.",required=true) @PathVariable("todo-id") Integer todoId) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"id\" : 1,  \"title\" : \"clean fridge\",  \"description\" : \"It's a mess\",  \"dueDate\" : \"2018-08-27T12:34:56.789Z\",  \"done\" : false}", TodoFull.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default TodosApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "List todos", nickname = "getTodos", notes = "Get a list of todos.", response = TodoList.class, responseContainer = "List", tags={ "Todos", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of todos.", response = TodoList.class, responseContainer = "List"),
        @ApiResponse(code = 204, message = "Empty list of todos"),
        @ApiResponse(code = 206, message = "Partial list of todos.", response = TodoList.class),
        @ApiResponse(code = 400, message = "Invalid query params", response = ErrorResponse.class, responseContainer = "List") })
    @RequestMapping(value = "/todos",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    default ResponseEntity<List<TodoList>> getTodos(@ApiParam(value = "Filters all or unfinished todos in the response", allowableValues = "all, unfinished", defaultValue = "unfinished") @Valid @RequestParam(value = "state", required = false, defaultValue="unfinished") String state,@Min(0) @Max(10) @ApiParam(value = "Maximal number of todos in the response", defaultValue = "5") @Valid @RequestParam(value = "limit", required = false, defaultValue="5") Integer limit,@Min(0) @Max(100) @ApiParam(value = "Offset for the todos in the response") @Valid @RequestParam(value = "offset", required = false) Integer offset) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("[ {  \"id\" : 1,  \"title\" : \"clean fridge\",  \"dueDate\" : \"2018-08-27T12:34:56.789Z\",  \"done\" : false}, {  \"id\" : 2,  \"title\" : \"clean bathrom\",  \"dueDate\" : \"2018-08-28T09:00:00.000Z\",  \"done\" : false}, {  \"id\" : 3,  \"title\" : \"bring out garbage\",  \"dueDate\" : \"2018-08-29T11:00:00.000Z\",  \"done\" : false}, {  \"id\" : 4,  \"title\" : \"go to supermarket\",  \"dueDate\" : \"2018-08-25T14:30:00.000Z\",  \"done\" : true}, {  \"id\" : 5,  \"title\" : \"write user stories\",  \"dueDate\" : \"2018-09-01T10:00:00.000Z\",  \"done\" : false}, {  \"id\" : 6,  \"title\" : \"pay bills\",  \"dueDate\" : \"2018-09-01T16:00:00.000Z\",  \"done\" : false}, {  \"id\" : 7,  \"title\" : \"call mum\",  \"dueDate\" : \"2018-09-01T19:00:00.000Z\",  \"done\" : false} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default TodosApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "Update Todo", nickname = "updateTodo", notes = "Update an existing todo.", tags={ "Todos", })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Todo updated."),
        @ApiResponse(code = 400, message = "Invalid modified todo.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Todo not found.") })
    @RequestMapping(value = "/todos/{todo-id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    default ResponseEntity<Void> updateTodo(@ApiParam(value = "The todo identifier.",required=true) @PathVariable("todo-id") Integer todoId,@ApiParam(value = "The modified todo."  )  @Valid @RequestBody TodoBase body) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default TodosApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
