package org.eclipse.ant.internal.ui;import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;import org.eclipse.jface.viewers.*;import org.eclipse.swt.widgets.Listener;
import java.util.*;import org.apache.tools.ant.Target;import org.eclipse.ant.core.EclipseProject;import org.eclipse.core.resources.IFile;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class AntLaunchDialog extends Dialog {
	private IFile antFile;
	private IStructuredSelection selectedTargets;
	private CheckboxTableViewer listViewer;
	private EclipseProject project;
	
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 100;
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 500;
	private final static int SIZING_OUTPUT_HEIGHT = 300;
	private final static int SIZING_OUTPUT_WIDTH = 500;
	
	public AntLaunchDialog(Shell parent,IFile antFile) {
		super(parent);
		this.antFile = antFile;
		parent.setText("Execute Ant Script");
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);
		
		new Label(composite,SWT.NONE).setText("Available Targets:");
		
		listViewer = new CheckboxTableViewer(composite,SWT.BORDER | SWT.CHECK | SWT.MULTI);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		listViewer.getTable().setLayoutData(data);
		listViewer.setLabelProvider(AntLaunchDialogLabelProvider.getInstance());
		listViewer.setContentProvider(AntLaunchDialogContentProvider.getInstance(this));
		listViewer.setInput(antFile);
		
		new Label(composite,SWT.NONE).setText("Arguments:");
		Text argumentsField = new Text(composite,SWT.BORDER);
		argumentsField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(composite,SWT.NONE).setText("Output:");
		Text output = new Text(composite,SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_OUTPUT_HEIGHT;
		data.widthHint = SIZING_OUTPUT_WIDTH;
		output.setLayoutData(data);
				
		return composite;
	}
	
	protected void okPressed() {
		Target targets[] = (Target [])listViewer.getCheckedElements();

		// build a vector containing the name of the selected target so that we can run them
		Vector targetVect  = new Vector(targets.length);
		for (int i = 0; i < targets.length; i++)
			targetVect.add(targets[i].getName());
		
		// Build Listener - TEST
		//project.addBuildListener(new UIBuildListener(new ProgressMonitorDialog(AntUIPlugin.getPlugin().getWorkbench().getActiveWorkbenchWindow().getShell()).getProgressMonitor(), antFile));
		
		// and then ask the project to execute them
		project.executeTargets(targetVect);
		
		close();
	}
	
	protected void setProject(EclipseProject newProject) {
		project = newProject;
	}
	
}
