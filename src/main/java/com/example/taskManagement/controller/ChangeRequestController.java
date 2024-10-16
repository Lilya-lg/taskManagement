package com.example.taskManagement.controller;



import com.example.taskManagement.entity.ChangeRequest;
import com.example.taskManagement.service.ChangeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ChangeRequestController {

    @Autowired
    private ChangeRequestService changeRequestService;

    @GetMapping("/change-requests")
    public String getChangeRequests(Model model) {
        List<ChangeRequest> changeRequests = changeRequestService.getAllChangeRequests();
        model.addAttribute("changeRequests", changeRequests);
        return "change-request-list";
    }
}
