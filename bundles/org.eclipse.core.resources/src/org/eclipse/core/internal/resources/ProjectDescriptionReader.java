/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import javax.xml.parsers.*;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.internal.localstore.SafeFileInputStream;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

public class ProjectDescriptionReader extends DefaultHandler implements IModelObjectConstants {

	//states
	protected static final int S_BUILD_COMMAND = 0;
	protected static final int S_BUILD_COMMAND_ARGUMENTS = 1;
	protected static final int S_BUILD_COMMAND_NAME = 2;
	protected static final int S_BUILD_COMMAND_TRIGGERS = 3;
	protected static final int S_BUILD_SPEC = 4;
	protected static final int S_DICTIONARY = 5;
	protected static final int S_DICTIONARY_KEY = 6;
	protected static final int S_DICTIONARY_VALUE = 7;
	protected static final int S_INITIAL = 8;
	protected static final int S_LINK = 9;
	protected static final int S_LINK_LOCATION = 10;
	protected static final int S_LINK_LOCATION_URI = 11;
	protected static final int S_LINK_PATH = 12;
	protected static final int S_LINK_TYPE = 13;
	protected static final int S_LINKED_RESOURCES = 14;
	protected static final int S_NATURE_NAME = 15;
	protected static final int S_NATURES = 16;
	protected static final int S_PROJECT_COMMENT = 17;
	protected static final int S_PROJECT_DESC = 18;
	protected static final int S_PROJECT_NAME = 19;
	protected static final int S_PROJECTS = 20;
	protected static final int S_REFERENCED_PROJECT_NAME = 21;

	/**
	 * Singleton sax parser factory
	 */
	private static SAXParserFactory singletonParserFactory;

	/**
	 * Singleton sax parser
	 */
	private static SAXParser singletonParser;

	protected final StringBuffer charBuffer = new StringBuffer();

	protected Stack objectStack;
	protected MultiStatus problems;

	/**
	 * The project we are reading the description for, or null if unknown.
	 */
	private final IProject project;
	// The project description we are creating.
	ProjectDescription projectDescription = null;

	protected int state = S_INITIAL;


	/**
	 * Returns the SAXParser to use when parsing project description files.
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	private static synchronized SAXParser createParser() throws ParserConfigurationException, SAXException{
		//the parser can't be used concurrently, so only use singleton when workspace is locked
		if (!isWorkspaceLocked())
			return createParserFactory().newSAXParser();
		if (singletonParser == null) {
			singletonParser =  createParserFactory().newSAXParser();
		}
		return singletonParser;
	}
	
	/**
	 * Returns the SAXParserFactory to use when parsing project description files.
	 * @throws ParserConfigurationException 
	 */
	private static synchronized SAXParserFactory createParserFactory() throws ParserConfigurationException{
		if (singletonParserFactory == null) {
			singletonParserFactory =  SAXParserFactory.newInstance();
			singletonParserFactory.setNamespaceAware(true);
			try {
				singletonParserFactory.setFeature("http://xml.org/sax/features/string-interning", true); //$NON-NLS-1$
			} catch (SAXException e) {
				// In case support for this feature is removed
			}
		}
		return singletonParserFactory;
	}
	
	private static boolean isWorkspaceLocked() {
		try {
			return ((Workspace) ResourcesPlugin.getWorkspace()).getWorkManager().isLockAlreadyAcquired();
		} catch (CoreException e) {
			return false;
		}
	}


	public ProjectDescriptionReader() {
		this.project = null;
	}

	public ProjectDescriptionReader(IProject project) {
		this.project = project;
	}

	/**
	 * @see ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] chars, int offset, int length) {
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
	 * End a build triggers element and set the triggers for the current
	 * build command element.
	 */
	private void endBuildTriggersElement(String elementName) {
		if (elementName.equals(BUILD_TRIGGERS)) {
			state = S_BUILD_COMMAND;
			BuildCommand command = (BuildCommand) objectStack.peek();
			//presence of this element indicates the builder is configurable
			command.setConfigurable(true);
			//clear all existing values
			command.setBuilding(IncrementalProjectBuilder.AUTO_BUILD, false);
			command.setBuilding(IncrementalProjectBuilder.CLEAN_BUILD, false);
			command.setBuilding(IncrementalProjectBuilder.FULL_BUILD, false);
			command.setBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD, false);

			//set new values according to value in the triggers element
			StringTokenizer tokens = new StringTokenizer(charBuffer.toString(), ","); //$NON-NLS-1$
			while (tokens.hasMoreTokens()) {
				String next = tokens.nextToken();
				if (next.toLowerCase().equals(TRIGGER_AUTO)) {
					command.setBuilding(IncrementalProjectBuilder.AUTO_BUILD, true);
				} else if (next.toLowerCase().equals(TRIGGER_CLEAN)) {
					command.setBuilding(IncrementalProjectBuilder.CLEAN_BUILD, true);
				} else if (next.toLowerCase().equals(TRIGGER_FULL)) {
					command.setBuilding(IncrementalProjectBuilder.FULL_BUILD, true);
				} else if (next.toLowerCase().equals(TRIGGER_INCREMENTAL)) {
					command.setBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD, true);
				}
			}
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
				parseProblem(NLS.bind(Messages.projRead_whichKey, oldKey, newKey));
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
				parseProblem(NLS.bind(Messages.projRead_whichValue, oldValue, newValue));
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
	public void endElement(String uri, String elementName, String qname) {
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
			case S_BUILD_COMMAND_TRIGGERS :
				endBuildTriggersElement(elementName);
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
			case S_LINK_PATH :
				endLinkPath(elementName);
				break;
			case S_LINK_TYPE :
				endLinkType(elementName);
				break;
			case S_LINK_LOCATION :
				endLinkLocation(elementName);
				break;
			case S_LINK_LOCATION_URI :
				endLinkLocationURI(elementName);
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
			IPath path = link.getProjectRelativePath();
			int type = link.getType();
			URI location = link.getLocationURI();
			if (location == null) {
				parseProblem(NLS.bind(Messages.projRead_badLinkLocation, path, Integer.toString(type)));
				return;
			}
			if ((path == null) || path.segmentCount() == 0) {
				parseProblem(NLS.bind(Messages.projRead_emptyLinkName, Integer.toString(type), location));
				return;
			}
			if (type == -1) {
				parseProblem(NLS.bind(Messages.projRead_badLinkType, path, location));
				return;
			}

			// The HashMap of linked resources is the next thing on the stack
			((HashMap) objectStack.peek()).put(link.getProjectRelativePath(), link);
		}
	}

	/**
	 * For backwards compatibility, link locations in the local file system are represented 
	 * in the project description under the "location" tag.
	 * @param elementName
	 */
	private void endLinkLocation(String elementName) {
		if (elementName.equals(LOCATION)) {
			// A link location is an URI.  URIs cannot have leading/trailing whitespace
			String newLocation = charBuffer.toString().trim();
			// objectStack has a LinkDescription on it. Set the type on this LinkDescription.
			URI oldLocation = ((LinkDescription) objectStack.peek()).getLocationURI();
			if (oldLocation != null) {
				parseProblem(NLS.bind(Messages.projRead_badLocation, oldLocation, newLocation));
			} else {
				((LinkDescription) objectStack.peek()).setLocationURI(URIUtil.toURI(Path.fromPortableString(newLocation)));
			}
			state = S_LINK;
		}
	}

	/**
	 * Link locations that are not stored in the local file system are represented 
	 * in the project description under the "locationURI" tag.
	 * @param elementName
	 */
	private void endLinkLocationURI(String elementName) {
		if (elementName.equals(LOCATION_URI)) {
			// A link location is an URI.  URIs cannot have leading/trailing whitespace
			String newLocation = charBuffer.toString().trim();
			// objectStack has a LinkDescription on it. Set the type on this LinkDescription.
			URI oldLocation = ((LinkDescription) objectStack.peek()).getLocationURI();
			if (oldLocation != null) {
				parseProblem(NLS.bind(Messages.projRead_badLocation, oldLocation, newLocation));
			} else {
				try {
					((LinkDescription) objectStack.peek()).setLocationURI(new URI(newLocation));
				} catch (URISyntaxException e) {
					String msg = Messages.projRead_failureReadingProjectDesc;
					problems.add(new Status(IStatus.WARNING, ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_READ_METADATA, msg, e));
				}
			}
			state = S_LINK;
		}
	}

	private void endLinkPath(String elementName) {
		if (elementName.equals(NAME)) {
			IPath newPath = new Path(charBuffer.toString());
			// objectStack has a LinkDescription on it. Set the name
			// on this LinkDescription.
			IPath oldPath = ((LinkDescription) objectStack.peek()).getProjectRelativePath();
			if (oldPath.segmentCount() != 0) {
				parseProblem(NLS.bind(Messages.projRead_badLinkName, oldPath, newPath));
			} else {
				((LinkDescription) objectStack.peek()).setPath(newPath);
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
				parseProblem(NLS.bind(Messages.projRead_badLinkType2, Integer.toString(oldType), Integer.toString(newType)));
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
	public void error(SAXParseException error) {
		log(error);
	}

	/**
	 * @see ErrorHandler#fatalError(SAXParseException)
	 */
	public void fatalError(SAXParseException error) throws SAXException {
		// ensure a null value is not passed as message to Status constructor (bug 42782)
		String message = error.getMessage();
		if (project != null)
			message = NLS.bind(Messages.resources_readMeta, project.getName());
		problems.add(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_READ_METADATA, message == null ? "" : message, error)); //$NON-NLS-1$
		throw error;
	}

	protected void log(Exception ex) {
		String message = ex.getMessage();
		if (project != null)
			message = NLS.bind(Messages.resources_readMeta, project.getName());
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
		problems = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_READ_METADATA, Messages.projRead_failureReadingProjectDesc, null);
		objectStack = new Stack();
		state = S_INITIAL;
		try {
			createParser().parse(input, this);
		} catch (ParserConfigurationException e) {
			log(e);
		} catch (IOException e) {
			log(e);
		} catch (SAXException e) {
			log(e);
		}
		switch (problems.getSeverity()) {
			case IStatus.ERROR :
				Policy.log(problems);
				return null;
			case IStatus.WARNING :
			case IStatus.INFO :
				Policy.log(problems);
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
					throw (new SAXException(NLS.bind(Messages.projRead_notProjectDescription, elementName)));
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
				} else if (elementName.equals(BUILD_TRIGGERS)) {
					state = S_BUILD_COMMAND_TRIGGERS;
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
					state = S_LINK_PATH;
				} else if (elementName.equals(TYPE)) {
					state = S_LINK_TYPE;
				} else if (elementName.equals(LOCATION)) {
					state = S_LINK_LOCATION;
				} else if (elementName.equals(LOCATION_URI)) {
					state = S_LINK_LOCATION_URI;
				}
				break;
		}
	}

	/**
	 * @see ErrorHandler#warning(SAXParseException)
	 */
	public void warning(SAXParseException error) {
		log(error);
	}
}
