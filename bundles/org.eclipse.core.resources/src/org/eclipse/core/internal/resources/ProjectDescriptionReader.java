/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.internal.localstore.SafeFileInputStream;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

public class ProjectDescriptionReader extends DefaultHandler implements IModelObjectConstants {

	/** constants */
	protected static final IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];
	protected static final String[] EMPTY_STRING_ARRAY = new String[0];
	protected static final int S_BUILD_COMMAND = 4;
	protected static final int S_BUILD_COMMAND_ARGUMENTS = 11;
	protected static final int S_BUILD_COMMAND_NAME = 10;
	protected static final int S_BUILD_SPEC = 3;
	protected static final int S_DICTIONARY = 12;
	protected static final int S_DICTIONARY_KEY = 13;
	protected static final int S_DICTIONARY_VALUE = 14;
	//states
	protected static final int S_INITIAL = 0;
	protected static final int S_LINK = 17;
	protected static final int S_LINK_LOCATION = 20;
	protected static final int S_LINK_NAME = 18;
	protected static final int S_LINK_TYPE = 19;
	protected static final int S_LINKED_RESOURCES = 16;
	protected static final int S_NATURE_NAME = 15;
	protected static final int S_NATURES = 5;
	protected static final int S_PROJECT_COMMENT = 8;
	protected static final int S_PROJECT_DESC = 2;
	protected static final int S_PROJECT_NAME = 7;
	protected static final int S_PROJECTS = 6;
	protected static final int S_REFERENCED_PROJECT_NAME = 9;
	protected static final int S_WORKSPACE_DESC = 1;
	protected final StringBuffer charBuffer = new StringBuffer();

	protected Stack objectStack;
	protected MultiStatus problems;

	// The project description we are creating.
	ProjectDescription projectDescription = null;
	protected int state = S_INITIAL;

	/**
	 * @see ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] chars, int offset, int length) throws SAXException {
		//accumulate characters and process them when endElement is reached
		charBuffer.append(chars, offset, length);
	}

	/**
	 * End of an element that is part of a build command
	 */
	private void endBuildCommandElement(String elementName) {
		if (elementName.equals(BUILD_COMMAND)) {
			// Pop this BuildCommand off the stack.
			BuildCommand command = (BuildCommand) objectStack.pop();
			// Add this BuildCommand to a array list of BuildCommands.
			ArrayList commandList = (ArrayList) objectStack.peek();
			commandList.add(command);
			state = S_BUILD_SPEC;
		}
	}

	/**
	 * End of an element that is part of a build spec
	 */
	private void endBuildSpecElement(String elementName) {
		if (elementName.equals(BUILD_SPEC)) {
			// Pop off the array list of BuildCommands and add them to the
			// ProjectDescription which is the next thing on the stack.
			ArrayList commands = (ArrayList) objectStack.pop();
			state = S_PROJECT_DESC;
			if (commands.isEmpty())
				return;
			ICommand[] commandArray = ((ICommand[]) commands.toArray(new ICommand[commands.size()]));
			projectDescription.setBuildSpec(commandArray);
		}
	}

	/**
	 * End of a dictionary element
	 */
	private void endDictionary(String elementName) {
		if (elementName.equals(DICTIONARY)) {
			// Pick up the value and then key off the stack and add them
			// to the HashMap which is just below them on the stack.
			// Leave the HashMap on the stack to pick up more key/value
			// pairs if they exist.
			String value = (String) objectStack.pop();
			String key = (String) objectStack.pop();
			((HashMap) objectStack.peek()).put(key, value);
			state = S_BUILD_COMMAND_ARGUMENTS;
		}
	}

	private void endDictionaryKey(String elementName) {
		if (elementName.equals(KEY)) {
			// There is a value place holder on the top of the stack and
			// a key place holder just below it.
			String value = (String) objectStack.pop();
			String oldKey = (String) objectStack.pop();
			String newKey = charBuffer.toString();
			if (oldKey != null && oldKey.length() != 0) {
				parseProblem(Policy.bind("projectDescriptionReader.whichKey", oldKey, newKey)); //$NON-NLS-1$
				objectStack.push(oldKey);
			} else {
				objectStack.push(newKey);
			}
			//push back the dictionary value
			objectStack.push(value);
			state = S_DICTIONARY;
		}
	}

	private void endDictionaryValue(String elementName) {
		if (elementName.equals(VALUE)) {
			String newValue = charBuffer.toString();
			// There is a value place holder on the top of the stack
			String oldValue = (String) objectStack.pop();
			if (oldValue != null && oldValue.length() != 0) {
				parseProblem(Policy.bind("projectDescriptionReader.whichValue", oldValue, newValue)); //$NON-NLS-1$
				objectStack.push(oldValue);
			} else {
				objectStack.push(newValue);
			}
			state = S_DICTIONARY;
		}
	}

	/**
	 * @see ContentHandler#endElement(String, String, String)
	 */
	public void endElement(String uri, String elementName, String qname) throws SAXException {
		switch (state) {
			case S_PROJECT_DESC :
				// Don't think we need to do anything here.
				break;
			case S_PROJECT_NAME :
				if (elementName.equals(NAME)) {
					// Project names cannot have leading/trailing whitespace
					// as they are IResource names.
					projectDescription.setName(charBuffer.toString().trim());
					state = S_PROJECT_DESC;
				}
				break;
			case S_PROJECTS :
				if (elementName.equals(PROJECTS)) {
					endProjectsElement(elementName);
					state = S_PROJECT_DESC;
				}
				break;
			case S_DICTIONARY :
				endDictionary(elementName);
				break;
			case S_BUILD_COMMAND_ARGUMENTS :
				if (elementName.equals(ARGUMENTS)) {
					// There is a hashmap on the top of the stack with the
					// arguments (if any).
					HashMap dictionaryArgs = (HashMap) objectStack.pop();
					state = S_BUILD_COMMAND;
					if (dictionaryArgs.isEmpty())
						break;
					// Below the hashMap on the stack, there is a BuildCommand.
					((BuildCommand) objectStack.peek()).setArguments(dictionaryArgs);
				}
				break;
			case S_BUILD_COMMAND :
				endBuildCommandElement(elementName);
				break;
			case S_BUILD_SPEC :
				endBuildSpecElement(elementName);
				break;
			case S_NATURES :
				endNaturesElement(elementName);
				break;
			case S_LINK :
				endLinkElement(elementName);
				break;
			case S_LINKED_RESOURCES :
				endLinkedResourcesElement(elementName);
				return;
			case S_PROJECT_COMMENT :
				if (elementName.equals(COMMENT)) {
					projectDescription.setComment(charBuffer.toString());
					state = S_PROJECT_DESC;
				}
				break;
			case S_REFERENCED_PROJECT_NAME :
				if (elementName.equals(PROJECT)) {
					//top of stack is list of project references
					// Referenced projects are just project names and, therefore,
					// are also IResource names and cannot have leading/trailing 
					// whitespace.
					((ArrayList) objectStack.peek()).add(charBuffer.toString().trim());
					state = S_PROJECTS;
				}
				break;
			case S_BUILD_COMMAND_NAME :
				if (elementName.equals(NAME)) {
					//top of stack is the build command
					// A build command name is an extension id and
					// cannot have leading/trailing whitespace.
					((BuildCommand) objectStack.peek()).setName(charBuffer.toString().trim());
					state = S_BUILD_COMMAND;
				}
				break;
			case S_DICTIONARY_KEY :
				endDictionaryKey(elementName);
				break;
			case S_DICTIONARY_VALUE :
				endDictionaryValue(elementName);
				break;
			case S_NATURE_NAME :
				if (elementName.equals(NATURE)) {
					//top of stack is list of nature names
					// A nature name is an extension id and cannot
					// have leading/trailing whitespace.
					((ArrayList) objectStack.peek()).add(charBuffer.toString().trim());
					state = S_NATURES;
				}
				break;
			case S_LINK_NAME :
				endLinkName(elementName);
				break;
			case S_LINK_TYPE :
				endLinkType(elementName);
				break;
			case S_LINK_LOCATION :
				endLinkLocation(elementName);
				break;
		}
		charBuffer.setLength(0);
	}

	/**
	 * End this group of linked resources and add them to the project description.
	 */
	private void endLinkedResourcesElement(String elementName) {
		if (elementName.equals(LINKED_RESOURCES)) {
			HashMap linkedResources = (HashMap) objectStack.pop();
			state = S_PROJECT_DESC;
			if (linkedResources.isEmpty())
				return;
			projectDescription.setLinkDescriptions(linkedResources);
		}
	}

	/**
	 * End a single linked resource and add it to the HashMap.
	 */
	private void endLinkElement(String elementName) {
		if (elementName.equals(LINK)) {
			state = S_LINKED_RESOURCES;
			// Pop off the link description
			LinkDescription link = (LinkDescription) objectStack.pop();
			// Make sure that you have something reasonable
			String name = link.getName();
			int type = link.getType();
			IPath location = link.getLocation();
			if ((name == null) || name.length() == 0) {
				parseProblem(Policy.bind("projectDescriptionReader.emptyLinkName", Integer.toString(type), location.toString())); //$NON-NLS-1$
				return;
			}
			if (type == -1) {
				parseProblem(Policy.bind("projectDescriptionReader.badLinkType", name, location.toString())); //$NON-NLS-1$
				return;
			}
			if (location.isEmpty()) {
				parseProblem(Policy.bind("projectDescriptionReader.badLinkLocation", name, Integer.toString(type))); //$NON-NLS-1$
				return;
			}

			// The HashMap of linked resources is the next thing on the stack
			((HashMap) objectStack.peek()).put(link.getName(), link);
		}
	}

	private void endLinkLocation(String elementName) {
		if (elementName.equals(LOCATION)) {
			// A link location is an IPath.  IPath segments cannot have
			// leading/trailing whitespace
			String newLocation = charBuffer.toString().trim();
			// objectStack has a LinkDescription on it. Set the type on this LinkDescription.
			IPath oldLocation = ((LinkDescription) objectStack.peek()).getLocation();
			if (!oldLocation.isEmpty()) {
				parseProblem(Policy.bind("projectDescriptionReader.badLocation", oldLocation.toString(), newLocation)); //$NON-NLS-1$
			} else {
				((LinkDescription) objectStack.peek()).setLocation(Path.fromPortableString(newLocation));
			}
			state = S_LINK;
		}
	}

	private void endLinkName(String elementName) {
		if (elementName.equals(NAME)) {
			// A link name is an IResource name.  IResource names
			// cannot have leading/trailing whitespace.
			String newName = charBuffer.toString().trim();
			// objectStack has a LinkDescription on it. Set the name
			// on this LinkDescription.
			String oldName = ((LinkDescription) objectStack.peek()).getName();
			if (oldName.length() != 0) {
				parseProblem(Policy.bind("projectDescriptionReader.badLinkName", oldName, newName)); //$NON-NLS-1$
			} else {
				((LinkDescription) objectStack.peek()).setName(newName);
			}
			state = S_LINK;
		}
	}

	private void endLinkType(String elementName) {
		if (elementName.equals(TYPE)) {
			//FIXME we should handle this case by removing the entire link
			//for now we default to a file link
			int newType = IResource.FILE;
			try {
				// parseInt expects a string containing only numerics
				// or a leading '-'.  Ensure there is no leading/trailing
				// whitespace.
				newType = Integer.parseInt(charBuffer.toString().trim());
			} catch (NumberFormatException e) {
				log(e);
			}
			// objectStack has a LinkDescription on it. Set the type
			// on this LinkDescription.
			int oldType = ((LinkDescription) objectStack.peek()).getType();
			if (oldType != -1) {
				parseProblem(Policy.bind("projectDescriptionReader.badLinkType", Integer.toString(oldType), Integer.toString(newType))); //$NON-NLS-1$
			} else {
				((LinkDescription) objectStack.peek()).setType(newType);
			}
			state = S_LINK;
		}
	}

	/**
	 * End of an element that is part of a nature list
	 */
	private void endNaturesElement(String elementName) {
		if (elementName.equals(NATURES)) {
			// Pop the array list of natures off the stack
			ArrayList natures = (ArrayList) objectStack.pop();
			state = S_PROJECT_DESC;
			if (natures.size() == 0)
				return;
			String[] natureNames = (String[]) natures.toArray(new String[natures.size()]);
			projectDescription.setNatureIds(natureNames);
		}
	}

	/**
	 * End of an element that is part of a project references list
	 */
	private void endProjectsElement(String elementName) {
		// Pop the array list that contains all the referenced project names
		ArrayList referencedProjects = (ArrayList) objectStack.pop();
		if (referencedProjects.size() == 0)
			// Don't bother adding an empty group of referenced projects to the
			// project descriptor.
			return;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = new IProject[referencedProjects.size()];
		for (int i = 0; i < projects.length; i++) {
			projects[i] = root.getProject((String) referencedProjects.get(i));
		}
		projectDescription.setReferencedProjects(projects);
	}

	/**
	 * @see ErrorHandler#error(SAXParseException)
	 */
	public void error(SAXParseException error) throws SAXException {
		log(error);
	}

	/**
	 * @see ErrorHandler#fatalError(SAXParseException)
	 */
	public void fatalError(SAXParseException error) throws SAXException {
		// ensure a null value is not passed as message to Status constructor (bug 42782)
		String message = error.getMessage();
		problems.add(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_READ_METADATA, message == null ? "" : message, error)); //$NON-NLS-1$
		throw error;
	}

	protected void log(Exception ex) {
		// ensure a null value is not passed as message to Status constructor (bug 42782)		
		String message = ex.getMessage();
		problems.add(new Status(IStatus.WARNING, ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_READ_METADATA, message == null ? "" : message, ex)); //$NON-NLS-1$
	}

	private void parseProblem(String errorMessage) {
		problems.add(new Status(IStatus.WARNING, ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_READ_METADATA, errorMessage, null));
	}

	private void parseProjectDescription(String elementName) {
		if (elementName.equals(NAME)) {
			state = S_PROJECT_NAME;
			return;
		}
		if (elementName.equals(COMMENT)) {
			state = S_PROJECT_COMMENT;
			return;
		}
		if (elementName.equals(PROJECTS)) {
			state = S_PROJECTS;
			// Push an array list on the object stack to hold the name
			// of all the referenced projects.  This array list will be
			// popped off the stack, massaged into the right format
			// and added to the project description when we hit the
			// end element for PROJECTS.
			objectStack.push(new ArrayList());
			return;
		}
		if (elementName.equals(BUILD_SPEC)) {
			state = S_BUILD_SPEC;
			// Push an array list on the object stack to hold the build commands
			// for this build spec.  This array list will be popped off the stack,
			// massaged into the right format and added to the project's build
			// spec when we hit the end element for BUILD_SPEC.
			objectStack.push(new ArrayList());
			return;
		}
		if (elementName.equals(NATURES)) {
			state = S_NATURES;
			// Push an array list to hold all the nature names.
			objectStack.push(new ArrayList());
			return;
		}
		if (elementName.equals(LINKED_RESOURCES)) {
			// Push a HashMap to collect all the links.
			objectStack.push(new HashMap());
			state = S_LINKED_RESOURCES;
			return;
		}
	}

	public ProjectDescription read(InputSource input) {
		problems = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_READ_METADATA, Policy.bind("projectDescriptionReader.failureReadingProjectDesc"), null); //$NON-NLS-1$
		objectStack = new Stack();
		state = S_INITIAL;
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			try {
				factory.setFeature("http://xml.org/sax/features/string-interning", true); //$NON-NLS-1$
			} catch (SAXException e) {
				// In case support for this feature is removed
			}
			SAXParser parser = factory.newSAXParser();
			parser.parse(input, this);
		} catch (ParserConfigurationException e) {
			log(e);
		} catch (IOException e) {
			log(e);
		} catch (SAXException e) {
			log(e);
		}
		switch (problems.getSeverity()) {
			case IStatus.ERROR :
				ResourcesPlugin.getPlugin().getLog().log(problems);
				return null;
			case IStatus.WARNING :
			case IStatus.INFO :
				ResourcesPlugin.getPlugin().getLog().log(problems);
			case IStatus.OK :
			default :
				return projectDescription;
		}
	}

	/**
	 * Reads and returns a project description stored at the given location
	 */
	public ProjectDescription read(IPath location) throws IOException {
		BufferedInputStream file = null;
		try {
			file = new BufferedInputStream(new FileInputStream(location.toFile()));
			return read(new InputSource(file));
		} finally {
			if (file != null)
				file.close();
		}
	}

	/**
	 * Reads and returns a project description stored at the given location, or 
	 * temporary location.
	 */
	public ProjectDescription read(IPath location, IPath tempLocation) throws IOException {
		SafeFileInputStream file = new SafeFileInputStream(location.toOSString(), tempLocation.toOSString());
		try {
			return read(new InputSource(file));
		} finally {
			file.close();
		}
	}

	/**
	 * @see ContentHandler#startElement(String, String, String, Attributes)
	 */
	public void startElement(String uri, String elementName, String qname, Attributes attributes) throws SAXException {
		//clear the character buffer at the start of every element
		charBuffer.setLength(0);
		switch (state) {
			case S_INITIAL :
				if (elementName.equals(PROJECT_DESCRIPTION)) {
					state = S_PROJECT_DESC;
					projectDescription = new ProjectDescription();
				} else {
					throw (new SAXException(Policy.bind("projectDescriptionReader.notProjectDescription", elementName))); //$NON-NLS-1$
				}
				break;
			case S_PROJECT_DESC :
				parseProjectDescription(elementName);
				break;
			case S_PROJECTS :
				if (elementName.equals(PROJECT)) {
					state = S_REFERENCED_PROJECT_NAME;
				}
				break;
			case S_BUILD_SPEC :
				if (elementName.equals(BUILD_COMMAND)) {
					state = S_BUILD_COMMAND;
					objectStack.push(new BuildCommand());
				}
				break;
			case S_BUILD_COMMAND :
				if (elementName.equals(NAME)) {
					state = S_BUILD_COMMAND_NAME;
				} else if (elementName.equals(ARGUMENTS)) {
					state = S_BUILD_COMMAND_ARGUMENTS;
					// Push a HashMap to hold all the key/value pairs which
					// will become the argument list.
					objectStack.push(new HashMap());
				}
				break;
			case S_BUILD_COMMAND_ARGUMENTS :
				if (elementName.equals(DICTIONARY)) {
					state = S_DICTIONARY;
					// Push 2 strings for the key/value pair to be read
					objectStack.push(new String()); // key
					objectStack.push(new String()); // value
				}
				break;
			case S_DICTIONARY :
				if (elementName.equals(KEY)) {
					state = S_DICTIONARY_KEY;
				} else if (elementName.equals(VALUE)) {
					state = S_DICTIONARY_VALUE;
				}
				break;
			case S_NATURES :
				if (elementName.equals(NATURE)) {
					state = S_NATURE_NAME;
				}
				break;
			case S_LINKED_RESOURCES :
				if (elementName.equals(LINK)) {
					state = S_LINK;
					// Push place holders for the name, type and location of
					// this link.
					objectStack.push(new LinkDescription());
				}
				break;
			case S_LINK :
				if (elementName.equals(NAME)) {
					state = S_LINK_NAME;
				} else if (elementName.equals(TYPE)) {
					state = S_LINK_TYPE;
				} else if (elementName.equals(LOCATION)) {
					state = S_LINK_LOCATION;
				}
				break;
		}
	}

	/**
	 * @see ErrorHandler#warning(SAXParseException)
	 */
	public void warning(SAXParseException error) throws SAXException {
		log(error);
	}
}