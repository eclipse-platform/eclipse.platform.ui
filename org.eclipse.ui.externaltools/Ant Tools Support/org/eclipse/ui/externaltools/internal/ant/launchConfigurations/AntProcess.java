package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * 
 */
public class AntProcess implements IProcess {
	
	/**
	 * Process attribute with process identifier - links the ant process build
	 * logger to a process.
	 */
	public static final String ATTR_ANT_PROCESS_ID = IExternalToolConstants.PLUGIN_ID + ".ATTR_ANT_PROCESS_ID";
	
	private AntStreamsProxy fProxy = new AntStreamsProxy();
	private String fLabel = null;
	private ILaunch fLaunch = null;
	private Map fAttributes = null;
	private boolean fTerminated = false;
	private IConsole fConsole = null;
	
	public AntProcess(String label, ILaunch launch, Map attributes) {
		fLabel = label;
		fLaunch = launch;
		if (attributes == null) {
			fAttributes = new HashMap();
		} else {
			fAttributes = attributes;
		}
		launch.addProcess(this);
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getLabel()
	 */
	public String getLabel() {
		return fLabel;
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getLaunch()
	 */
	public ILaunch getLaunch() {
		return fLaunch;
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getStreamsProxy()
	 */
	public IStreamsProxy getStreamsProxy() {
		return fProxy;
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#setAttribute(java.lang.String, java.lang.String)
	 */
	public void setAttribute(String key, String value) {
		fAttributes.put(key, value);
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getAttribute(java.lang.String)
	 */
	public String getAttribute(String key) {
		return (String)fAttributes.get(key);
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getExitValue()
	 */
	public int getExitValue() throws DebugException {
		return 0;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return false;
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return fTerminated;
	}
	
	protected void terminated() {
		if (!fTerminated) {
			fTerminated = true;
			DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {new DebugEvent(this, DebugEvent.TERMINATE)});
		}
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
	}

	/**
	 * Returns the console associated with this process, or <code>null</code> if
	 * none.
	 * 
	 * @return console, or <code>null</code>
	 */
	public IConsole getConsole() {
		return fConsole;
	}
	
	/**
	 * Sets the console associated with this process.
	 * 
	 * @param console	 */
	protected void setConsole(IConsole console) {
		fConsole = console;
	}
}

