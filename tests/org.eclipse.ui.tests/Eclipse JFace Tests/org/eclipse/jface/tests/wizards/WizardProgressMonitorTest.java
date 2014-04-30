/*******************************************************************************
 * Copyright (c) 2009, 2014 Remy Chi Jian Suen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433608
 ******************************************************************************/

package org.eclipse.jface.tests.wizards;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;

public class WizardProgressMonitorTest extends TestCase {

	private ProgressMonitoringWizardDialog dialog;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// initialize a display
		Display.getDefault();
		dialog = new ProgressMonitoringWizardDialog(new TheTestWizard());
		dialog.setBlockOnOpen(false);
	}

	@Override
	protected void tearDown() throws Exception {
		if (dialog != null) {
			dialog.close();
		}
		dialog = null;
		super.tearDown();
	}

	/**
	 * This test ensures that the labels of a progress monitor are cleared
	 * in-between calls to the wizard dialog's run(boolean, boolean,
	 * IRunnableWithProgress) method. If they are not cleared in between runs,
	 * they end up spilling over to the successive call and causes flickering of
	 * text in the label.
	 */
	public void testProgressLabelsClearedBug271530() throws Exception {
		// make up some random task names
		final String[] taskNames = { "Task A", "Task B" }; //$NON-NLS-1$ //$NON-NLS-2$

		// normal "stop button" behavior
		dialog.useStopButton = true;
		
		// open the dialog
		dialog.open();

		// run task A, we don't fork so we can make a UI call within the
		// runnable
		dialog.run(false, true, getRunnable(taskNames[0]));
		
		performAsserts();

		// run task B now, again, we don't fork so we can make a UI call within
		// the runnable
		dialog.run(false, true, getRunnable(taskNames[1]));
		
		// check that the label has been cleared
		performAsserts();
	}

	protected void performAsserts() {
		
		assertEquals("The progress monitor's label should have been cleared", //$NON-NLS-1$
				"", dialog.getProgressMonitorLabelText()); //$NON-NLS-1$
		
		String subTask = dialog.getProgressMonitorSubTaskText();
		if(subTask !=null && subTask.length() != 0)
			fail("The progress monitor's subtask should have been cleared"); //$NON-NLS-1$
	}

	
	protected IRunnableWithProgress getRunnable(final String taskName) {
		return new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) {
				
				// check that the label is empty
				assertEquals(
						"The progress monitor's label is not initially empty", //$NON-NLS-1$
						"", dialog.getProgressMonitorLabelText()); //$NON-NLS-1$

				// check the subtask as well
				String subTask = dialog.getProgressMonitorSubTaskText();
				if(subTask !=null && subTask.length() != 0)
					fail("The progress monitor's subtask is not initially empty"); //$NON-NLS-1$
				
				monitor.beginTask(taskName, 1);
				monitor.subTask("some sub task"); //$NON-NLS-1$
			}
		};
	}
	
	/**
	 * A wizard dialog that leverages ProgressMonitorPartSubclass to expose the
	 * progress monitor's label text.
	 */
	class ProgressMonitoringWizardDialog extends WizardDialog {
		
		boolean useStopButton;

		ProgressMonitoringWizardDialog(IWizard newWizard) {
			super(null, newWizard);
		}

		@Override
		protected ProgressMonitorPart createProgressMonitorPart(
				Composite composite, GridLayout pmlayout) {
			return new ProgressMonitorPartSubclass(composite, pmlayout, useStopButton);
		}

		public String getProgressMonitorLabelText() {
			ProgressMonitorPartSubclass monitor = (ProgressMonitorPartSubclass) getProgressMonitor();
			return monitor.getLabelText();
		}
		
		public String getProgressMonitorSubTaskText() {
			ProgressMonitorPartSubclass monitor = (ProgressMonitorPartSubclass) getProgressMonitor();
			return monitor.getSubTaskText();
		}

	}

	/**
	 * A special subclass of the ProgressMonitorPart that exposes this monitor's
	 * label's text.
	 */
	class ProgressMonitorPartSubclass extends ProgressMonitorPart {

		ProgressMonitorPartSubclass(Composite parent, Layout layout, boolean useStopButton) {
			super(parent, layout, useStopButton);
		}

		public String getLabelText() {
			return fLabel.getText();
		}
		
		public String getSubTaskText() {
			return fSubTaskName;
		}

	}
	
	/**
	 * This test ensures that a wizard dialog subclass which overrides the
	 * #getProgressMonitorPart method and returns a monitor without the stop button
	 * will fail gracefully.  That is, the runnable will run as expected.
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=287887#c57
	 */
	public void testProgressMonitorWithoutStopButtonBug287887() throws Exception {
		// make up some random task names
		final String[] taskNames = { "Task A", "Task B" }; //$NON-NLS-1$ //$NON-NLS-2$

		// no stop button, this is an invalid configuration
		dialog.useStopButton = false;
		
		// open the dialog
		dialog.open();

		// run task A, we don't fork so we can make a UI call within the
		// runnable
		dialog.run(false, true, getRunnable(taskNames[0]));
		
		performAsserts();

		// run task B now, again, we don't fork so we can make a UI call within
		// the runnable
		dialog.run(false, true, getRunnable(taskNames[1]));
		
		// check that the label has been cleared
		performAsserts();
		
		// we are successful simply by getting here without exception
	}

}
