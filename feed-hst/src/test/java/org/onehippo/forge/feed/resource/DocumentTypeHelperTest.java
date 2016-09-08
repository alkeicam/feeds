/*
 * Copyright 2013 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.feed.resource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DocumentTypeHelperTest {

    private Set<DocType> docTypes;

    @Before
    public void setUp() throws Exception {
        docTypes = new HashSet<>();
        docTypes.add(new DocType("a",new HashSet<String>(Arrays.asList("x","y","z"))));
        docTypes.add(new DocType("b",new HashSet<String>(Arrays.asList("u","v","w"))));

    }

    @Test
    public void process_happyflow() throws Exception {
        DocumentTypeHelper helper = new DocumentTypeHelper(
                docTypes, new HashSet<String>(Arrays.asList("x", "y")));
        helper.process();
        Set<String> actual = helper.getDocTypes();
        Set<String> expected = new HashSet<String>(Arrays.asList("z","b"));
        assertArrayEquals(expected.toArray(),actual.toArray());
    }

    @Test
    public void process_noIntersection() throws Exception {
        DocumentTypeHelper helper = new DocumentTypeHelper(
                docTypes, new HashSet<String>(Arrays.asList("d", "e")));
        helper.process();
        Set<String> actual = helper.getDocTypes();
        Set<String> expected = new HashSet<String>(Arrays.asList("a","b"));
        assertArrayEquals(expected.toArray(),actual.toArray());
    }

    @Test
    public void process_noExcludedTypes() throws Exception {
        DocumentTypeHelper helper = new DocumentTypeHelper(
                docTypes, new HashSet<String>());
        helper.process();
        Set<String> actual = helper.getDocTypes();
        Set<String> expected = new HashSet<String>(Arrays.asList("a","b"));
        assertArrayEquals(expected.toArray(),actual.toArray());
    }

    @Test
    public void process_bothEmpty() throws Exception {
        DocumentTypeHelper helper = new DocumentTypeHelper(
                new HashSet<DocType>(), new HashSet<String>());
        helper.process();
        Set<String> actual = helper.getDocTypes();
        Set<String> expected = new HashSet<String>();
        assertArrayEquals(expected.toArray(),actual.toArray());
    }

}