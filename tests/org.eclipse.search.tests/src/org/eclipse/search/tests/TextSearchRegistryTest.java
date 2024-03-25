/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.search.tests;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.search.internal.core.text.TextSearchEngineRegistry;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.junit.Test;

public class TextSearchRegistryTest {

	@Test
	public void testRegistry() {
		TextSearchEngineRegistry textSearchEngineRegistry= SearchPlugin.getDefault().getTextSearchEngineRegistry();
		assertTrue(Arrays.stream(textSearchEngineRegistry.getAvailableEngines()).map(array -> array[1]).anyMatch("org.eclipse.search.tests.testSearchEngine"::equals));
	}
}
