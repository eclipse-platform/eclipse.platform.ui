package org.eclipse.ui.externaltools.internal.core;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

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
			IPath basePath = path.removeLastSegments(1).addTrailingSeparator();
			return createReadRoot(reader, basePath.toOSString());
		} catch (FileNotFoundException e) {
			processException(e, "AntUtil.antFileNotFound", -1); //$NON-NLS-1$
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
	 * @param baseDir the directory to use to resolve relative file names
	 * 		in XML document. This directory must exist and include a
	 * 		trailing separator. The directory format, including the separators,
	 * 		must be valid for the platform.
	 * @return the root memento for reading the document
	 * @throws CoreException if IO problems, or invalid XML format.
	 */
	private static XMLMemento createReadRoot(Reader reader, String baseDir) throws CoreException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();
			parser.setEntityResolver(new AntEntityResolver(baseDir));
			InputSource source = new InputSource(reader);
			source.setSystemId(baseDir);
			Document document = parser.parse(source);
			NodeList list = document.getChildNodes();
			for(int i=0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if (node instanceof Element) {
					return new XMLMemento(document, (Element) node);
				}
			}
		} catch (ParserConfigurationException e) {
			processException(e, "AntUtil.parserConfigError", -1); //$NON-NLS-1$
		} catch (IOException e) {
			processException(e, "AntUtil.ioError", -1); //$NON-NLS-1$
		} catch (SAXParseException e) {
			processException(e, "AntUtil.formatError", e.getLineNumber());
		} catch (SAXException e) {
			processException(e, "AntUtil.formatError", -1); //$NON-NLS-1$
		}
		
		// Invalid ant file
		processException(null, "AntUtil.invalidAntBuildFile", -1); //$NON-NLS-1$
		
		// Will never get here as processException will always throw
		// a Core exception...but compiler does not know that!
		return null;
	}

	/**
	 * Returns the list of targets of the Ant file represented by the
	 * supplied IMemento, or <code>null</code> if the memento is null or
	 * does not represent a valid Ant file.
	 */
	private static AntTargetList getTargetList(IMemento memento) throws CoreException {
		if (memento == null)
			return null;
		AntTargetList targets = new AntTargetList();
		
		String defaultTarget = memento.getString(ATT_DEFAULT);
		targets.setDefaultTarget(defaultTarget);
		
		IMemento[] targetMementos = memento.getChildren(TAG_TARGET);
		for (int i=0; i < targetMementos.length; i++) {
			IMemento targetMemento = targetMementos[i];
			String target = targetMemento.getString(ATT_NAME);
			if (target!= null)
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
	private static void processException(Exception e, String messageKey, int lineNumber) throws CoreException {
		StringBuffer problem= new StringBuffer();
		
		if (lineNumber > 0) {
			problem.append(MessageFormat.format("At line number: {0}", new String[]{Integer.toString(lineNumber)}));
			problem.append(System.getProperty("line.separator")); //$NON-NLS-1$;
		}
		if (e != null) {
			problem.append(e.getMessage());
		}
		if (problem.length() == 0) {
			problem.append(ToolMessages.getString(messageKey));
		}
		IStatus status = new Status(IStatus.ERROR, ExternalToolsPlugin.PLUGIN_ID, 0, problem.toString(), e);
		throw new CoreException(status);
	}
	
	/**
	 * EntityResolver used when parsing Ant files to find targets.
	 */
	private static class AntEntityResolver implements EntityResolver {
		private String baseDir;
		
		public AntEntityResolver(String baseDir) {
			this.baseDir = baseDir;
		}
		
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			if (systemId == null) {
				return null;
			}
			
			// remove "file:" prefix
			if (systemId.startsWith("file:")) { //$NON-NLS-1$
				// 5 is the length of the string "file:"
				systemId = systemId.substring(5);
				File file = new File(systemId);
				// If the file is not absolute, the systemId is relative
				// to the baseDir.
				if (!file.isAbsolute())
					file = new File(baseDir, systemId);
				Reader reader = new FileReader(file);
				return new InputSource(reader);
			}
			
			// use default behaviour if systemId does not have "file:" prefix
			return null;
		}
	}
}
