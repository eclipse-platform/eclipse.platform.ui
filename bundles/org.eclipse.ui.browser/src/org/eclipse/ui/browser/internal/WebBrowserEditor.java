/*******************************************************************************
 * Copyright (c) 203, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.browser.internal;

import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
/**
 * An integrated Web browser, defined as an editor to make
 * better use of the desktop.
 */
public class WebBrowserEditor extends EditorPart {
	public static final String WEB_BROWSER_EDITOR_ID = "org.eclipse.ui.browser";

	protected BrowserViewer webBrowser;
	protected String initialURL;
	protected Image image;

	protected TextAction cutAction;
	protected TextAction copyAction;
	protected TextAction pasteAction;
	
	protected IResourceChangeListener resourceListener;

	/**
	 * WebBrowserEditor constructor comment.
	 */
	public WebBrowserEditor() {
		super();
	}
	
	/*
	 * Creates the SWT controls for this workbench part.
	 */
	public void createPartControl(Composite parent) {
		WebBrowserEditorInput input = getWebBrowserEditorInput();
		
		int style = 0;
		if (input.isLocationBarLocal()) {
			cutAction = new TextAction(webBrowser, TextAction.CUT);
			copyAction = new TextAction(webBrowser, TextAction.COPY);
			pasteAction = new TextAction(webBrowser, TextAction.PASTE);
			style += BrowserViewer.LOCATION_BAR;
		}
		if (input.isToolbarLocal()) {
			style += BrowserViewer.BUTTON_BAR;
		}
		webBrowser = new BrowserViewer(parent, style);
		
		webBrowser.setURL(initialURL);
		webBrowser.editor = this;
	}
	
	public void dispose() {
		if (image != null && !image.isDisposed())
			image.dispose();
		image = null;

		if (resourceListener != null)
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
		
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * Saves the contents of this editor.
	 */
	public void doSave(IProgressMonitor monitor) {
		// do nothing
	}

	/* (non-Javadoc)
	 * Saves the contents of this editor to another object.
	 */
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
	 * Returns the web editor input, if available.
	 *
	 * @return org.eclipse.ui.browser.internal.IWebBrowserEditorInput
	 */
	protected WebBrowserEditorInput getWebBrowserEditorInput() {
		IEditorInput input = getEditorInput();
		if (input instanceof WebBrowserEditorInput)
			return (WebBrowserEditorInput) input;
		throw new RuntimeException("Incorrect editor input");
	}

	/**
	 * Returns the paste action.
	 *
	 * @return org.eclipse.jface.action.IAction
	 */
	public IAction getPasteAction() {
		return pasteAction;
	}

	/* (non-Javadoc)
	 * Sets the cursor and selection state for this editor to the passage defined
	 * by the given marker.
	 */
	public void gotoMarker(IMarker marker) {
		// do nothing
	}
	
	/* (non-Javadoc)
	 * Initializes the editor part with a site and input.
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		Trace.trace(Trace.FINEST, "Opening browser: " + input);
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fei = (IFileEditorInput) input;
			IFile file = fei.getFile();
			URL url = null;
			try {
				if (file != null && file.exists())
					url = file.getLocation().toFile().toURL();
			} catch (Exception e) {
				Trace.trace(Trace.SEVERE, "Error getting URL to file");
			}
			addResourceListener(file);
			input = new WebBrowserEditorInput(url, 0);
		}
		if (input instanceof WebBrowserEditorInput) {
			WebBrowserEditorInput wbei = (WebBrowserEditorInput) input;
			initialURL = null;
			if (wbei.getURL() != null)
				initialURL = wbei.getURL().toExternalForm();
			if (webBrowser != null) {
				webBrowser.setURL(initialURL);
				site.getWorkbenchWindow().getActivePage().bringToTop(this);
			}
	
			setPartName(wbei.getName());
			setTitleToolTip(wbei.getToolTipText());

			Image oldImage = image;
			ImageDescriptor id = wbei.getImageDescriptor();
			image = id.createImage();

			setTitleImage(image);
			if (oldImage != null && !oldImage.isDisposed())
				oldImage.dispose();
		} else
			throw new PartInitException(WebBrowserUIPlugin.getResource("%errorInvalidEditorInput", input.getName()));
		
		setSite(site);
		setInput(input);
	}
	
	/* (non-Javadoc)
	 * Returns whether the contents of this editor have changed since the last save
	 * operation.
	 */
	public boolean isDirty() {
		return false;
	}
	
	/* (non-Javadoc)
	 * Returns whether the "save as" operation is supported by this editor.
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Open the input in the internal Web browser.
	 */
	public static void open(WebBrowserEditorInput input) {
		IWorkbenchWindow workbenchWindow = WebBrowserUIPlugin.getInstance().getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();

		try {
			IEditorReference[] editors = page.getEditorReferences();
			int size = editors.length;
			for (int i = 0; i < size; i++) {
				if (WEB_BROWSER_EDITOR_ID.equals(editors[i].getId())) {
					IEditorPart editor = editors[i].getEditor(true);
					if (editor != null && editor instanceof WebBrowserEditor) {
						WebBrowserEditor webEditor = (WebBrowserEditor) editor;
						if (input.canReplaceInput(webEditor.getWebBrowserEditorInput())) {
							editor.init(editor.getEditorSite(), input);
							return;
						}
					}
				}
			}
			
			page.openEditor(input, WebBrowserEditor.WEB_BROWSER_EDITOR_ID);
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error opening Web browser", e);
		}
	}
	
	/*
	 * Asks this part to take focus within the workbench.
	 */
	public void setFocus() {
		if (webBrowser != null)
			webBrowser.setFocus();
	}

	/**
	 * Close the editor correctly.
	 */
	public void closeEditor() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				getEditorSite().getPage().closeEditor(WebBrowserEditor.this, false);
			}
		});
	}
	
	/**
	 * Adds a resource change listener to see if the file is deleted.
	 */
	protected void addResourceListener(final IResource resource) {
		if (resource == null)
			return;
	
		resourceListener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				try {
					event.getDelta().accept(new IResourceDeltaVisitor() {
						public boolean visit(IResourceDelta delta) {
							IResource res = delta.getResource();
														
							if (res == null || !res.equals(resource))
								return true;

							if (delta.getKind() != IResourceDelta.REMOVED)
								return true;
							
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									String title = WebBrowserUIPlugin.getResource("%dialogResourceDeletedTitle");
									String message = WebBrowserUIPlugin.getResource("%dialogResourceDeletedMessage", resource.getName());
									String[] labels = new String[] {WebBrowserUIPlugin.getResource("%dialogResourceDeletedIgnore"), IDialogConstants.CLOSE_LABEL};
									MessageDialog dialog = new MessageDialog(getEditorSite().getShell(), title, null, message, MessageDialog.INFORMATION, labels, 0);

									if (dialog.open() != 0)
										closeEditor();
								}
							});
							return false;
						}
					});
				} catch (Exception e) {
					Trace.trace(Trace.SEVERE, "Error listening for resource deletion", e);
				}
			}
		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener);
	}
}