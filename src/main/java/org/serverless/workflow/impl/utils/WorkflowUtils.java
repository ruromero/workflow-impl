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

package org.serverless.workflow.impl.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.serverless.workflow.api.WorkflowManager;
import org.serverless.workflow.api.actions.Action;
import org.serverless.workflow.api.events.Event;
import org.serverless.workflow.api.events.EventTrigger;
import org.serverless.workflow.api.functions.Function;
import org.serverless.workflow.api.interfaces.State;
import org.serverless.workflow.api.states.DefaultState;
import org.serverless.workflow.api.states.EventState;

public class WorkflowUtils {

    public static boolean hasEventTriggers(WorkflowManager workflowManager) {
        return workflowManager.getWorkflow().getEventTriggers() != null && !workflowManager.getWorkflow().getEventTriggers().isEmpty();
    }

    public static Map<String, EventTrigger> getEventTriggers(WorkflowManager workflowManager) {
        if (workflowManager.getWorkflow().getEventTriggers() != null) {
            return workflowManager.getWorkflow().getEventTriggers().stream()
                .collect(Collectors.toMap(EventTrigger::getName,
                                          trigger -> trigger));
        }

        return null;
    }

    public static boolean haveStates(WorkflowManager workflowManager) {
        return workflowManager.getWorkflow().getStates() != null && !workflowManager.getWorkflow().getStates().isEmpty();
    }

    public static Map<String, State> getUniqueStates(WorkflowManager workflowManager) {
        if (workflowManager.getWorkflow().getEventTriggers() != null) {
            return workflowManager.getWorkflow().getStates().stream()
                .collect(Collectors.toMap(State::getName,
                                          state -> state));
        }

        return null;
    }

    public static List<Event> getEventsByTrigger(EventTrigger trigger, WorkflowManager workflowManager) {
        return workflowManager.getWorkflow().getStates()
            .stream()
            .filter(state -> DefaultState.Type.EVENT.equals(state.getType()))
            .map(state -> ((EventState) state).getEvents())
            .flatMap(List::stream)
            .filter(e -> e.getEvent().equalsIgnoreCase(trigger.getName()))
            .collect(Collectors.toList());
    }

    public static List<EventTrigger> getTriggersByState(EventState eventState, WorkflowManager workflowManager) {
        return workflowManager.getWorkflow().getEventTriggers()
            .stream()
            .filter(trigger -> eventState.getEvents()
                .stream()
                .anyMatch(e -> e.getEvent().equalsIgnoreCase(trigger.getName())))
            .collect(Collectors.toList());
    }

    public static List<Function> getFunctions(List<Action> actions) {
        List<Function> functions = new ArrayList<>();
        actions.forEach(action -> functions.add(action.getFunction()));
        return functions;
    }

    public static State getStartState(WorkflowManager workflowManager) {
        return workflowManager.getWorkflow().getStates()
            .stream()
            .filter(s -> s.getName().equals(workflowManager.getWorkflow().getStartAt()))
            .findFirst()
            .orElse(null);
    }

    public static State getStateByName(String stateName,
                                       WorkflowManager workflowManager) {
        return workflowManager.getWorkflow().getStates()
            .stream()
            .filter(state -> state.getName().equals(stateName))
            .findFirst()
            .orElse(null);
    }

    public static boolean hasEndState(WorkflowManager workflowManager) {
        return workflowManager.getWorkflow().getStates()
            .stream()
            .anyMatch(state -> state.isEnd());
    }
}
