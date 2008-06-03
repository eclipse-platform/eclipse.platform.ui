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
package org.eclipse.core.internal.resources;

import java.net.URI;
import java.util.*;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.internal.events.PathVariableChangeEvent;
import org.eclipse.core.internal.utils.FileUtil;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

/**
 * Core's implementation of IPathVariableManager. 
 */
public class PathVariableManager implements IPathVariableManager, IManager {

	static final String VARIABLE_PREFIX = "pathvariable."; //$NON-NLS-1$
	private Set listeners;

	private Preferences preferences;

	/**
	 * Constructor for the class.
	 */
	public PathVariableManager() {
		this.listeners = Collections.synchronizedSet(new HashSet());
		this.preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
	}

	/**
	 * @see org.eclipse.core.resources.
	 * IPathVariableManager#addChangeListener(IPathVariableChangeListener)
	 */
	public void addChangeListener(IPathVariableChangeListener listener) {
		listeners.add(listener);
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
	 * Throws an exception if the given path is not valid as a path variable
	 * value.
	 */
	private void checkIsValidValue(IPath newValue) throws CoreException {
		IStatus status = validateValue(newValue);
		if (!status.isOK())
			throw new CoreException(status);
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
	 * @see IPathVariableChangeEvent#VARIABLE_CREATED
	 * @see IPathVariableChangeEvent#VARIABLE_CHANGED
	 * @see IPathVariableChangeEvent#VARIABLE_DELETED
	 */
	private void fireVariableChangeEvent(String name, IPath value, int type) {
		if (this.listeners.size() == 0)
			return;
		// use a separate collection to avoid interference of simultaneous additions/removals 
		Object[] listenerArray = this.listeners.toArray();
		final PathVariableChangeEvent pve = new PathVariableChangeEvent(this, name, value, type);
		for (int i = 0; i < listenerArray.length; ++i) {
			final IPathVariableChangeListener l = (IPathVariableChangeListener) listenerArray[i];
			ISafeRunnable job = new ISafeRunnable() {
				public void handleException(Throwable exception) {
					// already being logged in SafeRunner#run()
				}

				public void run() throws Exception {
					l.pathVariableChanged(pve);
				}
			};
			SafeRunner.run(job);
		}
	}

	/**
	 * Return a key to use in the Preferences.
	 */
	private String getKeyForName(String varName) {
		return VARIABLE_PREFIX + varName;
	}

	/**
	 * @see org.eclipse.core.resources.IPathVariableManager#getPathVariableNames()
	 */
	public String[] getPathVariableNames() {
		List result = new LinkedList();
		String[] names = preferences.propertyNames();
		for (int i = 0; i < names.length; i++) {
			if (names[i].startsWith(VARIABLE_PREFIX)) {
				String key = names[i].substring(VARIABLE_PREFIX.length());
				// filter out names for preferences which might be valid in the 
				// preference store but does not have valid path variable names
				// and/or values. We can get in this state if the user has 
				// edited the file on disk or set a preference using the prefix 
				// reserved to path variables (#VARIABLE_PREFIX).
				// TODO: we may want to look at removing these keys from the
				// preference store as a garbage collection means
				if (validateName(key).isOK() && validateValue(getValue(key)).isOK())
					result.add(key);
			}
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

	/**
	 * Note that if a user changes the key in the preferences file to be invalid
	 * and then calls #getValue using that key, they will get the value back for
	 * that. But then if they try and call #setValue using the same key it will throw
	 * an exception. We may want to revisit this behaviour in the future.
	 * 
	 * @see org.eclipse.core.resources.IPathVariableManager#getValue(String)
	 */
	public IPath getValue(String varName) {
		String key = getKeyForName(varName);
		String value = preferences.getString(key);
		return value.length() == 0 ? null : Path.fromPortableString(value);
	}

	/**
	 * @see org.eclipse.core.resources.IPathVariableManager#isDefined(String)
	 */
	public boolean isDefined(String varName) {
		return getValue(varName) != null;
	}

	/**
	 * @see org.eclipse.core.resources.
	 * IPathVariableManager#removeChangeListener(IPathVariableChangeListener)
	 */
	public void removeChangeListener(IPathVariableChangeListener listener) {
		listeners.remove(listener);
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

	public URI resolveURI(URI uri) {
		if (uri == null || uri.isAbsolute())
			return uri;
		IPath raw = new Path(uri.getSchemeSpecificPart());
		IPath resolved = resolvePath(raw);
		return raw == resolved ? uri : URIUtil.toURI(resolved);
	}

	/**
	 * @see org.eclipse.core.resources.IPathVariableManager#setValue(String, IPath)
	 */
	public void setValue(String varName, IPath newValue) throws CoreException {
		checkIsValidName(varName);
		//convert path value to canonical form
		if (newValue != null && newValue.isAbsolute())
			newValue = FileUtil.canonicalPath(newValue);
		checkIsValidValue(newValue);
		int eventType;
		// read previous value and set new value atomically in order to generate the right event		
		synchronized (this) {
			IPath currentValue = getValue(varName);
			boolean variableExists = currentValue != null;
			if (!variableExists && newValue == null)
				return;
			if (variableExists && currentValue.equals(newValue))
				return;
			if (newValue == null) {
				preferences.setToDefault(getKeyForName(varName));
				eventType = IPathVariableChangeEvent.VARIABLE_DELETED;
			} else {
				preferences.setValue(getKeyForName(varName), newValue.toPortableString());
				eventType = variableExists ? IPathVariableChangeEvent.VARIABLE_CHANGED : IPathVariableChangeEvent.VARIABLE_CREATED;
			}
		}
		// notify listeners from outside the synchronized block to avoid deadlocks
		fireVariableChangeEvent(varName, newValue, eventType);
	}

	/**
	 * @see org.eclipse.core.internal.resources.IManager#shutdown(IProgressMonitor)
	 */
	public void shutdown(IProgressMonitor monitor) {
		// The preferences for this plug-in are saved in the Plugin.shutdown
		// method so we don't have to do it here.
	}

	/**
	 * @see org.eclipse.core.internal.resources.IManager#startup(IProgressMonitor)
	 */
	public void startup(IProgressMonitor monitor) {
		// since we are accessing the preference store directly, we don't
		// need to do any setup here.
	}

	/**
	 * @see org.eclipse.core.resources.IPathVariableManager#validateName(String)
	 */
	public IStatus validateName(String name) {
		String message = null;
		if (name.length() == 0) {
			message = Messages.pathvar_length;
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		char first = name.charAt(0);
		if (!Character.isLetter(first) && first != '_') {
			message = NLS.bind(Messages.pathvar_beginLetter, String.valueOf(first));
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		for (int i = 1; i < name.length(); i++) {
			char following = name.charAt(i);
			if (Character.isWhitespace(following))
				return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, Messages.pathvar_whitespace);
			if (!Character.isLetter(following) && !Character.isDigit(following) && following != '_') {
				message = NLS.bind(Messages.pathvar_invalidChar, String.valueOf(following));
				return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * @see IPathVariableManager#validateValue(IPath)
	 */
	public IStatus validateValue(IPath value) {
		if (value != null && (!value.isValidPath(value.toString()) || !value.isAbsolute())) {
			String message = Messages.pathvar_invalidValue;
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}
		return Status.OK_STATUS;
	}
}
