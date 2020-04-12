package com.example.demo.service.impl;

import com.example.demo.service.ListService;

public class LinuxListService implements ListService {
    @Override
    public String showListCmd() {
        return "ls";
    }
}
