package org.eclipse.ant.internal.ui;import java.util.Vector;import org.apache.tools.ant.Target;import org.eclipse.ant.core.AntRunner;import org.eclipse.ant.core.EclipseProject;import org.eclipse.core.resources.IFile;import org.eclipse.core.runtime.IProgressMonitor;import org.eclipse.jface.operation.IRunnableWithProgress;import org.eclipse.jface.wizard.Wizard;

public class AntLaunchWizard extends Wizard {
	
	private EclipseProject project = null;
	private AntLaunchWizardPage page1 = null;
	private IFile antFile = null;
	
	public AntLaunchWizard(EclipseProject project,IFile antFile) {
		super();
		this.project = project;
		this.antFile = antFile;
	}
	
	public boolean performFinish() {
		Vector targetVect = page1.getSelectedTargets();

		// Build Listener - TEST
		//project.addBuildListener(new UIBuildListener(new ProgressMonitorDialog(AntUIPlugin.getPlugin().getWorkbench().getActiveWorkbenchWindow().getShell()).getProgressMonitor(), antFile));
		
		// and then ask the project to execute them
		project.executeTargets(targetVect);
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
		return true;
	}
	
	public void addPages() {
		page1 = new AntLaunchWizardPage(project);
		addPage(page1);
	}

}
