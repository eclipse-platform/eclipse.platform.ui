/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Lucas Bullen (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.tests.util.DisplayHelper;

import org.eclipse.ui.texteditor.IDocumentProvider;

public class HighlightTest extends AbstratGenericEditorTest {

	private static final String ANNOTATION_TYPE = "org.eclipse.ui.genericeditor.text"; //$NON-NLS-1$

	@Test
	public void testHighlightReconciler() {
		IDocumentProvider dp = editor.getDocumentProvider();
		IAnnotationModel am = dp.getAnnotationModel(editor.getEditorInput());
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return getAnnotationsFromAnnotationModel(am).size() == 1;
			}
		}.waitForCondition(Display.getDefault().getActiveShell().getDisplay(), 2000);
		Assert.assertTrue("file does not have highlighting", getAnnotationsFromAnnotationModel(am).size() == 1);
	}
	private List<Annotation> getAnnotationsFromAnnotationModel(IAnnotationModel annotationModel) {
		List<Annotation> annotationList = new ArrayList<>();
		Iterator<Annotation> annotationIterator=annotationModel.getAnnotationIterator();
		while (annotationIterator.hasNext()) {
			Annotation ann = annotationIterator.next();
			if (ann.getType().indexOf(ANNOTATION_TYPE) > -1) {
				annotationList.add(ann);
			}
		}
		return annotationList;
	}
}
