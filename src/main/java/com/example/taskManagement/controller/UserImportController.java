package com.example.taskManagement.controller;

import com.example.taskManagement.entity.User;
import com.example.taskManagement.repository.UserRepository;
import com.example.taskManagement.service.UserImportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserImportController {
    private final UserRepository userRepository;
    private final UserImportService userService;
    @GetMapping("/upload-users")
    public String showUploadForm() {
        return "upload-users"; // This will show upload-users.html
    }

    @PostMapping("/upload-users")
    public String uploadUsers(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        List<User> savedUsers = new ArrayList<>();
        savedUsers = userService.importUsersFromExcel(file);

        model.addAttribute("users", savedUsers);
        model.addAttribute("message", "Successfully imported " + savedUsers.size() + " users!");

        return "upload-users";
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC)
            return String.valueOf((int) cell.getNumericCellValue());
        return cell.getStringCellValue().trim();
    }
}