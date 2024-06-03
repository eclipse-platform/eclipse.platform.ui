/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace.overlay;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.ResourceBundle;

import org.junit.Test;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.ui.internal.findandreplace.FindReplaceUITest;
import org.eclipse.ui.internal.findandreplace.SearchOptions;

public class FindReplaceOverlayTest extends FindReplaceUITest<OverlayAccess> {

	@Override
	public OverlayAccess openUIFromTextViewer(TextViewer viewer) {
		OverlayAccess ret;

		Accessor fFindReplaceAction;
		fFindReplaceAction= new Accessor("org.eclipse.ui.texteditor.FindReplaceAction", getClass().getClassLoader(),
				new Class[] { ResourceBundle.class, String.class, Shell.class, IFindReplaceTarget.class },
				new Object[] { ResourceBundle.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), "Editor.FindReplace.", viewer.getControl().getShell(),
						getTextViewer().getFindReplaceTarget() });
		fFindReplaceAction.invoke("showOverlayInEditor", null);
		Accessor overlayAccessor= new Accessor(fFindReplaceAction.get("overlay"), "org.eclipse.ui.internal.findandreplace.overlay.FindReplaceOverlay", getClass().getClassLoader());

		ret= new OverlayAccess(overlayAccessor);
		return ret;
	}

	@Test
	public void testDirectionalSearchButtons() {
		initializeTextViewerWithFindReplaceUI("line\nline\nline\nline");
		OverlayAccess dialog= getDialog();

		dialog.setFindText("line");
		IFindReplaceTarget target= dialog.getTarget();

		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.pressSearch(true);
		assertEquals(5, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.pressSearch(true);
		assertEquals(10, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.pressSearch(false);
		assertEquals(5, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.pressSearch(true);
		assertEquals(10, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.pressSearch(false);
		assertEquals(5, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.pressSearch(false);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);
	}

	@Test
	public void testIncrementalSearchUpdatesAfterChangingOptions() {
		initializeTextViewerWithFindReplaceUI("alinee\naLinee\nline\nline");
		OverlayAccess dialog= getDialog();
		IFindReplaceTarget target= dialog.getTarget();

		dialog.setFindText("Line");
		dialog.select(SearchOptions.CASE_SENSITIVE);
		assertThat(dialog.getTarget().getSelectionText(), is("Line"));

		dialog.unselect(SearchOptions.CASE_SENSITIVE);
		assertEquals(1, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.select(SearchOptions.WHOLE_WORD);
		assertEquals(14, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.unselect(SearchOptions.CASE_SENSITIVE);
		dialog.unselect(SearchOptions.WHOLE_WORD);
		assertEquals(1, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);
		assertThat(dialog.getTarget().getSelectionText(), is("line"));
	}

	@Test
	public void testCantOpenReplaceDialogInReadOnlyEditor() {
		openTextViewer("text");
		getTextViewer().setEditable(false);
		initializeFindReplaceUIForTextViewer();
		OverlayAccess dialog= getDialog();

		dialog.openReplaceDialog();
		reopenFindReplaceUIForTextViewer();
		dialog= getDialog();
		assertThat(dialog.isReplaceDialogOpen(), is(false));
	}

	@Test
	public void testRememberReplaceExpandState() {
		initializeTextViewerWithFindReplaceUI("text");
		OverlayAccess dialog= getDialog();

		dialog.openReplaceDialog();
		assertThat(dialog.isReplaceDialogOpen(), is(true));
		reopenFindReplaceUIForTextViewer();
		dialog= getDialog();
		assertThat(dialog.isReplaceDialogOpen(), is(true));

		dialog.closeReplaceDialog();
		reopenFindReplaceUIForTextViewer();
		dialog= getDialog();
		assertThat(dialog.isReplaceDialogOpen(), is(false));

		dialog.openReplaceDialog();
		getTextViewer().setEditable(false);
		reopenFindReplaceUIForTextViewer();
		dialog= getDialog();
		assertThat(dialog.isReplaceDialogOpen(), is(false));
	}

	@Test
	public void testSearchBackwardsWithRegEx() {
		initializeTextViewerWithFindReplaceUI("text text text");

		OverlayAccess dialog= getDialog();
		dialog.select(SearchOptions.REGEX);
		dialog.setFindText("text"); // with RegEx enabled, there is no incremental search!
		dialog.pressSearch(true);
		assertThat(dialog.getTarget().getSelection().x, is(0));
		assertThat(dialog.getTarget().getSelection().y, is(4));
		dialog.pressSearch(true);
		assertThat(dialog.getTarget().getSelection().x, is("text ".length()));
		assertThat(dialog.getTarget().getSelection().y, is(4));
		dialog.pressSearch(true);
		assertThat(dialog.getTarget().getSelection().x, is("text text ".length()));
		assertThat(dialog.getTarget().getSelection().y, is(4));
		dialog.pressSearch(false);
		assertThat(dialog.getTarget().getSelection().x, is("text ".length()));
		assertThat(dialog.getTarget().getSelection().y, is(4));
	}

}
