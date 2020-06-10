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

	@Override
	public String getLabel() {
		return fLabel;
	}

	@Override
	public ILaunch getLaunch() {
		return fLaunch;
	}

	@Override
	public IStreamsProxy getStreamsProxy() {
		return fProxy;
	}

	@Override
	public void setAttribute(String key, String value) {
		fAttributes.put(key, value);
	}

	@Override
	public String getAttribute(String key) {
		return fAttributes.get(key);
	}

	@Override
	public int getExitValue() {
		return 0;
	}

	@Override
	public boolean canTerminate() {
		return !isCanceled() && !isTerminated();
	}

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

	@Override
	public void terminate() {
		setCanceled(true);
	}

	// IProgressMonitor implemented to support termination.

	@Override
	public void beginTask(String name, int totalWork) {
		if (fMonitor != null) {
			fMonitor.beginTask(name, totalWork);
		}
	}

	@Override
	public void done() {
		if (fMonitor != null) {
			fMonitor.done();
		}
	}

	@Override
	public void internalWorked(double work) {
		if (fMonitor != null) {
			fMonitor.internalWorked(work);
		}
	}

	@Override
	public boolean isCanceled() {
		return fCancelled;
	}

	@Override
	public void setCanceled(boolean value) {
		fCancelled = value;
		if (fMonitor != null) {
			fMonitor.setCanceled(value);
		}
	}

	@Override
	public void setTaskName(String name) {
		if (fMonitor != null) {
			fMonitor.setTaskName(name);
		}
	}

	@Override
	public void subTask(String name) {
		if (fMonitor != null) {
			fMonitor.subTask(name);
		}
	}

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
