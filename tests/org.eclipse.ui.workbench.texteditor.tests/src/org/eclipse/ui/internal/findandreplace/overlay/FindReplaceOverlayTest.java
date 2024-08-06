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
import static org.junit.Assert.assertNull;

import org.junit.Test;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.ui.internal.findandreplace.FindReplaceUITest;
import org.eclipse.ui.internal.findandreplace.SearchOptions;

import org.eclipse.ui.texteditor.FindReplaceAction;

public class FindReplaceOverlayTest extends FindReplaceUITest<OverlayAccess> {

	private static final String INSTANCE_SCOPE_NODE_NAME= "org.eclipse.ui.editors"; //$NON-NLS-1$

	private static final String USE_FIND_REPLACE_OVERLAY= "useFindReplaceOverlay"; //$NON-NLS-1$

	@Override
	public OverlayAccess openUIFromTextViewer(TextViewer viewer) {
		Accessor actionAccessor= new Accessor(getFindReplaceAction(), FindReplaceAction.class);
		actionAccessor.invoke("showOverlayInEditor", null);
		Accessor overlayAccessor= new Accessor(actionAccessor.get("overlay"), "org.eclipse.ui.internal.findandreplace.overlay.FindReplaceOverlay", getClass().getClassLoader());
		return new OverlayAccess(overlayAccessor);
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
		assertThat(dialog.isReplaceDialogOpen(), is(false));
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
		assertThat(dialog.getTarget().getSelection().y, is(4));
		dialog.pressSearch(true);
		assertThat(dialog.getTarget().getSelection().x, is("text ".length()));
		dialog.pressSearch(true);
		assertThat(dialog.getTarget().getSelection().x, is("text text ".length()));
		dialog.pressSearch(false);
		assertThat(dialog.getTarget().getSelection().x, is("text ".length()));
	}

	@Test
	public void testDisableOverlayViaPreference() {
		initializeTextViewerWithFindReplaceUI("");
		IEclipsePreferences preferences= InstanceScope.INSTANCE.getNode(INSTANCE_SCOPE_NODE_NAME);
		boolean useOverlayPreference= preferences.getBoolean(USE_FIND_REPLACE_OVERLAY, true);
		try {
			preferences.putBoolean(USE_FIND_REPLACE_OVERLAY, false);
			assertNull("dialog should be closed after changing preference", getDialog().getActiveShell());
		} finally {
			preferences.putBoolean(USE_FIND_REPLACE_OVERLAY, useOverlayPreference);
			reopenFindReplaceUIForTextViewer();
		}
	}

}
