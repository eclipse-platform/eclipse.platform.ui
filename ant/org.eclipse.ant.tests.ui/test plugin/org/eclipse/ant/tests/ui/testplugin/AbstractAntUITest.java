/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.ui.testplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.ant.ui.internal.editor.outline.AntModel;
import org.eclipse.ant.ui.internal.editor.outline.XMLCore;
import org.eclipse.ant.ui.internal.editor.support.TestLocationProvider;
import org.eclipse.ant.ui.internal.launchConfigurations.IAntLaunchConfigurationConstants;
import org.eclipse.ant.ui.internal.model.AntUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.console.ConsoleDocumentPartitioner;
import org.eclipse.debug.internal.ui.views.console.HyperlinkPosition;
import org.eclipse.debug.internal.ui.views.console.StreamPartition;
import org.eclipse.debug.ui.console.IConsoleColorProvider;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.graphics.Color;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class AbstractAntUITest extends TestCase {
	private IDocument currentDocument;

	public AbstractAntUITest(String name) {
		super(name);
	}
		
	protected File getBuildFile(String buildFileName) {
		IFile file = getProject().getFolder("buildfiles").getFile(buildFileName);
		assertTrue("Could not find build file named: " + buildFileName, file.exists());
		return file.getLocation().toFile();
	}
	
	/**
	 * Returns the 'AntUITests' project.
	 * 
	 * @return the test project
	 */
	protected IProject getProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(ProjectHelper.PROJECT_NAME);
	}
	
	protected IDocument getDocument(String fileName) {
		File file = getBuildFile(fileName);
		InputStream in;
		try {
			in = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return null;
		}
		String initialContent= getStreamContentAsString(in);
		return new Document(initialContent);
	}

	protected String getStreamContentAsString(InputStream inputStream) {
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(inputStream, ResourcesPlugin.getEncoding());
		} catch (UnsupportedEncodingException e) {
			AntUIPlugin.log(e);
			return ""; //$NON-NLS-1$
		}
		BufferedReader tempBufferedReader = new BufferedReader(reader);

		return getReaderContentAsString(tempBufferedReader);
	}
	
	protected String getReaderContentAsString(BufferedReader tempBufferedReader) {
		StringBuffer tempResult = new StringBuffer();
		try {
			String tempLine= tempBufferedReader.readLine();

			while(tempLine != null) {
				if(tempResult.length() != 0) {
					tempResult.append("\n"); //$NON-NLS-1$
				}
				tempResult.append(tempLine);
				tempLine = tempBufferedReader.readLine();
			}
		} catch (IOException e) {
			AntUIPlugin.log(e);
			return null;
		}

		return tempResult.toString();
	}
		
	protected AntModel getAntModel(String fileName) {
		currentDocument= getDocument(fileName);
		AntModel model= new AntModel(XMLCore.getDefault(), currentDocument, null, new TestLocationProvider(fileName));
		model.reconcile();
		return model;
	}
	
	/**
	 * @return
	 */
	public IDocument getCurrentDocument() {
		return currentDocument;
	}

	/**
	 * @param currentDocument
	 */
	public void setCurrentDocument(IDocument currentDocument) {
		this.currentDocument = currentDocument;
	}
	
	/**
	 * Launches the Ant build with the buildfile name (no extension).
	 * 
	 * @param mainTypeName the program to launch
	 * @return thread in which the first suspend event occurred
	 */
	protected void launch(String buildFileName) throws CoreException {
		ILaunchConfiguration config = getLaunchConfiguration(buildFileName);
		assertNotNull("Could not locate launch configuration for " + buildFileName, config);
		launchAndTerminate(config, 10000);
	}
	
	/**
	 * Returns the launch configuration for the given buildfile
	 * 
	 * @param buildFileName buildfile to launch
	 * @see ProjectCreationDecorator
	 */
	protected ILaunchConfiguration getLaunchConfiguration(String buildFileName) {
		IFile file = getJavaProject().getProject().getFolder("launchConfigurations").getFile(buildFileName + ".launch");
		ILaunchConfiguration config = getLaunchManager().getLaunchConfiguration(file);
		assertTrue("Could not find launch configuration for " + buildFileName, config.exists());
		return config;
	}

	/**
	 * Returns the content of the specified file as <code>String</code>.
	 */
	protected String getFileContentAsString(File aFile) throws FileNotFoundException {
	    InputStream stream = new FileInputStream(aFile);
	    InputStreamReader reader = new InputStreamReader(stream);
	    BufferedReader bufferedReader = new BufferedReader(reader);
	
	    String result = "";
	    try {
	        String line= bufferedReader.readLine();
	    
	        while(line != null) {
	            result += "\n";
	            result += line;
	            line = bufferedReader.readLine();
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
		return result;
	}
	
	protected SAXParser getSAXParser() throws SAXException {
		SAXParser parser = parser = new SAXParser();
		parser.setFeature("http://xml.org/sax/features/namespaces", false); //$NON-NLS-1$
		return parser;
	}
	
	protected void parse(InputStream stream, SAXParser parser, DefaultHandler handler, File editedFile) {
		InputSource inputSource= new InputSource(stream);
		if (editedFile != null) {
			//needed for resolving relative external entities
			inputSource.setSystemId(editedFile.getAbsolutePath());
		}

		parser.setContentHandler(handler);
		parser.setDTDHandler(handler);
		parser.setEntityResolver(handler);
		parser.setErrorHandler(handler);
		try {
			parser.parse(inputSource);
		} catch (SAXException e) {
		} catch (IOException e) {
		}
	}
	
	/**
	 * Returns the launch manager
	 * 
	 * @return launch manager
	 */
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	
	/**
	 * Returns the 'AntUITests' project.
	 * 
	 * @return the test project
	 */
	protected IJavaProject getJavaProject() {
		return JavaCore.create( getProject());
	}
	
	protected void launchAndTerminate(ILaunchConfiguration config, int timeout) throws CoreException {
		DebugEventWaiter waiter= new DebugElementKindEventWaiter(DebugEvent.TERMINATE, IProcess.class);
		waiter.setTimeout(timeout);

		Object terminatee = launchAndWait(config, waiter);		
		assertNotNull("Program did not terminate.", terminatee);
		assertTrue("terminatee is not an IProcess", terminatee instanceof IProcess);
		IProcess process = (IProcess) terminatee;
		assertTrue("process is not terminated", process.isTerminated());
	}
	
	/**
	 * Launches the given configuration and waits for an event. Returns the
	 * source of the event. If the event is not received, the launch is
	 * terminated and an exception is thrown.
	 * 
	 * @param configuration the configuration to launch
	 * @param waiter the event waiter to use
	 * @return Object the source of the event
	 * @exception Exception if the event is never received.
	 */
	protected Object launchAndWait(ILaunchConfiguration configuration, DebugEventWaiter waiter) throws CoreException {
		ILaunch launch = configuration.launch(ILaunchManager.RUN_MODE, null);
		Object suspendee= waiter.waitForEvent();
		if (suspendee == null) {
			try {
				launch.terminate();
			} catch (CoreException e) {
				e.printStackTrace();
				fail("Program did not suspend, and unable to terminate launch.");
			}
		}
		assertNotNull("Program did not suspend, launch terminated.", suspendee);
		return suspendee;		
	}
	
	protected IConsoleHyperlink getHyperlink(int offset, IDocument doc) {
		if (offset >= 0 && doc != null) {
			Position[] positions = null;
			try {
				positions = doc.getPositions(HyperlinkPosition.HYPER_LINK_CATEGORY);
			} catch (BadPositionCategoryException ex) {
				// no links have been added
				return null;
			}
			for (int i = 0; i < positions.length; i++) {
				Position position = positions[i];
				if (offset >= position.getOffset() && offset <= (position.getOffset() + position.getLength())) {
					return ((HyperlinkPosition)position).getHyperLink();
				}
			}
		}
		return null;
	}
	
	protected Color getColorAtOffset(int offset, IDocument document) throws BadLocationException {
		if (document != null) {
			ConsoleDocumentPartitioner partitioner = (ConsoleDocumentPartitioner)document.getDocumentPartitioner();
			if (partitioner != null) {
				IConsoleColorProvider colorProvider = DebugUIPlugin.getDefault().getConsoleDocumentManager().getColorProvider(IAntLaunchConfigurationConstants.ID_ANT_PROCESS_TYPE);
				ITypedRegion[] regions= partitioner.computePartitioning(offset, document.getLineInformationOfOffset(offset).getLength());
				
				for (int i = 0; i < regions.length; i++) {
					StreamPartition partition = (StreamPartition)regions[i];
					return colorProvider.getColor(partition.getStreamIdentifier());
				}	
			}
		}
		return null;
	}
}
