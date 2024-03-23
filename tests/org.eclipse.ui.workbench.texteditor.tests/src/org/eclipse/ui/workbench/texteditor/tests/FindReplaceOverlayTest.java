package org.eclipse.ui.workbench.texteditor.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.ResourceBundle;

import org.junit.Test;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.ui.internal.findandreplace.SearchOptions;

import org.eclipse.ui.texteditor.FindReplaceOverlay;

public class FindReplaceOverlayTest extends FindReplaceUITest<OverlayAccess> {

	@Override
	public OverlayAccess openUIFromTextViewer(TextViewer viewer) {
		OverlayAccess ret;

		Accessor fFindReplaceAction;
		fFindReplaceAction= new Accessor("org.eclipse.ui.texteditor.FindReplaceAction", getClass().getClassLoader(),
				new Class[] { ResourceBundle.class, String.class, Shell.class, IFindReplaceTarget.class },
				new Object[] { ResourceBundle.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), "Editor.FindReplace.", viewer.getControl().getShell(),
						fTextViewer.getFindReplaceTarget() });
		fFindReplaceAction.invoke("showModernOverlay", null);
		FindReplaceOverlay overlay= (FindReplaceOverlay) fFindReplaceAction.get("overlay");

		ret= new OverlayAccess(this, new Accessor(overlay, FindReplaceOverlay.class));
		return ret;
	}

	@Override
	public void assertInitialConfiguration() {
		assertUnselected(SearchOptions.REGEX);
		assertUnselected(SearchOptions.WHOLE_WORD);
		assertUnselected(SearchOptions.CASE_SENSITIVE);
		assertSelected(SearchOptions.GLOBAL);
	}

	@Test
	public void testDirectionalSearchButtons() {
		initializeFindReplaceUI("line\nline\nline\nline");

		dialog.setFindText("line", false);
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
		initializeFindReplaceUI("alinee\naLinee\nline\nline");
		IFindReplaceTarget target= dialog.getTarget();

		dialog.setFindText("Line", false);
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

}
