/*******************************************************************************
 * Copyright (c) 2017, 2025 Stephan Wahlbrink and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Wahlbrink <sw@wahlbrink.eu>
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import static org.eclipse.ui.genericeditor.tests.contributions.BarContentAssistProcessor.BAR_CONTENT_ASSIST_PROPOSAL;
import static org.eclipse.ui.tests.harness.util.DisplayHelper.runEventLoop;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Test;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.source.SourceViewer;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.TextOperationAction;


public class ContextInfoTest extends AbstratGenericEditorTest {


	private Shell completionShell;


	@Test
	public void testContextInfo() throws Exception {
		cleanFileAndEditor();
		createAndOpenFile("foobar.txt", BAR_CONTENT_ASSIST_PROPOSAL);

		final Set<Shell> beforeShells = Arrays.stream(editor.getSite().getShell().getDisplay().getShells()).filter(Shell::isVisible).collect(Collectors.toSet());
		TextOperationAction action = (TextOperationAction) editor.getAction(ITextEditorActionConstants.CONTENT_ASSIST_CONTEXT_INFORMATION);

		editor.selectAndReveal(4, 0);
		runEventLoop(PlatformUI.getWorkbench().getDisplay(),0);

		action.update();
		action.run();
		this.completionShell= findNewShell(beforeShells);
		assertEquals("idx= 0", getInfoText(this.completionShell));

		editor.selectAndReveal(8, 0);
		runEventLoop(PlatformUI.getWorkbench().getDisplay(),0);

		action.update();
		action.run();
		this.completionShell= findNewShell(beforeShells);
		assertEquals("idx= 1", getInfoText(this.completionShell));
	}

	@Test
	public void testContextInfo_hide_Bug512251() throws Exception {
		cleanFileAndEditor();
		createAndOpenFile("foobar.txt", BAR_CONTENT_ASSIST_PROPOSAL);

		final Set<Shell> beforeShells = Arrays.stream(editor.getSite().getShell().getDisplay().getShells()).filter(Shell::isVisible).collect(Collectors.toSet());
		TextOperationAction action = (TextOperationAction) editor.getAction(ITextEditorActionConstants.CONTENT_ASSIST_CONTEXT_INFORMATION);

		editor.selectAndReveal(4, 0);
		runEventLoop(PlatformUI.getWorkbench().getDisplay(),0);

		action.update();
		action.run();
		this.completionShell= findNewShell(beforeShells);

		editor.selectAndReveal(8, 0);
		runEventLoop(PlatformUI.getWorkbench().getDisplay(),0);

		action.update();
		action.run();
		this.completionShell= findNewShell(beforeShells);

		editor.getAction(ITextEditorActionConstants.DELETE_LINE).run();

		ITextViewer sourceViewer= editor.getAdapter(ITextViewer.class);
		ContentAssistant assist= (ContentAssistant) new Accessor(sourceViewer, SourceViewer.class).get("fContentAssistant");
		new Accessor(assist, ContentAssistant.class).invoke("hide");
	}


	private Shell findNewShell(Set<Shell> beforeShells) {
		runEventLoop(PlatformUI.getWorkbench().getDisplay(), 100);
		Shell[] afterShells= findNewShells(beforeShells);
		if(afterShells.length == 0) {
			runEventLoop(PlatformUI.getWorkbench().getDisplay(),1000);
		}
		afterShells= findNewShells(beforeShells);
		assertEquals("No new shell found", 1, afterShells.length);
		return afterShells[0];
	}

	private Shell[] findNewShells(Set<Shell> beforeShells) {
		Shell[] afterShells = Arrays.stream(editor.getSite().getShell().getDisplay().getShells())
				.filter(Shell::isVisible)
				.filter(shell -> !beforeShells.contains(shell))
				.toArray(Shell[]::new);
		return afterShells;
	}

	private String getInfoText(final Shell shell) {
		assertTrue(shell.isVisible());
		Control[] children= shell.getChildren();
		for (Control child : children) {
			if (child instanceof Text) {
				return ((Text) child).getText();
			}
			if (child instanceof StyledText) {
				return ((StyledText) child).getText();
			}
		}
		return null;
	}

	@After
	public void closeShell() {
		if (this.completionShell != null && !completionShell.isDisposed()) {
			completionShell.close();
		}
	}

}
