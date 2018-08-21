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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.text.tests.util.DisplayHelper;

import org.eclipse.ui.genericeditor.tests.contributions.EnabledPropertyTester;

public class FoldingTest extends AbstratGenericEditorTest {

	@Override
	protected void createAndOpenFile() throws Exception {
		//leave editor creation to individual tests
	}

	@Test
	public void testDefaultIndentFoldingOneFold() throws Exception {
		createAndOpenFile("bar.xml", "<a>\n b</a>");
		checkFolding(pos(0, 10));
	}

	@Test
	public void testDefaultIndentFoldingTwoFold() throws Exception {
		createAndOpenFile("bar.xml", "<a>\n <b>\n  c\n </b>\n</a>");
		checkFolding(pos(0, 19), pos(4, 9));
	}

	@Test
	public void testCustomFoldingReconciler() throws Exception {
		createAndOpenFile("bar.txt", "<a>\n <b>\n  c\n </b>\n</a>\n");
		checkFolding(pos(0, 24), pos(5, 14));
	}

	@Test
	public void testEnabledWhenCustomFoldingReconciler() throws Exception {
		EnabledPropertyTester.setEnabled(true);
		createAndOpenFile("enabledWhen.txt", "<a>\n <b>\n  c\n </b>\n</a>\n");
		checkFolding(pos(0, 24), pos(5, 14));
		cleanFileAndEditor();

		EnabledPropertyTester.setEnabled(false);
		createAndOpenFile("enabledWhen.txt", "<a>\n <b>\n  c\n </b>\n</a>\n");
		checkFolding();
	}

	private static Position pos(int offset, int length) {
		return new Position(offset, length);
	}

	private void checkFolding(Position... expectedPositions) {
		if (expectedPositions == null) {
			expectedPositions= new Position[0];
		}
		waitForAnnotations(expectedPositions.length);
		List<Annotation> folderAnnotations= getAnnotationsFromAnnotationModel();
		Assert.assertEquals(expectedPositions.length, folderAnnotations.size());
		List<Position> actualPositions= new ArrayList<>(expectedPositions.length);
		for (int i= 0; i < expectedPositions.length; i++) {
			Annotation folderAnnotation= folderAnnotations.get(i);
			Position actualPosition= getProjectionAnnotationModel().getPosition(folderAnnotation);
			actualPositions.add(actualPosition);
		}
		// Sort actual positions by offset
		Collections.sort(actualPositions, (p1, p2) -> p1.offset - p2.offset);
		Assert.assertArrayEquals(expectedPositions, actualPositions.toArray());
	}

	private IAnnotationModel getProjectionAnnotationModel() {
		ProjectionViewer dp= (ProjectionViewer) editor.getAdapter(ITextViewer.class);
		IAnnotationModel am= dp.getProjectionAnnotationModel();
		return am;
	}

	private void waitForAnnotations(int count) {
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return getAnnotationsFromAnnotationModel().size() == count;
			}
		}.waitForCondition(Display.getDefault(), 2000);
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
}
