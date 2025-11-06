package com.example.taskManagement.service;

import com.example.taskManagement.entity.User;
import com.example.taskManagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserImportService {

    private final UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    public List<User> importUsersFromExcel(MultipartFile file) throws IOException {
        List<User> importedUsers = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // skip header row
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String firstName = getCellValueAsString(row.getCell(0));
                String lastName = getCellValueAsString(row.getCell(1));
                String email = getCellValueAsString(row.getCell(2));

                if (email == null || email.isBlank()) continue;
                if (userRepository.existsByEmailIgnoreCase(email)) continue;

                User user = new User();
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode("12345"));
                user.setRole(User.Role.USER);
                userRepository.save(user);
                importedUsers.add(user);
            }
        }
        return importedUsers;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC)
            return String.valueOf((int) cell.getNumericCellValue());
        return cell.getStringCellValue().trim();
    }
}
