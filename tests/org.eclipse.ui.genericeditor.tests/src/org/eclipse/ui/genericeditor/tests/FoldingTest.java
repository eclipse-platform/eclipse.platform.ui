/**
 *  Copyright (c) 2018 Angelo ZERR.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [generic editor] Default Code folding for generic editor should use IndentFoldingStrategy - Bug 520659
 */
package org.eclipse.ui.genericeditor.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import org.eclipse.core.commands.Command;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.genericeditor.tests.contributions.EnabledPropertyTester;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.DisplayHelper;

import org.eclipse.ui.editors.text.IFoldingCommandIds;

public class FoldingTest extends AbstratGenericEditorTest {

	@Override
	protected void createAndOpenFile() throws Exception {
		//leave editor creation to individual tests
	}

	@Test
	public void testDefaultIndentFoldingOneFold() throws Exception {
		createAndOpenFile("bar.xml", "<a>\n b</a>");
		assertFoldingAsync(pos(0, 10));
	}

	@Test
	public void testDefaultIndentFoldingTwoFold() throws Exception {
		createAndOpenFile("bar.xml", "<a>\n <b>\n  c\n </b>\n</a>");
		assertFoldingAsync(pos(0, 19), pos(4, 9));
	}

	@Test
	public void testCustomFoldingReconciler() throws Exception {
		createAndOpenFile("bar.txt", "<a>\n <b>\n  c\n </b>\n</a>\n");
		assertFoldingAsync(pos(0, 24), pos(5, 14));
	}

	@Test
	public void testEnabledWhenCustomFoldingReconciler() throws Exception {
		EnabledPropertyTester.setEnabled(true);
		createAndOpenFile("enabledWhen.txt", "<a>\n <b>\n  c\n </b>\n</a>\n");
		assertFoldingAsync(pos(0, 24), pos(5, 14));
		cleanFileAndEditor();

		EnabledPropertyTester.setEnabled(false);
		createAndOpenFile("enabledWhen.txt", "<a>\n <b>\n  c\n </b>\n</a>\n");
		assertFoldingAsync();
	}

	private static Position pos(int offset, int length) {
		return new Position(offset, length);
	}

	private void assertFoldingAsync(final Position... expectedPositions) {
		DisplayHelper.waitForCondition(editor.getSite().getShell().getDisplay(), 5000, () -> {
			Position[] actualPositions = getAnnotationsFromAnnotationModel().stream() //
					.map(getProjectionAnnotationModel()::getPosition) //
					.filter(Objects::nonNull)
					.sorted(Comparator.comparingInt(Position::getOffset))
					.toArray(Position[]::new);
			return Arrays.deepEquals(actualPositions, expectedPositions);
		});
	}

	private IAnnotationModel getProjectionAnnotationModel() {
		ProjectionViewer dp= (ProjectionViewer) editor.getAdapter(ITextViewer.class);
		IAnnotationModel am= dp.getProjectionAnnotationModel();
		return am;
	}

	private List<Annotation> getAnnotationsFromAnnotationModel() {
		List<Annotation> annotationList= new ArrayList<>();
		Iterator<Annotation> annotationIterator= getProjectionAnnotationModel().getAnnotationIterator();
		while (annotationIterator.hasNext()) {
			Annotation ann= annotationIterator.next();
			if (ann.getType().equals(ProjectionAnnotation.TYPE)) {
				annotationList.add(ann);
			}
		}
		return annotationList;
	}

	@Test
	public void testFoldingCommandsEnabled() throws Exception {
		createAndOpenFile("bar.xml", "<a>\n b</a>");
		ICommandService commandService = editor.getEditorSite().getService(ICommandService.class);
		Command collapseAllCommand = commandService.getCommand(IFoldingCommandIds.FOLDING_COLLAPSE_ALL);
		assertTrue(collapseAllCommand.isEnabled() && collapseAllCommand.isHandled());
	}

	@Test
	public void testToggleFoldingCommand() throws Exception {
		createAndOpenFile("bar.xml", "<a>\n b</a>");
		ProjectionViewer viewer = (ProjectionViewer) editor.getAdapter(ITextViewer.class);
		IHandlerService handlerService = editor.getEditorSite().getService(IHandlerService.class);
		assertTrue(viewer.isProjectionMode());
		handlerService.executeCommand(IFoldingCommandIds.FOLDING_TOGGLE, null);
		assertFalse(viewer.isProjectionMode());
		handlerService.executeCommand(IFoldingCommandIds.FOLDING_TOGGLE, null);
		assertTrue(viewer.isProjectionMode());
	}

	@Test
	public void testRulerMenuShowsFoldingSubmenu() throws Exception {
		createAndOpenFile("bar.xml", "<a>\n b</a>");
		assertTrue(rulerMenuContains(editor, "Folding"));
	}

	@Test
	public void testRulerMenuHidesFoldingSubmenuWithoutFoldingSupport() throws Exception {
		createAndOpenFile("bar.xml", "<a>\n b</a>");
		IWorkbenchPage page = editor.getSite().getPage();
		page.closeEditor(editor, false);
		editor = null;
		IEditorPart textEditor = page.openEditor(new FileEditorInput(file), "org.eclipse.ui.DefaultTextEditor");
		try {
			assertFalse(rulerMenuContains(textEditor, "Folding"));
		} finally {
			page.closeEditor(textEditor, false);
		}
	}

	private static boolean rulerMenuContains(IEditorPart part, String label) {
		Menu menu = part.getAdapter(IVerticalRulerInfo.class).getControl().getMenu();
		menu.notifyListeners(SWT.Show, null);
		try {
			for (MenuItem item : menu.getItems()) {
				if (label.equals(item.getText().replace("&", ""))) {
					return true;
				}
			}
			return false;
		} finally {
			menu.notifyListeners(SWT.Hide, null);
		}
	}
}
