package io.spring.isomorphic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class MockDataService {
    protected final ApplicationContext applicationContext;

    @Autowired
    public MockDataService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    protected JsonNode getMockJson(String resourceName) {
        try {
            try (InputStream stream = applicationContext.getResource("classpath:" + resourceName + ".json").getInputStream()) {
                return new ObjectMapper().readValue(stream, new TypeReference<JsonNode>() { });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
