/*******************************************************************************
 * Copyright (c) 2016, 2025 Red Hat Inc. and others
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
 *     Christoph Läubrich - [Generic Editor] misses quick fix if not at start of line
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import static org.eclipse.ui.tests.harness.util.DisplayHelper.runEventLoop;
import static org.eclipse.ui.tests.harness.util.DisplayHelper.waitForCondition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Test;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.core.resources.IMarker;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.genericeditor.tests.contributions.MarkerResolutionGenerator;

import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.TextOperationAction;

/**
 * @since 1.2
 */
public class TestQuickAssist extends AbstratGenericEditorTest {

	private static final String FIXME_PROPOSAL= "org.eclipse.ui.genericeditor.tests.contributions.MarkerResolutionGenerator.fixme";

	private static final String DEFAULT_PROPOSAL= "QUICK ASSIST PROPOSAL";
	private Shell completionShell;

	@Test
	public void testCompletion() throws Exception {
		this.completionShell=openQuickAssist();
		final Table completionProposalList = CompletionTest.findCompletionSelectionControl(completionShell);
		checkCompletionContent(completionProposalList, new String[] { DEFAULT_PROPOSAL });
	}

	@Test
	public void testMarkerQuickAssist() throws Exception {
		runEventLoop(Display.getDefault(), 0);
		IMarker marker= null;
		try {
			marker= this.file.createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.LINE_NUMBER, 1);
			marker.setAttribute(IMarker.CHAR_START, 0);
			marker.setAttribute(IMarker.CHAR_END, 5);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			marker.setAttribute(IMarker.MESSAGE, "We have a problem");
			marker.setAttribute(MarkerResolutionGenerator.FIXME, true);
			this.completionShell= openQuickAssist();
			final Table completionProposalList= CompletionTest.findCompletionSelectionControl(completionShell);
			checkCompletionContent(completionProposalList, new String[] { DEFAULT_PROPOSAL, FIXME_PROPOSAL });
		} finally {
			if (marker != null && marker.exists()) {
				marker.delete();
			}
		}
	}

	@Test
	public void testMarkerQuickAssistLineOnly() throws Exception {
		runEventLoop(Display.getDefault(), 0);
		IMarker marker= null;
		try {
			marker= this.file.createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.LINE_NUMBER, 1);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			marker.setAttribute(IMarker.MESSAGE, "We have a problem");
			marker.setAttribute(MarkerResolutionGenerator.FIXME, true);
			this.completionShell= openQuickAssist();
			final Table completionProposalList= CompletionTest.findCompletionSelectionControl(completionShell);
			checkCompletionContent(completionProposalList, new String[] { DEFAULT_PROPOSAL, FIXME_PROPOSAL });
		} finally {
			if (marker != null && marker.exists()) {
				marker.delete();
			}
		}
	}

	private Shell openQuickAssist() {
		editor.selectAndReveal(3, 0);
		TextOperationAction action = (TextOperationAction) editor.getAction(ITextEditorActionConstants.QUICK_ASSIST);
		action.update();
		final Set<Shell> beforeShells = Arrays.stream(editor.getSite().getShell().getDisplay().getShells()).filter(Shell::isVisible).collect(Collectors.toSet());
		action.run();
		Shell shell= CompletionTest.findNewShell(beforeShells, editor.getSite().getShell().getDisplay(), true);
		runEventLoop(PlatformUI.getWorkbench().getDisplay(),100);
		return shell;
	}

	/**
	 * Checks that a mock quick assist proposal comes up
	 * 
	 * @param completionProposalList the quick assist proposal list
	 * @param proposals expected proposals
	 */
	private void checkCompletionContent(final Table completionProposalList, String[] proposals) {
		// should be instantaneous, but happens to go asynchronous on CI so let's allow a wait
		waitForCondition(completionProposalList.getDisplay(), 200,
				() -> completionProposalList.getItemCount() >= proposals.length);
		assertEquals(proposals.length, completionProposalList.getItemCount());
		Set<String> existing= Arrays.stream(completionProposalList.getItems()).map(TableItem::getText).collect(Collectors.toSet());
		for (String proposal : proposals) {
			assertTrue("Missing quick assist proposal '" + proposal + "', found " + existing, existing.contains(proposal)); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}


	@After
	public void closeShell() {
		if (this.completionShell != null && !completionShell.isDisposed()) {
			completionShell.close();
		}
	}
}
