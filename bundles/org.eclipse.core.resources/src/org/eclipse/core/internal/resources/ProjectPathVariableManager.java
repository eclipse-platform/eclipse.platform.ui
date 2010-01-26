/*******************************************************************************
 * Copyright (c) 2008, 2010 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.runtime.IPath;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.osgi.util.NLS;

/**
 * The {@link IPathVariableManager} for a single project
 * @see IProject#getPathVariableManager()
 */
public class ProjectPathVariableManager implements IPathVariableManager, IManager {

	private Project project;
	private ProjectVariableProviderManager.Descriptor variableProviders[] = null;

	/**
	 * Constructor for the class.
	 */
	public ProjectPathVariableManager(Project project) {
		this.project = project;
		variableProviders = ProjectVariableProviderManager.getDefault().getDescriptors();
	}

	PathVariableManager getWorkspaceManager() {
		return (PathVariableManager) project.getWorkspace().getPathVariableManager();
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
	private void checkIsValidValue(URI newValue) throws CoreException {
		IStatus status = validateValue(newValue);
		if (!status.isOK())
			throw new CoreException(status);
	}

	/**
	 * @see org.eclipse.core.resources.IPathVariableManager#getPathVariable(String, IResource)
	 */
	public IPathVariable getPathVariable(String name, IResource resource) {
		if (isDefined(name, resource))
			return new PathVariable(name);
		return null;
	}

	/**
	 * @see org.eclipse.core.resources.IPathVariableManager#getPathVariableNames()
	 */
	public String[] getPathVariableNames() {
		List result = new LinkedList();
		HashMap map;
		try {
			map = ((ProjectDescription) project.getDescription()).getVariables();
		} catch (CoreException e) {
			return new String[0];
		}
		for (int i = 0; i < variableProviders.length; i++) {
			result.add(variableProviders[i].getName());
		}
		if (map != null)
			result.addAll(map.keySet());
		result.addAll(Arrays.asList(getWorkspaceManager().getPathVariableNames()));
		return (String[]) result.toArray(new String[0]);
	}

	/**
	 * If the variable is not listed in the project description, we fall back on
	 * the workspace variables.
	 * 
	 * @see org.eclipse.core.resources.IPathVariableManager#getValue(String)
	 */
	public IPath getValue(String varName) {
		URI uri = getValue(varName, project);
		if (uri != null)
			return URIUtil.toPath(uri);
		return null;
	}
	
	/**
	 * If the variable is not listed in the project description, we fall back on
	 * the workspace variables.
	 * 
	 * @see org.eclipse.core.resources.IPathVariableManager#getValue(String, IResource)
	 */
	public URI getValue(String varName, IResource resource) {
		String value = internalGetValue(varName, resource);
		if (value != null) {
			if (value.indexOf("..") != -1) { //$NON-NLS-1$
				// if the path is 'reducible', lets resolve it first.
				int index = value.indexOf(IPath.SEPARATOR);
				if (index > 0) { // if its the first character, its an
					// absolute path on unix, so we don't
					// resolve it
					URI resolved = resolveVariable(value, resource);
					if (resolved != null)
						return resolved;
				}
			}
			return URI.create(value);
		}
		return getWorkspaceManager().getValue(varName, resource);
	}

	public String internalGetValue(String varName, IResource resource) {
		HashMap map;
		try {
			map = ((ProjectDescription) project.getDescription()).getVariables();
		} catch (CoreException e) {
			return null;
		}
		if (map != null && map.containsKey(varName))
			return ((VariableDescription) map.get(varName)).getValue();

		String name;
		int index = varName.indexOf('-');
		if (index != -1)
			name = varName.substring(0, index);
		else
			name = varName;
		for (int i = 0; i < variableProviders.length; i++) {
			if (variableProviders[i].getName().equals(name))
				return variableProviders[i].getValue(varName, resource);
		}
		return null;
	}

	/**
	 * @see org.eclipse.core.resources.IPathVariableManager#isDefined(String)
	 */
	public boolean isDefined(String varName) {
		for (int i = 0; i < variableProviders.length; i++) {
			if (variableProviders[i].getName().equals(varName))
				return true;
		}
		
		try {
			HashMap map = ((ProjectDescription) project.getDescription()).getVariables();
			if (map != null) {
				Iterator it = map.keySet().iterator();
				while(it.hasNext()) {
					String name = (String) it.next();
					if (name.equals(varName))
						return true;
				}
			}
		} catch (CoreException e) {
			return false;
		}
		return getWorkspaceManager().isDefined(varName);
	}

	/**
	 * @see org.eclipse.core.resources.IPathVariableManager#resolvePath(IPath)
	 */
	public IPath resolvePath(IPath path) {
		if (path == null || path.segmentCount() == 0 || path.isAbsolute() || path.getDevice() != null)
			return path;
		URI value = resolveURI(URIUtil.toURI(path));
		return value == null ? path : URIUtil.toPath(value);
	}

	public IPath resolveVariable(String variable) {
		URI uri = resolveVariable(variable, project);
		if (uri != null)
			return URIUtil.toPath(uri);
		return null;
	}
	
	public URI resolveVariable(String variable, IResource resource) {
		LinkedList variableStack = new LinkedList();

		String value = resolveVariable(variable, resource, variableStack);
		if (value != null) {
			try {
				return URI.create(value);
			} catch (IllegalArgumentException e) {
				return URIUtil.toURI(Path.fromPortableString(value));
			}
		}
		return null;
	}

	public String resolveVariable(String value, IResource resource, LinkedList variableStack) {
		if (variableStack == null)
			variableStack = new LinkedList();

		String tmp = internalGetValue(value, resource);
		if (tmp == null) {
			URI result = getWorkspaceManager().getValue(value, resource);
			if (result != null)
				return result.toASCIIString();
		} else
			value = tmp;

		while (true) {
			String stringValue;
			try {
				URI uri = URI.create(value);
				if (uri != null) {
					IPath path = URIUtil.toPath(uri);
					if (path != null)
						stringValue = path.toPortableString();
					else
						stringValue = value;
				} else
					stringValue = value;
			} catch (IllegalArgumentException e) {
				stringValue = value;
			}
			// we check if the value contains referenced variables with ${VAR}
			int index = stringValue.indexOf("${"); //$NON-NLS-1$
			if (index != -1) {
				int endIndex = PathVariableUtil.getMatchingBrace(stringValue, index);
				String macro = stringValue.substring(index + 2, endIndex);
				String resolvedMacro = ""; //$NON-NLS-1$
				if (!variableStack.contains(macro)) {
					variableStack.add(macro);
					resolvedMacro = resolveVariable(macro, resource, variableStack);
					if (resolvedMacro == null)
						resolvedMacro = ""; //$NON-NLS-1$
				}
				if (stringValue.length() > endIndex)
					stringValue = stringValue.substring(0, index) + resolvedMacro + stringValue.substring(endIndex + 1);
				else
					stringValue = resolvedMacro;
				value = stringValue;
			} else
				break;
		}
		return value;
	}

	public URI resolveURI(URI uri) {
		return resolveURI(uri, project);
	}
	
	public URI resolveURI(URI uri, IResource resource) {
		if (resource == null)
			resource = project;
		if (uri == null || uri.isAbsolute() || (uri.getSchemeSpecificPart() == null))
			return uri;
		IPath raw = new Path(uri.getSchemeSpecificPart());
		if (raw == null || raw.segmentCount() == 0 || raw.isAbsolute() || raw.getDevice() != null)
			return URIUtil.toURI(raw);
		URI value = resolveVariable(raw.segment(0), resource);
		if (value == null)
			return uri;
		
		String path = value.getPath();
		if (path != null) {
			IPath p = Path.fromPortableString(path);
			p = p.append(raw.removeFirstSegments(1));
			try {
				value = new URI(value.getScheme(), value.getHost(), p.toPortableString(), value.getFragment());
			} catch (URISyntaxException e) {
				return uri;
			}
			return value;
		}
		else
			return uri;
	}

	/**
	 * @see org.eclipse.core.resources.IPathVariableManager#setValue(String,
	 *      IPath)
	 */
	public void setValue(String varName, IPath newValue) throws CoreException {
		if (newValue == null)
			setValue(varName, project, null);
		else
			setValue(varName, project, URIUtil.toURI(newValue));
			
	}
		/**
		 * @see org.eclipse.core.resources.IPathVariableManager#setValue(String,
		 *      IPath)
		 */
	public void setValue(String varName, IResource resource, URI newValue) throws CoreException {
		checkIsValidName(varName);
		checkIsValidValue(newValue);
		// read previous value and set new value atomically in order to generate
		// the right event
		boolean changeWorkspaceValue = false;
		int eventType = 0;
		synchronized (this) {
			String value = internalGetValue(varName, resource);
			URI currentValue = null;
			if (value == null)
				currentValue = getWorkspaceManager().getValue(varName, resource);
			else
				currentValue = URI.create(value);
			boolean variableExists = currentValue != null;
			if (!variableExists && newValue == null)
				return;
			if (variableExists && currentValue.equals(newValue))
				return;

			for (int i = 0; i < variableProviders.length; i++) {
				if (variableProviders[i].getName().equals(varName))
					return;
			}

			if (value == null && variableExists)
				changeWorkspaceValue = true;
			else {
				IProgressMonitor monitor = new NullProgressMonitor();
				final ISchedulingRule rule = project; // project.workspace.getRuleFactory().modifyRule(project);
				try {
					project.workspace.prepareOperation(rule, monitor);
					project.workspace.beginOperation(true);
					// save the location in the project description
					ProjectDescription description = project.internalGetDescription();
					if (newValue == null) {
						description.setVariableDescription(varName, null);
						eventType = IPathVariableChangeEvent.VARIABLE_DELETED;
					} else {
						description.setVariableDescription(varName, new VariableDescription(varName, newValue.toASCIIString()));
						eventType = variableExists ? IPathVariableChangeEvent.VARIABLE_CHANGED : IPathVariableChangeEvent.VARIABLE_CREATED;
					}
					project.writeDescription(IResource.NONE);
				} finally {
					project.workspace.endOperation(rule, true, Policy.subMonitorFor(monitor, Policy.endOpWork));
				}
			}
		}
		if (changeWorkspaceValue)
			getWorkspaceManager().setValue(varName, resource, newValue);
		else {
			// notify listeners from outside the synchronized block to avoid deadlocks
			getWorkspaceManager().fireVariableChangeEvent(project, varName, newValue != null? URIUtil.toPath(newValue):null, eventType);
		}
	}

	/**
	 * @see org.eclipse.core.internal.resources.IManager#shutdown(IProgressMonitor)
	 */
	public void shutdown(IProgressMonitor monitor) {
		// nothing to do here
	}

	/**
	 * @see org.eclipse.core.internal.resources.IManager#startup(IProgressMonitor)
	 */
	public void startup(IProgressMonitor monitor) {
		// nothing to do here
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
		// check

		return Status.OK_STATUS;
	}

	/**
	 * @see IPathVariableManager#validateValue(IPath)
	 */
	public IStatus validateValue(IPath value) {
		// accept any format
		return Status.OK_STATUS;
	}

	/**
	 * @see IPathVariableManager#validateValue(URI)
	 */
	public IStatus validateValue(URI value) {
		// accept any format
		return Status.OK_STATUS;
	}

	/**
	 * @throws CoreException 
	 * @see IPathVariableManager#convertToRelative(URI, IResource, boolean, String)
	 */
	public URI convertToRelative(URI path, IResource resource, boolean force, String variableHint) throws CoreException {
		return PathVariableUtil.convertToRelative(this, path, resource, force, variableHint);
	}

	/**
	 * @see IPathVariableManager#convertToUserEditableFormat(String)
	 */
	public String convertToUserEditableFormat(String value) { 
		return PathVariableUtil.convertToUserEditableFormatInternal(value);
	}
	
	public String convertFromUserEditableFormat(String userFormat, IResource resource) {
		return PathVariableUtil.convertFromUserEditableFormatInternal(this, userFormat, resource);
	}
	
	public void addChangeListener(IPathVariableChangeListener listener) {
		getWorkspaceManager().addChangeListener(listener, project);
	}

	public void removeChangeListener(IPathVariableChangeListener listener) {
		getWorkspaceManager().removeChangeListener(listener, project);
	}

	public String[] getPathVariableNames(IResource resource) {
		return getPathVariableNames();
	}

	public boolean isDefined(String name, IResource resource) {
		return isDefined(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IPathVariableManager#getVariableRelativePathLocation(IResource, URI)
	 */
	public URI getVariableRelativePathLocation(URI location, IResource resource) {
		try {
			URI result = convertToRelative(location, resource, false, null);
			if (!result.equals(location))
				return result;
		} catch (CoreException e) {
			// handled by returning null
		}
		return null;
	}
}
