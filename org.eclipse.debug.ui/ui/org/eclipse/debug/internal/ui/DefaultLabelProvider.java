package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class DefaultLabelProvider implements ILabelProvider {

	/**
	 * @see ILabelProvider#getImage(Object)
	 */
	public Image getImage(Object element) {
		String key= getImageKey(element);
		if (key == null && element instanceof IAdaptable) {
			IWorkbenchAdapter de= (IWorkbenchAdapter) ((IAdaptable) element).getAdapter(IWorkbenchAdapter.class);
			if (de != null) {
				ImageDescriptor descriptor= de.getImageDescriptor(element);
				if( descriptor != null) {
					return descriptor.createImage();
				}
			}
			return null;
		}
		return DebugPluginImages.getImage(key);
	}
	
	/**
	 * Returns the key (<code>String</code>) of the default image
	 * appropriate for the given element or <code>null</code>
	 * if no default image is defined.
	 */
	public String getImageKey(Object element) {
		if (element instanceof IDebugElement) {
			// Group elements into debug elements and non-debug elements
			// to reduce the number of instanceof checks performed
			if (element instanceof IStackFrame) {
				if (((IStackFrame)element).getThread().isSuspended()) {
					return IDebugUIConstants.IMG_OBJS_STACKFRAME;
				} else {
					return IDebugUIConstants.IMG_OBJS_STACKFRAME_RUNNING;					
				}
			} 
			else if (element instanceof IThread) {
				IThread thread = (IThread)element;
				if (thread.isSuspended()) {
					return IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED;
				} else if (thread.isTerminated()) {
					return IDebugUIConstants.IMG_OBJS_THREAD_TERMINATED;
				} else {
					return IDebugUIConstants.IMG_OBJS_THREAD_RUNNING;
				}
			} else if (element instanceof IDebugTarget) {
				IDebugTarget target= (IDebugTarget) element;
				if (target.isTerminated() || target.isDisconnected()) {
					return IDebugUIConstants.IMG_OBJS_DEBUG_TARGET_TERMINATED;
				} else {
					return IDebugUIConstants.IMG_OBJS_DEBUG_TARGET;
				}
			} else if (element instanceof IExpression) {
				return IDebugUIConstants.IMG_OBJS_EXPRESSION;
			}
		} else {
			if (element instanceof IMarker) {
				return getMarkerImageKey((IMarker)element);
			} 
			else if (element instanceof IProcess) {
				if (((IProcess) element).isTerminated()) {
					return IDebugUIConstants.IMG_OBJS_OS_PROCESS_TERMINATED;
				} else {
					return IDebugUIConstants.IMG_OBJS_OS_PROCESS;
				}
			} else if (element instanceof ILauncher) {
				return ((ILauncher)element).getIdentifier();
			} else if (element instanceof ILaunch) {
				if (((ILaunch)element).getLaunchMode().equals(ILaunchManager.DEBUG_MODE)) {
					return IDebugUIConstants.IMG_ACT_DEBUG;
				} else {
					return IDebugUIConstants.IMG_ACT_RUN;
				}
			} else if (element instanceof ILaunchConfigurationType) {
				return ((ILaunchConfigurationType)element).getIdentifier();
			} else if (element instanceof ILaunchConfiguration) {
				try {
					return ((ILaunchConfiguration)element).getType().getIdentifier();
				} catch (CoreException e) {
					return null;
				}
			} 
		}
		return null;		
	}

	/**
	 * @see ILabelProvider#getText(Object)
	 */
	public String getText(Object element) {
		StringBuffer label= new StringBuffer();
		try {
			// Group elements into debug elements and non-debug elements
			// to reduce the number of instanceof checks performed
			if (element instanceof IDebugElement) {
				if (element instanceof IStackFrame) {
					label.append(((IStackFrame)element).getName());
				} else if (element instanceof IVariable) {
					label.append(getVariableText((IVariable)element));
				} else if (element instanceof IThread) {
					label.append(((IThread)element).getName());
				} else if (element instanceof IDebugTarget) {
					label.append((((IDebugTarget)element).getName()));
				} else if (element instanceof IExpression) {
					label.append(getExpressionText((IExpression)element));
				} 
			} else {
				if (element instanceof IMarker) {
					label.append(getMarkerText((IMarker) element));
				} else if (element instanceof IProcess) {
					label.append(((IProcess) element).getLabel());
				} else if (element instanceof ILaunch) {
					label.append(getLaunchText((ILaunch) element));
				} else if (element instanceof ILaunchConfiguration) {
					label.append(((ILaunchConfiguration)element).getName());
				} else if (element instanceof ILaunchConfigurationType) {
					label.append(((ILaunchConfigurationType)element).getName());
				} else if (element instanceof ILauncher) {
					label.append(((ILauncher)element).getLabel());
				} else {
					label.append(getAdapterLabel(element));
				}
			}
			if (element instanceof ITerminate) {
				if (((ITerminate) element).isTerminated()) {
					label.insert(0, DebugUIMessages.getString("DefaultLabelProvider.<terminated>_1")); //$NON-NLS-1$
				}
			} else if (element instanceof IDisconnect) {
				if (((IDisconnect) element).isDisconnected()) {
					label.insert(0, DebugUIMessages.getString("DefaultLabelProvider.<disconnected>_1")); //$NON-NLS-1$
				}
			}
		} catch (DebugException e) {
			DebugUIPlugin.logError(e);
			label.append(DebugUIMessages.getString("DefaultLabelProvider.<unknown>_1")); //$NON-NLS-1$
		}
		return label.toString();
	}
	
	public String getAdapterLabel(Object object) {
		if (object instanceof IAdaptable) {
			IWorkbenchAdapter de= (IWorkbenchAdapter) ((IAdaptable) object).getAdapter(IWorkbenchAdapter.class);
			if (de != null) {
				return de.getLabel(object);
			}
		}
		return DebugUIMessages.getString("DefaultLabelProvider.<unknown>_1"); //$NON-NLS-1$
	}
	
	/**
	 * Used to render launch history items in the re-launch drop downs
	 */
	protected String getLaunchText(ILaunch launch) {
		if (launch.getLaunchConfiguration() == null) {
			// old launcher
			StringBuffer buff= new StringBuffer(getAdapterLabel(launch.getElement()));
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

	protected String getExpressionText(IExpression expression) {
		StringBuffer buffer= new StringBuffer(expression.getExpressionText());
		String valueString= null;
		IValue value= expression.getValue();
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
		return buffer.toString();
	}	
	
	protected String getVariableText(IVariable variable) {
		StringBuffer buffer= new StringBuffer();
		try {
			IValue value = variable.getValue();
			
			buffer.append(variable.getName());
			buffer.append(" = "); //$NON-NLS-1$
			buffer.append(value.getValueString());
		} catch (DebugException de) {
		}
		return buffer.toString();
	}
	
	protected String getMarkerText(IMarker marker) {
		try {
			if (marker.exists() && marker.isSubtypeOf(IBreakpoint.BREAKPOINT_MARKER)) {
				return DebugUIMessages.getString("DefaultLabelProvider.Breakpoint_1"); //$NON-NLS-1$
			}
		} catch (CoreException e) {
			DebugUIPlugin.logError(e);
		}
		return ""; //$NON-NLS-1$
	}
	
	protected String getMarkerImageKey(IMarker marker) {
		try {
			IBreakpoint breakpoint= DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
			if (breakpoint != null && marker.exists()) {
				if (breakpoint.isEnabled()) {
					return IDebugUIConstants.IMG_OBJS_BREAKPOINT;
				} else {
					return IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED;
				}
			}
		} catch (CoreException e) {
			DebugUIPlugin.logError(e);
		}
		return null;
	}

	/*
	 * @see IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/*
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/*
	 * @see IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/*
	 * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

}

