/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.workingsets;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.util.Assert;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.eclipse.search.ui.IWorkingSet;

import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;

/**
 * Writes working sets to an underlying OutputStream
 * 
 * @deprecated use org.eclipse.ui.IWorkingSet support - this class will be removed soon
 */
public class WorkingSetWriter extends Object {
	
	protected OutputStream fOutputStream;
	
	/**
	 * Create a WorkingSetWriter on the given output stream.
	 * It is the clients responsibility to close the output stream.
	 **/
	public WorkingSetWriter(OutputStream outputStream) {
		Assert.isNotNull(outputStream);
		fOutputStream= new BufferedOutputStream(outputStream);
	}

	/**
	 * Hook for possible subclasses
	 **/
	protected WorkingSetWriter() {
	}

	/**
     * Writes an XML representation of the working set
     * to to the underlying stream.
     * 
     * @exception IOException	if writing to the underlying stream fails
     */
    public void writeXML(IWorkingSet[] workingSets) throws IOException {
    	Assert.isNotNull(workingSets);
    	DocumentBuilder docBuilder= null;
    	DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
    	factory.setValidating(true);
 		try {   	
	    	docBuilder= factory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			throw new IOException(WorkingSetMessages.getString("WorkingSetWriter.error.couldNotGetXmlBuilder")); //$NON-NLS-1$
 		}
		Document document= docBuilder.newDocument();

		// Root node for this working set
		Element xmlRoot= document.createElement(WorkingSet.TAG_WORKINGSETS);
		document.appendChild(xmlRoot);

		// Write each working set		
		for (int i= 0; i < workingSets.length; i++)
			writeXML(workingSets[i], xmlRoot, document);
		
		// Write the document to the stream
		OutputFormat format= new OutputFormat();
		format.setIndenting(true);
		SerializerFactory serializerFactory= SerializerFactory.getSerializerFactory(Method.XML);
		Serializer serializer= serializerFactory.makeSerializer(fOutputStream,	format);
		serializer.asDOMSerializer().serialize(document);
    }

	/**
     * Writes an XML representation of the working set
     * to the given XML element
     */
    public void writeXML(IWorkingSet workingSet, Element xmlRoot, Document document) {
    	Assert.isNotNull(workingSet);
    	Assert.isNotNull(document);
		
		// Root node for this working set
		Element xml= document.createElement(WorkingSet.TAG_WORKINGSET);
		xmlRoot.appendChild(xml);

		// Name
		xml.setAttribute(WorkingSet.TAG_NAME, workingSet.getName());
		
		// Contents
		Element contents= document.createElement(WorkingSet.TAG_CONTENTS);
		xml.appendChild(contents);
		IResource[] resources= workingSet.getResources();
		for (int i= 0; i < resources.length; i++)
			add(resources[i], contents, document);
    }

	/**
     * Closes this stream.
     * It is the client's responsibility to close the stream.
     * 
	 * @exception IOException
     */
    public void close() throws IOException {
    	if (fOutputStream != null)
			fOutputStream.close();
	}

	private void add(IResource resource, Element parent, Document document) {
		Element element= null;
		if (resource.getType() == IResource.PROJECT) {
			element= document.createElement(WorkingSet.TAG_PROJECT);
			parent.appendChild(element);
			element.setAttribute(WorkingSet.TAG_NAME, resource.getName());
			return;
		}
		if (resource.getType() == IResource.FILE)
			element= document.createElement(WorkingSet.TAG_FILE);
		else if (resource.getType() == IResource.FOLDER)
			element= document.createElement(WorkingSet.TAG_FOLDER);
		parent.appendChild(element);
		element.setAttribute(WorkingSet.TAG_PATH, resource.getFullPath().toString()); //$NON-NLS-1$
	}
}
