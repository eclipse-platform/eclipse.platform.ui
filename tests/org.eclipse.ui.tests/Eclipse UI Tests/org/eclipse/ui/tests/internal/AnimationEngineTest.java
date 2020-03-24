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

package org.eclipse.ui.tests.internal;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.PreferenceMemento;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.internal.AnimationEngine;
import org.eclipse.ui.internal.AnimationFeedbackBase;
import org.eclipse.ui.internal.util.PrefUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.5
 *
 */
public class AnimationEngineTest {

	private class TestFeedback extends AnimationFeedbackBase {
		/**
		 * @param parentShell
		 */
		public TestFeedback(Shell parentShell) {
			super(parentShell);
		}

		public int count = 0;
		public int initCalled = -1;
		public int renderCalled = -1;
		public int disposeCalled = -1;

		@Override
		public void dispose() {
			disposeCalled = count++;
		}

		@Override
		public void initialize(AnimationEngine animationEngine) {
			initCalled = count++;
		}

		@Override
		public void renderStep(AnimationEngine engine) {
			if(renderCalled == -1) {
				renderCalled = count++;
			}
		}

	}

	Shell shell;
	TestFeedback feedback;
	AnimationEngine engine;
	private PreferenceMemento memento;

	@Before
	public void doSetUp() {
		shell = new Shell(Display.getCurrent());
		memento = new PreferenceMemento();
	}

	@After
	public void doTearDown() {
		memento.resetPreferences();
		shell.dispose();
		shell = null;
	}

	/**
	 * Ensure that the protocol expected by the animation implementations works
	 * as defined:
	 * <ol>
	 * <li>The feedback's initialize gets called on creation of the engine</li>
	 * <li>The feedback's renderStep gets called at least once</li>
	 * <li>The feedback's dispose gets called at least once (after a render)</li>
	 * </ol>
	 * @throws InterruptedException
	 */
	@Test
	public void testAnimationEngine() throws InterruptedException {
		// Ensure that animations are turned on
		memento.setValue(PrefUtil.getAPIPreferenceStore(), IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS,
				true);

		feedback = new TestFeedback(shell);
		engine = new AnimationEngine(feedback, 250, 0);

		// ensure that the init method gets called on construction
		assertTrue("'initialize' was not called", feedback.initCalled == 0);

		// Run the animation
		engine.schedule();

		// Wait for the animation to finish
		Display display = shell.getDisplay();
		while(engine.getState() != Job.NONE) {
			while (display.readAndDispatch()) {
			}
			Thread.sleep(20);
		}

		// Make sure all methods were called...and in the correct order
		assertTrue("'render' was not called", feedback.renderCalled >= 0);
		assertTrue("'dispose' was not called", feedback.disposeCalled >= 0);

		//...in the correct order
		assertTrue("'dispose' called before 'render", feedback.renderCalled < feedback.disposeCalled);
	}
}
