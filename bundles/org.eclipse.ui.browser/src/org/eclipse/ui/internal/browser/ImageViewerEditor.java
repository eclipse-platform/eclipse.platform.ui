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
package org.eclipse.ui.internal.browser;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * Shows an image file. A click zooms in and a click with the
 * {@link org.eclipse.swt.SWT#MOD1} key zooms out, see {@link ImageZoomCanvas}.
 * <p>
 * The file is decoded in a background job, so that a large image does not block
 * the workbench. The SWT images are created in the UI thread.
 * </p>
 * <p>
 * An SVG file is not loaded here: SWT would rasterize it once into a bitmap
 * that gets blurry when zoomed, whereas the {@link WebBrowserEditor} renders it
 * as a vector. It is opened in the browser editor right away, whether or not an
 * SVG rasterizer is installed.
 * </p>
 * <p>
 * A file that SWT cannot load, that holds no image, or that cannot be shown for
 * lack of memory or handles, is opened in the {@link WebBrowserEditor} instead.
 * The browser editor is opened before this editor is closed, so that the tab
 * stays where it is. If the browser editor cannot be opened, a message is shown
 * in this editor.
 * </p>
 */
public class ImageViewerEditor extends EditorPart {
	public static final String IMAGE_VIEWER_EDITOR_ID = "org.eclipse.ui.browser.imageViewer"; //$NON-NLS-1$

	private IPath path;
	private ImageZoomCanvas canvas;
	private Job loadJob;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		IPathEditorInput pathInput = input instanceof IPathEditorInput pei ? pei
				: Adapters.adapt(input, IPathEditorInput.class);
		path = pathInput != null ? pathInput.getPath() : null;
		if (path == null) {
			throw new PartInitException(NLS.bind(Messages.errorInvalidImageInput, input.getName()));
		}
		setSite(site);
		setInput(input);
		setPartName(input.getName());
		String toolTip = input.getToolTipText();
		setTitleToolTip(toolTip != null ? toolTip : input.getName());
	}

	@Override
	public void createPartControl(Composite parent) {
		canvas = new ImageZoomCanvas(parent, 0);
		Display display = parent.getDisplay();
		IWorkbenchPage page = getSite().getPage();
		IEditorInput input = getEditorInput();
		if (isSvg(input.getName())) {
			// not directly: the editor is still being opened
			display.asyncExec(() -> reopenInBrowser(page, input));
			return;
		}
		loadJob = Job.createSystem(NLS.bind(Messages.imageViewerLoading, input.getName()), monitor -> {
			Runnable result;
			try {
				ImageLoader loader = new ImageLoader();
				ImageData[] data = loader.load(path.toOSString());
				result = () -> showImage(loader, data, page, input);
			} catch (RuntimeException | SWTError | OutOfMemoryError e) {
				result = () -> showFailure(e, page, input);
			}
			if (monitor.isCanceled()) {
				return;
			}
			try {
				display.asyncExec(result);
			} catch (SWTException e) {
				// the display is disposed, the workbench is closing
			}
		});
		loadJob.schedule();
	}

	private static boolean isSvg(String file) {
		return file.regionMatches(true, file.length() - 4, ".svg", 0, 4); //$NON-NLS-1$
	}

	/** Runs in the UI thread. */
	private void showImage(ImageLoader loader, ImageData[] data, IWorkbenchPage page, IEditorInput input) {
		if (canvas.isDisposed()) {
			return;
		}
		if (data.length == 0) {
			// a TIFF file with a header but no directory
			showFailure(null, page, input);
			return;
		}
		try {
			canvas.setImageData(loader, data);
		} catch (RuntimeException | SWTError | OutOfMemoryError e) {
			showFailure(e, page, input);
		}
	}

	/** Runs in the UI thread. */
	private void showFailure(Throwable error, IWorkbenchPage page, IEditorInput input) {
		Trace.trace(Trace.WARNING, "Cannot show " + input.getName() + " in the image viewer", error); //$NON-NLS-1$ //$NON-NLS-2$
		reopenInBrowser(page, input);
	}

	@Override
	public void dispose() {
		if (loadJob != null) {
			loadJob.cancel();
		}
		super.dispose();
	}

	private void reopenInBrowser(IWorkbenchPage page, IEditorInput input) {
		if (canvas.isDisposed()) {
			return;
		}
		try {
			// MATCH_ID is needed: with MATCH_INPUT alone this editor would be found
			// and activated again instead of opening the browser editor.
			IEditorPart opened = page.openEditor(input, WebBrowserEditor.WEB_BROWSER_EDITOR_ID,
					page.getActivePart() == this,
					IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_IGNORE_SIZE);
			if (opened instanceof WebBrowserEditor) {
				page.closeEditor(this, false);
			} else {
				// the workbench replaces a browser editor that fails to initialize with an
				// error part, without throwing
				if (opened != null) {
					page.closeEditor(opened, false);
				}
				canvas.setMessage(NLS.bind(Messages.errorImageNotShown, input.getName()));
			}
		} catch (PartInitException e) {
			Trace.trace(Trace.SEVERE, "Error opening " + input.getName() + " in the browser", e); //$NON-NLS-1$ //$NON-NLS-2$
			canvas.setMessage(NLS.bind(Messages.errorImageNotShown, input.getName()));
		}
	}

	@Override
	public void setFocus() {
		if (canvas != null && !canvas.isDisposed()) {
			canvas.setFocus();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// read only
	}

	@Override
	public void doSaveAs() {
		// read only
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
}
