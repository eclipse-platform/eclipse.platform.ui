package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class DefaultLabelProvider implements ILabelProvider {

	/**
	 * @see ILabelProvider#getImage(Object)
	 */
	public Image getImage(Object element) {
		String key= getDefaultImageKey(element);
		if (key == null) {
			return null;
		}
		return DebugPluginImages.getImage(key);
	}
	
	/**
	 * Returns the key (<code>String</code>) of the default image
	 * appropriate for the given element or <code>null</code>
	 * if no default image is defined.
	 */
	public String getDefaultImageKey(Object element) {
		if (element instanceof IThread) {
			IThread thread = (IThread)element;
			if (thread.isSuspended()) {
				return IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED;
			} else if (thread.isTerminated()) {
				return IDebugUIConstants.IMG_OBJS_THREAD_TERMINATED;
			} else {
				return IDebugUIConstants.IMG_OBJS_THREAD_RUNNING;
			}
		} else
			if (element instanceof IStackFrame) {
				IThread thread = ((IStackFrame)element).getThread();
				if (thread.isSuspended()) {
					return IDebugUIConstants.IMG_OBJS_STACKFRAME;
				} else {
					return IDebugUIConstants.IMG_OBJS_STACKFRAME_RUNNING;					
				}
			} else
				if (element instanceof IProcess) {
					if (((IProcess) element).isTerminated()) {
						return IDebugUIConstants.IMG_OBJS_OS_PROCESS_TERMINATED;
					} else {
						return IDebugUIConstants.IMG_OBJS_OS_PROCESS;
					}
				} else
					if (element instanceof IDebugTarget) {
						IDebugTarget target= (IDebugTarget) element;
						if (target.isTerminated() || target.isDisconnected()) {
							return IDebugUIConstants.IMG_OBJS_DEBUG_TARGET_TERMINATED;
						} else {
							return IDebugUIConstants.IMG_OBJS_DEBUG_TARGET;
						}
					} else
						if (element instanceof IMarker) {
							try {
								IMarker marker= (IMarker) element;
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
						}
		return null;		
	}

	/**
	 * @see ILabelProvider#getText(Object)
	 */
	public String getText(Object element) {
		try {
			if (element instanceof IDebugTarget) {
				return ((IDebugTarget)element).getName();
			} else if (element instanceof IThread) {
				return ((IThread)element).getName();
			} else if (element instanceof IStackFrame) {
				return ((IStackFrame)element).getName();
			} else if (element instanceof IVariable) {
				return ((IVariable)element).getName();
			} else if (element instanceof IMarker) {
				IMarker m= (IMarker) element;
				try {
					if (m.exists() && m.isSubtypeOf(IBreakpoint.BREAKPOINT_MARKER)) {
						return DebugUIMessages.getString("DelegatingModelPresentation.Breakpoint_3"); //$NON-NLS-1$
					}
				} catch (CoreException e) {
					DebugUIPlugin.logError(e);
				}
			}
		} catch (DebugException e) {
			DebugUIPlugin.logError(e);
		}
		
		return DebugUIMessages.getString("DelegatingModelPresentation.<unknown>_4"); //$NON-NLS-1$
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

