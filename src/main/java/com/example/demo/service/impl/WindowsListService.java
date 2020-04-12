package com.example.demo.service.impl;

import com.example.demo.service.ListService;

public class WindowsListService implements ListService {
    @Override
    public String showListCmd() {
        return "dir";
    }
}
