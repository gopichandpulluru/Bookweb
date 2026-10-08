package com.shopease.controller;

import com.shopease.exception.GlobalExceptionHandler;
import com.shopease.exception.OutOfStockException;
import com.shopease.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Plain MockMvc test (no Spring context, no database): proves each exception becomes the right JSON. */
class GlobalExceptionHandlerTest {

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void resourceNotFound_returns404WithMessage() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id: 42"))
                .andExpect(jsonPath("$.path").value("/test/not-found"));
    }

    @Test
    void outOfStock_returns409() throws Exception {
        mockMvc.perform(get("/test/out-of-stock"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void unexpectedError_returns500WithoutLeakingDetails() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Something went wrong. Please try again later."));
    }

    @RestController
    static class ThrowingController {

        @GetMapping("/test/not-found")
        String notFound() {
            throw new ResourceNotFoundException("Product", "id", 42);
        }

        @GetMapping("/test/out-of-stock")
        String outOfStock() {
            throw new OutOfStockException("Mouse", 5, 2);
        }

        @GetMapping("/test/boom")
        String boom() {
            throw new IllegalStateException("secret internal detail");
        }
    }
}
