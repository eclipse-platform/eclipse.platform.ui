package org.eclipse.ant.internal.ui;/* * (c) Copyright IBM Corp. 2000, 2001. * All Rights Reserved. */import java.lang.reflect.InvocationTargetException;import java.util.*;import org.apache.tools.ant.Target;import org.eclipse.ant.core.*;import org.eclipse.core.resources.IFile;import org.eclipse.core.runtime.CoreException;import org.eclipse.core.runtime.IProgressMonitor;import org.eclipse.core.runtime.IStatus;import org.eclipse.core.runtime.QualifiedName;import org.eclipse.core.runtime.Status;import org.eclipse.jface.operation.IRunnableWithProgress;import org.eclipse.jface.wizard.Wizard;

public class AntLaunchWizard extends Wizard {
	
	private EclipseProject project = null;
	private AntLaunchWizardPage page1 = null;
	private IFile antFile = null;
	
	private final static String SEPARATOR_TARGETS = "\"";
	private final static String PROPERTY_SELECTEDTARGETS = "selectedTargets";
	
	public AntLaunchWizard(EclipseProject project,IFile antFile) {
		super();
		this.project = project;
		this.antFile = antFile;
	}
	
	public void addPages() {
		page1 = new AntLaunchWizardPage(project);
		addPage(page1);
		page1.setInitialTargetSelections(getTargetNamesToPreselect());
	}
	
	public String[] getTargetNamesToPreselect() {
		String propertyString = null;
		try {
			propertyString = antFile.getPersistentProperty(
				new QualifiedName(AntUIPlugin.PI_ANTUI,PROPERTY_SELECTEDTARGETS));
		} catch (CoreException e) {
			new Status(
				IStatus.WARNING,
				AntUIPlugin.PI_ANTUI,
				IStatus.WARNING,
				Policy.bind("status.targetNotRead", antFile.getFullPath().toString()),
				e);
		}
				if (propertyString == null)			return new String[0];
		StringTokenizer tokenizer = new StringTokenizer(propertyString,SEPARATOR_TARGETS);
		String result[] = new String[tokenizer.countTokens()];
		int index = 0;
		while (tokenizer.hasMoreTokens())
			result[index++] = tokenizer.nextToken();

		return result;
	}
			
	
			
	public boolean performFinish() {
		Vector targetVect = page1.getSelectedTargets();

		// and then ask the project to execute them -- SEEMS TO CRASH
		// project.executeTargets(targetVect);				// get the command line arguments		String commandArgs = page1.getArgumentsFromField();		// and tokenize them		StringTokenizer tokenizer = new StringTokenizer(commandArgs," ");		String args[] = new String[tokenizer.countTokens()+targetVect.size()+2];		int index = 0;		while (tokenizer.hasMoreTokens())			args[index++] = tokenizer.nextToken();		args[index++] = "-buildfile";		args[index++] = antFile.getLocation().toOSString();		Iterator argsIterator = targetVect.iterator();		while (argsIterator.hasNext())			args[index++] = ((Target) argsIterator.next()).getName();				//monitor.beginTask("Running Ant", IProgressMonitor.UNKNOWN);		try {			//TBD: should remove the build listener somehow			new AntRunner().run(args/*, new UIBuildListener(monitor, antFile)*/);		} 		catch (BuildCanceledException e) {			// build was canceled don't propagate exception			return false;		}		catch (Exception e) {			// should do something here			return false;		}					
/*		
		this.getContainer().run(true,true,new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				String buildFileName= null;
				buildFileName= antFile.getLocation().toOSString();

				String[] args= {"-buildfile", buildFileName};
				monitor.beginTask("Running Ant", IProgressMonitor.UNKNOWN);

				try {
					//TBD: should remove the build listener somehow
					new AntRunner().run(args, new UIBuildListener(monitor, antFile));
				} 
				catch (BuildCanceledException e) {
					// build was canceled don't propagate exception
					return;
				}
				catch (Exception e) {
					throw new InvocationTargetException(e);
				}
				finally {
					monitor.done();
				}
			};
		});
*/

		// asuming that all went well...
		storeTargetsOnFile(targetVect);
		return true;
	}

	protected void storeTargetsOnFile(Vector targets) {
		StringBuffer targetString = new StringBuffer();
		Iterator targetsIt = targets.iterator();
		
		while (targetsIt.hasNext()) {
			targetString.append(((Target)targetsIt.next()).getName());
			targetString.append(SEPARATOR_TARGETS);
		}
		
		try {
			antFile.setPersistentProperty(
				new QualifiedName(AntUIPlugin.PI_ANTUI,PROPERTY_SELECTEDTARGETS),
				targetString.toString());
		} catch (CoreException e) {
			AntUIPlugin.getPlugin().getLog().log(
				new Status(
					IStatus.WARNING,
					AntUIPlugin.PI_ANTUI,
					IStatus.WARNING,
					Policy.bind("status.targetNotWritten", antFile.getFullPath().toString()),
					e));
		}
	}
}
