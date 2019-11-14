/*******************************************************************************
 * Copyright (c) 2016-2019 Red Hat Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Obuchowicz (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Test;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.jface.text.tests.util.DisplayHelper;

import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.TextOperationAction;

/**
 * @since 1.2
 */
public class TestQuickAssist extends AbstratGenericEditorTest {

	private Shell completionShell;

	@Test
	public void testCompletion() throws Exception {
		final Set<Shell> beforeShells = Arrays.stream(editor.getSite().getShell().getDisplay().getShells()).filter(Shell::isVisible).collect(Collectors.toSet());
		openQuickAssist();
		this.completionShell= CompletionTest.findNewShell(beforeShells, editor.getSite().getShell().getDisplay());
		final Table completionProposalList = CompletionTest.findCompletionSelectionControl(completionShell);
		checkCompletionContent(completionProposalList);
	}

	private void openQuickAssist() {
		editor.selectAndReveal(3, 0);
		TextOperationAction action = (TextOperationAction) editor.getAction(ITextEditorActionConstants.QUICK_ASSIST);
		action.update();
		action.run();
		waitAndDispatch(100);
	}

	/**
	 * Checks that a mock quick assist proposal comes up
	 * @param completionProposalList the quick assist proposal list
	 */
	private void checkCompletionContent(final Table completionProposalList) {
		// should be instantaneous, but happens to go asynchronous on CI so let's allow a wait
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return completionProposalList.getItemCount() == 1;
			}
		}.waitForCondition(completionProposalList.getDisplay(), 200);
		assertEquals(1, completionProposalList.getItemCount());
		final TableItem quickAssistItem = completionProposalList.getItem(0);
		assertTrue("Missing quick assist proposal", quickAssistItem.getText().contains("QUICK ASSIST PROPOSAL")); //$NON-NLS-1$ //$NON-NLS-2$

	}


	@After
	public void closeShell() {
		if (this.completionShell != null && !completionShell.isDisposed()) {
			completionShell.close();
		}
	}
}
