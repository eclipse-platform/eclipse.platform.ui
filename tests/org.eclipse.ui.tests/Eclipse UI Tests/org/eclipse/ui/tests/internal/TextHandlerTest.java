/*******************************************************************************
 * Copyright (c) 2009, 2020 IBM Corporation and others.
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

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.junit.Assume.assumeFalse;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.TextActionHandler;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test for {@link TextActionHandler}.
 *
 * @since 3.5
 */
@RunWith(JUnit4.class)
public class TextHandlerTest extends UITestCase {

	public TextHandlerTest() {
		super(TextHandlerTest.class.getSimpleName());
	}

	@Test
	public void testEditableText() throws Exception {
		assumeFalse("Test fails on Mac: Bug 544675", Platform.OS_MACOSX.equals(Platform.getOS()));

		IWorkbenchWindow window = openTestWindow();
		TextControlView view = (TextControlView) window.getActivePage()
				.showView(TextControlView.ID);
		view.editableText.setFocus();
		Clipboard clipboard = new Clipboard(window.getWorkbench().getDisplay());
		try {
			// Clipboard clearContents will only clear own content. If clipboard contains
			// content from another application when test starts it will be cleared and test
			// can fail. Fix is to set anything and then clear it.
			clipboard.setContents(new Object[] { "https://www.eclipse.org" },
					new Transfer[] { URLTransfer.getInstance() });
			clipboard.clearContents();
			processEvents();
			view.updateEnabledState();
			processEvents();

			assertFalse(view.getCopyAction().isEnabled());
			assertFalse(view.getCutAction().isEnabled());
			assertFalse(view.getPasteAction().isEnabled());
			assertFalse(view.getSelectAllAction().isEnabled());

			clipboard.setContents(new Object[] { "http://www.google.ca" },
					new Transfer[] { URLTransfer.getInstance() });
			processEvents();
			view.updateEnabledState();
			processEvents();

			assertFalse(view.getCopyAction().isEnabled());
			assertFalse(view.getCutAction().isEnabled());
			assertFalse(view.getPasteAction().isEnabled());
			assertFalse(view.getSelectAllAction().isEnabled());

			view.editableText.setText("Hello");
			processEvents();
			view.updateEnabledState();
			processEvents();

			assertFalse(view.getCopyAction().isEnabled());
			assertFalse(view.getCutAction().isEnabled());
			assertFalse(view.getPasteAction().isEnabled());
			assertTrue(view.getSelectAllAction().isEnabled());

			view.editableText.setSelection(0, 3);
			processEvents();
			view.updateEnabledState();
			processEvents();

			assertTrue(view.getCopyAction().isEnabled());
			assertTrue(view.getCutAction().isEnabled());
			assertFalse(view.getPasteAction().isEnabled());
			assertTrue(view.getSelectAllAction().isEnabled());

			clipboard.setContents(new Object[] { "http://www.google.ca" },
					new Transfer[] { TextTransfer.getInstance() });
			processEvents();
			view.updateEnabledState();
			processEvents();

			assertTrue(view.getCopyAction().isEnabled());
			assertTrue(view.getCutAction().isEnabled());
			assertTrue(view.getPasteAction().isEnabled());
			assertTrue(view.getSelectAllAction().isEnabled());
		} finally {
			try {
				clipboard.clearContents();
			} finally {
				clipboard.dispose();
			}
		}
	}

	@Test
	public void testNonEditableText() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		TextControlView view = (TextControlView) window.getActivePage()
				.showView(TextControlView.ID);

		Clipboard clipboard = new Clipboard(window.getWorkbench().getDisplay());
		try {
			clipboard.setContents(new Object[] { "http://www.google.ca" },
					new Transfer[] { URLTransfer.getInstance() });
			view.nonEditableText.setFocus();
			processEvents();
			view.updateEnabledState();
			processEvents();

			assertFalse(view.getCopyAction().isEnabled());
			assertFalse(view.getCutAction().isEnabled());
			assertFalse(view.getPasteAction().isEnabled());
			assertFalse(view.getSelectAllAction().isEnabled());

			view.nonEditableText.setText("Hello");
			processEvents();
			view.updateEnabledState();
			processEvents();

			assertFalse(view.getCopyAction().isEnabled());
			assertFalse(view.getCutAction().isEnabled());
			assertFalse(view.getPasteAction().isEnabled());
			assertTrue(view.getSelectAllAction().isEnabled());

			view.nonEditableText.setSelection(0, 3);
			processEvents();
			view.updateEnabledState();
			processEvents();

			assertTrue(view.getCopyAction().isEnabled());
			assertFalse(view.getCutAction().isEnabled());
			assertFalse(view.getPasteAction().isEnabled());
			assertTrue(view.getSelectAllAction().isEnabled());

			clipboard.setContents(new Object[] { "http://www.google.ca" },
					new Transfer[] { TextTransfer.getInstance() });
			processEvents();
			view.updateEnabledState();
			processEvents();

			assertTrue(view.getCopyAction().isEnabled());
			assertFalse(view.getCutAction().isEnabled());
			assertFalse(view.getPasteAction().isEnabled());
			assertTrue(view.getSelectAllAction().isEnabled());
		} finally {
			try {
				clipboard.clearContents();
			} finally {
				clipboard.dispose();
			}
		}
	}
}
