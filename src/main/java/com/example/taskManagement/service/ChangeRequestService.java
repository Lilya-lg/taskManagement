package com.example.taskManagement.service;


import com.example.taskManagement.entity.ChangeRequest;
import com.example.taskManagement.repository.ChangeRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChangeRequestService {

    @Autowired
    private ChangeRequestRepository changeRequestRepository;

    public List<ChangeRequest> getAllChangeRequests() {
        return changeRequestRepository.findAll();
    }
}
