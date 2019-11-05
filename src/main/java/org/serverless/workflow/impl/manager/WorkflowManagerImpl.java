/*
 *
 *   Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.serverless.workflow.impl.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.serverless.workflow.api.Workflow;
import org.serverless.workflow.api.WorkflowManager;
import org.serverless.workflow.api.WorkflowValidator;
import org.serverless.workflow.api.interfaces.Extension;
import org.serverless.workflow.api.mapper.JsonObjectMapper;
import org.serverless.workflow.api.mapper.YamlObjectMapper;
import org.serverless.workflow.impl.validator.WorkflowValidatorImpl;
import org.serverless.workflow.spi.WorkflowPropertySourceProvider;
import org.serverless.workflow.spi.WorkflowValidatorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowManagerImpl implements WorkflowManager {

    private Workflow workflow;
    private WorkflowValidator workflowValidator;
    private WorkflowValidator defaultWorkflowValidator = new WorkflowValidatorImpl();
    private JsonObjectMapper jsonObjectMapper = new JsonObjectMapper();
    private YamlObjectMapper yamlObjectMapper = new YamlObjectMapper();

    private static Logger logger = LoggerFactory.getLogger(WorkflowManagerImpl.class);

    public WorkflowManagerImpl() {
        workflowValidator = WorkflowValidatorProvider.getInstance().get();

        if (workflowValidator == null) {
            throw new RuntimeException("Unable to retrieve workflow validator");
        }

        workflowValidator.setWorkflowManager(this);

        try {
            jsonObjectMapper = new JsonObjectMapper(WorkflowPropertySourceProvider.getInstance().get());
            yamlObjectMapper = new YamlObjectMapper(WorkflowPropertySourceProvider.getInstance().get());
        } catch (Exception e) {
            logger.warn("Unable to load application.properties");
        }
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
    public WorkflowManager setMarkup(String workflowMarkup) {
        this.workflow = toWorkflow(workflowMarkup);
        return this;
    }

    @Override
    public WorkflowValidator getWorkflowValidator() {
        return workflowValidator == null ? defaultWorkflowValidator : workflowValidator;
    }

    @Override
    public String toJson() {
        try {
            return jsonObjectMapper.writeValueAsString(workflow);
        } catch (JsonProcessingException e) {
            logger.error("Error mapping to json: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String toYaml() {
        try {
            String jsonString = jsonObjectMapper.writeValueAsString(workflow);
            JsonNode jsonNode = jsonObjectMapper.readTree(jsonString);
            YAMLFactory yamlFactory = new YAMLFactory()
                .disable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
            return new YAMLMapper(yamlFactory).writeValueAsString(jsonNode);
        } catch (Exception e) {
            logger.error("Error mapping to yaml: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Workflow toWorkflow(String markup) {
        // try it as json markup first, if fails try yaml
        try {
            return jsonObjectMapper.readValue(markup,
                                              Workflow.class);
        } catch (Exception e) {
            try {
                return yamlObjectMapper.readValue(markup,
                                                  Workflow.class);
            } catch (Exception ee) {
                throw new IllegalArgumentException("Could not convert markup to Workflow: " + ee.getMessage());
            }
        }
    }

    @Override
    public void registerExtension(String extensionId,
                                  Class<? extends Extension> extensionClass) {
        jsonObjectMapper.getWorkflowModule().getExtensionSerializer().addExtension(extensionId,
                                                                                   extensionClass);
        jsonObjectMapper.getWorkflowModule().getExtensionDeserializer().addExtension(extensionId,
                                                                                     extensionClass);

        yamlObjectMapper.getWorkflowModule().getExtensionSerializer().addExtension(extensionId,
                                                                                   extensionClass);
        yamlObjectMapper.getWorkflowModule().getExtensionDeserializer().addExtension(extensionId,
                                                                                     extensionClass);
    }
}
