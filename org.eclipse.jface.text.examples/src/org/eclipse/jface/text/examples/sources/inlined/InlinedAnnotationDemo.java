/**
 *  Copyright (c) 2017 Angelo ZERR.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide inline annotations support - Bug 527675
 */
package org.eclipse.jface.text.examples.sources.inlined;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.inlined.AbstractInlinedAnnotation;
import org.eclipse.jface.text.source.inlined.InlinedAnnotationSupport;
import org.eclipse.jface.text.source.inlined.LineContentAnnotation;
import org.eclipse.jface.text.source.inlined.LineHeaderAnnotation;
import org.eclipse.jface.text.source.inlined.Positions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * An inlined demo with {@link LineHeaderAnnotation} and
 * {@link LineContentAnnotation} annotations both:
 *
 * <ul>
 * <li>a status OK, NOK is displayed before the line which starts with 'color:'.
 * This status is the result of the content after 'color' which must be a rgb
 * content. Here {@link ColorStatusAnnotation} is used.</li>
 * <li>a colorized square is displayed before the rgb declaration (inside the
 * line content). Here {@link ColorAnnotation} is used.</li>
 * </ul>
 *
 */
public class InlinedAnnotationDemo {

	public static void main(String[] args) throws Exception {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setText("Inlined annotation demo");

		// Create source viewer and initialize the content
		ISourceViewer sourceViewer = new SourceViewer(shell, null, SWT.V_SCROLL | SWT.BORDER);
		sourceViewer.setDocument(new Document("\ncolor:rgb(255, 255, 0)"), new AnnotationModel());

		// Initialize inlined annotations support
		InlinedAnnotationSupport support = new InlinedAnnotationSupport();
		support.install(sourceViewer, createAnnotationPainter(sourceViewer));

		// Refresh inlined annotation in none UI Thread with reconciler.
		MonoReconciler reconciler = new MonoReconciler(new IReconcilingStrategy() {

			@Override
			public void setDocument(IDocument document) {
				Set<AbstractInlinedAnnotation> annotations = getInlinedAnnotation(sourceViewer, support);
				support.updateAnnotations(annotations);
			}

			@Override
			public void reconcile(IRegion partition) {
				Set<AbstractInlinedAnnotation> anns = getInlinedAnnotation(sourceViewer, support);
				support.updateAnnotations(anns);
			}

			@Override
			public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {

			}
		}, false);
		reconciler.setDelay(1);
		reconciler.install(sourceViewer);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	/**
	 * Create annotation painter.
	 *
	 * @param viewer
	 *            the viewer.
	 * @return annotation painter.
	 */
	private static AnnotationPainter createAnnotationPainter(ISourceViewer viewer) {
		IAnnotationAccess annotationAccess = new IAnnotationAccess() {
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

		};
		AnnotationPainter painter = new AnnotationPainter(viewer, annotationAccess);
		((ITextViewerExtension2) viewer).addPainter(painter);
		return painter;
	}

	/**
	 * Returns the inlined annotations list to display in the given viewer.
	 *
	 * @param viewer
	 *            the viewer
	 * @param support
	 *            the inlined annotation suppor.
	 * @return the inlined annotations list to display in the given viewer.
	 */
	private static Set<AbstractInlinedAnnotation> getInlinedAnnotation(ISourceViewer viewer,
			InlinedAnnotationSupport support) {
		IDocument document = viewer.getDocument();
		Set<AbstractInlinedAnnotation> annotations = new HashSet<>();
		int lineCount = document.getNumberOfLines();
		for (int i = 0; i < lineCount; i++) {
			String line = getLineText(document, i).trim();
			int index = line.indexOf("color:");
			if (index == 0) {
				String rgb = line.substring(index + "color:".length(), line.length()).trim();
				try {
					String status = "OK!";
					Color color = parse(rgb, viewer.getTextWidget().getDisplay());
					if (color != null) {
					} else {
						status = "ERROR!";
					}
					// Status color annotation
					Position pos = Positions.of(i, document, true);
					ColorStatusAnnotation statusAnnotation = support.findExistingAnnotation(pos);
					if (statusAnnotation == null) {
						statusAnnotation = new ColorStatusAnnotation(pos, viewer);
					}
					statusAnnotation.setStatus(status);
					annotations.add(statusAnnotation);

					// Color annotation
					if (color != null) {
						Position colorPos = new Position(pos.offset + index + "color:".length(), 1);
						ColorAnnotation colorAnnotation = support.findExistingAnnotation(colorPos);
						if (colorAnnotation == null) {
							colorAnnotation = new ColorAnnotation(colorPos, viewer);
						}
						colorAnnotation.setColor(color);
						annotations.add(colorAnnotation);
					}

					// rgb parameter names annotations
					int rgbIndex = line.indexOf("rgb");
					if (rgbIndex != -1) {
						rgbIndex = rgbIndex + "rgb".length();
						int startOffset = pos.offset + rgbIndex;
						String rgbContent = line.substring(rgbIndex, line.length());
						int startIndex = addRGBParamNameAnnotation("red:", rgbContent, 0, startOffset, viewer, support,
								annotations);
						if (startIndex != -1) {
							startIndex = addRGBParamNameAnnotation("green:", rgbContent, startIndex, startOffset, viewer,
									support, annotations);
							if (startIndex != -1) {
								startIndex = addRGBParamNameAnnotation("blue:", rgbContent, startIndex, startOffset,
										viewer, support, annotations);
							}
						}
					}

				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
		return annotations;
	}

	/**
	 * Add RGB parameter name annotation
	 * 
	 * @param paramName
	 * @param rgbContent
	 * @param startIndex
	 * @param startOffset
	 * @param viewer
	 * @param support
	 * @param annotations
	 * @return the current parsed index
	 */
	private static int addRGBParamNameAnnotation(String paramName, String rgbContent, int startIndex, int startOffset,
			ISourceViewer viewer, InlinedAnnotationSupport support, Set<AbstractInlinedAnnotation> annotations) {
		char startChar = startIndex == 0 ? '(' : ',';
		char[] chars = rgbContent.toCharArray();
		for (int i = startIndex; i < chars.length; i++) {
			char c = chars[i];
			if (c == startChar) {
				if (i == chars.length - 1) {
					return -1;
				}
				Position paramPos = new Position(startOffset + i + 1, 1);
				LineContentAnnotation colorParamAnnotation = support.findExistingAnnotation(paramPos);
				if (colorParamAnnotation == null) {
					colorParamAnnotation = new LineContentAnnotation(paramPos, viewer);
				}
				colorParamAnnotation.setText(paramName);
				annotations.add(colorParamAnnotation);
				return i + 1;
			}
		}
		return -1;
	}

	/**
	 * Parse the given input rgb color and returns an instance of SWT Color and null
	 * otherwise.
	 *
	 * @param input
	 *            the rgb string color
	 * @param device
	 * @return the created color and null otherwise.
	 */
	private static Color parse(String input, Device device) {
		Pattern c = Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)");
		Matcher m = c.matcher(input);
		if (m.matches()) {
			try {
				return new Color(device, Integer.valueOf(m.group(1)), // r
						Integer.valueOf(m.group(2)), // g
						Integer.valueOf(m.group(3))); // b
			} catch (Exception e) {

			}
		}
		return null;
	}

	/**
	 * Returns the line text.
	 *
	 * @param document
	 *            the document.
	 * @param line
	 *            the line index.
	 * @return the line text.
	 */
	private static String getLineText(IDocument document, int line) {
		try {
			int offset = document.getLineOffset(line);
			int length = document.getLineLength(line);
			return document.get(offset, length);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
