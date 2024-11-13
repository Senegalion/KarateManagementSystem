package com.example.karatemanagementsystem.controllers;

import com.example.karatemanagementsystem.model.User;
import com.example.karatemanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminRESTControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

//    @Test
//    void getAllUsers_ShouldReturnUserList() throws Exception {
//        mockMvc.perform(get("/admin/users"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//    }
//
//    @Test
//    void deleteUser_ShouldReturnNoContent_WhenUserDeleted() throws Exception {
//        User user = new User();
//        user.setId(1L);
//        userRepository.save(user);
//
//        mockMvc.perform(delete("/admin/users/1"))
//                .andExpect(status().isNoContent());
//    }
//
//    @Test
//    void deleteUser_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
//        mockMvc.perform(delete("/admin/users/100"))
//                .andExpect(status().isNotFound());
//    }
}