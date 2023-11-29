/*******************************************************************************
 * Copyright (c) 2008, 2017 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.markers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.TestExpression;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 *
 * @since 3.5
 * @author Prakash G.R.
 */
@RunWith(JUnit4.class)
public class MarkerTesterTest extends UITestCase {

	private static final String MARKER_NAMESPACE = "org.eclipse.ui.ide.marker";
	private IProject project;

	public MarkerTesterTest() {
		super(MarkerTesterTest.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		project = ResourcesPlugin.getWorkspace().getRoot().getProject("tests");
		if (!project.exists()) {
			project.create(null);
		}

		if (!project.isOpen()) {
			project.open(null);
		}
	}

	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();
		if (project.exists()) {
			project.delete(true, null);
		}
	}

	@Test
	public void testSeverity() throws Exception {

		IMarker errorMarker = project.createMarker(IMarker.PROBLEM);
		Map<String, Object> attributes = new HashMap<>();
		attributes.put(IMarker.SEVERITY, Integer.valueOf(IMarker.SEVERITY_ERROR));
		errorMarker.setAttributes(attributes);

		EvaluationContext context = new EvaluationContext(null, errorMarker);
		TestExpression testExpression = new TestExpression(MARKER_NAMESPACE,
				"severity", null, Integer.valueOf(IMarker.SEVERITY_ERROR));
		EvaluationResult result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

		IMarker warningMarker = project.createMarker(IMarker.PROBLEM);
		attributes = new HashMap<>();
		attributes.put(IMarker.SEVERITY, Integer.valueOf(IMarker.SEVERITY_WARNING));
		warningMarker.setAttributes(attributes);

		context = new EvaluationContext(null, warningMarker);
		testExpression = new TestExpression(MARKER_NAMESPACE, "severity", null,
				Integer.valueOf(IMarker.SEVERITY_WARNING));
		result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

	}

	@Test
	public void testType() throws Exception {

		IMarker problemMarker = project.createMarker(IMarker.PROBLEM);

		EvaluationContext context = new EvaluationContext(null, problemMarker);
		TestExpression testExpression = new TestExpression(MARKER_NAMESPACE,
				"type", null, IMarker.PROBLEM);
		EvaluationResult result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

		IMarker bookmarkMarker = project.createMarker(IMarker.BOOKMARK);

		context = new EvaluationContext(null, bookmarkMarker);
		testExpression = new TestExpression(MARKER_NAMESPACE, "type", null,
				IMarker.BOOKMARK);
		result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);
	}

	@Test
	public void testSuperType() throws Exception {

		IMarker problemMarker = project.createMarker(IMarker.PROBLEM);

		EvaluationContext context = new EvaluationContext(null, problemMarker);
		TestExpression testExpression = new TestExpression(MARKER_NAMESPACE,
				"superType", null, IMarker.MARKER);
		EvaluationResult result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

		testExpression = new TestExpression(MARKER_NAMESPACE, "superType",
				null, IMarker.BOOKMARK);
		result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.FALSE, result);
	}

	@Test
	public void testPriority() throws Exception {

		Map<String, Object> attributes = new HashMap<>();
		attributes.put(IMarker.PRIORITY, Integer.valueOf(IMarker.PRIORITY_HIGH));
		IMarker highPriority = project.createMarker(IMarker.PROBLEM, attributes);

		EvaluationContext context = new EvaluationContext(null, highPriority);
		TestExpression testExpression = new TestExpression(MARKER_NAMESPACE,
				"priority", null, Integer.valueOf(IMarker.PRIORITY_HIGH));
		EvaluationResult result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

		attributes = new HashMap<>();
		attributes.put(IMarker.PRIORITY, Integer.valueOf(IMarker.PRIORITY_LOW));
		IMarker lowPriority = project.createMarker(IMarker.PROBLEM, attributes);

		context = new EvaluationContext(null, lowPriority);
		testExpression = new TestExpression(MARKER_NAMESPACE, "priority", null,
				Integer.valueOf(IMarker.PRIORITY_LOW));
		result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);
	}

	@Test
	public void testDone() throws Exception {

		IMarker done = project.createMarker(IMarker.TASK);
		Map<String, Object> attributes = new HashMap<>();
		attributes.put(IMarker.DONE, Boolean.TRUE);
		done.setAttributes(attributes);

		EvaluationContext context = new EvaluationContext(null, done);
		TestExpression testExpression = new TestExpression(MARKER_NAMESPACE,
				"done", null, Boolean.TRUE);
		EvaluationResult result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

		IMarker notDone = project.createMarker(IMarker.TASK);
		attributes = new HashMap<>();
		attributes.put(IMarker.DONE, Boolean.FALSE);
		notDone.setAttributes(attributes);

		context = new EvaluationContext(null, notDone);
		testExpression = new TestExpression(MARKER_NAMESPACE, "done", null,
				Boolean.FALSE);
		result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

		IMarker notSet = project.createMarker(IMarker.TASK);

		context = new EvaluationContext(null, notSet);
		testExpression = new TestExpression(MARKER_NAMESPACE, "done", null,
				Boolean.TRUE);
		result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.FALSE, result);
	}

	@Test
	public void testMessage() throws Exception {

		Map<String, String> attributes = new HashMap<>();
		attributes.put(IMarker.MESSAGE, "Some nice message to test");
		IMarker someTaskMarker = project.createMarker(IMarker.TASK, attributes);

		EvaluationContext context = new EvaluationContext(null, someTaskMarker);
		TestExpression testExpression = new TestExpression(MARKER_NAMESPACE,
				"message", null, "Some nice message to test");
		EvaluationResult result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

		testExpression = new TestExpression(MARKER_NAMESPACE, "message", null,
				"Some*");
		result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

		testExpression = new TestExpression(MARKER_NAMESPACE, "message", null,
				"*test");
		result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

		testExpression = new TestExpression(MARKER_NAMESPACE, "message", null,
				"*nice*");
		result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

		testExpression = new TestExpression(MARKER_NAMESPACE, "message", null,
				"*noway*");
		result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.FALSE, result);

	}

	@Test
	public void testResourceType() throws Exception {

		Map<String, String> attributes = new HashMap<>();
		attributes.put(IMarker.MESSAGE, "Some nice message to test");
		IMarker someTaskMarker = project.createMarker(IMarker.TASK, attributes);

		EvaluationContext context = new EvaluationContext(null, someTaskMarker);
		TestExpression testExpression = new TestExpression(MARKER_NAMESPACE,
				"resourceType", null, Integer.valueOf(IResource.PROJECT));
		EvaluationResult result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

		IFolder folder = project.getFolder("forMarker");
		folder.create(true, true, null);

		attributes = new HashMap<>();
		attributes.put(IMarker.MESSAGE, "Some nice message to test");
		IMarker someOtherMarker = folder.createMarker(IMarker.TASK, attributes);

		context = new EvaluationContext(null, someOtherMarker);
		testExpression = new TestExpression(MARKER_NAMESPACE, "resourceType",
				null, Integer.valueOf(IResource.FOLDER));
		result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

	}
}
