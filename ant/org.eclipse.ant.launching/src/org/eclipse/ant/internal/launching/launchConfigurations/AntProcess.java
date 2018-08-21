/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.launching.launchConfigurations;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

public class AntProcess extends PlatformObject implements IProcess, IProgressMonitor {

	private AntStreamsProxy fProxy;
	private String fLabel = null;
	private ILaunch fLaunch = null;
	private Map<String, String> fAttributes = null;
	private boolean fTerminated = false;
	private boolean fCancelled = false;
	// progress monitor to delegate or null if none
	private IProgressMonitor fMonitor;

	public AntProcess(String label, ILaunch launch, Map<String, String> attributes) {
		fLabel = label;
		fLaunch = launch;
		if (attributes == null) {
			fAttributes = new HashMap<>();
		} else {
			fAttributes = attributes;
		}
		String captureOutput = launch.getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT);
		if (!("false".equals(captureOutput))) { //$NON-NLS-1$
			fProxy = new AntStreamsProxy();
		}
		launch.addProcess(this);
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getLabel()
	 */
	@Override
	public String getLabel() {
		return fLabel;
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getLaunch()
	 */
	@Override
	public ILaunch getLaunch() {
		return fLaunch;
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getStreamsProxy()
	 */
	@Override
	public IStreamsProxy getStreamsProxy() {
		return fProxy;
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#setAttribute(java.lang.String, java.lang.String)
	 */
	@Override
	public void setAttribute(String key, String value) {
		fAttributes.put(key, value);
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getAttribute(java.lang.String)
	 */
	@Override
	public String getAttribute(String key) {
		return fAttributes.get(key);
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getExitValue()
	 */
	@Override
	public int getExitValue() {
		return 0;
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	@Override
	public boolean canTerminate() {
		return !isCanceled() && !isTerminated();
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	@Override
	public boolean isTerminated() {
		return fTerminated;
	}

	protected void terminated() {
		if (!fTerminated) {
			fTerminated = true;
			if (DebugPlugin.getDefault() != null) {
				DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { new DebugEvent(this, DebugEvent.TERMINATE) });
			}
		}
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	@Override
	public void terminate() {
		setCanceled(true);
	}

	// IProgressMonitor implemented to support termination.

	/**
	 * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
	 */
	@Override
	public void beginTask(String name, int totalWork) {
		if (fMonitor != null) {
			fMonitor.beginTask(name, totalWork);
		}
	}

	/**
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	@Override
	public void done() {
		if (fMonitor != null) {
			fMonitor.done();
		}
	}

	/**
	 * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
	 */
	@Override
	public void internalWorked(double work) {
		if (fMonitor != null) {
			fMonitor.internalWorked(work);
		}
	}

	/**
	 * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
	 */
	@Override
	public boolean isCanceled() {
		return fCancelled;
	}

	/**
	 * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
	 */
	@Override
	public void setCanceled(boolean value) {
		fCancelled = value;
		if (fMonitor != null) {
			fMonitor.setCanceled(value);
		}
	}

	/**
	 * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
	 */
	@Override
	public void setTaskName(String name) {
		if (fMonitor != null) {
			fMonitor.setTaskName(name);
		}
	}

	/**
	 * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
	 */
	@Override
	public void subTask(String name) {
		if (fMonitor != null) {
			fMonitor.subTask(name);
		}
	}

	/**
	 * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
	 */
	@Override
	public void worked(int work) {
		if (fMonitor != null) {
			fMonitor.worked(work);
		}
	}

	/**
	 * Sets a progress monitor to delegate to or <code>null</code> if none.
	 * 
	 * @param monitor
	 *            delegate monitor or <code>null</code>
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		fMonitor = monitor;
	}
}
