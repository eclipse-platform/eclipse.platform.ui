/*******************************************************************************
 * Copyright (c) 2003, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.part.EditorPart;
/**
 * An integrated Web browser, defined as an editor to make
 * better use of the desktop.
 */
public class WebBrowserEditor extends EditorPart implements IBrowserViewerContainer {
	public static final String WEB_BROWSER_EDITOR_ID = "org.eclipse.ui.browser.editor"; //$NON-NLS-1$

	protected BrowserViewer webBrowser;
	protected String initialURL;
	protected ImageDescriptor imageDescriptor;

	protected TextAction cutAction;
	protected TextAction copyAction;
	protected TextAction pasteAction;

	private boolean disposed;
	private boolean lockName;

	/**
	 * WebBrowserEditor constructor comment.
	 */
	public WebBrowserEditor() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		WebBrowserEditorInput input = getWebBrowserEditorInput();

		int style = 0;
		if (input == null || input.isLocationBarLocal()) {
			style += BrowserViewer.LOCATION_BAR;
		}
		if (input == null || input.isToolbarLocal()) {
			style += BrowserViewer.BUTTON_BAR;
		}
		webBrowser = new BrowserViewer(parent, style);

		webBrowser.setURL(initialURL);
		webBrowser.setContainer(this);

		ImageResourceManager manager = new ImageResourceManager(webBrowser);
		setTitleImage(manager.getImage(imageDescriptor));

		if (input == null || input.isLocationBarLocal()) {
			cutAction = new TextAction(webBrowser, TextAction.CUT);
			copyAction = new TextAction(webBrowser, TextAction.COPY);
			pasteAction = new TextAction(webBrowser, TextAction.PASTE);
		}

		if (!lockName) {
			PropertyChangeListener propertyChangeListener = event -> {
				if (BrowserViewer.PROPERTY_TITLE.equals(event.getPropertyName())) {
					setPartName((String) event.getNewValue());
				}
			};
			webBrowser.addPropertyChangeListener(propertyChangeListener);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		// mark this instance as disposed to avoid stale references
		disposed = true;
	}

	public boolean isDisposed() {
		return disposed;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// do nothing
	}

	@Override
	public void doSaveAs() {
		// do nothing
	}

	/**
	 * Returns the copy action.
	 *
	 * @return org.eclipse.jface.action.IAction
	 */
	public IAction getCopyAction() {
		return copyAction;
	}

	/**
	 * Returns the cut action.
	 *
	 * @return org.eclipse.jface.action.IAction
	 */
	public IAction getCutAction() {
		return cutAction;
	}

	/**
	 * Returns the web editor input, if available. If the input was of
	 * another type, <code>null</code> is returned.
	 *
	 * @return org.eclipse.ui.internal.browser.IWebBrowserEditorInput
	 */
	protected WebBrowserEditorInput getWebBrowserEditorInput() {
		IEditorInput input = getEditorInput();
		if (input instanceof WebBrowserEditorInput)
			return (WebBrowserEditorInput) input;
		return null;
	}

	/**
	 * Returns the paste action.
	 *
	 * @return org.eclipse.jface.action.IAction
	 */
	public IAction getPasteAction() {
		return pasteAction;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		Trace.trace(Trace.FINEST, "Opening browser: " + input); //$NON-NLS-1$
		if (input instanceof IPathEditorInput) {
			IPathEditorInput pei = (IPathEditorInput) input;
			final IPath path= pei.getPath();
			URL url = null;
			try {
				if (path != null) {
					setPartName(path.lastSegment());
					url = path.toFile().toURI().toURL();
				}
				if (url != null)
					initialURL= url.toExternalForm();
			} catch (Exception e) {
				Trace.trace(Trace.SEVERE, "Error getting URL to file"); //$NON-NLS-1$
			}
			if (webBrowser != null) {
				if (initialURL != null)
					webBrowser.setURL(initialURL);
				site.getWorkbenchWindow().getActivePage().activate(this);
			}

			if (url != null)
				setTitleToolTip(url.getFile());

			imageDescriptor = ImageResourceManager.getImageDescriptor("$nl$/icons/obj16/" + "internal_browser.svg"); //$NON-NLS-1$ //$NON-NLS-2$
			//addResourceListener(file);
		} else if (input instanceof WebBrowserEditorInput) {
			WebBrowserEditorInput wbei = (WebBrowserEditorInput) input;
			initialURL = null;
			if (wbei.getURL() != null)
				initialURL = wbei.getURL().toExternalForm();
			if (webBrowser != null) {
				webBrowser.setURL(initialURL);
				site.getWorkbenchWindow().getActivePage().activate(this);
			}

			setPartName(wbei.getName());
			setTitleToolTip(wbei.getToolTipText());
			lockName = wbei.isNameLocked();

			imageDescriptor = wbei.getImageDescriptor();
		} else {
			IPathEditorInput pinput = Adapters.adapt(input, IPathEditorInput.class);
			if (pinput != null) {
				init(site, pinput);
			} else {
				throw new PartInitException(NLS.bind(Messages.errorInvalidEditorInput, input.getName()));
			}
		}

		setSite(site);
		setInput(input);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Open the input in the internal Web browser.
	 */
	public static void open(WebBrowserEditorInput input) {
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();

		try {
			IEditorReference[] editors = page.getEditorReferences();
			int size = editors.length;
			for (int i = 0; i < size; i++) {
				if (WEB_BROWSER_EDITOR_ID.equals(editors[i].getId())) {
					IEditorPart editor = editors[i].getEditor(true);
					if (editor != null && editor instanceof WebBrowserEditor) {
						WebBrowserEditor webEditor = (WebBrowserEditor) editor;
						WebBrowserEditorInput input2 = webEditor.getWebBrowserEditorInput();
						if (input2 == null || input.canReplaceInput(input2)) {
							editor.init(editor.getEditorSite(), input);
							return;
						}
					}
				}
			}

			page.openEditor(input, WebBrowserEditor.WEB_BROWSER_EDITOR_ID);
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error opening Web browser", e); //$NON-NLS-1$
		}
	}

	/*
	 * Asks this part to take focus within the workbench.
	 */
	@Override
	public void setFocus() {
		if (webBrowser != null)
			webBrowser.setFocus();
	}

	/**
	 * Close the editor correctly.
	 */
	@Override
	public boolean close() {
		final boolean [] result = new boolean[1];
		Display.getDefault()
				.asyncExec(() -> result[0] = getEditorSite().getPage().closeEditor(WebBrowserEditor.this, false));
		return result[0];
	}

	@Override
	public IActionBars getActionBars() {
		return getEditorSite().getActionBars();
	}

	@Override
	public void openInExternalBrowser(String url) {
		final IEditorInput input = getEditorInput();
		final String id = getEditorSite().getId();
		Runnable runnable = () -> doOpenExternalEditor(id, input);
		Display display = getSite().getShell().getDisplay();
		close();
		display.asyncExec(runnable);
	}

	protected void doOpenExternalEditor(String id, IEditorInput input) {
		IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
		String name = input.getName();
		IEditorDescriptor [] editors = registry.getEditors(name);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		String editorId = null;
		for (IEditorDescriptor editor : editors) {
			if (editor.getId().equals(id))
				continue;
			editorId = editor.getId();
			break;
		}

		IEditorDescriptor ddesc = registry.getDefaultEditor(name);
		if (ddesc!=null && ddesc.getId().equals(id)) {
			int dot = name.lastIndexOf('.');
			String ext = name;
			if (dot!= -1)
				ext = "*."+name.substring(dot+1); //$NON-NLS-1$
			registry.setDefaultEditor(ext, null);
		}

		if (editorId==null) {
			// no editor
			// next check with the OS for an external editor
			if (registry.isSystemExternalEditorAvailable(name))
				editorId = IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID;
		}

		if (editorId!=null) {
			try {
				page.openEditor(input, editorId);
				return;
			} catch (PartInitException e) {
					// ignore
			}
		}

		// no registered editor - open using browser support
		try {
			URL theURL = new URL(webBrowser.getURL());
			IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
			support.getExternalBrowser().openURL(theURL);
		}
		catch (MalformedURLException | PartInitException e) {
			//TODO handle this
		}
	}
}