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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import org.eclipse.core.commands.Command;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.ui.tests.harness.util.DisplayHelper;

import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.genericeditor.tests.contributions.EnabledPropertyTester;

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
}
