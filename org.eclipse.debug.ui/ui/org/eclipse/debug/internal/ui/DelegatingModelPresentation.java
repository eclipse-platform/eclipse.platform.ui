package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;

/**
 * A model presentation that delegates to the appropriate extension. This
 * presentation contains a table of specialized presentations that are defined
 * as <code>org.eclipse.debug.ui.debugModelPresentations</code> extensions. When
 * asked to render an object from a debug model, this presentation delegates
 * to the extension registered for that debug model. 
 */
public class DelegatingModelPresentation implements IDebugModelPresentation {
	
	/**
	 * A mapping of attribute ids to their values
	 * @see IDebugModelPresentation#setAttribute
	 */
	private HashMap fAttributes= new HashMap(3);
	/**
	 * A table of label providers keyed by debug model identifiers.
	 */
	private HashMap fLabelProviders= new HashMap(5);

	/**
	 * Constructs a new DelegatingLabelProvider that delegates to extensions
	 * of kind <code>org.eclipse.debug.ui.debugLabelProvider</code>
	 */
	public DelegatingModelPresentation() {
		IPluginDescriptor descriptor= DebugUIPlugin.getDefault().getDescriptor();
		IExtensionPoint point= descriptor.getExtensionPoint(IDebugUIConstants.ID_DEBUG_MODEL_PRESENTATION);
		if (point != null) {
			IExtension[] extensions= point.getExtensions();
			for (int i= 0; i < extensions.length; i++) {
				IExtension extension= extensions[i];
				IConfigurationElement[] configElements= extension.getConfigurationElements();
				for (int j= 0; j < configElements.length; j++) {
					IConfigurationElement elt= configElements[j];
					String id= elt.getAttribute("id"); //$NON-NLS-1$
					if (id != null) {
						IDebugModelPresentation lp= new LazyModelPresentation(elt);
						getLabelProviders().put(id, lp);
					}
				}
			}
		}
	}

	/**
	 * Delegate to all extensions.
	 *
	 * @see IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
		Iterator i= getLabelProviders().values().iterator();
		while (i.hasNext()) {
			((ILabelProvider) i.next()).addListener(listener);
		}
	}

	/**
	 * Delegate to all extensions.
	 *
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		Iterator i= getLabelProviders().values().iterator();
		while (i.hasNext()) {
			((ILabelProvider) i.next()).dispose();
		}
	}

	/**
	 * @see IDebugModelPresentation#getImage(Object)
	 */
	public Image getImage(Object item) {
		// Attempt to delegate
		IDebugModelPresentation lp= getConfiguredPresentation(item);
		if (lp != null) {
			Image image= lp.getImage(item);
			if (image != null) {
				return image;
			}
		}
		// If no delegate returned an image, use the default
		return getDefaultImage(item);
	}
	
	/**
	 * @see IDebugModelPresentation#getText(Object)
	 */
	public String getText(Object item) {
		// Attempt to delegate
		IDebugModelPresentation lp= getConfiguredPresentation(item);
		if (lp != null) {
			String label= lp.getText(item);
			if (label != null) {
				return label;
			}
		}
		// If no delegate returned a text label, use the default
		if (showVariableTypeNames()) {
			try {
				if (item instanceof IExpression) {
					return new StringBuffer(((IExpression)item).getValue().getReferenceTypeName()).append(' ').append(getDefaultText(item)).toString(); //$NON-NLS-1$
				} else if (item instanceof IVariable) {
					return new StringBuffer(((IVariable)item).getValue().getReferenceTypeName()).append(' ').append(getDefaultText(item)).toString(); //$NON-NLS-1$
				}
			} catch (DebugException de) {
				DebugUIPlugin.log(de.getStatus());
			}
		}
		return getDefaultText(item);
	}
	
	/**
	 * @see IDebugModelPresentation#getEditorInput(Object)
	 */
	public IEditorInput getEditorInput(Object item) {
		IDebugModelPresentation lp= getConfiguredPresentation(item);
		if (lp != null) {
			return lp.getEditorInput(item);
		}
		return null;
	}
	
	/**
	 * @see IDebugModelPresentation#getEditorId(IEditorInput, Object)
	 */
	public String getEditorId(IEditorInput input, Object objectInput) {
		IDebugModelPresentation lp= getConfiguredPresentation(objectInput);
		if (lp != null) {
			return lp.getEditorId(input, objectInput);
		}
		return null;
	}


	/**
	 * Returns a default text label for the debug element
	 */
	protected String getDefaultText(Object element) {
		return DebugUITools.getDefaultLabelProvider().getText(element);
	}

	/**
	 * Returns a default image for the debug element
	 */
	protected Image getDefaultImage(Object element) {
		return DebugUITools.getDefaultLabelProvider().getImage(element);
	}
	
	/*
	 * @see IDebugModelPresentation#computeDetail(IValue, IValueDetailListener)
	 */
	public void computeDetail(IValue value, IValueDetailListener listener) {
		IDebugModelPresentation lp= getConfiguredPresentation(value);
		if (lp != null) {
			lp.computeDetail(value, listener);			
		} else {
			listener.detailComputed(value, getText(value));
		}
	}	

	/**
	 * Delegate to all extensions.
	 *
	 * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
		Iterator i= getLabelProviders().values().iterator();
		while (i.hasNext()) {
			((ILabelProvider) i.next()).removeListener(listener);
		}
	}

	/**
	 * Delegate to the appropriate label provider.
	 *
	 * @see IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		if (element instanceof IDebugElement) {
			IDebugModelPresentation lp= getConfiguredPresentation((IDebugElement) element);
			if (lp != null) {
				return lp.isLabelProperty(element, property);
			}
		}

		return true;
	}

	/**
	 * Returns a configured model presentation for the given object,
	 * or <code>null</code> if one is not registered.
	 */
	protected IDebugModelPresentation getConfiguredPresentation(Object element) {
		String id= null;
		if (element instanceof IDebugElement) {
			IDebugElement de= (IDebugElement) element;
			id= de.getModelIdentifier();
		} else if (element instanceof IMarker) {
			IMarker m= (IMarker) element;
			IBreakpoint bp = DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(m);
			if (bp != null) {
				id= bp.getModelIdentifier();
			}
		} else if (element instanceof IBreakpoint) {
			id = ((IBreakpoint)element).getModelIdentifier();
		}
		if (id != null) {
			return getPresentation(id);
		}

		return null;
	}
	
	/**
	 * Returns the presentation registered for the given id, or <code>null</code>
	 * of nothing is registered for the id.
	 */
	public IDebugModelPresentation getPresentation(String id) {
		IDebugModelPresentation lp= (IDebugModelPresentation) getLabelProviders().get(id);
		if (lp != null) {
			Iterator keys= getAttributes().keySet().iterator();
			while (keys.hasNext()) {
				String key= (String)keys.next();
				lp.setAttribute(key, getAttributes().get(key));
			}
			return lp;
		}
		return null;
	}
	
	/**
	 * @see IDebugModelPresentation#setAttribute(String, Object)
	 */
	public void setAttribute(String id, Object value) {
		if (value == null) {
			return;
		}
		getAttributes().put(id, value);
	}

	/**
	 * Whether or not to show variable type names.
	 * This option is configured per model presentation.
	 * This allows this option to be set per view, for example.
	 */
	protected boolean showVariableTypeNames() {
		Boolean show= (Boolean) fAttributes.get(DISPLAY_VARIABLE_TYPE_NAMES);
		show= show == null ? new Boolean(false) : show;
		return show.booleanValue();
	}
		
	protected HashMap getAttributes() {
		return fAttributes;
	}

	protected void setAttributes(HashMap attributes) {
		fAttributes = attributes;
	}

	protected HashMap getLabelProviders() {
		return fLabelProviders;
	}

	protected void setLabelProviders(HashMap labelProviders) {
		fLabelProviders = labelProviders;
	}
}

