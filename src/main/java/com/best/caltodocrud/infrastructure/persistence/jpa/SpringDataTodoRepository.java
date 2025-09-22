package com.best.caltodocrud.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataTodoRepository extends JpaRepository<TodoEntity, Long> {
    List<TodoEntity> findByDateOrderByIdAsc(String date);

    @Query("select t from TodoEntity t where t.date between :from and :to order by t.date asc, t.id asc")
    List<TodoEntity> findRange(@Param("from") String from, @Param("to") String to);
}
