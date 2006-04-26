/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.context;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IDropToFrame;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStep;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousDisconnectAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousDropToFrameAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepFiltersAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousSuspendResumeAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousTerminateAdapter;

/**
 * Adapter factory for debug capabilities.
 * 
 * @since 3.2
 *
 */
public class ActionAdapterFactory implements IAdapterFactory {
	
	private static IAsynchronousDisconnectAdapter fgDisconnectAdapter = new DisconnectAdapter();
	private static IAsynchronousDropToFrameAdapter fgDropToFrameAdapter = new DropToFrameAdapter();
	private static IAsynchronousStepAdapter fgStepAdapter = new StepAdapter();
	private static IAsynchronousStepFiltersAdapter fgStepFiltersAdapter = new StepFiltersAdapter();
	private static IAsynchronousSuspendResumeAdapter fgSuspendResumeAdapter = new SuspendResumeAdapter();
	private static IAsynchronousTerminateAdapter fgTerminateAdapter = new TerminateAdapter();

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IAsynchronousDisconnectAdapter.class.equals(adapterType)) {
			if (adaptableObject instanceof IDisconnect) {
				return fgDisconnectAdapter;
			}
		}
		if (IAsynchronousDropToFrameAdapter.class.equals(adapterType)) {
			if (adaptableObject instanceof IDropToFrame) {
				return fgDropToFrameAdapter;
			}
		}
		if (IAsynchronousStepAdapter.class.equals(adapterType)) {
			if (adaptableObject instanceof IStep) {
				return fgStepAdapter;
			}
		}		
		if (IAsynchronousStepFiltersAdapter.class.equals(adapterType)) {
			if (adaptableObject instanceof IDebugElement ||
				adaptableObject instanceof ILaunch || 
				adaptableObject instanceof IProcess) {
				return fgStepFiltersAdapter;
			}
		}
		if (IAsynchronousSuspendResumeAdapter.class.equals(adapterType)) {
			if (adaptableObject instanceof ISuspendResume) {
				return fgSuspendResumeAdapter;
			}
		}	
		if (IAsynchronousTerminateAdapter.class.equals(adapterType)) {
			if (adaptableObject instanceof ITerminate) {
				return fgTerminateAdapter;
			}
		}			
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[]{IAsynchronousDisconnectAdapter.class,
				IAsynchronousDropToFrameAdapter.class,
				IAsynchronousStepAdapter.class,
				IAsynchronousStepFiltersAdapter.class,
				IAsynchronousSuspendResumeAdapter.class,
				IAsynchronousTerminateAdapter.class};
	}

}
