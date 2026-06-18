package com.securetask.taskmanager.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.securetask.taskmanager.model.Task;
import com.securetask.taskmanager.model.User;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Paginated version — returns a Page object with metadata
    @Override
    Page<Task> findAll(Pageable pageable);

    // Paginated + filtered by status
    Page<Task> findByStatus(Task.Status status, Pageable pageable);

    // Paginated + filtered by assigned user
    Page<Task> findByAssignedTo(User user, Pageable pageable);

    // Paginated + filtered by created user
    Page<Task> findByCreatedBy(User user, Pageable pageable);

    // Non-paginated versions — still needed internally
    List<Task> findByAssignedTo(User user);
    List<Task> findByCreatedBy(User user);
    List<Task> findByStatus(Task.Status status);

    // Find all tasks that are past deadline and not completed
    @Query("SELECT t FROM Task t WHERE t.deadline < :today " +
        "AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks(@Param("today") LocalDate today);

    // Paginated version
    @Query("SELECT t FROM Task t WHERE t.deadline < :today " +
        "AND t.status != 'COMPLETED'")
    Page<Task> findOverdueTasks(@Param("today") LocalDate today, Pageable pageable);

    // Count tasks by status
    long countByStatus(Task.Status status);

    // Count overdue tasks
    @Query("SELECT COUNT(t) FROM Task t WHERE t.deadline < :today " +
        "AND t.status != 'COMPLETED'")
    long countOverdueTasks(@Param("today") LocalDate today);

    // Search in title or description — case insensitive
@Query("SELECT t FROM Task t WHERE " +
       "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
       "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
Page<Task> searchTasks(@Param("keyword") String keyword, Pageable pageable);

// Search within a specific user's tasks
@Query("SELECT t FROM Task t WHERE " +
       "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
       "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
       "AND (t.assignedTo = :user OR t.createdBy = :user)")
Page<Task> searchMyTasks(@Param("keyword") String keyword,
        @Param("user") User user, Pageable pageable);

        // Tasks due between today and a future date
@Query("SELECT t FROM Task t WHERE t.deadline BETWEEN :today AND :futureDate " +
       "AND t.status != 'COMPLETED'")
Page<Task> findTasksDueBetween(
        @Param("today") LocalDate today,
        @Param("futureDate") LocalDate futureDate,
        Pageable pageable);

// Same but filtered to a specific user
@Query("SELECT t FROM Task t WHERE t.deadline BETWEEN :today AND :futureDate " +
       "AND t.status != 'COMPLETED' " +
       "AND (t.assignedTo = :user OR t.createdBy = :user)")
Page<Task> findMyTasksDueBetween(
        @Param("today") LocalDate today,
        @Param("futureDate") LocalDate futureDate,
        @Param("user") User user,
        Pageable pageable);

        // Count tasks by assigned user and status
long countByAssignedToAndStatus(User user, Task.Status status);

// Count all tasks assigned to user
long countByAssignedTo(User user);

// Count overdue tasks for a specific user
@Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo = :user " +
       "AND t.deadline < :today AND t.status != 'COMPLETED'")
long countOverdueTasksForUser(
        @Param("user") User user,
        @Param("today") LocalDate today);
}
