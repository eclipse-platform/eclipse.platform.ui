package org.eclipse.ant.internal.ui;import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Listener;
import java.util.*;import org.apache.tools.ant.Target;import org.eclipse.ant.core.EclipseProject;import org.eclipse.core.resources.IFile;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class AntLaunchWizardPage extends WizardPage implements ICheckStateListener {
	private Vector selectedTargets = new Vector();
	private CheckboxTableViewer listViewer;
	private EclipseProject project;
	private TargetsListLabelProvider labelProvider = new TargetsListLabelProvider();
	
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 100;
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 500;
	private final static int SIZING_OUTPUT_HEIGHT = 300;
	private final static int SIZING_OUTPUT_WIDTH = 500;
	
	public AntLaunchWizardPage(EclipseProject project) {
		super("execute ant script","Execute Ant Script",null);
		this.project = project;
	}
	
	public void checkStateChanged(CheckStateChangedEvent e) {
		Target checkedTarget = (Target)e.getElement();
		if (e.getChecked())
			selectedTargets.addElement(checkedTarget);
		else
			selectedTargets.removeElement(checkedTarget);
			
		labelProvider.setSelectedTargets(selectedTargets);
		listViewer.refresh();
	}
	
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(
		GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		
		new Label(composite,SWT.NONE).setText("Available Targets:");
		
		listViewer = new CheckboxTableViewer(composite,SWT.BORDER | SWT.CHECK);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		listViewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer,Object o1,Object o2) {
				return ((Target)o1).getName().compareTo(((Target)o2).getName());
			}
		});
		
		listViewer.addCheckStateListener(this);
		listViewer.getTable().setLayoutData(data);
		listViewer.setLabelProvider(labelProvider);
		listViewer.setContentProvider(TargetsListContentProvider.getInstance());
		listViewer.setInput(project);
		
		new Label(composite,SWT.NONE).setText("Arguments:");
		Text argumentsField = new Text(composite,SWT.BORDER);
		argumentsField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(composite,SWT.NONE).setText("Output:");
		Text output = new Text(composite,SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_OUTPUT_HEIGHT;
		data.widthHint = SIZING_OUTPUT_WIDTH;
		output.setLayoutData(data);
		
		setControl(composite);
	}
	
	public Vector getSelectedTargets() {
		return (Vector)selectedTargets.clone();
	}
}
