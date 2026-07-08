package com.homelens.handler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.io.IOException;
import java.sql.*;

public class JsonNodeTypeHandler extends BaseTypeHandler<JsonNode> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(
            PreparedStatement ps, int i, JsonNode parameter, JdbcType jdbcType
    ) throws SQLException {
        ps.setString(i, parameter.toString()); // INSERT/UPDATE 시
    }

    @Override
    public JsonNode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        if (json == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (IOException e) {
            throw new SQLException("Failed to parse JSON column '" + columnName + "'", e);
        }
    }

    @Override
    public JsonNode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        if (json == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (IOException e) {
            throw new SQLException("Failed to parse JSON column at index " + columnIndex, e);
        }
    }

    @Override
    public JsonNode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        if (json == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (IOException e) {
            throw new SQLException("Failed to parse JSON column from callable statement", e);
        }
    }
}
