package org.eclipse.debug.internal.ui.views.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

public class ConsoleDocumentManager implements ILaunchListener {

	private static ConsoleDocumentManager fgConsoleDocumentManager= null;
	
	/**
	 * The process that is/can provide output to the console
	 * view.
	 */
	private IProcess fCurrentProcess= null;
	
	/**
	 * The mappings of processes to their console documents.
	 */
	protected Map fConsoleDocuments= new HashMap(3);
	
	public static ConsoleDocumentManager getDefault() {
		if (fgConsoleDocumentManager == null) {
			fgConsoleDocumentManager= new ConsoleDocumentManager();
		}	
		return fgConsoleDocumentManager;
	}
	
	/**
	 * Returns whether the singleton instance of the manager exists
	 */
	public static boolean defaultExists() {
		return fgConsoleDocumentManager != null;
	}
	
	private ConsoleDocumentManager() {
		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		launchManager.addLaunchListener(this);	

		//set up the docs for launches already registered
		ILaunch[] launches= launchManager.getLaunches();
		for (int i = 0; i < launches.length; i++) {
			launchAdded(launches[i]);
		}
	}
	
	/**
	 * @see ILaunchListener#launchRemoved(ILaunch)
	 */
	public void launchRemoved(final ILaunch launch) {
		DebugUIPlugin.getDefault().getStandardDisplay().syncExec(new Runnable () {
			public void run() {
				IProcess currentProcess= getCurrentProcess();
				IProcess[] processes= launch.getProcesses();
				for (int i= 0; i < processes.length; i++) {
					IProcess iProcess = processes[i];
					ConsoleDocument doc= (ConsoleDocument)getConsoleDocument(iProcess);
					if (doc != null) {
						doc.close();
						setConsoleDocument(processes[i], null);
					}
					if (iProcess.equals(currentProcess)) {
						fCurrentProcess= null;
					}
				}
			}
		});
	}

	/**
	 * @see ILaunchListener#launchAdded(ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
		launchChanged(launch);
	}

	/**
	 * @see ILaunchListener#launchChanged(ILaunch)
	 */
	public void launchChanged(final ILaunch launch) {
		IProcess newProcess= null;
		IDebugTarget target= launch.getDebugTarget();
		if (target != null) {
			newProcess= target.getProcess();
		} else {
			IProcess[] processes= launch.getProcesses();
			if (processes.length > 0) {
				newProcess= processes[processes.length - 1];
			}
		}
		setCurrentProcess(newProcess);
				
		DebugUIPlugin.getDefault().getStandardDisplay().syncExec(new Runnable () {
			public void run() {
				IProcess[] processes= launch.getProcesses();
				for (int i= 0; i < processes.length; i++) {
					if (getConsoleDocument(processes[i]) == null) {
						ConsoleDocument doc= new ConsoleDocument(processes[i]);
						doc.startReading();
						setConsoleDocument(processes[i], doc);
					}
				}
				
				notifyConsoleViews();
			}
		});
		
		
	}

	/**
	 * Notify all existing console views of the current process.
	 * Must be called in the UI thread.
	 */
	private void notifyConsoleViews() {		
		IWorkbenchWindow[] windows= DebugUIPlugin.getActiveWorkbenchWindow().getWorkbench().getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchWindow iWorkbenchWindow = windows[i];
			IWorkbenchPage[] pages= iWorkbenchWindow.getPages();
			for (int j = 0; j < pages.length; j++) {
				IWorkbenchPage iWorkbenchPage = pages[j];
				IViewPart part= iWorkbenchPage.findView(IDebugUIConstants.ID_CONSOLE_VIEW);
				if (part instanceof ConsoleView) {
					ConsoleView view= (ConsoleView)part;
					view.setViewerInputFromConsoleDocumentManager(getCurrentProcess());
				}
			}
		}
	}
	
	protected IProcess getCurrentProcess() {
		return fCurrentProcess;
	}

	protected void setCurrentProcess(IProcess currentProcess) {
		fCurrentProcess = currentProcess;
	}
	
	/**
	 * Returns the document for the process, or <code>null</code>
	 * if none.
	 */
	public IDocument getConsoleDocument(IProcess process) {
		return (IDocument) fConsoleDocuments.get(process);
	}
	
	/**
	 * Sets the console document for the specified process.
	 * If the document is <code>null</code> the mapping for the
	 * process is removed.
	 */
	protected void setConsoleDocument(IProcess process, IDocument doc) {
		if (doc == null) {
			fConsoleDocuments.remove(process);
		} else {
			fConsoleDocuments.put(process, doc);
		}
	}
	
	public void shutdown() throws CoreException {
		Iterator docs= fConsoleDocuments.values().iterator();
		while (docs.hasNext()) {
			ConsoleDocument doc= (ConsoleDocument)docs.next();
			doc.kill();
		}
		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		launchManager.removeLaunchListener(this);
	}
}
