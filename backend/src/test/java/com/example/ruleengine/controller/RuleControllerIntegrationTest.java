package com.example.ruleengine.controller;

import com.example.ruleengine.entity.RuleDefinition;
import com.example.ruleengine.service.RuleDefinitionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 规则管理 API 控制器集成测试
 * <p>
 * 测试规则管理相关的 RESTful API 接口，包括：
 * <ul>
 *   <li>规则的 CRUD 接口</li>
 *   <li>规则执行接口</li>
 *   <li>规则导入导出接口</li>
 *   <li>批量操作接口</li>
 * </ul>
 * </p>
 *
 * @author 开发团队
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class RuleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RuleDefinitionService ruleDefinitionService;

    private static final String BASE_URL = "/api/rules";
    private static final String SCENE_CODE = "INFECTIOUS_DISEASE";

    // ==================== 规则列表查询测试 ====================

    @Test
    @DisplayName("测试获取规则列表 - 成功")
    void testGetRuleList() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("page", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("测试获取规则列表 - 按场景筛选")
    void testGetRuleListByScene() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("sceneCode", SCENE_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取规则列表 - 按名称模糊查询")
    void testGetRuleListByName() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("ruleName", "HIV")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== 规则创建测试 ====================

    @Test
    @DisplayName("测试创建规则 - 成功")
    void testCreateRule() throws Exception {
        Map<String, Object> ruleData = createRuleRequestData("API_TEST_CREATE_001", "API创建测试规则");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ruleData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("测试创建规则 - 规则编码重复")
    void testCreateRuleDuplicateCode() throws Exception {
        // 先创建一个规则
        Map<String, Object> ruleData = createRuleRequestData("API_TEST_DUP_001", "重复编码测试");
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ruleData)));

        // 再次创建相同编码的规则
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ruleData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("已存在")));
    }

    @Test
    @DisplayName("测试创建规则 - 缺少必填字段")
    void testCreateRuleMissingFields() throws Exception {
        Map<String, Object> ruleData = new HashMap<>();
        ruleData.put("ruleCode", "API_TEST_MISSING");
        // 缺少 ruleName, sceneCode 等必填字段

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ruleData)))
                .andExpect(status().isBadRequest());
    }

    // ==================== 规则详情查询测试 ====================

    @Test
    @DisplayName("测试获取规则详情 - 成功")
    void testGetRuleDetail() throws Exception {
        // 先创建规则
        Map<String, Object> ruleData = createRuleRequestData("API_TEST_DETAIL_001", "详情查询测试");
        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ruleData)))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        Long ruleId = ((Number) response.get("data")).longValue();

        // 查询详情
        mockMvc.perform(get(BASE_URL + "/" + ruleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.ruleCode").value("API_TEST_DETAIL_001"))
                .andExpect(jsonPath("$.data.ruleName").value("详情查询测试"));
    }

    @Test
    @DisplayName("测试获取规则详情 - 规则不存在")
    void testGetRuleDetailNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/99999999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    // ==================== 规则更新测试 ====================

    @Test
    @DisplayName("测试更新规则 - 成功")
    void testUpdateRule() throws Exception {
        // 先创建规则
        Map<String, Object> createData = createRuleRequestData("API_TEST_UPDATE_001", "更新测试原规则");
        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createData)))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        Long ruleId = ((Number) response.get("data")).longValue();

        // 更新规则
        Map<String, Object> updateData = createRuleRequestData("API_TEST_UPDATE_001", "更新后的规则名称");
        updateData.put("id", ruleId);
        updateData.put("priority", 200);

        mockMvc.perform(put(BASE_URL + "/" + ruleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证更新结果
        mockMvc.perform(get(BASE_URL + "/" + ruleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.ruleName").value("更新后的规则名称"))
                .andExpect(jsonPath("$.data.priority").value(200));
    }

    // ==================== 规则删除测试 ====================

    @Test
    @DisplayName("测试删除规则 - 成功")
    void testDeleteRule() throws Exception {
        // 先创建规则
        Map<String, Object> ruleData = createRuleRequestData("API_TEST_DELETE_001", "删除测试规则");
        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ruleData)))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        Long ruleId = ((Number) response.get("data")).longValue();

        // 删除规则
        mockMvc.perform(delete(BASE_URL + "/" + ruleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证已删除
        mockMvc.perform(get(BASE_URL + "/" + ruleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(404));
    }

    // ==================== 规则复制测试 ====================

    @Test
    @DisplayName("测试复制规则 - 成功")
    void testCopyRule() throws Exception {
        // 先创建规则
        Map<String, Object> ruleData = createRuleRequestData("API_TEST_COPY_001", "复制测试规则");
        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ruleData)))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        Long ruleId = ((Number) response.get("data")).longValue();

        // 复制规则
        mockMvc.perform(post(BASE_URL + "/" + ruleId + "/copy")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNumber());
    }

    // ==================== 规则状态更新测试 ====================

    @Test
    @DisplayName("测试启用/禁用规则 - 成功")
    void testUpdateRuleStatus() throws Exception {
        // 先创建规则
        Map<String, Object> ruleData = createRuleRequestData("API_TEST_STATUS_001", "状态测试规则");
        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ruleData)))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        Long ruleId = ((Number) response.get("data")).longValue();

        // 禁用规则
        mockMvc.perform(put(BASE_URL + "/" + ruleId + "/status")
                        .param("status", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证状态
        mockMvc.perform(get(BASE_URL + "/" + ruleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.status").value(0));
    }

    // ==================== 批量操作测试 ====================

    @Test
    @DisplayName("测试批量删除规则")
    void testBatchDeleteRules() throws Exception {
        // 创建多个规则
        List<Long> ruleIds = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> ruleData = createRuleRequestData(
                    "API_TEST_BATCH_DEL_" + i, "批量删除测试" + i);
            MvcResult result = mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ruleData)))
                    .andReturn();
            String responseBody = result.getResponse().getContentAsString();
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
            ruleIds.add(((Number) response.get("data")).longValue());
        }

        // 批量删除
        mockMvc.perform(delete(BASE_URL + "/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ruleIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试批量更新状态")
    void testBatchUpdateStatus() throws Exception {
        // 创建多个规则
        List<Long> ruleIds = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> ruleData = createRuleRequestData(
                    "API_TEST_BATCH_STATUS_" + i, "批量状态测试" + i);
            MvcResult result = mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ruleData)))
                    .andReturn();
            String responseBody = result.getResponse().getContentAsString();
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
            ruleIds.add(((Number) response.get("data")).longValue());
        }

        // 批量禁用
        Map<String, Object> batchRequest = new HashMap<>();
        batchRequest.put("ruleIds", ruleIds);
        batchRequest.put("status", 0);

        mockMvc.perform(put(BASE_URL + "/batch/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== 规则导出测试 ====================

    @Test
    @DisplayName("测试导出规则")
    void testExportRules() throws Exception {
        mockMvc.perform(get(BASE_URL + "/export")
                        .param("sceneCode", SCENE_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== 规则导入测试 ====================

    @Test
    @DisplayName("测试导入规则 - 跳过策略")
    void testImportRulesWithSkip() throws Exception {
        Map<String, Object> importRequest = new HashMap<>();

        Map<String, Object> ruleData = new HashMap<>();
        ruleData.put("ruleCode", "API_TEST_IMPORT_SKIP");
        ruleData.put("ruleName", "导入测试规则");
        ruleData.put("sceneCode", SCENE_CODE);
        ruleData.put("ruleType", "CONDITION");
        ruleData.put("priority", 100);
        ruleData.put("status", 1);
        ruleData.put("conditions", Collections.emptyList());
        ruleData.put("actions", Collections.emptyList());

        importRequest.put("rules", Collections.singletonList(ruleData));
        importRequest.put("conflictStrategy", "SKIP");
        importRequest.put("enableAfterImport", false);

        mockMvc.perform(post(BASE_URL + "/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(importRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    // ==================== 辅助方法 ====================

    private Map<String, Object> createRuleRequestData(String ruleCode, String ruleName) {
        Map<String, Object> ruleData = new HashMap<>();
        ruleData.put("ruleCode", ruleCode);
        ruleData.put("ruleName", ruleName);
        ruleData.put("ruleDesc", "API测试规则描述");
        ruleData.put("sceneCode", SCENE_CODE);
        ruleData.put("ruleType", "CONDITION");
        ruleData.put("priority", 100);
        ruleData.put("status", 1);
        ruleData.put("effectiveStartTime", LocalDateTime.now().toString());
        ruleData.put("effectiveEndTime", LocalDateTime.now().plusYears(10).toString());

        // 添加条件
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("fieldCode", "lab_hiv_antibody");
        condition.put("operator", "EQ");
        condition.put("fieldValue", "阳性");
        condition.put("valueType", "STRING");
        condition.put("groupId", 1);
        condition.put("groupLogic", "AND");
        condition.put("sortOrder", 0);
        conditions.add(condition);
        ruleData.put("conditions", conditions);

        // 添加动作
        List<Map<String, Object>> actions = new ArrayList<>();
        Map<String, Object> action = new HashMap<>();
        action.put("actionType", "RETURN");
        action.put("actionParams", "{\"need_report\":true}");
        action.put("sortOrder", 0);
        actions.add(action);
        ruleData.put("actions", actions);

        return ruleData;
    }
}
