package com.example.ruleengine.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 自定义反序列化器：将数组或对象转换为JSON字符串
 * 用于处理前端发送数组但后端字段为String类型的情况
 */
public class ArrayToJsonStringDeserializer extends JsonDeserializer<String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken currentToken = p.getCurrentToken();

        if (currentToken == JsonToken.START_ARRAY || currentToken == JsonToken.START_OBJECT) {
            // 如果是数组或对象，读取整个结构并转换为JSON字符串
            Object value = p.readValueAs(Object.class);
            return objectMapper.writeValueAsString(value);
        } else if (currentToken == JsonToken.VALUE_STRING) {
            // 如果已经是字符串，直接返回
            return p.getText();
        } else if (currentToken == JsonToken.VALUE_NUMBER_INT || currentToken == JsonToken.VALUE_NUMBER_FLOAT) {
            // 如果是数字，转换为字符串
            return p.getText();
        } else if (currentToken == JsonToken.VALUE_TRUE || currentToken == JsonToken.VALUE_FALSE) {
            // 如果是布尔值，转换为字符串
            return p.getText();
        } else if (currentToken == JsonToken.VALUE_NULL) {
            // 如果是null，返回null
            return null;
        }

        return p.getText();
    }
}
