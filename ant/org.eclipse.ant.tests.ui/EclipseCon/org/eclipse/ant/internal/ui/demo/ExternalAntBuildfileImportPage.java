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
package org.eclipse.ant.internal.ui.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.taskdefs.Javac;
import org.eclipse.ant.internal.ui.editor.model.AntElementNode;
import org.eclipse.ant.internal.ui.editor.model.AntProjectNode;
import org.eclipse.ant.internal.ui.editor.model.AntTargetNode;
import org.eclipse.ant.internal.ui.editor.model.AntTaskNode;
import org.eclipse.ant.internal.ui.editor.outline.AntModel;
import org.eclipse.ant.internal.ui.editor.outline.ILocationProvider;
import org.eclipse.ant.internal.ui.editor.outline.XMLCore;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IHelpContextIds;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

public class ExternalAntBuildfileImportPage extends WizardPage {

	private FileFilter projectFilter = new FileFilter() {
			//Only accept those files that are .xml
	public boolean accept(File pathName) {
			return pathName.getName().endsWith(".xml");
		}
	};
	
	private static class ImportOverwriteQuery implements IOverwriteQuery {
		public String queryOverwrite(String file) {
			return ALL;
		}	
	}	

	//Keep track of the directory that we browsed to last time
	//the wizard was invoked.
	private static String previouslyBrowsedDirectory = ""; //$NON-NLS-1$

	// widgets
	private Text projectNameField;
	private Text locationPathField;
	private Button browseButton;
	private IProjectDescription description;
	
	private AntModel fAntModel;
	private IDocument fCurrentDocument;

	private Listener locationModifyListener = new Listener() {
		public void handleEvent(Event e) {
			setPageComplete(validatePage());
		}
	};

	// constants
	private static final int SIZING_TEXT_FIELD_WIDTH = 250;
	
	/**
	 * Creates a new project creation wizard page.
	 *
	 * @param pageName the name of this page
	 */
	public ExternalAntBuildfileImportPage() {
		super("externalAntBuildfilePage"); //$NON-NLS-1$
		setPageComplete(false);
		setTitle("Import a Project from an Ant Buildfile");
		setDescription("Creates a new project based on the specification in the javac task of the Ant buildfile. This does not copy the source contents to the workspace");

	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		
		initializeDialogUnits(parent);
		
		Composite composite = new Composite(parent, SWT.NULL);

		WorkbenchHelp.setHelp(composite, IHelpContextIds.NEW_PROJECT_WIZARD_PAGE);

		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());

		createProjectNameGroup(composite);
		createProjectLocationGroup(composite);
		validatePage();
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
	}

	/**
	 * Creates the project location specification controls.
	 *
	 * @param parent the parent composite
	 */
	private final void createProjectLocationGroup(Composite parent) {

		// project specification group
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projectGroup.setFont(parent.getFont());

		// new project label
		Label projectContentsLabel = new Label(projectGroup, SWT.NONE);
		projectContentsLabel.setText("&Ant Buildfile:");
		projectContentsLabel.setFont(parent.getFont());

		createUserSpecifiedProjectLocationGroup(projectGroup);
	}
	/**
	 * Creates the project name specification controls.
	 *
	 * @param parent the parent composite
	 */
	private final void createProjectNameGroup(Composite parent) {
		
		Font dialogFont = parent.getFont();
		
		// project specification group
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		projectGroup.setFont(dialogFont);
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// new project label
		Label projectLabel = new Label(projectGroup, SWT.NONE);
		projectLabel.setText("&Project Name:");
		projectLabel.setFont(dialogFont);

		// new project name entry field
		projectNameField = new Text(projectGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		projectNameField.setLayoutData(data);
		projectNameField.setFont(dialogFont);
	}
	/**
	 * Creates the project location specification controls.
	 *
	 * @param projectGroup the parent composite
	 * @param boolean - the initial enabled state of the widgets created
	 */
	private void createUserSpecifiedProjectLocationGroup(Composite projectGroup) {
		
		Font dialogFont = projectGroup.getFont();

		// project location entry field
		this.locationPathField = new Text(projectGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		this.locationPathField.setLayoutData(data);
		this.locationPathField.setFont(dialogFont);

		// browse button
		this.browseButton = new Button(projectGroup, SWT.PUSH);
		this.browseButton.setText("B&rowse...");
		this.browseButton.setFont(dialogFont);
		setButtonLayoutData(this.browseButton);
		
		this.browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleBrowseButtonPressed();
			}
		});

		locationPathField.addListener(SWT.Modify, locationModifyListener);
	}
	/**
	 * Returns the current project location path as entered by 
	 * the user, or its anticipated initial value.
	 *
	 * @return the project location path, its anticipated initial value, or <code>null</code>
	 *   if no project location path is known
	 */
	public IPath getLocationPath() {

		return new Path(getProjectLocationFieldValue());
	}
	/**
	 * Creates a project resource handle for the current project name field value.
	 * <p>
	 * This method does not create the project resource; this is the responsibility
	 * of <code>IProject::create</code> invoked by the new project resource wizard.
	 * </p>
	 *
	 * @return the new project resource handle
	 */
	public IProject getProjectHandle() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName(getProjectNode()));
	}
	/**
	 * Returns the current project name as entered by the user, or its anticipated
	 * initial value.
	 *
	 * @return the project name, its anticipated initial value, or <code>null</code>
	 *   if no project name is known
	 */
	public String getProjectName(AntProjectNode projectNode) {
		String userSpecifiedName= getProjectNameFieldValue();
		String projectName= projectNode.getLabel();
		if (projectName == null) {
			projectName= "Ant Project";
		}
		return projectName;
	}
	/**
	 * Returns the value of the project name field
	 * with leading and trailing spaces removed.
	 * 
	 * @return the project name in the field
	 */
	private String getProjectNameFieldValue() {
		if (projectNameField == null)
			return ""; //$NON-NLS-1$
		else
			return projectNameField.getText().trim();
	}
	/**
	 * Returns the value of the project location field
	 * with leading and trailing spaces removed.
	 * 
	 * @return the project location directory in the field
	 */
	private String getProjectLocationFieldValue() {
		return locationPathField.getText().trim();
	}
	
	/**
	 * Determine the buildfile the user wishes to operate from
	 */
	private void handleBrowseButtonPressed() {
		
			String lastUsedPath= null;
			//lastUsedPath= dialogSettings.get(IAntUIConstants.DIALOGSTORE_LASTEXTFILE);
			if (lastUsedPath == null) {
				lastUsedPath= ""; //$NON-NLS-1$
			}
			FileDialog dialog = new FileDialog(getShell(), SWT.SINGLE);
			dialog.setFilterExtensions(new String[] { "*.xml" }); //$NON-NLS-1$;
			dialog.setFilterPath(lastUsedPath);

			String result = dialog.open();
			if (result == null) {
				return;
			}
			IPath filterPath= new Path(dialog.getFilterPath());
			String buildFileName= dialog.getFileName();
			IPath path= filterPath.append(buildFileName).makeAbsolute();	
			
//			previouslyBrowsedDirectory = selectedDirectory;
			locationPathField.setText(path.toOSString());
//			setProjectName(projectFile(previouslyBrowsedDirectory));
			
	}

	/**
	 * Returns whether this page's controls currently all contain valid 
	 * values.
	 *
	 * @return <code>true</code> if all controls are valid, and
	 *   <code>false</code> if at least one is invalid
	 */
	private boolean validatePage() {

		String locationFieldContents = getProjectLocationFieldValue();

		if (locationFieldContents.equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setMessage("No buildfile selected");
			return false;
		}

		IPath path = new Path(""); //$NON-NLS-1$
		if (!path.isValidPath(locationFieldContents)) {
			setErrorMessage("Location error");
			return false;
		}

//		File projectFile = projectFile(locationFieldContents);
//		if (projectFile == null) {
//			setErrorMessage(
//				DataTransferMessages.format(
//					"WizardExternalProjectImportPage.notAProject", //$NON-NLS-1$
//					new String[] { locationFieldContents }));
//			return false;
//		}
//		else{
//			setProjectName(projectFile);
//		}
		
		//if (getProjectHandle().exists()) {
		//	setErrorMessage("Project with this name already exists");
		//	return false;
		//}

		setErrorMessage(null);
		setMessage(null);
		return true;
	}
	private IWorkspace getWorkspace() {
		IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();
		return workspace;
	}

	/**
	 * Set the project name using either the name of the
	 * parent of the file or the name entry in the xml for 
	 * the file
	 */
	private void setProjectName(File projectFile) {

		//If there is no file or the user has already specified forget it
		if (projectFile == null)
			return;

		IPath path = new Path(projectFile.getPath());

		IProjectDescription newDescription = null;

		try {
			newDescription = getWorkspace().loadProjectDescription(path);
		} catch (CoreException exception) {
			//no good couldn't get the name
		}

		if (newDescription == null) {
			this.description = null;
			this.projectNameField.setText(""); //$NON-NLS-1$
		}
		else{			
			this.description = newDescription;
			this.projectNameField.setText(this.description.getName());
		}
	}

	/**
	 * Return a .xml file from the specified location.
	 * If there isn't one return null.
	 */
	private File getBuildFile(String locationFieldContents) {
		File buildFile = new File(locationFieldContents);
		if (!buildFile.isFile() && buildFile.exists()) { 
			return null;
		}

		return buildFile;
	}

	/**
	 * Creates a new project resource based on the Ant buildfile.
	 *
	 * @return the created project resource, or <code>null</code> if the project
	 *    was not created
	 */
	protected IJavaProject createProject() {
		
		fAntModel= getAntModel(getBuildFile(getProjectLocationFieldValue()));
		AntProjectNode projectNode= getProjectNode();
		
		final List javacNodes= new ArrayList();
		getJavacNodes(javacNodes, projectNode);
		final IJavaProject[] result= new IJavaProject[1];
		final String projectName= getProjectName(projectNode);
		final File buildFile= getBuildFile(getProjectLocationFieldValue());
		if (javacNodes.size() > 1) {
			MessageDialog.openInformation(
				getShell(),
				"Ant Demo",
				"Currently only supports creating a project from a single javac declaration");
			return null;
		}
		
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) throws CoreException {
				List javacTasks= resolveJavacTasks(javacNodes);
				//TODO no javactasks...throw CoreException
				ProjectCreator creator= new ProjectCreator();
				Iterator iter= javacTasks.iterator();
				while (iter.hasNext()) {
					Javac javacTask = (Javac) iter.next();
					IJavaProject javaProject= creator.createJavaProjectFromJavacNode(projectName, javacTask);
					importBuildFile(monitor, javaProject.getPath(), buildFile);
					result[0]= javaProject;
				}
			}
		};
		
		//run the new project creation operation
		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return null;
		} catch (InvocationTargetException e) {
			// ie.- one of the steps resulted in a core exception	
			Throwable t = e.getTargetException();
			if (t instanceof CoreException) {	
				ErrorDialog.openError(getShell(), "Error occurred creating project",
				null, ((CoreException) t).getStatus());
			}
		}
		
		return result[0];
	}
	
	protected void importBuildFile(IProgressMonitor monitor, IPath destPath, File buildFile) {
		IImportStructureProvider structureProvider = FileSystemStructureProvider.INSTANCE;
		List files = new ArrayList(1);
		
		files.add(buildFile);
		File rootDir= buildFile.getParentFile();
		try {
			ImportOperation op= new ImportOperation(destPath, rootDir, structureProvider, new ImportOverwriteQuery(), files);
			op.setCreateContainerStructure(false);
			op.run(monitor);
		} catch (InterruptedException e) {
			// should not happen
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof CoreException) {	
				ErrorDialog.openError(getShell(), "Error occurred importing buildfile",
				null, ((CoreException) t).getStatus());
			}
		}
	}

	private List resolveJavacTasks(List javacNodes) {
		List resolvedJavacTasks= new ArrayList(javacNodes.size());
		Iterator nodes= javacNodes.iterator();
		while (nodes.hasNext()) {
			AntTaskNode taskNode = (AntTaskNode) nodes.next();
			Task javacTask= taskNode.getTask();
			if (javacTask instanceof UnknownElement) {
				if (((UnknownElement)javacTask).getRealThing() == null) {
					javacTask.maybeConfigure();
				}
				
				resolvedJavacTasks.add(((UnknownElement)javacTask).getRealThing());
			} else {
				resolvedJavacTasks.add(javacTask);
			}
			
		}
		return resolvedJavacTasks;
	}
	
	private AntModel getAntModel(final File buildFile) {
		fCurrentDocument= getDocument(buildFile);
		AntModel model= new AntModel(XMLCore.getDefault(), fCurrentDocument, null, new ILocationProvider() {
			/* (non-Javadoc)
			 * @see org.eclipse.ant.internal.ui.editor.outline.ILocationProvider#getLocation()
			 */
			public IPath getLocation() {
				return new Path(buildFile.getAbsolutePath());
			}
		});
		model.reconcile();
		return model;
	}
	
	private IDocument getDocument(File buildFile) {
		InputStream in;
		try {
			in = new FileInputStream(buildFile);
		} catch (FileNotFoundException e) {
			return null;
		}
		String initialContent= getStreamContentAsString(in);
		return new Document(initialContent);
	}
	
	private AntProjectNode getProjectNode() {
		AntElementNode[] nodes= fAntModel.getRootElements();
		if (nodes.length == 0) {
			return null;
		}
		AntProjectNode projectNode= (AntProjectNode)nodes[0];
		return projectNode;
	}
	
	private String getStreamContentAsString(InputStream inputStream) {
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(inputStream, ResourcesPlugin.getEncoding());
		} catch (UnsupportedEncodingException e) {
			AntUIPlugin.log(e);
			return ""; //$NON-NLS-1$
		}

		return getReaderContentAsString( new BufferedReader(reader));
	}
	
	private String getReaderContentAsString(BufferedReader bufferedReader) {
		StringBuffer result = new StringBuffer();
		try {
			String line= bufferedReader.readLine();

			while(line != null) {
				if(result.length() != 0) {
					result.append("\n"); //$NON-NLS-1$
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
	
	private void getJavacNodes(List javacNodes, AntElementNode parent) {
		List children= parent.getChildNodes();
		for (Iterator iter = children.iterator(); iter.hasNext();) {
			AntElementNode node = (AntElementNode) iter.next();
			if (node instanceof AntTargetNode) {
				getJavacNodes(javacNodes, node);
			} else if (node instanceof AntTaskNode) {
				AntTaskNode task= (AntTaskNode)node;
				if (task.getName() == "javac") { //$NON-NLS-1$
					javacNodes.add(task);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if(visible)
			this.locationPathField.setFocus();
	}
}
