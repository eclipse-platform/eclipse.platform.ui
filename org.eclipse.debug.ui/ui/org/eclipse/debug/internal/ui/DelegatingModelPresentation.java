package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.model.IWorkbenchAdapter;

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
		if (item instanceof IDebugElement || item instanceof IMarker || item instanceof IBreakpoint) {
			IDebugModelPresentation lp= getConfiguredPresentation(item);
			if (lp != null) {
				Image image= lp.getImage(item);
				if (image != null) {
					return image;
				}
			}
			// default to show the simple element name
			return getDefaultImage(item);
		} else {
			ImageRegistry iRegistry= DebugUIPlugin.getDefault().getImageRegistry();
			if (item instanceof IProcess) {
				if (((IProcess) item).isTerminated()) {
					return iRegistry.get(IDebugUIConstants.IMG_OBJS_OS_PROCESS_TERMINATED);
				} else {
					return iRegistry.get(IDebugUIConstants.IMG_OBJS_OS_PROCESS);
				}
			} else if (item instanceof ILauncher) {
				return getLauncherImage((ILauncher)item);
			} else if (item instanceof ILaunch) {
				ILaunch launch = (ILaunch) item;
				String mode= launch.getLaunchMode();
				if (mode.equals(ILaunchManager.DEBUG_MODE)) {
					return iRegistry.get(IDebugUIConstants.IMG_ACT_DEBUG);
				} else {
					return iRegistry.get(IDebugUIConstants.IMG_ACT_RUN);
				}
			} else if (item instanceof IAdaptable) {
				IWorkbenchAdapter de= (IWorkbenchAdapter) ((IAdaptable) item).getAdapter(IWorkbenchAdapter.class);
				if (de != null) {
					ImageDescriptor descriptor= de.getImageDescriptor(item);
					if( descriptor != null) {
						return descriptor.createImage();
					}
				}
			}
	
			return null;

		}
	}

	protected Image getLauncherImage(ILauncher launcher) {
		return DebugPluginImages.getImage(launcher.getIdentifier());
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
	 * Returns a default image for the debug element
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

	/**
	 * @see IDebugModelPresentation#getText(Object)
	 */
	public String getText(Object item) {
		boolean displayVariableTypes= showVariableTypeNames();
		if (item instanceof IDebugElement || item instanceof IMarker || item instanceof IBreakpoint) { 
			IDebugModelPresentation lp= getConfiguredPresentation(item);
			if (lp != null) {
				String label= lp.getText(item);
				if (label != null) {
					return label;
				}
			}
			if (item instanceof IExpression) {
				return getExpressionText((IExpression)item);
			}		
			if (item instanceof IVariable) {
				IVariable var= (IVariable) item;
				StringBuffer buf= new StringBuffer();
				try {
					IValue value = var.getValue();
					
					if (displayVariableTypes) {
						buf.append(value.getReferenceTypeName());
						buf.append(' ');
					}
					buf.append(var.getName());
					buf.append(" = "); //$NON-NLS-1$
					buf.append(value.getValueString());
					return buf.toString();
				} catch (DebugException de) {
				}
			}
			// default to show the simple element name
			return getDefaultText(item);
		} else {

			String label= null;
			if (item instanceof IProcess) {
				label= ((IProcess) item).getLabel();
			} else if (item instanceof ILauncher) {
				label = ((ILauncher)item).getLabel();
			} else if (item instanceof ILaunch) {
				label= getLaunchText((ILaunch) item);
			} else if (item instanceof ILaunchConfiguration) {
				return ((ILaunchConfiguration)item).getName();
			} else if (item instanceof ILaunchConfigurationType) {
				return ((ILaunchConfigurationType)item).getName();
			} else {
				label= getDesktopLabel(item);
			}

			if ((item instanceof ITerminate) && ((ITerminate) item).isTerminated()) {
				label= DebugUIMessages.getString("DelegatingModelPresentation.<terminated>__7") + label; //$NON-NLS-1$
			}
			return label;
		}
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
	 * Expressions have their left halves rendered here, and their
	 * right halves rendered by the registered model presentation.
	 */
	protected String getExpressionText(IExpression expression) {
		StringBuffer buffer= new StringBuffer(expression.getExpressionText());
		String valueString= null;
		IDebugModelPresentation lp= getConfiguredPresentation(expression);
		IValue value= expression.getValue();
		if (lp != null) {
			valueString= lp.getText(value);
		} 
		if ((valueString == null) || (valueString.length() < 1)) {
			try {
				valueString= value.getValueString();
			} catch (DebugException de) {
			}
		}
		if (valueString != null && valueString.length() > 0) {
			buffer.append("= "); //$NON-NLS-1$
			buffer.append(valueString);		
		}
		if (showVariableTypeNames()) {
			String typeName = null;
			try {
				typeName = value.getReferenceTypeName();
				buffer.insert(0,' ');
				buffer.insert(0,typeName);
			} catch (DebugException de) {
			}
		}
		return buffer.toString();
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

	public String getDesktopLabel(Object object) {
		if (object instanceof IAdaptable) {
			IWorkbenchAdapter de= (IWorkbenchAdapter) ((IAdaptable) object).getAdapter(IWorkbenchAdapter.class);
			if (de != null) {
				return de.getLabel(object);
			}
		}

		return DebugUIMessages.getString("DelegatingModelPresentation.<unknown>_9"); //$NON-NLS-1$
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
	 * Used to render launch history items in the re-launch drop downs
	 */
	protected String getLaunchText(ILaunch launch) {
		if (launch.getLaunchConfiguration() == null) {
			// old launcher
			StringBuffer buff= new StringBuffer(getDesktopLabel(launch.getElement()));
			buff.append(" ["); //$NON-NLS-1$
			buff.append(getText(launch.getLauncher()));
			buff.append("]"); //$NON-NLS-1$
			return buff.toString();
		} else {
			// new launch configuration
			ILaunchConfiguration config = launch.getLaunchConfiguration();
			StringBuffer buff= new StringBuffer(config.getName());
			buff.append(" ["); //$NON-NLS-1$
			try {
				buff.append(config.getType().getName());
			} catch (CoreException e) {
				//XXX: unknown configuration type
			}
			buff.append("]"); //$NON-NLS-1$
			return buff.toString();			
		}

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

	protected boolean showVariableTypeNames() {
		Boolean show= (Boolean) fAttributes.get(DISPLAY_VARIABLE_TYPE_NAMES);
		show= show == null ? new Boolean(false) : show;
		return show.booleanValue();
	}
	
	protected boolean showQualifiedNames() {
		Boolean show= (Boolean) fAttributes.get(DISPLAY_QUALIFIED_NAMES);
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

