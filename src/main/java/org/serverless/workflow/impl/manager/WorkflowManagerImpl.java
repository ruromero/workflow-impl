package org.serverless.workflow.impl.manager;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.serverless.workflow.api.ExpressionEvaluator;
import org.serverless.workflow.api.Workflow;
import org.serverless.workflow.api.WorkflowManager;
import org.serverless.workflow.api.WorkflowValidator;
import org.serverless.workflow.api.mapper.WorkflowObjectMapper;
import org.serverless.workflow.impl.expression.JexlExpressionEvaluatorImpl;
import org.serverless.workflow.impl.validator.WorkflowValidatorImpl;
import org.serverless.workflow.spi.ExpressionEvaluatorProvider;
import org.serverless.workflow.spi.WorkflowValidatorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowManagerImpl implements WorkflowManager {

    private Workflow workflow;
    private String workflowJson;
    private Map<String, ExpressionEvaluator> expressionEvaluators;
    private ExpressionEvaluator defaultExpressionEvaluator = new JexlExpressionEvaluatorImpl();
    private WorkflowValidator workflowValidator;
    private WorkflowValidator defaultWorkflowValidator = new WorkflowValidatorImpl();
    private WorkflowObjectMapper objectMapper = new WorkflowObjectMapper();

    private static Logger logger = LoggerFactory.getLogger(WorkflowManagerImpl.class);

    public WorkflowManagerImpl() {
        expressionEvaluators = ExpressionEvaluatorProvider.getInstance().get();
        workflowValidator = WorkflowValidatorProvider.getInstance().get();
    }

    @Override
    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    @Override
    public Workflow getWorkflow() {
        return workflow;
    }

    @Override
    public void setJson(String workflowJson) {
        this.workflowJson = workflowJson;
        this.workflow = toWorkflow(workflowJson);
    }

    @Override
    public WorkflowValidator getWorkflowValidator() {
        return workflowValidator == null ? defaultWorkflowValidator : workflowValidator;
    }

    @Override
    public void setExpressionEvaluator(ExpressionEvaluator expressionEvaluator) {
        this.defaultExpressionEvaluator = expressionEvaluator;
    }

    @Override
    public ExpressionEvaluator getExpressionEvaluator() {
        return defaultExpressionEvaluator;
    }

    @Override
    public ExpressionEvaluator getExpressionEvaluator(String evaluatorName) {
        if (expressionEvaluators.containsKey(evaluatorName)) {
            return expressionEvaluators.get(evaluatorName);
        }

        return defaultExpressionEvaluator;
    }

    @Override
    public void setDefaultExpressionEvaluator(String evaluatorName) {
        if (expressionEvaluators.containsKey(evaluatorName)) {
            defaultExpressionEvaluator = expressionEvaluators.get(evaluatorName);
        }
    }

    @Override
    public JsonNode toJson() {
        try {
            return objectMapper.readTree(workflowJson);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toJsonString() {
        try {
            return objectMapper.writeValueAsString(workflow);
        } catch (JsonProcessingException e) {
            logger.error("Error mapping to json: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Workflow toWorkflow(String json) {
        try {
            return objectMapper.readValue(json,
                                          Workflow.class);
        } catch (Exception e) {
            logger.error("Error converting to workflow: " + e.getMessage());

            throw new IllegalArgumentException(e.getMessage());

        }
    }
}
