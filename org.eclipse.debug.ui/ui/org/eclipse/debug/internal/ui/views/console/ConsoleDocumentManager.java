///*******************************************************************************
// * Copyright (c) 2000, 2004 IBM Corporation and others.
// * All rights reserved. This program and the accompanying materials 
// * are made available under the terms of the Common Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/cpl-v10.html
// * 
// * Contributors:
// *     IBM Corporation - initial API and implementation
// *******************************************************************************/
//package org.eclipse.debug.internal.ui.views.console;
//
//
//import java.text.MessageFormat;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//import org.eclipse.core.runtime.CoreException;
//import org.eclipse.core.runtime.IConfigurationElement;
//import org.eclipse.core.runtime.IExtensionPoint;
//import org.eclipse.core.runtime.Platform;
//import org.eclipse.debug.core.DebugPlugin;
//import org.eclipse.debug.core.ILaunch;
//import org.eclipse.debug.core.ILaunchListener;
//import org.eclipse.debug.core.ILaunchManager;
//import org.eclipse.debug.core.model.IProcess;
//import org.eclipse.debug.internal.ui.DebugUIPlugin;
//import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
//import org.eclipse.debug.ui.IDebugUIConstants;
//import org.eclipse.debug.ui.console.IConsoleColorProvider;
//import org.eclipse.debug.ui.console.IConsoleLineTracker;
//import org.eclipse.jface.text.IDocument;
//import org.eclipse.ui.console.ConsolePlugin;
//import org.eclipse.ui.console.IConsole;
//import org.eclipse.ui.console.IConsoleManager;
//import org.eclipse.ui.texteditor.IDocumentProvider;
//
///**
// * Creates documents for processes as they are registered with a launch.
// * The singleton manager is accessible from the debug UI plugin.
// */
//public class ConsoleDocumentManager implements ILaunchListener {
//	
//	/**
//	 * Console document content provider extensions, keyed by extension id
//	 */
//	private Map fColorProviders;
//	
//	/**
//	 * Console line trackers; keyed by process type to list of trackers (1:N) 
//	 */
//	private Map fLineTrackers;
//	
//	/**
//	 * Default document provider.
//	 */
//	protected IDocumentProvider fDefaultDocumentProvider = null;
//	
//	/**
//	 * Map of processes for a launch to compute removed processes
//	 */
//	private Map fProcesses;
//	
//	/**
//	 * @see ILaunchListener#launchRemoved(ILaunch)
//	 */
//	public void launchRemoved(ILaunch launch) {
//		removeLaunch(launch);
//	}
//	
//	protected void removeLaunch(ILaunch launch) {
//		IProcess[] processes= launch.getProcesses(); 
//		for (int i= 0; i < processes.length; i++) {
//			IProcess iProcess = processes[i];
//			removeProcess(iProcess);
//		}		
//		if (fProcesses != null) {
//			fProcesses.remove(launch);
//		}
//	}
//	
//	/**
//	 * Removes the console and document associated with the given process.
//	 * 
//	 * @param iProcess process to clean up
//	 */
//	private void removeProcess(IProcess iProcess) {
//		IConsole console = getConsole(iProcess);
//		if (console != null) {
//			IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
//			manager.removeConsoles(new IConsole[]{console});
//		}
//		IDocumentProvider provider = getDocumentProvider();
//		provider.disconnect(iProcess);
//	}
//
//	/**
//	 * Returns the console for the given process, or <code>null</code> if none.
//	 * 
//	 * @param process
//	 * @return the console for the given process, or <code>null</code> if none
//	 */
//	public IConsole getConsole(IProcess process) {
//		IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager(); 
//		IConsole[] consoles = manager.getConsoles();
//		for (int i = 0; i < consoles.length; i++) {
//			IConsole console = consoles[i];
//			if (console instanceof ProcessConsole) {
//				ProcessConsole pc = (ProcessConsole)console;
//				if (pc.getProcess().equals(process)) {
//					return pc;
//				}
//			}
//		}
//		return null;
//	}
//
//	/**
//	 * @see ILaunchListener#launchAdded(ILaunch)
//	 */
//	public void launchAdded(ILaunch launch) {
//		launchChanged(launch);
//	}
//
//	/**
//	 * @see ILaunchListener#launchChanged(ILaunch)
//	 */
//	public void launchChanged(final ILaunch launch) {
//		DebugUIPlugin.getStandardDisplay().syncExec(new Runnable () {
//			public void run() {
//				IProcess[] processes= launch.getProcesses();
//				for (int i= 0; i < processes.length; i++) {
//					if (getConsoleDocument(processes[i]) == null) {
//						// create new document
//						IProcess process = processes[i];
//						IDocumentProvider provider = getDocumentProvider();
//						try {
//							provider.connect(process);
//						} catch (CoreException e) {
//						}
//						ProcessConsole pc = new ProcessConsole(process);
//						ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{pc});
//					}
//				}
//				List removed = getRemovedProcesses(launch);
//				if (removed != null) {
//					Iterator iterator = removed.iterator();
//					while (iterator.hasNext()) {
//						IProcess p = (IProcess) iterator.next();
//						removeProcess(p);
//					}
//				}
//			}
//		});
//	}
//	
//	/**
//	 * Returns the document for the process, or <code>null</code>
//	 * if none.
//	 */
//	public IDocument getConsoleDocument(IProcess process) {
//		IDocumentProvider provider = getDocumentProvider();
//		return provider.getDocument(process);
//	} 
//	
//	/**
//	 * Returns the document provider.
//	 * 
//	 * @return document provider
//	 */
//	private IDocumentProvider getDocumentProvider() {
//		if (fDefaultDocumentProvider == null) {
//			fDefaultDocumentProvider = new ConsoleDocumentProvider();
//		}
//		return fDefaultDocumentProvider;
//	}
//		
//	/**
//	 * Called by the debug ui plug-in on startup.
//	 * The console document manager starts listening for
//	 * launches to be registered and initializes if any launches
//	 * already exist.
//	 */
//	public void startup() {
//		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
//		launchManager.addLaunchListener(this);	
//
//		//set up the docs for launches already registered
//		ILaunch[] launches= launchManager.getLaunches();
//		for (int i = 0; i < launches.length; i++) {
//			launchAdded(launches[i]);
//		}
//	}
//	
//	/**
//	 * Called by the debug ui plug-in on shutdown.
//	 * The console document manager de-registers as a 
//	 * launch listener and kills all existing console documents.
//	 */
//	public void shutdown() {
//		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
//		ILaunch[] launches = launchManager.getLaunches();
//		for (int i = 0; i < launches.length; i++) {
//			ILaunch launch = launches[i];
//			removeLaunch(launch);
//		}
//		launchManager.removeLaunchListener(this);
//		if (fProcesses != null) {
//			fProcesses.clear();
//		}
//	}
//	
//	/**
//	 * Notifies the console document manager that system err is about to be written
//	 * to the console. The manager will open the console if the preference is
//	 * set to show the console on system err.
//	 */
//	protected void aboutToWriteSystemErr(IProcess process) {
//		if (DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN_ON_ERR)) {
//			showConsole(process);
//		}
//	}
//	
//	/**
//	 * Notifies the console document manager that system out is about to be written
//	 * to the console. The manager will open the console if the preference is
//	 * set to show the console on system out and the console document being written 
//	 * is associated with the current process.
//	 */	
//	protected void aboutToWriteSystemOut(IProcess process) {
//		if (DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT)) {
//			showConsole(process);
//		}
//	}
//	
//	/**
//	 * Opens the console view. If the view is already open, it is brought to the front.
//	 */
//	protected void showConsole(final IProcess process) {
//		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(getConsole(process));
//	}
//	
//	/**
//	 * Returns a new console document color provider extension for the given
//	 * process type, or <code>null</code> if none.
//	 * 
//	 * @param type corresponds to <code>IProcess.ATTR_PROCESS_TYPE</code>
//	 * @return IConsoleColorProvider
//	 */
//	public IConsoleColorProvider getColorProvider(String type) {
//		if (fColorProviders == null) {
//			fColorProviders = new HashMap();
//			IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.EXTENSION_POINT_CONSOLE_COLOR_PROVIDERS);
//			IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
//			for (int i = 0; i < elements.length; i++) {
//				IConfigurationElement extension = elements[i];
//				fColorProviders.put(extension.getAttributeAsIs("processType"), extension); //$NON-NLS-1$
//			}
//		}
//		IConfigurationElement extension = (IConfigurationElement)fColorProviders.get(type);
//		if (extension != null) {
//			try {
//				Object colorProvider = extension.createExecutableExtension("class"); //$NON-NLS-1$
//				if (colorProvider instanceof IConsoleColorProvider) {
//					return (IConsoleColorProvider)colorProvider;
//				} 
//				DebugUIPlugin.logErrorMessage(MessageFormat.format(ConsoleMessages.getString("ConsoleDocumentManager.1"),new String[]{extension.getDeclaringExtension().getUniqueIdentifier()} )); //$NON-NLS-1$
//			} catch (CoreException e) {
//				DebugUIPlugin.log(e);
//			}
//		}
//		return null;
//	} 
//	
//	/**
//	 * Creates and retuns a new line notifier for the given type of process, or
//	 * <code>null</code> if none. The notifier will be seeded with new console
//	 * line listeners registered for the given process type.
//	 * 
//	 * @param type process type
//	 * @return line notifier or <code>null</code>
//	 */
//	public ConsoleLineNotifier newLineNotifier(String type) {
//		if (fLineTrackers == null) {
//			fLineTrackers = new HashMap();
//			IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.EXTENSION_POINT_CONSOLE_LINE_TRACKERS);
//			IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
//			for (int i = 0; i < elements.length; i++) {
//				IConfigurationElement extension = elements[i];
//				String processType = extension.getAttributeAsIs("processType"); //$NON-NLS-1$
//				List list = (List)fLineTrackers.get(processType);
//				if (list == null) {
//					list = new ArrayList();
//					fLineTrackers.put(processType, list);
//				}
//				list.add(extension);
//			}
//		}
//		List extensions = (List)fLineTrackers.get(type);
//		ConsoleLineNotifier lineNotifier = null;
//		if (extensions != null) {
//			lineNotifier = new ConsoleLineNotifier();
//			Iterator iter = extensions.iterator();
//			while (iter.hasNext()) {
//				IConfigurationElement extension = (IConfigurationElement)iter.next();
//				try {
//					Object tracker = extension.createExecutableExtension("class"); //$NON-NLS-1$
//					if (tracker instanceof IConsoleLineTracker) {
//						lineNotifier.addConsoleListener((IConsoleLineTracker)tracker);
//					} else {
//						DebugUIPlugin.logErrorMessage(MessageFormat.format(ConsoleMessages.getString("ConsoleDocumentManager.2"),new String[]{extension.getDeclaringExtension().getUniqueIdentifier()})); //$NON-NLS-1$
//					}
//				} catch (CoreException e) {
//					DebugUIPlugin.log(e);
//				}
//			}
//		}
//		return lineNotifier;		
//	}
//	
//	/**
//	 * Returns the processes that have been removed from the given
//	 * launch, or <code>null</code> if none.
//	 * 
//	 * @param launch launch that has changed
//	 * @return removed processes or <code>null</code>
//	 */
//	private List getRemovedProcesses(ILaunch launch) {
//		List removed = null;
//		if (fProcesses == null) {
//			fProcesses = new HashMap();
//		}
//		IProcess[] old = (IProcess[]) fProcesses.get(launch);
//		IProcess[] curr = launch.getProcesses();
//		if (old != null) {
//			for (int i = 0; i < old.length; i++) {
//				IProcess process = old[i];
//				if (!contains(curr, process)) {
//					if (removed == null) {
//						removed = new ArrayList();
//					}
//					removed.add(process);
//				}
//			}
//		}
//		// update cache with current processes
//		fProcesses.put(launch, curr);
//		return removed;
//	}
//	
//	/**
//	 * Returns whether the given object is contained in the list.
//	 * 
//	 * @param list list to search
//	 * @param object object to search for
//	 * @return whether the given object is contained in the list
//	 */
//	private boolean contains(Object[] list, Object object) {
//		for (int i = 0; i < list.length; i++) {
//			Object object2 = list[i];
//			if (object2.equals(object)) {
//				return true;
//			}
//		}
//		return false;
//	}
//}
