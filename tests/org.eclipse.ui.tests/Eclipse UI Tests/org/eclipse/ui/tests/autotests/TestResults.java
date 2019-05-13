/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.autotests;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.IMemento;

/**
 * @since 3.1
 */
public class TestResults {
	private Map<String, TestResultFilter> results = new HashMap<>();
	private static final String ATT_NAME = "name";
	private static final String ATT_TEST = "test";

	public TestResults() {
	}

	public TestResults(IMemento toLoad) {
		IMemento[] tests = toLoad.getChildren(ATT_TEST);

		for (IMemento memento : tests) {
			String name = memento.getString(ATT_NAME);
			if (name == null) {
				continue;
			}

			results.put(name, new TestResultFilter(memento));
		}
	}

	public String[] getTestNames() {
		Collection<String> ids = results.keySet();

		return ids.toArray(new String[ids.size()]);
	}

	public TestResultFilter get(String testName) {
		return results.get(testName);
	}

	public void put(String testName, TestResultFilter filter) {
		results.put(testName, filter);
	}

	public boolean isEmpty() {
		return results.isEmpty();
	}

	public void saveState(IMemento memento) {
		for (Object element : results.keySet()) {
			String testName = (String) element;

			TestResultFilter next = get(testName);

			IMemento child = memento.createChild(ATT_TEST);
			child.putString(ATT_NAME, testName);
			next.saveState(child);
		}
	}
}
