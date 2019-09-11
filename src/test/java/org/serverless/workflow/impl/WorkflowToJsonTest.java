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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.serverless.workflow.api.Workflow;
import org.serverless.workflow.api.WorkflowManager;
import org.serverless.workflow.api.actions.Action;
import org.serverless.workflow.api.actions.Retry;
import org.serverless.workflow.api.branches.Branch;
import org.serverless.workflow.api.choices.AndChoice;
import org.serverless.workflow.api.choices.DefaultChoice;
import org.serverless.workflow.api.events.Event;
import org.serverless.workflow.api.events.TriggerEvent;
import org.serverless.workflow.api.filters.Filter;
import org.serverless.workflow.api.functions.Function;
import org.serverless.workflow.api.interfaces.Choice;
import org.serverless.workflow.api.interfaces.State;
import org.serverless.workflow.api.states.DelayState;
import org.serverless.workflow.api.states.EndState;
import org.serverless.workflow.api.states.EndState.Status;
import org.serverless.workflow.api.states.EventState;
import org.serverless.workflow.api.states.OperationState;
import org.serverless.workflow.api.states.ParallelState;
import org.serverless.workflow.api.states.SwitchState;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.serverless.workflow.impl.util.IsEqualJSON.equalToJSONInFile;

public class WorkflowToJsonTest extends BaseWorkflowTest {

    @Test
    public void testEmptyWorkflow() {
        Workflow workflow = new Workflow().withName("test-wf");

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertThat(workflowManager.toJsonString(),
                   equalToJSONInFile(getResourcePathFor("basic/emptyworkflow.json")));
    }

    @Test
    public void testSimpleWorkflowWithMetadata() {
        Workflow workflow = new Workflow().withName("test-wf")
                .withMetadata(
                        Stream.of(new Object[][]{
                                {"key1", "value1"},
                                {"key2", "value2"},
                        }).collect(Collectors.toMap(data -> (String) data[0],
                                                    data -> (String) data[1]))
                );

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertNotNull(workflowManager.toJsonString());

        assertThat(workflowManager.toJsonString(),
                   equalToJSONInFile(getResourcePathFor("basic/workflowwithmetadata.json")));
    }

    @Test
    public void testTrigger() {
        Workflow workflow = new Workflow().withName("test-wf").withTriggerDefs(
                Arrays.asList(
                        new TriggerEvent().withName("test-trigger").withEventID("testeventid")
                                .withCorrelationToken("testcorrelationtoken").withSource("testsource")
                )
        );

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertNotNull(workflowManager.toJsonString());

        assertThat(workflowManager.toJsonString(),
                   equalToJSONInFile(getResourcePathFor("basic/singletriggerevent.json")));
    }

    @Test
    public void testEndState() {

        Workflow workflow = new Workflow().withName("test-wf").withStates(new ArrayList<State>() {{
            add(new EndState().withName("test-state").withStatus(Status.SUCCESS));
        }});

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertNotNull(workflowManager.toJsonString());

        assertThat(workflowManager.toJsonString(),
                   equalToJSONInFile(getResourcePathFor("basic/singleendstate.json")));
    }

    @Test
    public void testEventState() {
        Workflow workflow = new Workflow().withName("test-wf").withStates(new ArrayList<State>() {{
            add(new EventState().withName("test-state").withStart(true)
                        .withEvents(Arrays.asList(
                                new Event().withEventExpression("testEventExpression").withTimeout("testTimeout")
                                        .withActionMode(Event.ActionMode.SEQUENTIAL)
                                        .withNextState("testNextState")
                                        .withActions(Arrays.asList(
                                                new Action().withFunction(new Function().withName("testFunction"))
                                                        .withTimeout(5)
                                                        .withRetry(new Retry().withMatch("testMatch").withMaxRetry(10)
                                                                           .withRetryInterval(2)
                                                                           .withNextState("testNextRetryState"))
                                        ))
                        ))
            );
        }});

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertNotNull(workflowManager.toJsonString());

        assertThat(workflowManager.toJsonString(),
                   equalToJSONInFile(getResourcePathFor("basic/singleeventstate.json")));
    }

    @Test
    public void testDelayState() {
        Workflow workflow = new Workflow().withName("test-wf").withStates(new ArrayList<State>() {{
            add(new DelayState().withName("test-state").withStart(false).withNextState("testNextState").withTimeDelay(5));
        }});

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertNotNull(workflowManager.toJsonString());

        assertThat(workflowManager.toJsonString(),
                   equalToJSONInFile(getResourcePathFor("basic/singledelaystate.json")));
    }

    @Test
    public void testOperationState() {
        Workflow workflow = new Workflow().withName("test-wf").withStates(new ArrayList<State>() {{
            add(new OperationState().withName("test-state").withStart(true).withActionMode(OperationState.ActionMode.SEQUENTIAL).withNextState("testnextstate")
                        .withFilter(new Filter()
                                            .withInputPath("$.owner.address.zipcode")
                                            .withResultPath("$.country.code")
                                            .withOutputPath("$.owner.address.countryCode"))
                        .withActions(Arrays.asList(
                                new Action().withFunction(new Function().withName("testFunction"))
                                        .withTimeout(5)
                                        .withRetry(new Retry().withMatch("testMatch").withMaxRetry(10)
                                                           .withRetryInterval(2)
                                                           .withNextState("testNextRetryState"))
                        )));
        }});

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertNotNull(workflowManager.toJsonString());

        assertThat(workflowManager.toJsonString(),
                   equalToJSONInFile(getResourcePathFor("basic/singleoperationstate.json")));
    }

    @Test
    public void testParallellState() {
        Workflow workflow = new Workflow().withName("test-wf").withStates(new ArrayList<State>() {{
            add(new ParallelState().withName("test-state").withStart(true).withNextState("testnextstate")
                        .withBranches(Arrays.asList(
                                new Branch().withName("firsttestbranch").withStates(
                                        new ArrayList<State>() {{
                                            add(new OperationState().withName("operationstate").withStart(true).withActionMode(OperationState.ActionMode.SEQUENTIAL).withNextState("testnextstate")
                                                        .withActions(Arrays.asList(
                                                                new Action().withFunction(new Function().withName("testFunction"))
                                                                        .withTimeout(5)
                                                                        .withRetry(new Retry().withMatch("testMatch").withMaxRetry(10)
                                                                                           .withRetryInterval(2)
                                                                                           .withNextState("testNextRetryState"))
                                                        )));
                                        }}
                                ),
                                new Branch().withName("secondtestbranch").withStates(
                                        new ArrayList<State>() {{
                                            add(new DelayState().withName("delaystate").withStart(false).withNextState("testNextState").withTimeDelay(5));
                                        }}
                                )
                        )));
        }});

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertNotNull(workflowManager.toJsonString());

        assertThat(workflowManager.toJsonString(),
                   equalToJSONInFile(getResourcePathFor("basic/singleparallelstate.json")));
    }

    @Test
    public void testSwitchState() {
        Workflow workflow = new Workflow().withName("test-wf").withStates(new ArrayList<State>() {{
            add(
                    new SwitchState().withName("test-state").withDefault("defaultteststate").withStart(false).withChoices(
                            new ArrayList<Choice>() {{
                                add(
                                        new AndChoice().withNextState("testnextstate").withAnd(
                                                Arrays.asList(
                                                        new DefaultChoice().withNextState("testnextstate")
                                                                .withOperator(DefaultChoice.Operator.EQ)
                                                                .withPath("testpath")
                                                                .withValue("testvalue")
                                                )
                                        )
                                );
                            }}
                    )
            );
        }});

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertNotNull(workflowManager.toJsonString());

        assertThat(workflowManager.toJsonString(),
                   equalToJSONInFile(getResourcePathFor("basic/singleswitchstateandchoice.json")));
    }
}