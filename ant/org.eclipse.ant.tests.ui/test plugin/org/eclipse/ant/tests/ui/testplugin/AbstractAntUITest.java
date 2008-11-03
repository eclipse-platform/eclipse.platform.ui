/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.ant.internal.ui.IAntUIPreferenceConstants;
import org.eclipse.ant.internal.ui.model.AntModel;
import org.eclipse.ant.tests.ui.editor.support.TestLocationProvider;
import org.eclipse.ant.tests.ui.editor.support.TestProblemRequestor;
import org.eclipse.ant.ui.launching.IAntLaunchConfigurationConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.internal.console.ConsoleHyperlinkPosition;
import org.eclipse.ui.internal.console.IOConsolePartition;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Abstract Ant UI test class
 */
public abstract class AbstractAntUITest extends TestCase {
    
    public static String ANT_EDITOR_ID= "org.eclipse.ant.ui.internal.editor.AntEditor";
    
    private IDocument currentDocument;

    /**
     * Constructor
     * @param name
     */
    public AbstractAntUITest(String name) {
        super(name);
    }
        
    /**
     * Returns the {@link IFile} for the given build file name
     * @param buildFileName
     * @return the associated {@link IFile} for the given build file name
     */
    protected IFile getIFile(String buildFileName) {
        return getProject().getFolder("buildfiles").getFile(buildFileName); 
    }
    
    /**
     * Returns the {@link File} for the given build file name
     * @param buildFileName
     * @return the {@link File} for the given build file name
     */
    protected File getBuildFile(String buildFileName) {
        IFile file = getIFile(buildFileName);
        assertTrue("Could not find build file named: " + buildFileName, file.exists());
        return file.getLocation().toFile();
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
    	super.setUp();
    	assertProject();
    }
    
    /**
     * Asserts that the testing project has been setup in the test workspace
     * @throws Exception
     * 
     * @since 3.5
     */
    protected void assertProject() throws Exception {
		IProject pro = ResourcesPlugin.getWorkspace().getRoot().getProject(ProjectHelper.PROJECT_NAME);
		if (!pro.exists()) {
			// create project and import build files and support files
			IProject project = ProjectHelper.createProject(ProjectHelper.PROJECT_NAME);
			IFolder folder = ProjectHelper.addFolder(project, "buildfiles");
			ProjectHelper.addFolder(project, "launchConfigurations");
			File root = AntUITestPlugin.getDefault().getFileInPlugin(ProjectHelper.TEST_BUILDFILES_DIR);
			ProjectHelper.importFilesFromDirectory(root, folder.getFullPath(), null);
			
			createLaunchConfigurationForBoth("echoing");
			createLaunchConfigurationForBoth("102282");
			createLaunchConfigurationForBoth("74840");
            createLaunchConfigurationForBoth("failingTarget");
			createLaunchConfiguration("build");
			createLaunchConfiguration("bad");
			createLaunchConfiguration("importRequiringUserProp");
            createLaunchConfigurationForSeparateVM("echoPropertiesSepVM", "echoProperties");
			createLaunchConfigurationForSeparateVM("extensionPointSepVM", null);
			createLaunchConfigurationForSeparateVM("extensionPointTaskSepVM", null);
			createLaunchConfigurationForSeparateVM("extensionPointTypeSepVM", null);
			createLaunchConfigurationForSeparateVM("input", null);
			createLaunchConfigurationForSeparateVM("environmentVar", null);
            
            createLaunchConfigurationForBoth("breakpoints");
            createLaunchConfigurationForBoth("debugAntCall");
            createLaunchConfigurationForBoth("96022");
            createLaunchConfigurationForBoth("macrodef");
            createLaunchConfigurationForBoth("85769");
			
			createLaunchConfiguration("big", ProjectHelper.PROJECT_NAME + "/buildfiles/performance/build.xml");
			
			//do not show the Ant build failed error dialog
			AntUIPlugin.getDefault().getPreferenceStore().setValue(IAntUIPreferenceConstants.ANT_ERROR_DIALOG, false);
		}
    }

    /**
     * Creates two launch configurations one standard one and one for a separate VM
     * @param launchConfigName
     * @throws Exception
     * 
     * @since 3.5
     */
    protected void createLaunchConfigurationForBoth(String launchConfigName) throws Exception {
		createLaunchConfiguration(launchConfigName);
		createLaunchConfigurationForSeparateVM(launchConfigName + "SepVM", launchConfigName);
	}

	/**
	 * Creates a shared launch configuration for launching Ant in a separate VM with the given
	 * name.
	 * 
	 * @since 3.5
	 */
	protected void createLaunchConfigurationForSeparateVM(String launchConfigName, String buildFileName) throws Exception {
		String bf = buildFileName;
		ILaunchConfigurationType type = getLaunchManager().getLaunchConfigurationType(IAntLaunchConfigurationConstants.ID_ANT_LAUNCH_CONFIGURATION_TYPE);
		ILaunchConfigurationWorkingCopy config = type.newInstance(getJavaProject().getProject().getFolder("launchConfigurations"), launchConfigName);
		
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "org.eclipse.ant.internal.ui.antsupport.InternalAntRunner"); //$NON-NLS-1$
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, "org.eclipse.ant.ui.AntClasspathProvider"); //$NON-NLS-1$
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, getJavaProject().getElementName());
		if (bf == null) {
			bf= launchConfigName;
		} 
		config.setAttribute(IExternalToolConstants.ATTR_LOCATION, "${workspace_loc:/" + ProjectHelper.PROJECT_NAME + "/buildfiles/" + bf + ".xml}");
		
		config.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, true);
		config.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, IAntUIConstants.REMOTE_ANT_PROCESS_FACTORY_ID);
		 
		setVM(config);
				
		config.doSave();
	}
	
	/**
	 * Sets the workspace default VM on the given working copy
	 * @param config
	 * 
	 * @since 3.5
	 */
	protected void setVM(ILaunchConfigurationWorkingCopy config) {
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		String vmName= vm.getName();
		String vmTypeID= vm.getVMInstallType().getId();			
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, vmName);
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, vmTypeID);
	}
	
	/**
	 * Creates a shared launch configuration for launching Ant in a separate VM with the given
	 * name.
	 */
	protected void createLaunchConfiguration(String launchConfigName) throws Exception {
	    createLaunchConfiguration(launchConfigName, ProjectHelper.PROJECT_NAME + "/buildfiles/" + launchConfigName + ".xml");
	}
	
	/**
	 * Creates a launch configuration with the given name in the given location
	 * @param launchConfigName
	 * @param path
	 * @return the handle to the new launch configuration
	 * @throws CoreException
	 */
	public static ILaunchConfiguration createLaunchConfiguration(String launchConfigName, String path) throws CoreException {
	    ILaunchConfigurationType type = getLaunchManager().getLaunchConfigurationType(IAntLaunchConfigurationConstants.ID_ANT_LAUNCH_CONFIGURATION_TYPE);
		ILaunchConfigurationWorkingCopy config = type.newInstance(getJavaProject().getProject().getFolder("launchConfigurations"), launchConfigName);
	
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, getJavaProject().getElementName());
		config.setAttribute(IExternalToolConstants.ATTR_LOCATION, "${workspace_loc:/" + path + "}");
		config.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, true);
			
		config.doSave();
		return config;
	}
    
    /**
     * Returns the 'AntUITests' project.
     * 
     * @return the test project
     */
    protected static IProject getProject() {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(ProjectHelper.PROJECT_NAME);
    }
    
    /**
     * Returns the underlying {@link IDocument} for the given file name
     * @param fileName
     * @return the underlying {@link IDocument} for the given file name
     */
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

    /**
     * Returns the contents of the given {@link InputStream} as a {@link String}
     * @param inputStream
     * @return the {@link InputStream} as a {@link String}
     */
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
    
    /**
     * Returns the contents of the given {@link BufferedReader} as a {@link String}
     * @param bufferedReader
     * @return the contents of the given {@link BufferedReader} as a {@link String}
     */
    protected String getReaderContentAsStringNew(BufferedReader bufferedReader) {
        StringBuffer result = new StringBuffer();
        try {
            char[] readBuffer= new char[2048];
            int n= bufferedReader.read(readBuffer);
            while (n > 0) {
                result.append(readBuffer, 0, n);
                n= bufferedReader.read(readBuffer);
            }
        } catch (IOException e) {
            AntUIPlugin.log(e);
            return null;
        }

        return result.toString();
    }
    
    /**
     * Returns the contents of the given {@link BufferedReader} as a {@link String}
     * @param bufferedReader
     * @return the contents of the given {@link BufferedReader} as a {@link String}
     */
    protected String getReaderContentAsString(BufferedReader bufferedReader) {
        StringBuffer result = new StringBuffer();
        try {
            String line= bufferedReader.readLine();

            while(line != null) {
                if(result.length() != 0) {
                    result.append(System.getProperty("line.separator")); //$NON-NLS-1$
                }
                result.append(line);
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            AntUIPlugin.log(e);
            return null;
        }

        return result.toString();
    }
        
    /**
     * Returns the {@link AntModel} for the given file name
     * @param fileName
     * @return the {@link AntModel} for the given file name
     */
    protected AntModel getAntModel(String fileName) {
        currentDocument= getDocument(fileName);
        AntModel model= new AntModel(currentDocument, new TestProblemRequestor(), new TestLocationProvider(getBuildFile(fileName)));
        model.reconcile();
        return model;
    }
    
    /**
     * @return the current {@link IDocument} context
     */
    public IDocument getCurrentDocument() {
        return currentDocument;
    }

    /**
     * Allows the current {@link IDocument} context to be set. This method accepts <code>null</code>
     * @param currentDocument
     */
    public void setCurrentDocument(IDocument currentDocument) {
        this.currentDocument = currentDocument;
    }
    
    /**
     * Launches the Ant build with the build file name (no extension).
     * 
     * @param buildFileName the ant build file name
     */
    protected void launch(String buildFileName) throws CoreException {
        ILaunchConfiguration config = getLaunchConfiguration(buildFileName);
        assertNotNull("Could not locate launch configuration for " + buildFileName, config);
        launchAndTerminate(config, 20000);
    }
    
    /**
     * Launches the Ant build with the build file name (no extension).
     * 
     * @param buildFileName the build file
     * @param arguments the ant arguments
     */
    protected void launch(String buildFileName, String arguments) throws CoreException {
        ILaunchConfiguration config = getLaunchConfiguration(buildFileName);
        assertNotNull("Could not locate launch configuration for " + buildFileName, config);
        ILaunchConfigurationWorkingCopy copy= config.getWorkingCopy();
        copy.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, arguments);
        launchAndTerminate(copy, 20000);
    }
    
    /**
    * Launches the Ant build in debug output mode with the build file name (no extension).
    * 
    * @param mainTypeName the program to launch
    * @return thread in which the first suspend event occurred
    */
    protected void launchWithDebug(String buildFileName) throws CoreException {
        ILaunchConfiguration config = getLaunchConfiguration(buildFileName);
        assertNotNull("Could not locate launch configuration for " + buildFileName, config);
        ILaunchConfigurationWorkingCopy copy= config.getWorkingCopy();
        copy.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, "-debug");
        launchAndTerminate(copy, 10000);
    }
    
    /**
     * Returns the launch configuration for the given build file
     * 
     * @param buildFileName build file to launch
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
    
        return getReaderContentAsString(bufferedReader);
    }
    
    /**
     * @return a new SAX parser instrance
     */
    protected SAXParser getSAXParser() {
        SAXParser parser = null;
        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
        } catch (ParserConfigurationException e) {
            AntUIPlugin.log(e);
        } catch (SAXException e) {
            AntUIPlugin.log(e);
        }
        return parser;
    }

    
    /**
     * Parses the given input stream with the given parser using the given handler
     * @param stream
     * @param parser
     * @param handler
     * @param editedFile
     */
    protected void parse(InputStream stream, SAXParser parser, DefaultHandler handler, File editedFile) {
        InputSource inputSource= new InputSource(stream);
        if (editedFile != null) {
            //needed for resolving relative external entities
            inputSource.setSystemId(editedFile.getAbsolutePath());
        }

        try {
            parser.parse(inputSource, handler);
        } catch (SAXException e) {
        } catch (IOException e) {
        }
    }
    
    /**
     * Returns the launch manager
     * 
     * @return launch manager
     */
    public static ILaunchManager getLaunchManager() {
        return DebugPlugin.getDefault().getLaunchManager();
    }
    
    /**
     * Returns the 'AntUITests' project.
     * 
     * @return the test project
     */
    public static IJavaProject getJavaProject() {
        return JavaCore.create( getProject());
    }
    
    /**
     * Launches the given configuration and waits for the terminated event or the length of the given timeout, 
     * whichever comes first 
     * @param config
     * @param timeout
     * @throws CoreException
     */
    protected void launchAndTerminate(ILaunchConfiguration config, int timeout) throws CoreException {
        DebugEventWaiter waiter= new DebugElementKindEventWaiter(DebugEvent.TERMINATE, IProcess.class);
        waiter.setTimeout(timeout);

        Object terminatee = launchAndWait(config, waiter);      
        assertNotNull("Program did not terminate.", terminatee);
        assertTrue("terminatee is not an IProcess", terminatee instanceof IProcess);
        IProcess process = (IProcess) terminatee;
        boolean terminated = process.isTerminated();
        assertTrue("process is not terminated", terminated);
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
        boolean terminated = launch.isTerminated();
        assertTrue("launch did not terminate", terminated);
        if (terminated && !ConsoleLineTracker.isClosed()) {
            ConsoleLineTracker.waitForConsole();
        }
        assertTrue("Console is not closed", ConsoleLineTracker.isClosed()); 
        return suspendee;       
    }
    
    /**
     * Returns the {@link IHyperlink} at the given offset on the given document,
     * or <code>null</code> if there is no {@link IHyperlink} at that offset on the document.
     * @param offset
     * @param doc
     * @return the {@link IHyperlink} at the given offset on the given document or <code>null</code>
     */
    protected IHyperlink getHyperlink(int offset, IDocument doc) {
        if (offset >= 0 && doc != null) {
            Position[] positions = null;
            try {
                positions = doc.getPositions(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
            } catch (BadPositionCategoryException ex) {
                // no links have been added
                return null;
            }
            for (int i = 0; i < positions.length; i++) {
                Position position = positions[i];
                if (offset >= position.getOffset() && offset <= (position.getOffset() + position.getLength())) {
                    return ((ConsoleHyperlinkPosition)position).getHyperLink();
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the {@link Color} at the given offset on the given document,
     * or <code>null</code> if there is no {@link Color} at that offset on the document.
     * @param offset
     * @param doc
     * @return the {@link Color} at the given offset on the given document or <code>null</code>
     */
    protected Color getColorAtOffset(int offset, IDocument document) throws BadLocationException {
        if (document != null) {
            IDocumentPartitioner partitioner = document.getDocumentPartitioner();
            if (partitioner != null) {
                ITypedRegion[] regions= partitioner.computePartitioning(offset, document.getLineInformationOfOffset(offset).getLength());
                
                for (int i = 0; i < regions.length; i++) {
                    IOConsolePartition partition = (IOConsolePartition)regions[i];
                    return partition.getColor();
                }   
            }
        }
        return null;
    }
}