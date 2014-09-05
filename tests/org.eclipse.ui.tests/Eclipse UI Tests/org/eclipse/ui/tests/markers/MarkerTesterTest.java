/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.markers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.internal.expressions.TestExpression;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * 
 * @since 3.5
 * @author Prakash G.R.
 * 
 */
public class MarkerTesterTest extends UITestCase {

	private static final String MARKER_NAMESPACE = "org.eclipse.ui.ide.marker";
	private IProject project;

	public MarkerTesterTest(String testName) {
		super(testName);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		project = ResourcesPlugin.getWorkspace().getRoot().getProject("tests");
		if (!project.exists())
			project.create(null);

		if (!project.isOpen())
			project.open(null);
	}

	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();
		if (project.exists())
			project.delete(true, null);
	}

	public void testSeverity() throws Exception {

		IMarker errorMarker = project.createMarker(IMarker.PROBLEM);
		Map attributes = new HashMap();
		attributes.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_ERROR));
		errorMarker.setAttributes(attributes);

		EvaluationContext context = new EvaluationContext(null, errorMarker);
		TestExpression testExpression = new TestExpression(MARKER_NAMESPACE,
				"severity", null, new Integer(IMarker.SEVERITY_ERROR));
		EvaluationResult result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

		IMarker warningMarker = project.createMarker(IMarker.PROBLEM);
		attributes = new HashMap();
		attributes.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_WARNING));
		warningMarker.setAttributes(attributes);

		context = new EvaluationContext(null, warningMarker);
		testExpression = new TestExpression(MARKER_NAMESPACE, "severity", null,
				new Integer(IMarker.SEVERITY_WARNING));
		result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

	}

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

	public void testPriority() throws Exception {

		IMarker highPriority = project.createMarker(IMarker.PROBLEM);
		Map attributes = new HashMap();
		attributes.put(IMarker.PRIORITY, new Integer(IMarker.PRIORITY_HIGH));
		highPriority.setAttributes(attributes);

		EvaluationContext context = new EvaluationContext(null, highPriority);
		TestExpression testExpression = new TestExpression(MARKER_NAMESPACE,
				"priority", null, new Integer(IMarker.PRIORITY_HIGH));
		EvaluationResult result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

		IMarker lowPriority = project.createMarker(IMarker.PROBLEM);
		attributes = new HashMap();
		attributes.put(IMarker.PRIORITY, new Integer(IMarker.PRIORITY_LOW));
		lowPriority.setAttributes(attributes);

		context = new EvaluationContext(null, lowPriority);
		testExpression = new TestExpression(MARKER_NAMESPACE, "priority", null,
				new Integer(IMarker.PRIORITY_LOW));
		result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);
	}

	public void testDone() throws Exception {

		IMarker done = project.createMarker(IMarker.TASK);
		Map attributes = new HashMap();
		attributes.put(IMarker.DONE, Boolean.TRUE);
		done.setAttributes(attributes);

		EvaluationContext context = new EvaluationContext(null, done);
		TestExpression testExpression = new TestExpression(MARKER_NAMESPACE,
				"done", null, Boolean.TRUE);
		EvaluationResult result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

		IMarker notDone = project.createMarker(IMarker.TASK);
		attributes = new HashMap();
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

	public void testMessage() throws Exception {

		IMarker someTaskMarker = project.createMarker(IMarker.TASK);
		Map attributes = new HashMap();
		attributes.put(IMarker.MESSAGE, "Some nice message to test");
		someTaskMarker.setAttributes(attributes);

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

	public void testResourceType() throws Exception {

		IMarker someTaskMarker = project.createMarker(IMarker.TASK);
		Map attributes = new HashMap();
		attributes.put(IMarker.MESSAGE, "Some nice message to test");
		someTaskMarker.setAttributes(attributes);

		EvaluationContext context = new EvaluationContext(null, someTaskMarker);
		TestExpression testExpression = new TestExpression(MARKER_NAMESPACE,
				"resourceType", null, new Integer(IResource.PROJECT));
		EvaluationResult result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

		IFolder folder = project.getFolder("forMarker");
		folder.create(true, true, null);

		IMarker someOtherMarker = folder.createMarker(IMarker.TASK);
		attributes = new HashMap();
		attributes.put(IMarker.MESSAGE, "Some nice message to test");
		someOtherMarker.setAttributes(attributes);

		context = new EvaluationContext(null, someOtherMarker);
		testExpression = new TestExpression(MARKER_NAMESPACE, "resourceType",
				null, new Integer(IResource.FOLDER));
		result = testExpression.evaluate(context);
		assertEquals(EvaluationResult.TRUE, result);

	}
}
