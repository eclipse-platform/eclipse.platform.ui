/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.core.internal.resources;

import java.util.*;
import org.eclipse.core.internal.events.PathVariableChangeEvent;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * Core's implementation of IPathVariableManager. 
 */
public class PathVariableManager implements IPathVariableManager, IManager {

	private Set listeners;
	private Workspace workspace;
	private Preferences preferences;

	static final String VARIABLE_PREFIX = "pathvariable."; //$NON-NLS-1$

	/**
	 * Constructor for the class.
	 */
	public PathVariableManager(Workspace workspace) {
		this.workspace = workspace;
		this.listeners = Collections.synchronizedSet(new HashSet());
		this.preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
	}

	/**
	 * Note that if a user changes the key in the preferences file to be invalid
	 * and then calls #getValue using that key, they will get the value back for
	 * that. But then if they try and call #setValue using the same key it will throw
	 * an exception. We may want to revisit this behaviour in the future.
	 * 
	 * @see org.eclipse.core.resources.IPathVariableManager#getValue
	 */
	public IPath getValue(String varName) {
		String key = getKeyForName(varName);
		String value = preferences.getString(key);
		return value.length() == 0 ? null : new Path(value);
	}

	/**
	 * @see org.eclipse.core.resources.IPathVariableManager#setValue
	 */
	public void setValue(String varName, IPath newValue) throws CoreException {
		checkIsValidName(varName);
		checkIsValidValue(newValue);
		IPath currentValue = getValue(varName);

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
	/**
	 * Throws an exception if the given path is not valid as a path variable
	 * value.
	 */
	private void checkIsValidValue(IPath newValue) throws CoreException {
		if (newValue == null || newValue.isAbsolute())
			return;
		throw new ResourceException(ResourceStatus.INVALID_VALUE, null, Policy.bind("pathvar.invalidValue"), null); //$NON-NLS-1$
	}
	/**
	 * Return a key to use in the Preferences.
	 */
	private String getKeyForName(String varName) {
		return VARIABLE_PREFIX + varName;
	}
	/**
	 * Remove the given variable from the table of path variables.
	 * Also include it in the set of all removed variables to help with
	 * updating the preference store later. Fire the appropriate change
	 * event to notify listeners.
	 */
	private void removeVariable(String varName) {
		preferences.setToDefault(getKeyForName(varName));
		fireVariableChangeEvent(varName, null, IPathVariableChangeEvent.VARIABLE_DELETED);
	}
	/**
	 * Create the given key-value pair in the path variable table. Fire the
	 * appropriate change event to notify listeners. 
	 */
	private void createVariable(String varName, IPath varValue) {
		preferences.setValue(getKeyForName(varName), varValue.toString());
		fireVariableChangeEvent(varName, varValue, IPathVariableChangeEvent.VARIABLE_CREATED);
	}
	/**
	 * Update the given path variable key to be mapped to the given value. Fire
	 * the appropriate change event to notify listeners. 
	 */
	private void updateVariable(String varName, IPath varValue) {
		preferences.setValue(getKeyForName(varName), varValue.toString());
		fireVariableChangeEvent(varName, varValue, IPathVariableChangeEvent.VARIABLE_CHANGED);
	}
	/**
	 * @see org.eclipse.core.resources.IPathVariableManager#resolvePath(IPath)
	 */
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
	 *      in the event object
	 * @param value the current value of the path variable or <code>null</code> if
	 *      the variable was deleted
	 * @param type one of <code>IPathVariableChangeEvent.VARIABLE_CREATED</code>,
	 *      <code>IPathVariableChangeEvent.VARIABLE_CHANGED</code>, or
	 *      <code>IPathVariableChangeEvent.VARIABLE_DELETED</code>
	 * @see IPathVariableChangeEvent
	 * @see IPathVariableChangeEvent.VARIABLE_CREATED
	 * @see IPathVariableChangeEvent.VARIABLE_CHANGED
	 * @see IPathVariableChangeEvent.VARIABLE_DELETED
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
		List result = new LinkedList();
		String[] names = preferences.propertyNames();
		for (int i = 0; i < names.length; i++) {
			if (names[i].startsWith(VARIABLE_PREFIX)) {
				String key = names[i].substring(VARIABLE_PREFIX.length());
				// filter out names which might be valid keys in the preference
				// store but are not valid path variable names. We can get in this
				// state if the user has edited the file on disk.
				//TODO: we may want to look at removing these keys from the
				// preference store as a garbage collection means
				if (validateName(key).isOK())
					result.add(key);
			}
		}
		return (String[]) result.toArray(new String[result.size()]);
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
	public boolean isDefined(String varName) {
		return getValue(varName) != null;
	}

	/**
	 * @see org.eclipse.core.resources.IPathVariableManager#validateName
	 */
	public IStatus validateName(String name) {
		String message = null;
		if (name.length() == 0) {
			message = Policy.bind("pathvar.length"); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		char first = name.charAt(0);
		if (!Character.isLetter(first) && first != '_') {
			message = Policy.bind("pathvar.beginChar", String.valueOf(first)); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		for (int i = 1; i < name.length(); i++) {
			char following = name.charAt(i);
			if (!Character.isLetter(following) && !Character.isDigit(following) && following != '_') {
				message = Policy.bind("pathvar.invalidChar", String.valueOf(following)); //$NON-NLS-1$
				return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
			}
		}
		return ResourceStatus.OK_STATUS;
	}
	/**
	 * Throws a runtime exception if the given name is not valid as a path
	 * variable name.
	 */
	private void checkIsValidName(String name) throws CoreException {
		IStatus status = validateName(name);
		if (!status.isOK())
			throw new CoreException(status);
	}
	/**
	 * @see org.eclipse.core.internal.resources.IManager#startup
	 */
	public void startup(IProgressMonitor monitor) throws CoreException {
		// since we are accessing the preference store directly, we don't
		// need to do any setup here.
	}

	/**
	 * @see org.eclipse.core.internal.resources.IManager#shutdown
	 */
	public void shutdown(IProgressMonitor monitor) throws CoreException {
		// The preferences for this plug-in are saved in the Plugin.shutdown
		// method so we don't have to do it here.
	}
}
