package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IEditorInput;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A proxy to an IDebugModelPresentation extension. Instantiates the extension
 * when it is needed.
 */

public class LazyModelPresentation implements IDebugModelPresentation {
	
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
	/**
	 * Constructs a lazy presentation from the config element.
	 */
	public LazyModelPresentation(IConfigurationElement configElement) {
		fConfig = configElement;
	}

	public Image getImage(Object element) {
		return getPresentation().getImage(element);
	}

	public String getText(Object element) {
		return getPresentation().getText(element);
	}
	
	/**
	 * @see IDebugModelPresentaion
	 */
	public IEditorInput getEditorInput(Object element) {
		return getPresentation().getEditorInput(element);
	}
	
	/**
	 * @see IDebugModelPresentaion
	 */
	public String getEditorId(IEditorInput input, Object inputObject) {
		return getPresentation().getEditorId(input, inputObject);
	}

	public void addListener(ILabelProviderListener listener) {
		if (fPresentation != null) {
			getPresentation().addListener(listener);
		}
		fListeners.add(listener);
	}

	public void dispose() {
		if (fPresentation != null) {
			getPresentation().dispose();
		}
		fListeners = null;
	}

	public boolean isLabelProperty(Object element, String property) {
		if (fPresentation != null) {
			return getPresentation().isLabelProperty(element, property);
		} 
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		if (fPresentation != null) {
			getPresentation().removeListener(listener);
		}
		fListeners.remove(listener);
	}

	/** 
	 * @deprecated
	 */
	public Image getLabelImage(Viewer viewer, Object element) {
		return getPresentation().getImage(element);
	}

	/** 
	 * @deprecated
	 */
	public String getLabelText(Viewer viewer, Object element) {
		return getPresentation().getText(element);
	}
	
	/**
	 * Returns the real presentation, instantiating if required.
	 */
	protected IDebugModelPresentation getPresentation() {
		if (fPresentation == null) {
			try {
				fPresentation= (IDebugModelPresentation) DebugUIPlugin.createExtension(fConfig, "class");
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
				DebugUIUtils.logError(e);
			}
		}
		return fPresentation;
	}

	/**
	 * @see IDebugModelPresentation
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
}
