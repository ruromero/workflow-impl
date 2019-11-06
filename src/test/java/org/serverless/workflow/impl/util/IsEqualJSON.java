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

package org.serverless.workflow.impl.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

public class IsEqualJSON extends DiagnosingMatcher<Object> {

    private final String expectedJSON;
    private JSONCompareMode jsonCompareMode;

    public IsEqualJSON(final String expectedJSON) {
        this.expectedJSON = expectedJSON;
        this.jsonCompareMode = JSONCompareMode.STRICT;
    }

    public void describeTo(final Description description) {
        description.appendText(expectedJSON);
    }

    @Override
    protected boolean matches(final Object actual,
                              final Description mismatchDescription) {
        final String actualJSON = toJSONString(actual);
        try {
            final JSONCompareResult result = JSONCompare.compareJSON(expectedJSON,
                                                                     actualJSON,
                                                                     jsonCompareMode);
            if (!result.passed()) {
                mismatchDescription.appendText(result.getMessage());
            }
            return result.passed();
        } catch (Exception e) {
            return false;
        }
    }

    private static String toJSONString(final Object o) {
        try {
            return o instanceof String ? (String) o : new ObjectMapper().writeValueAsString(o);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getFileContents(final Path path) {
        try {
            return new String(Files.readAllBytes(path),
                              StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static IsEqualJSON equalToJSON(final String expectedJSON) {
        return new IsEqualJSON(expectedJSON);
    }

    public static IsEqualJSON equalToJSONInFile(final Path expectedPath) {
        return equalToJSON(getFileContents(expectedPath));
    }

    public static IsEqualJSON equalToJSONInFile(final String expectedFileName) {
        return equalToJSONInFile(Paths.get(expectedFileName));
    }
}