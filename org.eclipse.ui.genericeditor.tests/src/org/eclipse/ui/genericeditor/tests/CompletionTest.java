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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * @since 3.11
 *
 */
public class CompletionTest {

	private AbstractTextEditor editor;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		GenericEditorTestUtils.setUpBeforeClass();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		GenericEditorTestUtils.tearDownAfterClass();
	}

	@Before
	public void setUp() throws Exception {
		GenericEditorTestUtils.closeIntro();
		editor = (AbstractTextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().openEditor(new FileEditorInput(GenericEditorTestUtils.getFile()), "org.eclipse.ui.genericeditor.GenericEditor");
	}

	@After
	public void tearDown() throws Exception {
		editor.getSite().getPage().closeEditor(editor, false);
		editor= null;
	}

	@Test
	public void testCompletion() throws Exception {
		Set<Shell> beforeShell = new HashSet<>(Arrays.asList(Display.getDefault().getShells()));
		editor.selectAndReveal(3, 0);
		ContentAssistAction action = (ContentAssistAction) editor.getAction(ITextEditorActionConstants.CONTENT_ASSIST);
		action.update();
		action.run();
		Set<Shell> afterShell = new HashSet<>(Arrays.asList(Display.getDefault().getShells()));
		afterShell.removeAll(beforeShell);
		assertEquals("No completion", 1, afterShell.size());
		Shell completionShell= afterShell.iterator().next();
		Table completionProposalList = findCompletionSelectionControl(completionShell);
		assertEquals(1, completionProposalList.getItemCount());
		TableItem completionProposalItem = completionProposalList.getItem(0);
		assertEquals("s are good for a beer.", ((ICompletionProposal)completionProposalItem.getData()).getDisplayString());
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
