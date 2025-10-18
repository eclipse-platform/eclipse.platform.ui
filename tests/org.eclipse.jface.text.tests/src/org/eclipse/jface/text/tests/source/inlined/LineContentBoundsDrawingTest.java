/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text.tests.source.inlined;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.inlined.InlinedAnnotationSupport;
import org.eclipse.jface.text.source.inlined.LineContentAnnotation;

import org.eclipse.ui.tests.harness.util.DisplayHelper;

/**
 * This test verify that the bounds of the text as returned by StyledText.getTextBounds()
 * actually match what's printed on the widget.
 */
public class LineContentBoundsDrawingTest {
	public static String[] contents() {
		return new String[] {
				"annotation inside text",
				" annotation just after initial space",
				"\tannoation just after initial tab"
		};
	}

	public static final class AccessAllAnnoations implements IAnnotationAccess {
		@Override
		public Object getType(Annotation annotation) {
			return annotation.getType();
		}

		@Override
		public boolean isMultiLine(Annotation annotation) {
			return true;
		}

		@Override
		public boolean isTemporary(Annotation annotation) {
			return true;
		}
	}

	public static final class TestAnnotationPainter extends AnnotationPainter {
		private boolean painted;

		public TestAnnotationPainter(ISourceViewer sourceViewer, IAnnotationAccess access) {
			super(sourceViewer, access);
		}

		@Override
		public void paint(int reason) {
			this.painted = true;
			super.paint(reason);
		}

		public boolean wasPainted() {
			return this.painted;
		}
	}

	private Shell fParent;

	@BeforeEach
	public void setUp() {
		fParent= new Shell();
	}

	@AfterEach
	public void tearDown() {
		fParent.dispose();
		fParent = null;
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("contents")
	public void testTextBoundsMatchPaintedArea(String text) {
		fParent.setLayout(new FillLayout());

		// Create source viewer and initialize the content
		ISourceViewer sourceViewer = new SourceViewer(fParent,null,
				SWT.V_SCROLL | SWT.BORDER);
		sourceViewer.setDocument(new Document(text), new AnnotationModel());

		// Initialize inlined annotations support
		InlinedAnnotationSupport support = new InlinedAnnotationSupport();
		IAnnotationAccess annotationAccess = new AccessAllAnnoations();
		TestAnnotationPainter painter = new TestAnnotationPainter(sourceViewer, annotationAccess);
		((ITextViewerExtension2) sourceViewer).addPainter(painter);
		support.install(sourceViewer, painter);

		// add annotations
		LineContentAnnotation annotation= new LineContentAnnotation(new Position(1, 1), sourceViewer);
		annotation.setText("longAnnationToDisplayOnTab");
		support.updateAnnotations(Collections.singleton(annotation));
		fParent.open();
		StyledText textWidget= sourceViewer.getTextWidget();
		Assertions.assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return textWidget.isVisible() && painter.wasPainted();
			}
		}.waitForCondition(textWidget.getDisplay(), 2000));
		DisplayHelper.sleep(textWidget.getDisplay(), 1000);
		Rectangle textBounds= textWidget.getTextBounds(0, textWidget.getText().length() - 1);
		int supposedMostRightPaintedPixel = textBounds.x + textBounds.width - 1;
		int mostRightPaintedPixel= getMostRightPaintedPixel(textWidget);
		Assertions.assertEquals(supposedMostRightPaintedPixel, mostRightPaintedPixel, 1.5); // use double comparison with delta to tolerate variation from a system to the other
	}

	public int getMostRightPaintedPixel(StyledText widget) {
		Image image = new Image(widget.getDisplay(), (gc, width, height) -> {}, widget.getSize().x, widget.getSize().y);
		GC gc = new GC(widget);
		gc.copyArea(image, 0, 0);
		gc.dispose();
		RGB backgroundRgb = widget.getBackground().getRGB();
		ImageData imageData = image.getImageData();
		for (int x = imageData.width - 50 /* magic number to avoid rulers and other */; x >= 0; x--) {
			for (int y = 3 /* magic number as well to avoid title bar */; y < imageData.height - 3; y++) {
				if (!imageData.palette.getRGB(imageData.getPixel(x, y)).equals(backgroundRgb)) {
					image.dispose();
					return x;
				}
			}
		}
		image.dispose();
		return -1;
	}
}
