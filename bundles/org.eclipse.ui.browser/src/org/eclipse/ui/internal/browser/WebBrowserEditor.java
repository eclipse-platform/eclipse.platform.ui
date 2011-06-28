/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
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
	protected Image image;

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
	
	/*
	 * Creates the SWT controls for this workbench part.
	 */
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
		
		if (input == null || input.isLocationBarLocal()) {
			cutAction = new TextAction(webBrowser, TextAction.CUT);
			copyAction = new TextAction(webBrowser, TextAction.COPY);
			pasteAction = new TextAction(webBrowser, TextAction.PASTE);
		}
		
		if (!lockName) {
			PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (BrowserViewer.PROPERTY_TITLE.equals(event.getPropertyName())) {
						setPartName((String) event.getNewValue());
					}
				}
			};
			webBrowser.addPropertyChangeListener(propertyChangeListener);
		}
	}
	
	public void dispose() {
		if (image != null && !image.isDisposed())
			image.dispose();
		image = null;

		super.dispose();
		// mark this instance as disposed to avoid stale references
		disposed = true;
	}
	
	public boolean isDisposed() {
		return disposed;
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

	/* (non-Javadoc)
	 * Initializes the editor part with a site and input.
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		Trace.trace(Trace.FINEST, "Opening browser: " + input); //$NON-NLS-1$
		if (input instanceof IPathEditorInput) {
			IPathEditorInput pei = (IPathEditorInput) input;
			IPath path = pei.getPath();
			URL url = null;
			try {
				if (path != null)
					url = path.toFile().toURI().toURL();
				initialURL = url.toExternalForm();
			} catch (Exception e) {
				Trace.trace(Trace.SEVERE, "Error getting URL to file"); //$NON-NLS-1$
			}
			if (webBrowser != null) {
				if (initialURL != null)
					webBrowser.setURL(initialURL);
				site.getWorkbenchWindow().getActivePage().activate(this);
			}
			
			setPartName(path.lastSegment());
			if (url != null)
				setTitleToolTip(url.getFile());

			Image oldImage = image;
			ImageDescriptor id = ImageResource.getImageDescriptor(ImageResource.IMG_INTERNAL_BROWSER);
			image = id.createImage();

			setTitleImage(image);
			if (oldImage != null && !oldImage.isDisposed())
				oldImage.dispose();
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

			Image oldImage = image;
			ImageDescriptor id = wbei.getImageDescriptor();
			image = id.createImage();

			setTitleImage(image);
			if (oldImage != null && !oldImage.isDisposed())
				oldImage.dispose();
		} else {
		    IPathEditorInput pinput = (IPathEditorInput) input.getAdapter(IPathEditorInput.class);
			if (pinput != null) {
				init(site, pinput);
			} else {
			    throw new PartInitException(NLS.bind(Messages.errorInvalidEditorInput, input.getName()));
			}
		}
		
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
	public void setFocus() {
		if (webBrowser != null)
			webBrowser.setFocus();
	}

	/**
	 * Close the editor correctly.
	 */
	public boolean close() {
        final boolean [] result = new boolean[1];
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				result[0] = getEditorSite().getPage().closeEditor(WebBrowserEditor.this, false);
			}
		});
        return result[0];
	}
	
    public IActionBars getActionBars() {
        return getEditorSite().getActionBars();
    }

    public void openInExternalBrowser(String url) {
        final IEditorInput input = getEditorInput();
        final String id = getEditorSite().getId();
        Runnable runnable = new Runnable() {
            public void run() {
                doOpenExternalEditor(id, input);
            }
        };
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
        for (int i = 0; i < editors.length; i++) {
            IEditorDescriptor editor = editors[i];
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
        catch (MalformedURLException e) {
            //TODO handle this
        }
        catch (PartInitException e) {
            //TODO handle this
        }
    }
}