/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.core.ListenerList;
import org.eclipse.debug.ui.IDebugEditorPresentation;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * A proxy to an IDebugModelPresentation extension. Instantiates the extension
 * when it is needed.
 */

public class LazyModelPresentation implements IDebugModelPresentation, IDebugEditorPresentation {
	
	/**
	 * A temporary mapping of attribute ids to their values
	 * @see IDebugModelPresentation#setAttribute
	 */
	protected HashMap fAttributes= new HashMap(3);

	/**
	 * The config element that defines the extension
	 */
	protected IConfigurationElement fConfig = null;
	
	/**
	 * The actual presentation instance - null until called upon
	 */
	protected IDebugModelPresentation fPresentation = null;
	
	/**
	 * Temp holding for listeners - we do not add to presentation until
	 * it needs to be instantiated.
	 */
	protected ListenerList fListeners= new ListenerList(5);	
		
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugEditorPresentation#removeAnntations(org.eclipse.ui.IEditorPart, org.eclipse.debug.core.model.IThread)
	 */
	public void removeAnnotations(IEditorPart editorPart, IThread thread) {
		IDebugModelPresentation presentation = getPresentation();
		if (presentation instanceof IDebugEditorPresentation) {
			((IDebugEditorPresentation)presentation).removeAnnotations(editorPart, thread);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugEditorPresentation#addAnnotations(org.eclipse.ui.IEditorPart, org.eclipse.debug.core.model.IStackFrame)
	 */
	public boolean addAnnotations(IEditorPart editorPart, IStackFrame frame) {
		IDebugModelPresentation presentation = getPresentation();
		if (presentation instanceof IDebugEditorPresentation) {
			return ((IDebugEditorPresentation)presentation).addAnnotations(editorPart, frame);
		}
		return false;
	}

	/**
	 * Constructs a lazy presentation from the config element.
	 */
	public LazyModelPresentation(IConfigurationElement configElement) {
		fConfig = configElement;
	}

	/**
	 * @see IDebugModelPresentation#getImage(Object)
	 */
	public Image getImage(Object element) {
		return getPresentation().getImage(element);
	}

	/**
	 * @see IDebugModelPresentation#getText(Object)
	 */
	public String getText(Object element) {
		return getPresentation().getText(element);
	}
	
	/**
	 * @see IDebugModelPresentation#computeDetail(IValue, IValueDetailListener)
	 */
	public void computeDetail(IValue value, IValueDetailListener listener) {
		getPresentation().computeDetail(value, listener);
	}	
	
	/**
	 * @see ISourcePresentation#getEditorInput(Object)
	 */
	public IEditorInput getEditorInput(Object element) {
		return getPresentation().getEditorInput(element);
	}
	
	/**
	 * @see ISourcePresentation#getEditorId(IEditorInput, Object)
	 */
	public String getEditorId(IEditorInput input, Object inputObject) {
		return getPresentation().getEditorId(input, inputObject);
	}

	/**
	 * @see IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
		if (fPresentation != null) {
			getPresentation().addListener(listener);
		}
		fListeners.add(listener);
	}

	/**
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		if (fPresentation != null) {
			getPresentation().dispose();
		}
		fListeners = null;
	}

	/**
	 * @see IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		if (fPresentation != null) {
			return getPresentation().isLabelProperty(element, property);
		} 
		return false;
	}

	/**
	 * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
		if (fPresentation != null) {
			getPresentation().removeListener(listener);
		}
		fListeners.remove(listener);
	}
	
	/**
	 * Returns the real presentation, instantiating if required.
	 */
	protected IDebugModelPresentation getPresentation() {
		if (fPresentation == null) {
			try {
				fPresentation= (IDebugModelPresentation) DebugUIPlugin.createExtension(fConfig, "class"); //$NON-NLS-1$
				// configure it
				if (fListeners != null) {
					Object[] list = fListeners.getListeners();
					for (int i= 0; i < list.length; i++) {
						fPresentation.addListener((ILabelProviderListener)list[i]);
					}
				}
				Iterator keys= fAttributes.keySet().iterator();
				while (keys.hasNext()) {
					String key= (String)keys.next();
					fPresentation.setAttribute(key, fAttributes.get(key));
				}
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		return fPresentation;
	}

	/**
	 * @see IDebugModelPresentation#setAttribute(String, Object)
	 */
	public void setAttribute(String id, Object value) {
		if (value == null) {
			return;
		}
		if (fPresentation != null) {
			getPresentation().setAttribute(id, value);
		}

		fAttributes.put(id, value);
	}
	
	/**
	 * Returns the identifier of the debug model this
	 * presentation is registered for.
	 */
	public String getDebugModelIdentifier() {
		return fConfig.getAttribute("id"); //$NON-NLS-1$
	}
	
	/**
	 * Returns a new source viewer configuration for the details
	 * area of the variables view, or <code>null</code> if
	 * unspecified.
	 * 
	 * @return source viewer configuration or <code>null</code>
	 * @exception CoreException if unable to create the specified
	 * 	source viewer configuration
	 */
	public SourceViewerConfiguration newDetailsViewerConfiguration() throws CoreException {
		String attr  = fConfig.getAttribute("detailsViewerConfiguration"); //$NON-NLS-1$
		if (attr != null) {
			return (SourceViewerConfiguration)fConfig.createExecutableExtension("detailsViewerConfiguration"); //$NON-NLS-1$
		}
		return null;
	}
	
	/**
	 * Returns a copy of the attributes in this model presentation.
	 * 
	 * @return a copy of the attributes in this model presentation
	 * @since 3.0
	 */
	public Map getAttributeMap() {
		return (Map) fAttributes.clone();
	}
}
