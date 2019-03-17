package io.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.model.TodoBase;
import java.time.OffsetDateTime;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * The full todo with identifier used as response object.
 */
@ApiModel(description = "The full todo with identifier used as response object.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-03-15T19:21:55.765Z")

@Entity
public class TodoFull extends TodoBase  {
  @JsonProperty("id")
  private Integer id = null;

  public TodoFull() {
  }

  public TodoFull id(Integer id) {
    this.id = id;
    return this;
  }

  public TodoFull(TodoBase todoBase) {
    this.setTitle(todoBase.getTitle());
    this.setDescription(todoBase.getDescription());
    this.setDueDate(todoBase.getDueDate());
    this.setDone(todoBase.isDone());
  }

  /**
   * Get id
   * minimum: 0
   * maximum: 1000000
   * @return id
  **/
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @ApiModelProperty(required = true, value = "")

@Min(0) @Max(1000000) 
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TodoFull todoFull = (TodoFull) o;
    return Objects.equals(this.id, todoFull.id) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TodoFull {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

