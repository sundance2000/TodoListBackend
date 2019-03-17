package de.oberdoerfer.todolist.model;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends PagingAndSortingRepository<TodoFull, Integer> {

    List<TodoFull> findAllByDone(Boolean done, Pageable pageable);

}
