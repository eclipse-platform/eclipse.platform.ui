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
 */
package org.eclipse.jface.text.examples.codemining;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension2;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A Code Mining demo with class references and implementations minings.
 */
public class CodeMiningDemo {

	public static void main(String[] args) throws Exception {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout());
		shell.setText("Code Mining demo");

		AtomicReference<String> endOfLineString = new AtomicReference<>("End of line");
		Text endOfLineText = new Text(shell, SWT.NONE);
		endOfLineText.setText(endOfLineString.get());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(endOfLineText);

		ISourceViewer sourceViewer = new SourceViewer(shell, null, SWT.V_SCROLL | SWT.BORDER);
		sourceViewer.setDocument(
				new Document("// Type class & new keyword and see references CodeMining\n"
						+ "// Name class with a number N to emulate Nms before resolving the references CodeMining\n"
						+ "// Empty lines show a header annotating they're empty.\n"
						+ "// The word `echo` is echoed.\n"
						+ "// Lines containing `end` get an annotation at their end\n\n"
						+ "class A\n" //
						+ "new A\n" //
						+ "new A\n\n" //
						+ "code mining at end here\n"
						+ "code mining at end here with CRLF\r\n"
						+ "class 5\n" //
						+ "new 5\n" //
						+ "new 5\n" //
						+ "new 5\n" //
						+ "multiline \n" //
						+ "multiline \n\n"),
				new AnnotationModel());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sourceViewer.getTextWidget());
		// Add AnnotationPainter (required by CodeMining)
		addAnnotationPainter(sourceViewer);
		// Initialize codemining providers
		((ISourceViewerExtension5) sourceViewer).setCodeMiningProviders(new ICodeMiningProvider[] {
				new ClassReferenceCodeMiningProvider(), //
				new ClassImplementationsCodeMiningProvider(), //
				new ToEchoWithHeaderAndInlineCodeMiningProvider("echo"), //
				new MultilineCodeMiningProvider(), //
				new EmptyLineCodeMiningProvider(), //
				new EchoAtEndOfLineCodeMiningProvider(endOfLineString) });
		// Execute codemining in a reconciler
		MonoReconciler reconciler = new MonoReconciler(new IReconcilingStrategy() {

			@Override
			public void setDocument(IDocument document) {
				((ISourceViewerExtension5) sourceViewer).updateCodeMinings();
			}

			@Override
			public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {

			}

			@Override
			public void reconcile(IRegion partition) {
				((ISourceViewerExtension5) sourceViewer).updateCodeMinings();
			}
		}, false);
		reconciler.install(sourceViewer);

		endOfLineText.addModifyListener(event -> {
			endOfLineString.set(endOfLineText.getText());
			((ISourceViewerExtension5) sourceViewer).updateCodeMinings();
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
