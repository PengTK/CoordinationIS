package com.den41k.service;

import com.den41k.model.Project;
import com.den41k.model.Task;
import com.den41k.model.TaskStatus;
import jakarta.inject.Singleton;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Singleton
public class ProjectExcelExportService {

    public byte[] exportProjectToExcel(Project project, List<Task> tasks) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Проект и задачи");

            // Стили
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLACK.getIndex());
            headerStyle.setFont(headerFont);

            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd.mm.yyyy"));

            // Информация о проекте
            Row projRow = sheet.createRow(0);
            projRow.createCell(0).setCellValue("Название проекта:");
            projRow.createCell(1).setCellValue(project.getTitle());

            Row descRow = sheet.createRow(1);
            descRow.createCell(0).setCellValue("Описание:");
            descRow.createCell(1).setCellValue(project.getDescription());

            Row statusRow = sheet.createRow(2);
            statusRow.createCell(0).setCellValue("Статус:");
            statusRow.createCell(1).setCellValue(project.getProjectStatus().toString());

            if (project.getDeadLine() != null) {
                Row dlRow = sheet.createRow(3);
                dlRow.createCell(0).setCellValue("Дедлайн:");
                Cell dlCell = dlRow.createCell(1);
                dlCell.setCellValue(java.sql.Date.valueOf(project.getDeadLine()));
                dlCell.setCellStyle(dateStyle);
            }

            int rowIdx = 5;
            sheet.createRow(rowIdx++).createCell(0).setCellValue("ЗАДАЧИ");

            // Заголовки таблицы задач
            String[] headers = {"ID", "Название", "Описание", "Статус", "Приоритет", "Исполнитель", "Проверяющий", "Дедлайн"};
            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Группировка по статусам
            for (TaskStatus status : TaskStatus.values()) {
                boolean hasTasks = false;
                for (Task task : tasks) {
                    if (task.getTaskStatus() == status) {
                        if (!hasTasks) {
                            sheet.createRow(rowIdx++).createCell(0)
                                    .setCellValue("→ " + status.getLabel());
                            hasTasks = true;
                        }

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(task.getId());
                        row.createCell(1).setCellValue(task.getTitle());
                        row.createCell(2).setCellValue(task.getDescription());
                        row.createCell(3).setCellValue(task.getTaskStatus().getLabel());
                        row.createCell(4).setCellValue(
                            task.getPriority() != null ? task.getPriority().toString() : ""
                        );
                        row.createCell(5).setCellValue(
                            task.getTaskExecutor() != null ?
                                task.getTaskExecutor().getName() + " " + task.getTaskExecutor().getSureName() : ""
                        );
                        row.createCell(6).setCellValue(
                            task.getApprover() != null ?
                                task.getApprover().getName() + " " + task.getApprover().getSureName() : ""
                        );
                        if (task.getDeadLine() != null) {
                            Cell dateCell = row.createCell(7);
                            dateCell.setCellValue(java.sql.Date.valueOf(task.getDeadLine()));
                            dateCell.setCellStyle(dateStyle);
                        }
                    }
                }
            }

            // Автоподбор ширины
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации Excel", e);
        }
    }
}