package com.securetask.taskmanager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.securetask.taskmanager.dto.TaskRequest;
import com.securetask.taskmanager.dto.TaskResponse;
import com.securetask.taskmanager.exception.ResourceNotFoundException;
import com.securetask.taskmanager.model.Task;
import com.securetask.taskmanager.model.User;
import com.securetask.taskmanager.repository.TaskRepository;
import com.securetask.taskmanager.repository.UserRepository;
import com.securetask.taskmanager.service.AuditService;
import com.securetask.taskmanager.service.TaskService;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TaskService taskService;

    private User currentUser;
    private Task sampleTask;
    private TaskRequest taskRequest;



    @Test
    void createTask_ShouldReturnTaskResponse_WhenValidRequest() {
        when(userRepository.findByEmail("priya@test.com"))
                .thenReturn(Optional.of(currentUser));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponse response = taskService.createTask(taskRequest);

        assertNotNull(response);
        assertEquals("Build the backend", response.getTitle());
        assertEquals("PENDING", response.getStatus());
        assertEquals("HIGH", response.getPriority());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_ShouldLogAuditEntry() {
        when(userRepository.findByEmail("priya@test.com"))
                .thenReturn(Optional.of(currentUser));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        taskService.createTask(taskRequest);

        verify(auditService).log(
                eq("CREATE"),
                eq("TASK"),
                eq(1L),
                anyString()
        );
    }

    @Test
    void createTask_ShouldDefaultToMediumPriority_WhenPriorityNotProvided() {
        taskRequest.setPriority(null);
        when(userRepository.findByEmail("priya@test.com"))
                .thenReturn(Optional.of(currentUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            assertEquals(Task.Priority.MEDIUM, saved.getPriority());
            return sampleTask;
        });

        taskService.createTask(taskRequest);
    }

    @Test
    void getMyTasks_ShouldReturnTasksForCurrentUser() {
        when(userRepository.findByEmail("priya@test.com"))
                .thenReturn(Optional.of(currentUser));
        when(taskRepository.findByAssignedTo(currentUser))
                .thenReturn(List.of(sampleTask));
        when(taskRepository.findByCreatedBy(currentUser))
                .thenReturn(List.of());

        List<TaskResponse> tasks = taskService.getMyTasks();

        assertFalse(tasks.isEmpty());
        assertEquals(1, tasks.size());
        assertEquals("Build the backend", tasks.get(0).getTitle());
    }

    @Test
    void updateStatus_ShouldChangeStatus_WhenTaskExists() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        taskService.updateStatus(1L, "IN_PROGRESS");

        verify(taskRepository).save(any(Task.class));
        verify(auditService).log(
                eq("UPDATE"),
                eq("TASK"),
                eq(1L),
                anyString()
        );
    }

    @Test
    void updateStatus_ShouldThrowException_WhenTaskNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.updateStatus(999L, "COMPLETED")
        );

        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    void deleteTask_ShouldThrowException_WhenTaskNotFound() {
        when(taskRepository.existsById(999L)).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.deleteTask(999L)
        );

        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void deleteTask_ShouldDelete_WhenTaskExists() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskService.deleteTask(1L);

        verify(taskRepository).deleteById(1L);
        verify(auditService).log(
                eq("DELETE"),
                eq("TASK"),
                eq(1L),
                anyString()
        );
    }
}
