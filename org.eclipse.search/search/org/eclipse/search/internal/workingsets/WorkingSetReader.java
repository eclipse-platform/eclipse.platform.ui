/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.workingsets;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.util.Assert;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.eclipse.search.ui.IWorkingSet;
import org.eclipse.search.ui.SearchUI;

import org.eclipse.search.internal.ui.SearchPlugin;

/**
 * Reads data from an InputStream and returns the working sets
 * 
 * @deprecated use org.eclipse.ui.IWorkingSet support - this class will be removed soon
 */
public class WorkingSetReader extends Object {

	protected InputStream fInputStream;

	private MultiStatus fWarnings;
	
	/**
	 * Reads a working sets from the underlying stream.
	 * It is the clients responsiblity to close the stream.
	 **/
	public WorkingSetReader(InputStream inputStream) {
		Assert.isNotNull(inputStream);
		fInputStream= new BufferedInputStream(inputStream);
		fWarnings= new MultiStatus(SearchUI.PLUGIN_ID, 0, WorkingSetMessages.getString("WorkingSetReader.warnings"), null); //$NON-NLS-1$
	}

	/**
	 * Hook for possible subclasses
	 **/
	protected WorkingSetReader() {
	}

	/**
     * Closes this stream.
	 * It is the clients responsiblity to close the stream.
	 * 
	 * @exception IOException
     */
    public void close() throws IOException {
    	if (fInputStream != null)
			fInputStream.close();
	}

	public IWorkingSet[] readXML() throws IOException, SAXException {
	  	DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
    	factory.setValidating(false);
		DocumentBuilder parser= null;
		try {
			parser= factory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			throw new IOException(ex.getMessage());
		} finally {
			// Note: Above code is ok since clients are responsible to close the stream
		}
		Element xml= parser.parse(new InputSource(fInputStream)).getDocumentElement();
		if (!xml.getNodeName().equals(WorkingSet.TAG_WORKINGSETS))
			throw new IOException(WorkingSetMessages.getString("WorkingSetReader.error.badFormat")); //$NON-NLS-1$

		NodeList topLevelElements= xml.getChildNodes();
		Set workingSets= new HashSet(5);
		for (int i= 0; i < topLevelElements.getLength(); i++) {
			Node node= topLevelElements.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			Element element= (Element)node;
			String workingSetName= element.getAttribute(WorkingSet.TAG_NAME);
			Set resources= new HashSet(5);
			if (element.getNodeName().equals(WorkingSet.TAG_WORKINGSET))
				readWorkingSetTag(resources, element.getChildNodes());
			workingSets.add(new WorkingSet(workingSetName, resources.toArray()));
		}
		return (IWorkingSet[]) workingSets.toArray(new IWorkingSet[workingSets.size()]);
	}

	private void readWorkingSetTag(Set resources, NodeList nodes) throws IOException {
		for (int j= 0; j < nodes.getLength(); j++) {
			Node node= nodes.item(j);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			Element element= (Element)node;
			if (element.getNodeName().equals(WorkingSet.TAG_CONTENTS))
				readContentsTag(resources, element.getChildNodes());
		}
	}

	private void readContentsTag(Set resources, NodeList contents) throws IOException {
		for (int k= 0; k < contents.getLength(); k++) {
			Node node= contents.item(k);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			Element element= (Element)node;
			if (element.getNodeName().equals(WorkingSet.TAG_FILE))
				addFile(resources, element);
			else if (element.getNodeName().equals(WorkingSet.TAG_FOLDER))
				addFolder(resources ,element);
			else if (element.getNodeName().equals(WorkingSet.TAG_PROJECT))
				addProject(resources ,element);
		}
	}

	private void addFile(Set selectedElements, Element element) throws IOException {
		IPath path= getPath(element);
		if (path != null) {
			IFile file= SearchPlugin.getWorkspace().getRoot().getFile(path);
			if (file != null)
				selectedElements.add(file);
		}
	}

	private void addFolder(Set selectedElements, Element element) throws IOException {
		IPath path= getPath(element);
		if (path != null) {
			IFolder folder= SearchPlugin.getWorkspace().getRoot().getFolder(path);
			if (folder != null)
				selectedElements.add(folder);
		}
	}

	private void addProject(Set selectedElements, Element element) throws IOException {
		String name= element.getAttribute(WorkingSet.TAG_NAME); //$NON-NLS-1$
		if (name.equals("")) //$NON-NLS-1$
			throw new IOException(WorkingSetMessages.getString("WorkingSetReader.error.tagNameNotFound")); //$NON-NLS-1$
		IProject project= SearchPlugin.getWorkspace().getRoot().getProject(name);
		if (project != null)
			selectedElements.add(project);
	}

	private IPath getPath(Element element) throws IOException {
		String pathString= element.getAttribute(WorkingSet.TAG_PATH);
		if (pathString.equals("")) //$NON-NLS-1$
			throw new IOException(WorkingSetMessages.getString("WorkingSetReader.error.tagPathNotFound")); //$NON-NLS-1$
		return new Path(element.getAttribute(WorkingSet.TAG_PATH));
	}
	

	/**
	 * Returns the warnings of this operation. If there are no
	 * warnings, a status object with IStatus.OK is returned.
	 *
	 * @return the status of this operation
	 */
	public IStatus getWarnings() {
		if (fWarnings.getChildren().length == 0)
			return new Status(IStatus.OK, SearchUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
		else
			return fWarnings;
	}

	/**
	 * Adds a new warning to the list with the passed information.
	 * Normally the export operation continues after a warning.
	 * 
	 * @param	message		the message
	 * @param	exception	the throwable that caused the warning, or <code>null</code>
	 */
	protected void addWarning(String message, Throwable error) {
		fWarnings.add(new Status(IStatus.WARNING, SearchUI.PLUGIN_ID, 0, message, error));
	}
}
