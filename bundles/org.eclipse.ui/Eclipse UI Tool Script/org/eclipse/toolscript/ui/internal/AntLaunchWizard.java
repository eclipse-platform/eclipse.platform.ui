package org.eclipse.toolscript.ui.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.apache.tools.ant.Project;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.wizard.Wizard;

/**
 * The wizard to run an Ant script file when the Run Ant...
 * context menu action is choosen by the user.
 * <p>
 * Note: Currently there is only one page in this wizard.
 * </p>
 */
public class AntLaunchWizard extends Wizard {
	/**
	 * The file that contains the Ant script.
	 */
	private IFile antFile = null;

	/**
	 * The Ant project described in the xml file.
	 */
	private Project antProject = null;

	/**
	 * The first page of the wizard.
	 */
	private AntLaunchWizardPage page1 = null;

	/**
	 * Creates a new wizard, given the project described in the
	 * file and the file itself.
	 * 
	 * @param antProject
	 * @param antFile
	 */
	public AntLaunchWizard(Project antProject, IFile antFile) {
		super();
		this.antProject = antProject;
		this.antFile = antFile;
		setWindowTitle(ToolScriptMessages.getString("AntLaunchWizard.shellTitle")); //$NON-NLS-1$;
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public void addPages() {
		page1 = new AntLaunchWizardPage(antProject);
		addPage(page1);
		page1.setInitialTargets(getInitialTargets());
		page1.setInitialArguments(getInitialArguments());
		page1.setInitialDisplayLog(getInitialDisplayLog());
	}
	
	/**
	 * Returns the initial Ant targets to use. This information is
	 * restored from the tool script created the last time it was run.
	 * 
	 * @return String[] the name of the targets
	 */
	public String[] getInitialTargets() {
		return new String[0];
	}
	
	/**
	 * Returns the initial arguments to use. This information is
	 * restored from the tool script created the last time it was run.
	 * 
	 * @return String the arguments string
	 */
	public String getInitialArguments() {
		return ""; //$NON-NLS-1$;
	}
	
	/**
	 * Returns the initial selection for showing results to the log.
	 * This information is restored from the tool script created the
	 * last time it was run.
	 * 
	 * @return boolean true if the user wants to show it, false if not
	 */
	public boolean getInitialDisplayLog() {
		return true;
	}

	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
/*		final Vector targetVect = page1.getSelectedTargets();
		AntConsole[] consoles = null;
		final boolean shouldLogMessages = page1.shouldLogMessages();
		final String arguments = page1.getArguments();
		if (shouldLogMessages) {
			try {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				page.showView(AntConsole.CONSOLE_ID);
				console = (AntConsole) page.findView(AntConsole.CONSOLE_ID);

				// Gets all the consoles
				consoles = new AntConsole[AntConsole.getInstances().size()];
				AntConsole.getInstances().toArray(consoles);

				// And clears the ouput for all of them
				for (int i = 0; i < consoles.length; i++)
					consoles[i].clearOutput();
			} catch (PartInitException e) {
				AntUIPlugin.getPlugin().getLog().log(new Status(IStatus.ERROR, AntUIPlugin.PI_ANTUI, 0, Policy.bind("status.consoleNotInitialized"), e));
			}
		}

		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

				monitor.beginTask(Policy.bind("monitor.runningAnt"), targetVect.size());
				try {
					AntRunner runner = new AntRunner();
					AntUIPlugin.getPlugin().setCurrentProgressMonitor(monitor);
					runner.setMessageOutputLevel(getMessageOutputLevel());
					runner.addBuildLogger("org.eclipse.ant.internal.ui.ant.UIBuildLogger");
					runner.setBuildFileLocation(antFile.getLocation().toOSString());
					runner.setArguments(arguments);
					runner.setExecutionTargets(getTargetNames());
					runner.run();
				} catch (BuildCanceledException e) {
					throw new InterruptedException();
				} catch (Exception e) {
					throw new InvocationTargetException(e, e.getMessage());
				} finally {
					monitor.done();
				}
			};
		};

		try {
			this.getContainer().run(true, true, runnable);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			IStatus status = new Status(IStatus.ERROR, AntUIPlugin.PI_ANTUI, EXCEPTION_ANT_EXECUTION, Policy.bind("error.antExecutionErrorGeneral"), e);
			ErrorDialog.openError(getShell(), Policy.bind("error.antExecutionErrorTitle"), Policy.bind("error.antExecutionError"), status);
			return false;
		}

		storeTargetsOnFile(targetVect);
		storeShouldLogMessages();
		storeArguments();
*/
		return true;
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean canFinish() {
		return (page1.getSelectedTargets().size() != 0) || (page1.getArguments().trim() != ""); //$NON-NLS-1$;
	}
}