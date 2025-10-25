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
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Add CodeMining support in SourceViewer - Bug 527515
 *  Dietrich Travkin <dietrich.travkin@solunar.de> - Fix code mining redrawing - Issue 3405
 */
package org.eclipse.jface.text.examples.codemining;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.WhitespaceCharacterPainter;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension5;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A Code Mining demo with class references and implementations minings.
 */
public class CodeMiningDemo {

	private static boolean showWhitespaces = false;
	private static AtomicReference<Boolean> useInLineCodeMinings = new AtomicReference<>(false);

	private static String LINE_HEADER = "Line header";
	private static String IN_LINE = "In-line";

	public static void main(String[] args) throws Exception {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(2, false));
		shell.setText("Code Mining demo");

		Button toggleInLineButton = new Button(shell, SWT.PUSH);
		toggleInLineButton.setText(LINE_HEADER);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL).grab(false, false).applyTo(toggleInLineButton);

		AtomicReference<String> endOfLineString = new AtomicReference<>("End of line");
		Text endOfLineText = new Text(shell, SWT.NONE);
		endOfLineText.setText(endOfLineString.get());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(endOfLineText);

		SourceViewer sourceViewer = new SourceViewer(shell, null, SWT.V_SCROLL | SWT.BORDER);
		sourceViewer.getTextWidget().setFont(JFaceResources.getTextFont());
		if (showWhitespaces) {
			WhitespaceCharacterPainter whitespaceCharPainter = new WhitespaceCharacterPainter(sourceViewer, true, true,
					true, true, true, true, true, true, true, true, true, 100);
			sourceViewer.addPainter(whitespaceCharPainter);
		}
		sourceViewer.setDocument(
				new Document("// Type class & new keyword and see references CodeMining\n"
						+ "// Name class with a number N to emulate Nms before resolving the references CodeMining\n"
						+ "// Empty lines show a header annotating they're empty.\n"
						+ "// The word `echo` is echoed.\n"
						+ "// Lines containing `end` get an annotation at their end\n"
						+ "// Press the toggle button in the upper left  corner to switch between\n"
						+ "// showing reference titles in-line and showing them in additional lines.\n\n"
						+ "class A\n" //
						+ "new A\n" //
						+ "new A\n\n" //
						+ "code mining at end here\n"
						+ "code mining at end here with CRLF\r\n"
						+ "class 5\n" //
						+ "new 5\n" //
						+ "new 5\n" //
						+ "new 5\n\n" //
						+ "Text with some references like [REF-X]\n" + "and [REF-Y] in it.\n\n"
						+ "multiline \n" //
						+ "multiline \n\n" //
						+ "suffix \n"),
				new AnnotationModel());
		GridDataFactory.fillDefaults().span(2, 1).grab(true, true).applyTo(sourceViewer.getTextWidget());
		// Add AnnotationPainter (required by CodeMining)
		addAnnotationPainter(sourceViewer);

		toggleInLineButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			useInLineCodeMinings.set(!useInLineCodeMinings.get());
			toggleInLineButton.setText(useInLineCodeMinings.get() ? IN_LINE : LINE_HEADER);
			sourceViewer.updateCodeMinings();
		}));

		// Initialize codemining providers
		sourceViewer.setCodeMiningProviders(new ICodeMiningProvider[] {
				new ClassReferenceCodeMiningProvider(), //
				new ClassImplementationsCodeMiningProvider(), //
				new ToEchoWithHeaderAndInlineCodeMiningProvider("echo"), //
				new MultilineCodeMiningProvider(), //
				new EmptyLineCodeMiningProvider(), //
				new EchoAtEndOfLineCodeMiningProvider(endOfLineString), //
				new LineContentCodeMiningAfterPositionProvider(), //
				new ReferenceCodeMiningProvider(useInLineCodeMinings) });

		// Execute codemining in a reconciler
		MonoReconciler reconciler = new MonoReconciler(new IReconcilingStrategy() {

			@Override
			public void setDocument(IDocument document) {
				sourceViewer.updateCodeMinings();
			}

			@Override
			public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {

			}

			@Override
			public void reconcile(IRegion partition) {
				sourceViewer.updateCodeMinings();
			}
		}, false);
		reconciler.install(sourceViewer);

		endOfLineText.addModifyListener(event -> {
			endOfLineString.set(endOfLineText.getText());
			sourceViewer.updateCodeMinings();
		});

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private static void addAnnotationPainter(ISourceViewer viewer) {
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
		// Register this annotation painter as CodeMining annotation painter.
		((ISourceViewerExtension5) viewer).setCodeMiningAnnotationPainter(painter);
	}

}
