package org.eclipse.ui.externaltools.internal.core;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.io.*;

import javax.xml.parsers.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * General utility class dealing with Ant files
 */
public final class AntUtil {
	private static final String ATT_DEFAULT = "default"; //NON-NLS-1$
	private static final String ATT_NAME = "name"; //NON-NLS-1$
	private static final String TAG_TARGET = "target"; //NON-NLS-1$
	
	/**
	 * No instances allowed
	 */
	private AntUtil() {
		super();
	}

	/**
	 * Returns the list of targets for the Ant file specified by the provided
	 * IPath, or <code>null</code> if no Ant targets found.
	 * 
	 * @throws CoreException if file does not exist, IO problems, or invalid format.
	 */
	public static AntTargetList getTargetList(IPath path) throws CoreException {
		IMemento memento = getMemento(path);
		return getTargetList(memento);	
	}
	
	/**
	 * Returns an IMemento representing the Ant builder found in
	 * the supplied IPath.
	 * 
	 * @throws CoreException if file does not exist, IO problems, or invalid format.
	 */
	private static IMemento getMemento(IPath path) throws CoreException {
		try {
			Reader reader = new FileReader(path.toFile());
			return createReadRoot(reader);
		} catch (FileNotFoundException e) {
			processException(e, "AntUtil.antFileNotFound"); //$NON-NLS-1$
		}
		
		// Will never get here as processException will always throw
		// a Core exception...but compiler does not know that!
		return null;
	}

	/**
	 * Creates a <code>Document</code> from the <code>Reader</code>
	 * and returns a root memento for reading the document.
	 * 
	 * @param reader the reader used to create the memento's document
	 * @return the root memento for reading the document
	 * @throws CoreException if IO problems, or invalid format.
	 */
	private static XMLMemento createReadRoot(Reader reader) throws CoreException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();
			Document document = parser.parse(new InputSource(reader));
			NodeList list = document.getChildNodes();
			for(int i=0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if (node instanceof Element)
					return new XMLMemento(document, (Element) node);
			}
		} catch (ParserConfigurationException e) {
			processException(e, "AntUtil.parserConfigError"); //$NON-NLS-1$
		} catch (IOException e) {
			processException(e, "AntUtil.ioError"); //$NON-NLS-1$
		} catch (SAXException e) {
			processException(e, "AntUtil.formatError"); //$NON-NLS-1$
		}
		
		// Invalid ant file
		processException(null, "AntUtil.invalidAntBuildFile"); //$NON-NLS-1$
		
		// Will never get here as processException will always throw
		// a Core exception...but compiler does not know that!
		return null;
	}

	/**
	 * Returns the list of targets of the Ant file represented by the
	 * supplied IMemento, or <code>null</code> if the memento is null or
	 * does not represent a valid Ant file.
	 */
	private static AntTargetList getTargetList(IMemento memento) {
		if (memento == null)
			return null;
		AntTargetList targets = new AntTargetList();
		
		String defaultTarget = memento.getString(ATT_DEFAULT);
		targets.setDefaultTarget(defaultTarget);
		
		IMemento[] targetMementos = memento.getChildren(TAG_TARGET);
		for (int i=0; i < targetMementos.length; i++) {
			IMemento targetMemento = targetMementos[i];
			String target = targetMemento.getString(ATT_NAME);
			targets.add(target);
		}
		
		// If the file has no targets, then it is not a
		// valid Ant file.
		if (targets.getTargetCount() == 0) {
			return null;
		} else {
			targets.validateDefaultTarget();
			return targets;
		}
	}
	
	/**
	 * Process the exception by creating a well formatted
	 * IStatus and throwing a CoreException with it.
	 */
	private static void processException(Exception e, String messageKey) throws CoreException {
		String problem = null;
		if (e != null)
			problem = e.getMessage();
		if (problem == null || problem.length() == 0)
			problem = ToolMessages.getString(messageKey);
		IStatus status = new Status(IStatus.ERROR, ExternalToolsPlugin.PLUGIN_ID, 0, problem, e);
		throw new CoreException(status);
	}
}
