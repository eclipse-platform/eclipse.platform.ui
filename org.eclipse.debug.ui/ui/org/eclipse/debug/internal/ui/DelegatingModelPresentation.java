package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.*;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
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

	private final static String TOP_PREFIX= "presentation.";
	private final static String BREAKPOINT_LABEL= TOP_PREFIX + "breakpoint_label";
	
	/**
	 * A mapping of attribute ids to their values
	 * @see IDebugModelPresentation#setAttribute
	 */
	protected HashMap fAttributes= new HashMap(3);
	/**
	 * A table of label providers keyed by debug model identifiers.
	 */
	protected HashMap fLabelProviders= new HashMap(5);

	// Resource String keys
	private static final String PREFIX= "label_provider.";
	private static final String TERMINATED= PREFIX + "terminated";
	private static final String UNKNOWN= PREFIX + "unknown";

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
					String id= elt.getAttribute("id");
					if (id != null) {
						IDebugModelPresentation lp= new LazyModelPresentation(elt);
						fLabelProviders.put(id, lp);
					}
				}
			}
		}
	}

	/**
	 * Delegate to all extensions.
	 *
	 * @see ILabelProvider
	 */
	public void addListener(ILabelProviderListener listener) {
		Iterator i= fLabelProviders.values().iterator();
		while (i.hasNext()) {
			((ILabelProvider) i.next()).addListener(listener);
		}
	}

	/**
	 * Delegate to all extensions.
	 *
	 * @see ILabelProvider
	 */
	public void dispose() {
		Iterator i= fLabelProviders.values().iterator();
		while (i.hasNext()) {
			((ILabelProvider) i.next()).dispose();
		}
	}

	/**
	 * Returns an image for the item
	 * Can return <code>null</code>
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
			} else
				if (item instanceof ILauncher) {
					return getLauncherImage((ILauncher)item);
				} else
					if (item instanceof ILaunch) {
						ILaunch launch = (ILaunch) item;
						String mode= launch.getLaunchMode();
						if (mode.equals(ILaunchManager.DEBUG_MODE)) {
							return iRegistry.get(IDebugUIConstants.IMG_ACT_DEBUG);
						} else {
							return iRegistry.get(IDebugUIConstants.IMG_ACT_RUN);
						}
					} else
						if (item instanceof InspectItem) {
							return iRegistry.get(IDebugUIConstants.IMG_OBJS_EXPRESSION);
						} else
							if (item instanceof IAdaptable) {
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
	 * @see IDebugModelPresentation
	 */
	public IEditorInput getEditorInput(Object item) {
		IDebugModelPresentation lp= getConfiguredPresentation(item);
		if (lp != null) {
			return lp.getEditorInput(item);
		}
		return null;
	}
	
	/**
	 * @see IDebugModelPresentation
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
	public String getDefaultText(Object element) {
		if (element instanceof IDebugElement) {
			try {
				switch (((IDebugElement) element).getElementType()) {
					case IDebugElement.DEBUG_TARGET:
						return ((IDebugTarget)element).getName();
					case IDebugElement.THREAD:
						return ((IThread)element).getName();
					case IDebugElement.STACK_FRAME:
						return ((IStackFrame)element).getName();
					case IDebugElement.VARIABLE:
						return ((IVariable)element).getName();
					default:
						return "";
				}
			} catch (DebugException de) {
			}
		} else
			if (element instanceof IMarker) {
				IMarker m= (IMarker) element;
				try {
					if (m.exists() && m.isSubtypeOf(IDebugConstants.BREAKPOINT_MARKER)) {
						return DebugUIUtils.getResourceString(BREAKPOINT_LABEL);
					}
				} catch (CoreException e) {
				}
			}
		return DebugUIUtils.getResourceString(UNKNOWN);
	}

	/**
	 * Returns a default image for the debug element
	 */
	public Image getDefaultImage(Object element) {
		ImageRegistry iRegistry= DebugUIPlugin.getDefault().getImageRegistry();
		if (element instanceof IThread) {
			IThread thread = (IThread)element;
			if (thread.isSuspended()) {
				return iRegistry.get(IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED);
			} else if (thread.isTerminated()) {
				return iRegistry.get(IDebugUIConstants.IMG_OBJS_THREAD_TERMINATED);
			} else {
				return iRegistry.get(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING);
			}
		} else
			if (element instanceof IStackFrame) {
				return iRegistry.get(IDebugUIConstants.IMG_OBJS_STACKFRAME);
			} else
				if (element instanceof IProcess) {
					if (((IProcess) element).isTerminated()) {
						return iRegistry.get(IDebugUIConstants.IMG_OBJS_OS_PROCESS_TERMINATED);
					} else {
						return iRegistry.get(IDebugUIConstants.IMG_OBJS_OS_PROCESS);
					}
				} else
					if (element instanceof IDebugTarget) {
						IDebugTarget target= (IDebugTarget) element;
						if (target.isTerminated() || target.isDisconnected()) {
							return iRegistry.get(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET_TERMINATED);
						} else {
							return iRegistry.get(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET);
						}
					} else
						if (element instanceof IMarker) {
							try {
								IMarker marker= (IMarker) element;
								IBreakpoint breakpoint= DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
								if (breakpoint != null && marker.exists()) {
									if (breakpoint.isEnabled()) {
										return DebugPluginImages.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT);
									} else {
										return DebugPluginImages.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED);
									}
								}
							} catch (CoreException e) {
								DebugUIUtils.logError(e);
							}
						}
		return null;
	}

	/**
	 * Returns a label for the item
	 */
	public String getText(Object item) {
		boolean displayVariableTypes= showVariableTypeNames();
		boolean displayQualifiedNames= showQualifiedNames();
		if (item instanceof InspectItem) {
			return getInspectItemText((InspectItem)item);
		} else if (item instanceof IDebugElement || item instanceof IMarker || item instanceof IBreakpoint) { 
			IDebugModelPresentation lp= getConfiguredPresentation(item);
			if (lp != null) {
				String label= lp.getText(item);
				if (label != null) {
					return label;
				}
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
					buf.append(" = ");
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
			} else
				if (item instanceof ILauncher) {
					label = ((ILauncher)item).getLabel();
				} else
					if (item instanceof ILaunch) {
						label= getLaunchText((ILaunch) item);
					} else if (item instanceof InspectItem) {
						try {
							InspectItem var= (InspectItem) item;
							StringBuffer buf= new StringBuffer();
							buf.append(var.getLabel());
							buf.append(" = ");
							IValue value = var.getValue();
							if (displayVariableTypes) {
								buf.append(value.getReferenceTypeName());
								buf.append(' ');
							}
							buf.append(value.getValueString());
							return buf.toString();
						} catch (DebugException de) {
							return getDefaultText(item);
						}
					} else {
						label= getDesktopLabel(item);
					}

			if ((item instanceof ITerminate) && ((ITerminate) item).isTerminated()) {
				label= DebugUIUtils.getResourceString(TERMINATED) + label;
			}
			return label;
		}
	}

	/**
	 * InspectItems have their left halves rendered here, and their
	 * right halves rendered by the registered model presentation.
	 */
	protected String getInspectItemText(InspectItem inspectItem) {
		StringBuffer buffer= new StringBuffer(inspectItem.getLabel());
		String valueString= null;
		IDebugModelPresentation lp= getConfiguredPresentation(inspectItem);
		IValue value= inspectItem.getValue();
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
			buffer.append("= ");
			buffer.append(valueString);		
		}
		return buffer.toString();
	}

	/**
	 * Delegate to all extensions.
	 *
	 * @see ILabelProvider
	 */
	public void removeListener(ILabelProviderListener listener) {
		Iterator i= fLabelProviders.values().iterator();
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

		return DebugUIUtils.getResourceString(UNKNOWN);
	}

	/**
	 * Delegate to the appropriate label provider.
	 *
	 * @see ILabelProvider
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
		} else if (element instanceof InspectItem) {
			IValue value= ((InspectItem)element).getValue();
			id= value.getModelIdentifier();
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
	protected IDebugModelPresentation getPresentation(String id) {
		IDebugModelPresentation lp= (IDebugModelPresentation) fLabelProviders.get(id);
		if (lp != null) {
			Iterator keys= fAttributes.keySet().iterator();
			while (keys.hasNext()) {
				String key= (String)keys.next();
				lp.setAttribute(key, fAttributes.get(key));
			}
			return lp;
		}
		return null;
	}

	/**
	 * Used to render launch history items in the re-launch drop downs
	 */
	protected String getLaunchText(ILaunch launch) {
		return getDesktopLabel(launch.getElement()) + " [" + getText(launch.getLauncher()) + "]";
	}
	
	/**
	 * @see IDebugModelPresentation
	 */
	public void setAttribute(String id, Object value) {
		if (value == null) {
			return;
		}
		fAttributes.put(id, value);
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
}

