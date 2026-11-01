package com.example.shop;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ShopIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;

  @Test
  void fullCrudAndValidation() throws Exception {
    String c =
        mvc.perform(
                post("/api/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Books\",\"description\":\"Demo\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    long cid = json.readTree(c).get("id").asLong();
    String body =
        "{\"name\":\"Java Book\",\"price\":100000,\"stock\":5,\"categoryId\":" + cid + "}";
    String p =
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    long pid = json.readTree(p).get("id").asLong();
    mvc.perform(get("/api/products").param("q", "Java").param("categoryId", String.valueOf(cid)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].category.name").value("Books"));
    mvc.perform(get("/products"))
        .andExpect(status().isOk())
        .andExpect(view().name("products/list"));
    mvc.perform(get("/products/" + pid + "/edit")).andExpect(status().isOk());
    mvc.perform(get("/categories")).andExpect(status().isOk());
    mvc.perform(get("/categories/" + cid + "/edit")).andExpect(status().isOk());
    mvc.perform(
            put("/api/products/" + pid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.replace("100000", "200000")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.price").value(200000));
    mvc.perform(
            put("/api/categories/" + cid)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated\"}"))
        .andExpect(status().isOk());
    mvc.perform(delete("/api/categories/" + cid)).andExpect(status().isConflict());
    mvc.perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.replace("100000", "0")))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\" \"}"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/products/999999")).andExpect(status().isNotFound());
    mvc.perform(delete("/api/products/" + pid)).andExpect(status().isNoContent());
    mvc.perform(delete("/api/categories/" + cid)).andExpect(status().isNoContent());
  }

  @Test
  void formValidation() throws Exception {
    mvc.perform(post("/products/save").param("name", "").param("price", "-1").param("stock", "-1"))
        .andExpect(status().isOk())
        .andExpect(
            model().attributeHasFieldErrors("product", "name", "price", "stock", "categoryId"));
    mvc.perform(post("/categories/save").param("name", ""))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("category", "name"));
    mvc.perform(get("/products/new")).andExpect(status().isOk());
    mvc.perform(get("/categories/new")).andExpect(status().isOk());
  }
}
