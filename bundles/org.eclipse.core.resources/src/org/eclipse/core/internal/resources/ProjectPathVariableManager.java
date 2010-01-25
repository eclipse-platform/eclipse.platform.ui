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

import org.eclipse.core.internal.resources.projectvariables.ProjectLocationVariableResolver;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IPathVariableChangeEvent;

import org.eclipse.core.resources.IResource;

import java.net.URISyntaxException;

import org.eclipse.core.resources.IPathVariableChangeListener;

import java.net.URI;
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
		if (value != null)
			return URI.create(value);
		return null;
	}

	/*
	 * Splits a value (returned by this.getValue(variable) in an array of
	 * string, where the array is divided between the value content and the
	 * value variables.
	 * 
	 * For example, if the value is "${ECLIPSE_HOME}/plugins", the value
	 * returned will be {"${ECLIPSE_HOME}" "/plugins"}
	 */
	static String[] splitVariablesAndContent(String value) {
		LinkedList result = new LinkedList();
		while (true) {
			// we check if the value contains referenced variables with ${VAR}
			int index = value.indexOf("${"); //$NON-NLS-1$
			if (index != -1) {
				int endIndex = getMatchingBrace(value, index);
				if (index > 0)
					result.add(value.substring(0, index));
				result.add(value.substring(index, endIndex + 1));
				value = value.substring(endIndex + 1);
			} else
				break;
		}
		if (value.length() > 0)
			result.add(value);
		return (String[]) result.toArray(new String[0]);
	}

	/*
	 * Splits a value (returned by this.getValue(variable) in an array of
	 * string of the variables contained in the value.
	 * 
	 * For example, if the value is "${ECLIPSE_HOME}/plugins", the value
	 * returned will be {"ECLIPSE_HOME"}. If the value is 
	 * "${ECLIPSE_HOME}/${FOO}/plugins", the value returned will be 
	 * {"ECLIPSE_HOME", "FOO"}.
	 */
	static String[] splitVariableNames(String value) {
		LinkedList result = new LinkedList();
		while (true) {
			int index = value.indexOf("${"); //$NON-NLS-1$
			if (index != -1) {
				int endIndex = getMatchingBrace(value, index);
				result.add(value.substring(index + 2, endIndex));
				value = value.substring(endIndex + 1);
			} else
				break;
		}
		return (String[]) result.toArray(new String[0]);
	}

	/*
	 * Extracts the variable name from a variable segment.
	 * 
	 * For example, if the value is "${ECLIPSE_HOME}", the value returned will
	 * be "ECLIPSE_HOME". If the segment doesn't contain any variable, the value
	 * returned will be "".
	 */
	static String extractVariable(String segment) {
		int index = segment.indexOf("${"); //$NON-NLS-1$
		if (index != -1) {
			int endIndex = getMatchingBrace(segment, index);
			return segment.substring(index + 2, endIndex);
		}
		return ""; //$NON-NLS-1$
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
				stringValue = URIUtil.toPath(uri).toPortableString();
			} catch (IllegalArgumentException e) {
				stringValue = value;
			}
			// we check if the value contains referenced variables with ${VAR}
			int index = stringValue.indexOf("${"); //$NON-NLS-1$
			if (index != -1) {
				int endIndex = getMatchingBrace(stringValue, index);
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

	// getMatchingBrace("${FOO}/something") returns 5
	// getMatchingBrace("${${OTHER}}/something") returns 10
	// getMatchingBrace("${FOO") returns 5
	static int getMatchingBrace(String value, int index) {
		int scope = 0;
		for (int i = index + 1; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '}') {
				if (scope == 0)
					return i;
				scope--;
			}
			if (c == '$') {
				if ((i + 1 < value.length()) && (value.charAt(i + 1) == '{'))
					scope++;
			}
		}
		return value.length();
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
		IPath p = Path.fromPortableString(path);
		p = p.append(raw.removeFirstSegments(1));
		try {
			value = new URI(value.getScheme(), value.getHost(), p.toPortableString(), value.getFragment());
		} catch (URISyntaxException e) {
			return uri;
		}
		return value;
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
	 * Converts the internal format of the linked resource location if the PARENT
	 * variables is used.  For example, if the value is "${PARENT-2-VAR}/foo", the
	 * converted result is "${VAR}/../../foo".
	 * @param value
	 * @return the converted path variable value
	 */
	public static String convertToUserEditableFormat(String value) {
		StringBuffer buffer = new StringBuffer();
		String components[] = splitVariablesAndContent(value);
		for (int i = 0; i < components.length; i++) {
			String variable = extractVariable(components[i]);
			if (PathVariableUtil.isParentVariable(variable)) {
				String argument = PathVariableUtil.getParentVariableArgument(variable);
				int count = PathVariableUtil.getParentVariableCount(variable);
				if (argument != null && count != -1) {
					buffer.append(PathVariableUtil.buildVariableMacro(Path.fromOSString(argument)));
					for (int j = 0; j < count; j++) {
						buffer.append("/.."); //$NON-NLS-1$
					}
				} else
					buffer.append(components[i]);
			} else
				buffer.append(components[i]);
		}
		return buffer.toString();
	}

	/**
	 * Converts the user editable format to the internal format.
	 * For example, if the value is "${VAR}/../../foo", the
	 * converted result is "${PARENT-2-VAR}/foo".
	 * If the string is not directly convertible to a ${PARENT-COUNT-VAR}
	 * syntax (for example, the editable string "${FOO}bar/../../"), intermediate
	 * path variables will be created.
	 * @param userFormat The user editable string
	 * @return the converted path variable value
	 */
	public String convertFromUserEditableFormat(String userFormat) {
		boolean isAbsolute = (userFormat.length() > 0) && (userFormat.charAt(0) == '/' || userFormat.charAt(0) == '\\');
		String components[] = splitPathComponents(userFormat);
		for (int i = 0; i < components.length; i++) {
			if (components[i] == null)
				continue;
			if (isDotDot(components[i])) {
				int parentCount = 1;
				components[i] = null;
				for (int j = i + 1; j < components.length; j++) {
					if (components[j] != null) {
						if (isDotDot(components[j])) {
							parentCount++;
							components[j] = null;
						} else
							break;
					}
				}
				if (i == 0) // this means the value is implicitly relative to the project location
					components[0] = PathVariableUtil.buildParentPathVariable(ProjectLocationVariableResolver.NAME, parentCount, false);
				else {
					for (int j = i - 1; j >= 0; j--) {
						if (parentCount == 0)
							break;
						if (components[j] == null)
							continue;
						String variable = extractVariable(components[j]);
						try {
							if (variable.length() > 0) {
								int indexOfVariable = components[j].indexOf(variable) - "${".length(); //$NON-NLS-1$
								String prefix = components[j].substring(0, indexOfVariable);
								String suffix = components[j].substring(indexOfVariable + "${".length() + variable.length() + "}".length()); //$NON-NLS-1$ //$NON-NLS-2$
								if (suffix.length() != 0) {
									// Create an intermediate variable, since a syntax of "${VAR}foo/../"
									// can't be converted to a "${PARENT-1-VAR}foo" variable.
									// So instead, an intermediate variable "VARFOO" will be created of value 
									// "${VAR}foo", and the string "${PARENT-1-VARFOO}" will be inserted.
									String intermediateVariable = PathVariableUtil.getValidVariableName(variable + suffix);
									IPath intermediateValue = Path.fromPortableString(components[j]);
									int intermediateVariableIndex = 1;
									String originalIntermediateVariableName = intermediateVariable;
									while (isDefined(intermediateVariable)) {
										IPath tmpValue = getValue(intermediateVariable);
										if (tmpValue.equals(intermediateValue))
											break;
										intermediateVariable = originalIntermediateVariableName + intermediateVariableIndex;
									}
									if (!isDefined(intermediateVariable))
										setValue(intermediateVariable, intermediateValue);
									variable = intermediateVariable;
									prefix = new String();
								}
								String newVariable = variable;
								if (PathVariableUtil.isParentVariable(variable)) {
									String argument = PathVariableUtil.getParentVariableArgument(variable);
									int count = PathVariableUtil.getParentVariableCount(variable);
									if (argument != null && count != -1)
										newVariable = PathVariableUtil.buildParentPathVariable(argument, count + parentCount, false);
									else
										newVariable = PathVariableUtil.buildParentPathVariable(variable, parentCount, false);
								} else
									newVariable = PathVariableUtil.buildParentPathVariable(variable, parentCount, false);
								components[j] = prefix + newVariable;
								break;
							}
							components[j] = null;
							parentCount--;
						} catch (CoreException e) {
							components[j] = null;
							parentCount--;
						}
					}
				}
			}
		}
		StringBuffer buffer = new StringBuffer();
		if (isAbsolute)
			buffer.append('/');
		for (int i = 0; i < components.length; i++) {
			if (components[i] != null) {
				if (i > 0)
					buffer.append('/');
				buffer.append(components[i]);
			}
		}
		return buffer.toString();
	}

	private static boolean isDotDot(String component) {
		return component.equals(".."); //$NON-NLS-1$
	}

	private static String[] splitPathComponents(String userFormat) {
		ArrayList list = new ArrayList();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < userFormat.length(); i++) {
			char c = userFormat.charAt(i);
			if (c == '/' || c == '\\') {
				if (buffer.length() > 0)
					list.add(buffer.toString());
				buffer = new StringBuffer();
			} else
				buffer.append(c);
		}
		if (buffer.length() > 0)
			list.add(buffer.toString());
		return (String[]) list.toArray(new String[0]);
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
