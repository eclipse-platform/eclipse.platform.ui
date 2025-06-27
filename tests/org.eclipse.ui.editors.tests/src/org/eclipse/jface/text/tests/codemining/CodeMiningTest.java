/*******************************************************************************
 * Copyright (c) 2024 SAP
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text.tests.codemining;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension5;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import org.eclipse.ui.editors.tests.TestUtil;

public class CodeMiningTest {
	private static String PROJECT_NAME = "test_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

	private static IProject project;

	@BeforeClass
	public static void beforeClass() throws Exception {
		hideWelcomePage();
		createProject(PROJECT_NAME);
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (project != null)
			project.delete(true, new NullProgressMonitor());
	}

	@After
	public void after() {
		closeAllEditors();
		drainEventQueue();
		CodeMiningTestProvider.provideContentMiningAtOffset = -1;
		CodeMiningTestProvider.provideHeaderMiningAtLine = -1;
		TestUtil.cleanUp();
	}

	private static void closeAllEditors() {
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			for (IWorkbenchPage page : window.getPages()) {
				page.closeAllEditors(false);
			}
		}
	}

	private static void createProject(String projectName) throws Exception {
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project.exists()) {
			project.delete(true, true, new NullProgressMonitor());
		}
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(monitor);
				project.open(monitor);
			}

		}, new NullProgressMonitor());
	}

	@Test
	public void testInlinedAnnotationSupportIsInLinesReturnsValidResultAfterDocumentChange() throws Exception {
		IFile file = project.getFile("test.testprojectionviewer");
		if (file.exists()) {
			file.delete(true, new NullProgressMonitor());
		}
		String source = "first\nsecond\nthird\n";
		file.create(new ByteArrayInputStream(source.getBytes("UTF-8")), true, new NullProgressMonitor());
		CodeMiningTestProvider.provideHeaderMiningAtLine = 2;
		CodeMiningTestProvider.lineHeaderMiningText = "    first line header\n    secone line header\n    third line header";
		int offset = source.indexOf("second") + "second".length();
		IEditorPart editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		drainEventQueue();
		ISourceViewer viewer = (ISourceViewer) editor.getAdapter(ITextViewer.class);
		StyledText widget = viewer.getTextWidget();

		var annotationModel = ((ProjectionViewer) viewer).getProjectionAnnotationModel();
		var deletionsArray = new Annotation[] {};
		var additions = new HashMap<Annotation, Position>();
		ProjectionAnnotation annot = new ProjectionAnnotation();
		additions.put(annot, new Position(0, source.length()));
		annotationModel.modifyAnnotations(deletionsArray, additions, null);

		Assert.assertTrue("Line header code mining above 3rd line not drawn",
				waitForCondition(widget.getDisplay(), 2000, new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						try {
							return existsPixelWithNonBackgroundColorAtLine(viewer, 2);
						} catch (BadLocationException e) {
							e.printStackTrace();
							return false;
						}
					}
				}));

		IDocument doc = viewer.getDocument();
		widget.setCaretOffset(offset);
		doc.replace(offset, 0, "\n        insert text");
		drainEventQueue();
		Assert.assertTrue("Line header code mining above 4th line after inserting text not drawn",
				waitForCondition(widget.getDisplay(), 2000, new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						try {
							return existsPixelWithNonBackgroundColorAtLine(viewer, 3);
						} catch (BadLocationException e) {
							e.printStackTrace();
							return false;
						}
					}
				}));
	}

	@Test
	public void testCodeMiningOnEmptyLine() throws Exception {
		IFile file = project.getFile("test.txt");
		if (file.exists()) {
			file.delete(true, new NullProgressMonitor());
		}
		String source = "first line\n" + //
				"\n" + //
				"third line\n";
		file.create(new ByteArrayInputStream(source.getBytes("UTF-8")), true, new NullProgressMonitor());
		IEditorPart editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		drainEventQueue();
		ISourceViewer viewer = (ISourceViewer) editor.getAdapter(ITextViewer.class);
		StyledText widget = viewer.getTextWidget();
		Assert.assertTrue("line content mining not available",
				waitForCondition(widget.getDisplay(), 1000, new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						return widget.getStyleRangeAtOffset(0) != null
								&& widget.getStyleRangeAtOffset(0).metrics != null;
					}
				}));
		drainEventQueue();

		CodeMiningTestProvider.provideHeaderMiningAtLine = 1;
		((ISourceViewerExtension5) viewer).updateCodeMinings();

		Assert.assertTrue("Code mining not drawn at empty line after calling updateCodeMinings",
				waitForCondition(widget.getDisplay(), 2000, new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						try {
							return existsPixelWithNonBackgroundColorAtLine(viewer, 1);
						} catch (BadLocationException e) {
							e.printStackTrace();
							return false;
						}
					}
				}));
	}

	@Test
	public void testCodeMiningAtEndOfLine() throws Exception {
		IFile file = project.getFile("test.txt");
		if (file.exists()) {
			file.delete(true, new NullProgressMonitor());
		}
		String firstPart = "first line\n" + //
				"second line";
		String secondPart = "\n" + //
				"third line\n";
		String source = firstPart + secondPart;
		file.create(new ByteArrayInputStream(source.getBytes("UTF-8")), true, new NullProgressMonitor());
		IEditorPart editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		drainEventQueue();
		ISourceViewer viewer = (ISourceViewer) editor.getAdapter(ITextViewer.class);
		StyledText widget = viewer.getTextWidget();
		Assert.assertTrue("line content mining not available",
				waitForCondition(widget.getDisplay(), 1000, new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						return widget.getStyleRangeAtOffset(0) != null
								&& widget.getStyleRangeAtOffset(0).metrics != null;
					}
				}));
		drainEventQueue();

		CodeMiningTestProvider.provideContentMiningAtOffset = firstPart.length();
		((ISourceViewerExtension5) viewer).updateCodeMinings();

		Assert.assertTrue("Code mining not drawn at the end of second line after calling updateCodeMinings",
				waitForCondition(widget.getDisplay(), 2000, new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						try {
							return existsPixelWithNonBackgroundColorAtEndOfLine(viewer, 1);
						} catch (BadLocationException e) {
							e.printStackTrace();
							return false;
						}
					}
				}));
	}

	private void drainEventQueue() {
		while (Display.getDefault().readAndDispatch()) {
		}
	}

	private static void hideWelcomePage() {
		IWorkbenchPage ap = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ap.hideView(ap.findViewReference("org.eclipse.ui.internal.introview"));
	}

	private static boolean existsPixelWithNonBackgroundColorAtLine(ITextViewer viewer, int line)
			throws BadLocationException {
		StyledText widget = viewer.getTextWidget();
		IDocument document = viewer.getDocument();
		String delim = document.getLineDelimiter(line);
		int delimLen = 0;
		if (delim != null) {
			delimLen = delim.length();
		}
		int lineLength = document.getLineLength(line) - delimLen;
		if (lineLength < 0) {
			lineLength = 0;
		}
		int verticalScroolBarWidth = viewer.getTextWidget().getVerticalBar().getThumbBounds().width;
		int lineOffset = document.getLineOffset(line);
		Rectangle lineBounds = widget.getTextBounds(lineOffset, lineOffset + lineLength);
		String lineStr = document.get(lineOffset, lineLength);
		Image image = new Image(widget.getDisplay(), (gc, width, height) -> {}, widget.getSize().x, widget.getSize().y);
		try {
			GC gc = new GC(widget);
			gc.copyArea(image, 0, 0);
			Point textExtent = gc.textExtent(lineStr);
			if (lineBounds.height - textExtent.y > textExtent.y) {
				lineBounds.height -= textExtent.y;
			}
			gc.dispose();
			ImageData imageData = image.getImageData();
			for (int x = lineBounds.x + 1; x < image.getBounds().width - verticalScroolBarWidth
					&& x < imageData.width - verticalScroolBarWidth; x++) {
				for (int y = lineBounds.y; y < lineBounds.y + lineBounds.height; y++) {
					if (!imageData.palette.getRGB(imageData.getPixel(x, y)).equals(widget.getBackground().getRGB())) {
						return true;
					}
				}
			}
		} finally {
			image.dispose();
		}
		return false;
	}

	private static boolean existsPixelWithNonBackgroundColorAtEndOfLine(ITextViewer viewer, int line)
			throws BadLocationException {
		StyledText widget = viewer.getTextWidget();
		IDocument document = viewer.getDocument();
		int verticalScroolBarWidth = viewer.getTextWidget().getVerticalBar().getThumbBounds().width;
		int lineOffset = document.getLineOffset(line);
		String lineStr = document.get(lineOffset,
				document.getLineLength(line) - document.getLineDelimiter(line).length());
		Rectangle lineBounds = widget.getTextBounds(lineOffset, lineOffset);
		Image image = new Image(widget.getDisplay(), (gc, width, height) -> {}, widget.getSize().x, widget.getSize().y);
		try {
			GC gc = new GC(widget);
			gc.copyArea(image, 0, 0);
			Point textExtent = gc.textExtent(lineStr);
			lineBounds.x += textExtent.x;
			gc.dispose();
			ImageData imageData = image.getImageData();
			for (int x = lineBounds.x + 1; x < image.getBounds().width - verticalScroolBarWidth
					&& x < imageData.width - verticalScroolBarWidth; x++) {
				for (int y = lineBounds.y; y < lineBounds.y + lineBounds.height; y++) {
					if (!imageData.palette.getRGB(imageData.getPixel(x, y)).equals(widget.getBackground().getRGB())) {
						return true;
					}
				}
			}
		} finally {
			image.dispose();
		}
		return false;
	}

	private final boolean waitForCondition(Display display, long timeout, Callable<Boolean> condition)
			throws Exception {
		// if the condition already holds, succeed
		if (condition.call()) {
			return true;
		}
		if (timeout < 0) {
			return false;
		}
		drainEventQueue();
		if (condition.call()) {
			return true;
		}
		if (timeout == 0) {
			return false;
		}
		// repeatedly sleep until condition becomes true or timeout elapses
		long start = System.currentTimeMillis();
		long diff = timeout;
		Boolean cond = false;
		do {
			if (display.sleep()) {
				drainEventQueue();
			}
			cond = condition.call();
			diff = System.currentTimeMillis() - start;
		} while (!cond && diff < timeout);
		return cond;
	}
}
