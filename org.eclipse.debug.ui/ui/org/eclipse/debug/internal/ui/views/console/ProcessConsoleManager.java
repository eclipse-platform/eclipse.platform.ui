/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.console;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.console.ConsoleColorProvider;
import org.eclipse.debug.ui.console.IConsoleColorProvider;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;

import com.ibm.icu.text.MessageFormat;

/**
 * Creates documents for processes as they are registered with a launch.
 * The singleton manager is accessible from the debug UI plugin.
 */
public class ProcessConsoleManager implements ILaunchListener {
    
    /**
     * Console document content provider extensions, keyed by extension id
     */
    private Map fColorProviders;
    
    /**
     * The default color provider. Used if no color provider is contributed
     * for the given process type.
     */
    private IConsoleColorProvider fDefaultColorProvider;
    
    /**
     * Console line trackers; keyed by process type to list of trackers (1:N) 
     */
    private Map fLineTrackers;
    
    /**
     * Map of processes for a launch to compute removed processes
     */
    private Map fProcesses;
    
    
    
    /**
     * @see ILaunchListener#launchRemoved(ILaunch)
     */
    public void launchRemoved(ILaunch launch) {
        removeLaunch(launch);
    }
    
    protected void removeLaunch(ILaunch launch) {
        IProcess[] processes= launch.getProcesses(); 
        for (int i= 0; i < processes.length; i++) {
            IProcess iProcess = processes[i];
            removeProcess(iProcess);
        }		
        if (fProcesses != null) {
            fProcesses.remove(launch);
        }
    }
    
    /**
     * Removes the console and document associated with the given process.
     * 
     * @param iProcess process to clean up
     */
    private void removeProcess(IProcess iProcess) {
        IConsole console = getConsole(iProcess);
        
        if (console != null) {
            IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
            manager.removeConsoles(new IConsole[]{console});
        }
    }
    
    /**
     * Returns the console for the given process, or <code>null</code> if none.
     * 
     * @param process
     * @return the console for the given process, or <code>null</code> if none
     */
    public IConsole getConsole(IProcess process) {
        IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager(); 
        IConsole[] consoles = manager.getConsoles();
        for (int i = 0; i < consoles.length; i++) {
            IConsole console = consoles[i];
            if (console instanceof ProcessConsole) {
                ProcessConsole pc = (ProcessConsole)console;
                if (pc.getProcess().equals(process)) {
                    return pc;
                }
            }
        }
        return null;
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
        IProcess[] processes= launch.getProcesses();
        for (int i= 0; i < processes.length; i++) {
            if (getConsoleDocument(processes[i]) == null) {
                IProcess process = processes[i];
                if (process.getStreamsProxy() == null) {
                    continue;
                }

                //create a new console.
                IConsoleColorProvider colorProvider = getColorProvider(process.getAttribute(IProcess.ATTR_PROCESS_TYPE));
                String encoding = launch.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING);
                ProcessConsole pc = new ProcessConsole(process, colorProvider, encoding);
                pc.setAttribute(IDebugUIConstants.ATTR_CONSOLE_PROCESS, process);

                //add new console to console manager.
                ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{pc});
            }
        }
        List removed = getRemovedProcesses(launch);
        if (removed != null) {
            Iterator iterator = removed.iterator();
            while (iterator.hasNext()) {
                IProcess p = (IProcess) iterator.next();
                removeProcess(p);
            }
        }
    }
    
    /**
     * Returns the document for the process, or <code>null</code>
     * if none.
     */
    public IDocument getConsoleDocument(IProcess process) {
        ProcessConsole console = (ProcessConsole) getConsole(process);
        return (console != null ? console.getDocument() : null);
    } 
    
    /**
     * Called by the debug ui plug-in on startup.
     * The console document manager starts listening for
     * launches to be registered and initializes if any launches
     * already exist.
     */
    public void startup() {
        ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
        launchManager.addLaunchListener(this);	
        
        //set up the docs for launches already registered
        ILaunch[] launches= launchManager.getLaunches();
        for (int i = 0; i < launches.length; i++) {
            launchAdded(launches[i]);
        }
    }
    
    /**
     * Called by the debug ui plug-in on shutdown.
     * The console document manager de-registers as a 
     * launch listener and kills all existing console documents.
     */
    public void shutdown() {
        ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
        ILaunch[] launches = launchManager.getLaunches();
        for (int i = 0; i < launches.length; i++) {
            ILaunch launch = launches[i];
            removeLaunch(launch);
        }
        launchManager.removeLaunchListener(this);
        if (fProcesses != null) {
            fProcesses.clear();
        }
    }
          
    /**
     * Returns a new console document color provider extension for the given
     * process type, or <code>null</code> if none.
     * 
     * @param type corresponds to <code>IProcess.ATTR_PROCESS_TYPE</code>
     * @return IConsoleColorProvider
     */
    public IConsoleColorProvider getColorProvider(String type) {
        if (fColorProviders == null) {
            fColorProviders = new HashMap();
            IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.EXTENSION_POINT_CONSOLE_COLOR_PROVIDERS);
            IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
            for (int i = 0; i < elements.length; i++) {
                IConfigurationElement extension = elements[i];
                fColorProviders.put(extension.getAttribute("processType"), extension); //$NON-NLS-1$
            }
        }
        IConfigurationElement extension = (IConfigurationElement)fColorProviders.get(type);
        if (extension != null) {
            try {
                Object colorProvider = extension.createExecutableExtension("class"); //$NON-NLS-1$
                if (colorProvider instanceof IConsoleColorProvider) {
                    return (IConsoleColorProvider)colorProvider;
                } 
                DebugUIPlugin.logErrorMessage(MessageFormat.format(
                		"Extension {0} must specify an instanceof IConsoleColorProvider for class attribute.", //$NON-NLS-1$
                		new String[]{extension.getDeclaringExtension().getUniqueIdentifier()}));
            } catch (CoreException e) {
                DebugUIPlugin.log(e);
            }
        }
        //no color provider found of specified type, return default color provider.
        if (fDefaultColorProvider == null) {
            fDefaultColorProvider = new ConsoleColorProvider();
        }
        return fDefaultColorProvider;
    } 
    
    /**
     * Returns the Line Trackers for a given process type.
     * @param process The process for which line trackers are required.
     * @return An array of line trackers which match the given process type.
     */
    public IConsoleLineTracker[] getLineTrackers(IProcess process) {
        String type = process.getAttribute(IProcess.ATTR_PROCESS_TYPE);
        
        if (fLineTrackers == null) {
            fLineTrackers = new HashMap();
            IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.EXTENSION_POINT_CONSOLE_LINE_TRACKERS);
            IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
            for (int i = 0; i < elements.length; i++) {
                IConfigurationElement extension = elements[i];
                String processType = extension.getAttribute("processType"); //$NON-NLS-1$
                List list = (List)fLineTrackers.get(processType);
                if (list == null) {
                    list = new ArrayList();
                    fLineTrackers.put(processType, list);
                }
                list.add(extension);
            }
        }
        
        ArrayList trackers = new ArrayList();
        if (type != null) {
            List lineTrackerExtensions = (List) fLineTrackers.get(type);
            if(lineTrackerExtensions != null) {   
                for(Iterator i = lineTrackerExtensions.iterator(); i.hasNext(); ) {
                    IConfigurationElement element = (IConfigurationElement) i.next();
                    try {
                        trackers.add(element.createExecutableExtension("class")); //$NON-NLS-1$
                    } catch (CoreException e) {
                        DebugUIPlugin.log(e);
                    }
                }
            }
        }
        return (IConsoleLineTracker[]) trackers.toArray(new IConsoleLineTracker[0]);
    }
    
    /**
     * Returns the processes that have been removed from the given
     * launch, or <code>null</code> if none.
     * 
     * @param launch launch that has changed
     * @return removed processes or <code>null</code>
     */
    private List getRemovedProcesses(ILaunch launch) {
        List removed = null;
        if (fProcesses == null) {
            fProcesses = new HashMap();
        }
        IProcess[] old = (IProcess[]) fProcesses.get(launch);
        IProcess[] curr = launch.getProcesses();
        if (old != null) {
            for (int i = 0; i < old.length; i++) {
                IProcess process = old[i];
                if (!contains(curr, process)) {
                    if (removed == null) {
                        removed = new ArrayList();
                    }
                    removed.add(process);
                }
            }
        }
        // update cache with current processes
        fProcesses.put(launch, curr);
        return removed;
    }
    
    /**
     * Returns whether the given object is contained in the list.
     * 
     * @param list list to search
     * @param object object to search for
     * @return whether the given object is contained in the list
     */
    private boolean contains(Object[] list, Object object) {
        for (int i = 0; i < list.length; i++) {
            Object object2 = list[i];
            if (object2.equals(object)) {
                return true;
            }
        }
        return false;
    }
}
