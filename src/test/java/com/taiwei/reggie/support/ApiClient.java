package com.taiwei.reggie.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A "browser": MockMvc plus a persistent HTTP session, so a login in one call is visible in the next ones.
 * URLs are passed as already-encoded strings (e.g. {@code %20}), exactly as a browser would send them.
 */
public class ApiClient {

    private final MockMvc mvc;
    private final ObjectMapper mapper;
    private MockHttpSession session = new MockHttpSession();

    public ApiClient(MockMvc mvc, ObjectMapper mapper) {
        this.mvc = mvc;
        this.mapper = mapper;
    }

    public Response get(String url) {
        return perform(MockMvcRequestBuilders.get(URI.create(url)));
    }

    public Response post(String url, Object body) {
        return perform(MockMvcRequestBuilders.post(URI.create(url)).contentType(MediaType.APPLICATION_JSON).content(write(body)));
    }

    public Response postNoBody(String url) {
        return perform(MockMvcRequestBuilders.post(URI.create(url)));
    }

    public Response put(String url, Object body) {
        return perform(MockMvcRequestBuilders.put(URI.create(url)).contentType(MediaType.APPLICATION_JSON).content(write(body)));
    }

    public Response delete(String url) {
        return perform(MockMvcRequestBuilders.delete(URI.create(url)));
    }

    public Response perform(MockHttpServletRequestBuilder request) {
        try {
            MvcResult result = mvc.perform(request.session(session)).andReturn();
            // login rotates the session id; keep following the same "browser" session
            if (result.getRequest().getSession(false) instanceof MockHttpSession current) {
                session = current;
            }
            return new Response(result, mapper);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String write(Object body) {
        try {
            return body instanceof String s ? s : mapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Response of the application's {@code R<T>} envelope. */
    public static class Response {
        private final MvcResult result;
        private final ObjectMapper mapper;

        Response(MvcResult result, ObjectMapper mapper) {
            this.result = result;
            this.mapper = mapper;
        }

        public int status() {
            return result.getResponse().getStatus();
        }

        public byte[] bytes() {
            return result.getResponse().getContentAsByteArray();
        }

        public String contentType() {
            return result.getResponse().getContentType();
        }

        public JsonNode json() {
            try {
                return mapper.readTree(result.getResponse().getContentAsString());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        public JsonNode data() {
            return json().path("data");
        }

        public boolean ok() {
            return json().path("code").asInt() == 1;
        }

        public String message() {
            return json().path("msg").asText();
        }

        public Response assertSuccess() {
            assertThat(ok()).as("expected success but got: %s", json()).isTrue();
            return this;
        }

        public Response assertFailure(String messageContains) {
            assertThat(ok()).as("expected failure but got: %s", json()).isFalse();
            assertThat(message()).contains(messageContains);
            return this;
        }

        public Response assertNotLoggedIn() {
            return assertFailure("NOTLOGIN");
        }
    }
}
