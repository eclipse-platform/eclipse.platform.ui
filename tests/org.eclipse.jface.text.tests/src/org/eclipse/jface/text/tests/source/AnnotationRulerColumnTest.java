/*******************************************************************************
 * Copyright (c) 2024 Martin Erich Jobst and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Martin Erich Jobst - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests.source;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

import org.eclipse.ui.tests.harness.util.DisplayHelper;

public class AnnotationRulerColumnTest {

	private Shell shell;

	private ILogListener listener;

	private IStatus errorStatus;

	@Before
	public void setUp() {
		shell= new Shell();
		listener= (status, plugin) -> {
			if (status.getSeverity() == IStatus.ERROR && "org.eclipse.ui".equals(status.getPlugin())
					&& "Unhandled event loop exception".equals(status.getMessage())) {
				errorStatus= status;
			}
		};
		Platform.addLogListener(listener);
	}

	@After
	public void tearDown() {
		shell.dispose();
		shell= null;
		Platform.removeLogListener(listener);
	}

	@Test
	public void testDrawWithEmptyProjection() throws Throwable {
		shell.setLayout(new FillLayout());

		AnnotationRulerColumn annotationRulerColumn= new AnnotationRulerColumn(12, new TestAnnotationAccess());
		CompositeRuler ruler= new CompositeRuler();
		ruler.addDecorator(0, annotationRulerColumn);
		ProjectionViewer projectionViewer= new ProjectionViewer(shell, ruler, null, false, SWT.NONE);
		projectionViewer.setDocument(new Document("test\ndocument"), new AnnotationModel(), 0, 0);

		TestPaintListener paintListener= new TestPaintListener();
		annotationRulerColumn.getControl().addPaintListener(paintListener);

		shell.open();

		Assert.assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return projectionViewer.getTextWidget().isVisible() && paintListener.wasPainted();
			}
		}.waitForCondition(shell.getDisplay(), 2000));

		if (errorStatus != null && errorStatus.getException() != null) {
			throw errorStatus.getException();
		}
	}

	private static class TestPaintListener implements PaintListener {

		private boolean painted;

		@Override
		public void paintControl(PaintEvent e) {
			painted= true;
		}

		public boolean wasPainted() {
			return painted;
		}
	}

	private static class TestAnnotationAccess implements IAnnotationAccess, IAnnotationAccessExtension {

		@Override
		public String getTypeLabel(Annotation annotation) {
			return annotation.getText();
		}

		@Override
		public int getLayer(Annotation annotation) {
			return IAnnotationAccessExtension.DEFAULT_LAYER;
		}

		@Override
		public void paint(Annotation annotation, GC gc, Canvas canvas, Rectangle bounds) {
		}

		@Override
		public boolean isPaintable(Annotation annotation) {
			return true;
		}

		@Override
		public boolean isSubtype(Object annotationType, Object potentialSupertype) {
			return false;
		}

		@Override
		public Object[] getSupertypes(Object annotationType) {
			return null;
		}

		@Override
		@Deprecated
		public Object getType(Annotation annotation) {
			return annotation.getType();
		}

		@Override
		@Deprecated
		public boolean isMultiLine(Annotation annotation) {
			return true;
		}

		@Override
		@Deprecated
		public boolean isTemporary(Annotation annotation) {
			return true;
		}
	}
}
