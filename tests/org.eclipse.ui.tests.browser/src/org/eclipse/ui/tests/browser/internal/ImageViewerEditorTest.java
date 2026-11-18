/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.browser.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.browser.ImageViewerEditor;
import org.eclipse.ui.internal.browser.WebBrowserEditor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Opens the image viewer on a file, to check the job that loads the image: a
 * valid image stays in the viewer, a file that SWT cannot load, and an SVG, are
 * reopened in the browser editor.
 */
public class ImageViewerEditorTest {

 private static final long TIMEOUT_MILLIS = 20_000;

	private IWorkbenchPage page;
	private Path file;
	private PathInput input;

	@BeforeEach
	public void setUp() {
		page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	@AfterEach
	public void tearDown() throws IOException {
		if (input != null) {
			IEditorPart editor = page.findEditor(input);
			if (editor != null) {
				page.closeEditor(editor, false);
			}
		}
		if (file != null) {
			Files.deleteIfExists(file);
		}
	}

	@Test
	public void testImageStaysInTheImageViewer() throws Exception {
		file = Files.createTempFile("imageViewerTest", ".png");
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { new ImageData(2, 2, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF)) };
		loader.save(file.toString(), SWT.IMAGE_PNG);

		openAndWaitForLoading();

		assertInstanceOf(ImageViewerEditor.class, page.findEditor(input));
	}

	@Test
	public void testFileThatCannotBeLoadedOpensInTheBrowser() throws Exception {
		file = Files.createTempFile("imageViewerTest", ".png");
		Files.writeString(file, "this is not an image", StandardCharsets.UTF_8);

		openAndWaitForLoading();

		assertInstanceOf(WebBrowserEditor.class, page.findEditor(input));
	}

	@Test
	public void testFileWithoutImageOpensInTheBrowser() throws Exception {
		// a TIFF header whose first directory offset is 0: SWT returns no image
		file = Files.createTempFile("imageViewerTest", ".tif");
		Files.write(file, new byte[] { 0x49, 0x49, 42, 0, 0, 0, 0, 0 });

		openAndWaitForLoading();

		assertInstanceOf(WebBrowserEditor.class, page.findEditor(input));
	}

	@Test
	public void testInputWithNullPathShowsErrorPart() throws Exception {
		// init() rejects the input: the workbench shows its error part instead
		IEditorPart editor = page.openEditor(new PathInput(null), ImageViewerEditor.IMAGE_VIEWER_EDITOR_ID);
		try {
			assertEquals("org.eclipse.ui.internal.ErrorEditorPart", editor.getClass().getName());
		} finally {
			// the error part has a different input, so tearDown() cannot find it
			page.closeEditor(editor, false);
		}
	}

	@Test
	public void testSvgOpensInTheBrowser() throws Exception {
		file = Files.createTempFile("imageViewerTest", ".SVG");
		Files.writeString(file, "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"2\" height=\"2\"/>",
				StandardCharsets.UTF_8);

		input = new PathInput(IPath.fromOSString(file.toString()));
		page.openEditor(input, ImageViewerEditor.IMAGE_VIEWER_EDITOR_ID);

		// no loading job: the viewer hands over to the browser from the UI thread
		Display display = Display.getCurrent();
		long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
		while (!(page.findEditor(input) instanceof WebBrowserEditor)) {
			assertTrue(System.currentTimeMillis() < deadline, "the SVG was not opened in the browser editor");
			if (!display.readAndDispatch()) {
				Thread.sleep(10);
			}
		}
	}

	/**
	 * Opens the file in the image viewer and returns when the loading job is gone
	 * and the result that it queued for the UI thread is handled.
	 */
	private void openAndWaitForLoading() throws Exception {
		input = new PathInput(IPath.fromOSString(file.toString()));
		page.openEditor(input, ImageViewerEditor.IMAGE_VIEWER_EDITOR_ID);

		Display display = Display.getCurrent();
		long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
		while (isLoading(input.getName())) {
			assertTrue(System.currentTimeMillis() < deadline, "the loading job did not finish");
			if (!display.readAndDispatch()) {
				Thread.sleep(10);
			}
		}
		// the job queues its result before it ends
		while (display.readAndDispatch()) {
			// handle the queued result
		}
	}

	private static boolean isLoading(String fileName) {
		for (Job job : Job.getJobManager().find(null)) {
			if (job.getName().contains(fileName)) {
				return true;
			}
		}
		return false;
	}

	private static final class PathInput implements IPathEditorInput {
		private final IPath path;

		PathInput(IPath path) {
			this.path = path;
		}

		@Override
		public IPath getPath() {
			return path;
		}

		@Override
		public boolean exists() {
			return true;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return ImageDescriptor.getMissingImageDescriptor();
		}

		@Override
		public String getName() {
			return path != null ? path.lastSegment() : "nullPath.png";
		}

		@Override
		public IPersistableElement getPersistable() {
			return null;
		}

		@Override
		public String getToolTipText() {
			return path != null ? path.toOSString() : "nullPath.png";
		}

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof PathInput pathInput && Objects.equals(path, pathInput.path);
		}

		@Override
		public int hashCode() {
			return Objects.hash(path);
		}
	}
}
