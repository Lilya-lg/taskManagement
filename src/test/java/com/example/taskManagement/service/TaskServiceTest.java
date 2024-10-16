package com.example.taskManagement.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.taskManagement.entity.Task;
import com.example.taskManagement.entity.User;
import com.example.taskManagement.repository.TaskRepository;
import com.example.taskManagement.service.TaskService;
import com.example.taskManagement.entity.Task.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllTasks() {
        List<Task> tasks = Arrays.asList(new Task(), new Task());
        when(taskRepository.findAll()).thenReturn(tasks);

        List<Task> result = taskService.getAllTasks();
        assertEquals(2, result.size());
    }

    @Test
    public void testSaveTask() {
        Task task = new Task();
        task.setTitle("New Task");
        taskService.saveTask(task);
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    public void testCountAllTasks() {
        when(taskRepository.count()).thenReturn(10L);
        long count = taskService.countAllTasks();
        assertEquals(10, count);
    }

    @Test
    public void testCountTasksByStatus() {
        when(taskRepository.countByStatus(Status.TODO)).thenReturn(5L);
        long count = taskService.countTasksByStatus("TODO");
        assertEquals(5, count);
    }

    @Test
    public void testCountOverdueTasks() {
        LocalDate today = LocalDate.now();
        when(taskRepository.countByDueDateBeforeAndStatusNot(today, Status.COMPLETED)).thenReturn(3L);
        long count = taskService.countOverdueTasks();
        assertEquals(3, count);
    }

    @Test
    public void testFindById() {
        Task task = new Task();
        task.setId(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task result = taskService.findById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void testFindById_NotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        Task result = taskService.findById(1L);
        assertNull(result);
    }
}
