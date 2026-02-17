package com.den41k.controller;

import com.den41k.model.Project;
import com.den41k.model.Task;
import com.den41k.service.ProjectExcelExportService;
import com.den41k.service.ProjectService;
import com.den41k.service.TaskService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller("/projects")
public class ProjectExportController {

    private final ProjectService projectService;
    private final TaskService taskService;
    private final ProjectExcelExportService excelExportService;

    public ProjectExportController(
        ProjectService projectService,
        TaskService taskService,
        ProjectExcelExportService excelExportService
    ) {
        this.projectService = projectService;
        this.taskService = taskService;
        this.excelExportService = excelExportService;
    }

    @Get("/{id}/export-excel")
    public HttpResponse<byte[]> exportProjectExcel(Long id) {
        Project project = projectService.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        List<Task> tasks = taskService.findTasksByProjectId(id);

        byte[] excelBytes = excelExportService.exportProjectToExcel(project, tasks);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmm"));
        String filename = "project_" + id + "_" + timestamp + ".xlsx";

        return HttpResponse.ok(excelBytes)
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    }
}