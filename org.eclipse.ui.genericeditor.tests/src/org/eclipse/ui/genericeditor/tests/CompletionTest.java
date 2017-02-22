/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria, Sopot Cela (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import org.eclipse.ui.genericeditor.tests.contributions.BarContentAssistProcessor;
import org.eclipse.ui.genericeditor.tests.contributions.LongRunningBarContentAssistProcessor;

import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * @since 1.0
 */
public class CompletionTest extends AbstratGenericEditorTest {

	@Test
	public void testCompletion() throws Exception {
		Set<Shell> beforeShell = new HashSet<>(Arrays.asList(Display.getDefault().getShells()));
		editor.selectAndReveal(3, 0);
		ContentAssistAction action = (ContentAssistAction) editor.getAction(ITextEditorActionConstants.CONTENT_ASSIST);
		action.update();
		action.run();
		waitAndDispatch(100);
		Set<Shell> afterShell = new HashSet<>(Arrays.asList(Display.getDefault().getShells()));
		afterShell.removeAll(beforeShell);
		assertEquals("No completion", 1, afterShell.size());
		Shell completionShell= afterShell.iterator().next();
		Table completionProposalList = findCompletionSelectionControl(completionShell);
		// instantaneous
		assertEquals(2, completionProposalList.getItemCount());
		TableItem computingItem = completionProposalList.getItem(0);
		assertTrue("Missing computing info entry", computingItem.getText().contains("Computing")); //$NON-NLS-1$ //$NON-NLS-2$
		TableItem completionProposalItem = completionProposalList.getItem(1);
		ICompletionProposal completionProposal = (ICompletionProposal)completionProposalItem.getData();
		assertEquals(BarContentAssistProcessor.PROPOSAL, completionProposal .getDisplayString());
		completionProposalList.setSelection(completionProposalItem);
		waitAndDispatch(LongRunningBarContentAssistProcessor.DELAY + 100);
		// asynchronous
		assertEquals(2, completionProposalList.getItemCount());
		completionProposalItem = completionProposalList.getItem(0);
		assertEquals(BarContentAssistProcessor.PROPOSAL, ((ICompletionProposal)completionProposalItem.getData()).getDisplayString());
		TableItem otherProposalItem = completionProposalList.getItem(1);
		assertEquals(LongRunningBarContentAssistProcessor.PROPOSAL, ((ICompletionProposal)otherProposalItem.getData()).getDisplayString());
		assertEquals("Addition of completion proposal should keep selection", completionProposal, completionProposalList.getSelection()[0].getData());
		
		// TODO find a way to actually trigger completion and verify result against Editor content
		// Assert.assertEquals("Completion didn't complete", "bars are good for a beer.", ((StyledText)editor.getAdapter(Control.class)).getText());
		completionShell.close();
	}

	private Table findCompletionSelectionControl(Widget control) {
		if (control instanceof Table) {
			return (Table)control;
		} else if (control instanceof Composite) {
			for (Widget child : ((Composite)control).getChildren()) {
				Table res = findCompletionSelectionControl(child);
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}

}
