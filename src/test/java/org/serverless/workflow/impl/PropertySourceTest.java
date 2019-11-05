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

package org.serverless.workflow.impl;

import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.serverless.workflow.api.Workflow;
import org.serverless.workflow.api.WorkflowManager;
import org.serverless.workflow.api.WorkflowValidator;
import org.serverless.workflow.api.events.Event;
import org.serverless.workflow.api.events.EventTrigger;
import org.serverless.workflow.api.states.EventState;
import org.serverless.workflow.impl.utils.WorkflowUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.serverless.workflow.impl.util.IsEqualJSON.equalToJSONInFile;

public class PropertySourceTest extends BaseWorkflowTest {

    @Test
    public void testFromPropertySource() {
        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath("propertysource/propertysourceeventstatewithtrigger.json")));

        Workflow workflow = workflowManager.getWorkflow();
        assertNotNull(workflow);

        assertEquals("test-wf", workflow.getName());

        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertTrue(workflowValidator.isValid());

        assertNotNull(workflow.getStates());
        assertThat(workflow.getStates().size(),
                   is(1));
        assertTrue(workflow.getStates().get(0) instanceof EventState);

        EventState eventState = (EventState) workflow.getStates().get(0);
        assertEquals("test-state",
                     eventState.getName());
        assertEquals(EventState.Type.EVENT,
                     eventState.getType());

        assertNotNull(eventState.getEvents());
        assertEquals(1,
                     eventState.getEvents().size());

        Event event = eventState.getEvents().get(0);
        assertEquals("testNextState",
                     event.getNextState());
        assertEquals("test-event",
                     event.getEvent());
        assertEquals("timeoutPeriod",
                     event.getTimeout().getPeriod());
        assertEquals("timeoutState",
                     event.getTimeout().getThen());

        assertNotNull(workflow.getEventTriggers());
        assertEquals(1,
                     workflow.getEventTriggers().size());

        assertEquals("test-event",
                     workflow.getEventTriggers().get(0).getName());
        assertEquals("testsource",
                     workflow.getEventTriggers().get(0).getSource());
        assertEquals("testeventtype",
                     workflow.getEventTriggers().get(0).getType());
        assertEquals("testcorrelationtoken",
                     workflow.getEventTriggers().get(0).getCorrelationToken());

        assertTrue(WorkflowUtils.hasEventTriggers(workflowManager));

        assertTrue(WorkflowUtils.haveStates(workflowManager));

        assertEquals(1, WorkflowUtils.getUniqueStates(workflowManager).size());
        assertEquals(1, WorkflowUtils.getEventTriggers(workflowManager).size());

        EventTrigger EventTrigger = WorkflowUtils.getEventTriggers(workflowManager).get("test-event");
        assertNotNull(EventTrigger);

        List<Event> eventStatesForTrigger = WorkflowUtils.getEventsByTrigger(EventTrigger, workflowManager);
        assertNotNull(eventStatesForTrigger);
        assertEquals(1, eventStatesForTrigger.size());
        Event eventForTrigger = eventStatesForTrigger.get(0);
        assertEquals("test-event", eventForTrigger.getEvent());

        assertThat(workflowManager.toJson(),
                   equalToJSONInFile(getResourcePathFor("propertysource/propertysourceeventstatewithtriggervaluesresolved.json")));
    }

    @Test
    public void testYamlFromPropertySource() {
        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath("propertysource/propertysourceeventstatewithtrigger.yml")));

        Workflow workflow = workflowManager.getWorkflow();
        assertNotNull(workflow);

        assertEquals("test-wf",
                     workflow.getName());

        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertTrue(workflowValidator.isValid());

        assertNotNull(workflow.getStates());
        assertThat(workflow.getStates().size(),
                   is(1));
        assertTrue(workflow.getStates().get(0) instanceof EventState);

        EventState eventState = (EventState) workflow.getStates().get(0);
        assertEquals("test-state",
                     eventState.getName());
        assertEquals(EventState.Type.EVENT,
                     eventState.getType());

        assertNotNull(eventState.getEvents());
        assertEquals(1,
                     eventState.getEvents().size());

        Event event = eventState.getEvents().get(0);
        assertEquals("testNextState",
                     event.getNextState());
        assertEquals("test-event",
                     event.getEvent());
        assertEquals("timeoutPeriod",
                     event.getTimeout().getPeriod());
        assertEquals("timeoutState",
                     event.getTimeout().getThen());

        assertNotNull(workflow.getEventTriggers());
        assertEquals(1,
                     workflow.getEventTriggers().size());

        assertEquals("test-event",
                     workflow.getEventTriggers().get(0).getName());
        assertEquals("testsource",
                     workflow.getEventTriggers().get(0).getSource());
        assertEquals("testeventtype",
                     workflow.getEventTriggers().get(0).getType());
        assertEquals("testcorrelationtoken",
                     workflow.getEventTriggers().get(0).getCorrelationToken());

        assertTrue(WorkflowUtils.hasEventTriggers(workflowManager));

        assertTrue(WorkflowUtils.haveStates(workflowManager));

        assertEquals(1,
                     WorkflowUtils.getUniqueStates(workflowManager).size());
        assertEquals(1,
                     WorkflowUtils.getEventTriggers(workflowManager).size());

        EventTrigger EventTrigger = WorkflowUtils.getEventTriggers(workflowManager).get("test-event");
        assertNotNull(EventTrigger);

        List<Event> eventStatesForTrigger = WorkflowUtils.getEventsByTrigger(EventTrigger, workflowManager);
        assertNotNull(eventStatesForTrigger);
        assertEquals(1,
                     eventStatesForTrigger.size());
        Event eventForTrigger = eventStatesForTrigger.get(0);
        assertEquals("test-event",
                     eventForTrigger.getEvent());

        assertEquals(workflowManager.toYaml(),
                     getFileContents(Paths.get(getResourcePathFor("propertysource/propertysourceeventstatewithtriggervaluesresolved.yml"))));
    }
}
