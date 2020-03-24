/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

package org.eclipse.ui.tests.keys;

import org.eclipse.core.runtime.CoreException;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests Bug 42627
 *
 * @since 3.0
 */
@Ignore("Logging piece of fix did not go in M4.") // See commit 74a677140dd3fc6a09fa1c769c0af2cac3c1c08b
public class Bug42627Test {

	// TODO See if this is needed for anything.
	//	private class DummyView extends ViewPart {
	//		public void createPartControl(Composite composite) {
	//			// Do nothing
	//		}
	//
	//		public void setFocus() {
	//			// Do nothing
	//		}
	//	}

	/**
	 * A dummy implementation of an <code>Action</code>.
	 *
	 * @since 3.0
	 */
	//	private class DummyAction extends Action {
	//		// Nothing to implement
	//	}
	//	private boolean logged;

	/**
	 * Tests that actions with no defined command ID are logged.
	 *
	 * @throws CoreException
	 *            If something fails when trying to open a new project.
	 */
	@Test
	public void testLogUndefined() /*throws CoreException*/{
		// TODO No log is being generated.  What was Chris' fix?
		//		IWorkbenchWindow window = openTestWindow();
		//		ResourcesPlugin.getPlugin().getLog().addLogListener(new ILogListener() {
		//			public void logging(IStatus status, String string) {
		//				logged = true;
		//			}
		//		});
		//		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		//		IProject testProject = workspace.getRoot().getProject("Bug42627Project"); //$NON-NLS-1$
		//		testProject.create(null);
		//		testProject.open(null);
		//		AbstractTextEditor editor = (AbstractTextEditor) window.getActivePage().openEditor(testProject.getFile(".project")); //$NON-NLS-1$
		//		editor.selectAndReveal(0, 1);
		//		EditorSite site = (EditorSite) editor.getSite();
		//		site.getActionBars().setGlobalActionHandler("Bogus action name that hopefully will not exist", new DummyAction()); //$NON-NLS-1$
		//		window.getShell().setFocus();
		//		assertTrue("Nothing has been logged.", logged); //$NON-NLS-1$
	}
}
