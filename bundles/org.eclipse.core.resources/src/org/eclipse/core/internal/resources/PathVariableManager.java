/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.core.internal.resources;

import java.io.*;
import java.io.File;
import java.util.*;
import org.eclipse.core.internal.events.PathVariableChangeEvent;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class PathVariableManager implements IPathVariableManager,IManager {
    private Map variables;
    
    private Set listeners;
    
    private Workspace workspace;    
    
public PathVariableManager(Workspace workspace) {
	this.workspace = workspace;
	this.variables = new TreeMap();
	this.listeners = Collections.synchronizedSet(new HashSet());
}

/**
 * @see org.eclipse.core.resources.IPathVariableManager#getValue
 */
public IPath getValue(String varName) {
	return (IPath) variables.get(varName);
}

/**
 * @see org.eclipse.core.resources.IPathVariableManager#setValue
 */
public void setValue(String varName, IPath newValue) {
    checkIsValidName(varName);    
    IPath currentValue = (IPath) variables.get(varName); 

	boolean variableExists = currentValue != null;

	if (!variableExists && newValue == null)
		return;

	if (variableExists && currentValue.equals(newValue))
		return;

	if (newValue == null)
		removeVariable(varName);
	else if (variableExists)
		updateVariable(varName, newValue);
	else
		createVariable(varName, newValue);
}

private void removeVariable(String varName) {
	variables.remove(varName);
	fireVariableChangeEvent(varName, null, IPathVariableChangeEvent.VARIABLE_DELETED);
}

private void createVariable(String varName, IPath varValue) {
	variables.put(varName, varValue);
	fireVariableChangeEvent(varName, varValue, IPathVariableChangeEvent.VARIABLE_CREATED);
}

private void updateVariable(String varName, IPath newValue) {
	variables.put(varName, newValue);
	fireVariableChangeEvent(varName, newValue, IPathVariableChangeEvent.VARIABLE_CHANGED);
}
/**
 * @see org.eclipse.core.resources.IPathVariableManager#resolvePath(IPath) */
public IPath resolvePath(IPath path) {
    if (path == null || path.segmentCount() == 0 || path.isAbsolute() || path.getDevice() != null)
        return path;    
    IPath value = getValue(path.segment(0));
    return value == null ? path : value.append(path.removeFirstSegments(1));
}

/**
 * Fires a property change event corresponding to a change to the
 * current value of the variable with the given name.
 *
 * @param name the name of the variable, to be used as the variable
 *  in the event object
 * @param oldValue the old value, or <code>null</code> if the variable was
 * just created
 * @param newValue the new value, or <code>null</code> if the variable was 
 * just deleted
 */
private void fireVariableChangeEvent(String name, IPath value, int type) {

	if (this.listeners.size() == 0)
		return;

	Object[] listeners = this.listeners.toArray();
	PathVariableChangeEvent pve = new PathVariableChangeEvent(this, name, value, type);
	for (int i = 0; i < listeners.length; ++i) {
		IPathVariableChangeListener l = (IPathVariableChangeListener) listeners[i];
		l.pathVariableChanged(pve);
	}
}

/**
 * @see org.eclipse.core.resources.IPathVariableManager#getPathVariableNames()
 */
public synchronized String[] getPathVariableNames() {
	return (String[]) variables.keySet().toArray(new String[variables.size()]);
}

/**
 * @see org.eclipse.core.resources.
 * IPathVariableManager#addChangeListener(IPathVariableChangeListener)
 */
public void addChangeListener(IPathVariableChangeListener listener) {
	listeners.add(listener);
}

/**
 * @see org.eclipse.core.resources.
 * IPathVariableManager#removeChangeListener(IPathVariableChangeListener)
 */
public void removeChangeListener(IPathVariableChangeListener listener) {
	listeners.remove(listener);
}

/**
 * @see org.eclipse.core.resources.IPathVariableManager#isDefined
 */
public boolean isDefined(String name) {
	return variables.containsKey(name);
}

/**
 * @see org.eclipse.core.resources.IPathVariableManager#validateName
 */
public IStatus validateName(String name) {
	if (name.length() == 0)
        return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, "Name must have length > 0.");

	char first = name.charAt(0);
	if (!Character.isLetter(first) && first != '_')
        return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, "Name must begin with a character or underscore.");

	for (int i = 1; i < name.length(); i++) {
		char following = name.charAt(i);
		if (!Character.isLetter(following) && !Character.isDigit(following) && following != '_')
            return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, "Name cannot contain character: " + following + ".");
	}
    return ResourceStatus.OK_STATUS;
}

private void checkIsValidName(String name) {
	if (!validateName(name).isOK())
		throw new IllegalArgumentException();
}

/**
 * @see org.eclipse.core.internal.resources.IManager#startup
 */
public void startup(IProgressMonitor monitor) throws CoreException {
    // load the path variables from disk
    IPath location = workspace.getMetaArea().getPathVariablesLocation();
    File path = location.toFile();
    if (!path.exists())
        return;

    //TODO: safety measures for first release...fix up asap.
    try {
        InputStream input = null;
        boolean exceptionOcurred = false;
        try {
            input = new BufferedInputStream(new FileInputStream(path));
            Properties properties = new Properties();
            properties.load(input);
            //TODO: this will send out notification to all the listeners but that 
            // may not be a problem since this is done in the startup() method 
            // of the resources plug-in and there should not yet be any listeners
            // registered. Should we change this?
            for (Iterator entries = properties.entrySet().iterator(); entries.hasNext();) {
                Map.Entry entry = (Map.Entry) entries.next();
                //TODO: creation of this path might fail. we should warn and continue
                setValue((String) entry.getKey(), new Path((String) entry.getValue()));
            }
        } catch (FileNotFoundException e) {
            // ignore and return
            return;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            exceptionOcurred = true;
            //TODO: report exception
        } finally {
            if (input != null)
                try {
                    input.close();
                } catch (IOException ioe) {
                    if (!exceptionOcurred)
                        ioe.printStackTrace(); //TODO: report exception
                }
        }
    } catch (Exception e) {
        // ignore for now
    }
}

/**
 * @see org.eclipse.core.internal.resources.IManager#shutdown
 */
public void shutdown(IProgressMonitor monitor) throws CoreException {
    // save the path variables to disk
    IPath location = workspace.getMetaArea().getPathVariablesLocation();

    //TODO: safety measures for initial release....fix asap
    try {
    OutputStream output = null;
    boolean exceptionOcurred = false;
    try {
        output = new BufferedOutputStream(new FileOutputStream(location.toFile()));
        Properties properties = new Properties();
        for (Iterator entries = variables.entrySet().iterator(); entries.hasNext();) {
            Map.Entry entry = (Map.Entry) entries.next();
            Object name = entry.getKey();
            IPath value = (IPath) entry.getValue();
            //TODO: should we be calling toString() here instead?
            properties.put(name, value.toOSString());
        }
        properties.store(output, "DO NOT EDIT THIS FILE MANUALLY");
    } catch (IOException ioe) {
        exceptionOcurred = true;
        ioe.printStackTrace();
        //TODO: report exception
    } finally {
        if (output != null)
            try {
                output.close();
            } catch (IOException ioe) {
                if (!exceptionOcurred)
                    ioe.printStackTrace(); //TODO: report exception
            }
    }
    } catch (Exception e) {
        // ignore for now
    }
}
}
