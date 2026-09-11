package userservice.controller;

import org.springframework.boot.test.mock.mockito.MockBean;
import userservice.dto.UserDto;
import userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto sampleDto() {
        return UserDto.builder()
                .id(1L)
                .name("Ivan")
                .email("ivan@example.com")
                .age(25)
                .build();
    }

    // ---------- GET ALL ----------

    @Test
    void getAll_returnsList() throws Exception {
        when(userService.findAll()).thenReturn(List.of(sampleDto()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.userDtoList[0].name").value("Ivan"))
                .andExpect(jsonPath("$._embedded.userDtoList[0].email").value("ivan@example.com"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    // ---------- GET BY ID ----------

    @Test
    void getById_returnsUser() throws Exception {
        when(userService.findById(1L)).thenReturn(sampleDto());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"));
    }

    // ---------- CREATE ----------

    @Test
    void create_returns201() throws Exception {
        UserDto input = UserDto.builder()
                .name("Ivan")
                .email("ivan@example.com")
                .age(25)
                .build();

        UserDto saved = sampleDto();
        when(userService.create(any(UserDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"));

        verify(userService).create(any(UserDto.class));
    }

    @Test
    void create_invalidData_returns400() throws Exception {
        UserDto bad = UserDto.builder()
                .name("")
                .email("not-email")
                .age(-1)
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any());
    }

    // ---------- UPDATE ----------

    @Test
    void update_returnsUpdatedUser() throws Exception {
        UserDto updated = UserDto.builder()
                .id(1L)
                .name("Petr")
                .email("petr@example.com")
                .age(30)
                .build();

        when(userService.update(eq(1L), any(UserDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Petr"));

        verify(userService).update(eq(1L), any(UserDto.class));
    }

    // ---------- DELETE ----------

    @Test
    void delete_returns204() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }
}