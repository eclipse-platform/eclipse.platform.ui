package org.eclipse.ant.internal.ui;/* * (c) Copyright IBM Corp. 2000, 2001. * All Rights Reserved. */import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Listener;
import java.util.*;import org.apache.tools.ant.Target;import org.eclipse.ant.core.EclipseProject;import org.eclipse.core.resources.IFile;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class AntLaunchWizardPage extends WizardPage implements ICheckStateListener {
	private Vector selectedTargets = new Vector();
	private CheckboxTableViewer listViewer;
	private EclipseProject project;
	private TargetsListLabelProvider labelProvider = new TargetsListLabelProvider();
	private String initialTargetSelections[];
	private Button showLog;	private Text argumentsField;
	
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 200;
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 200;
	
	public AntLaunchWizardPage(EclipseProject project) {
		super("execute ant script",Policy.bind("wizard.executeAntScriptTitle"),null);
		this.project = project;
	}
	
	public void checkStateChanged(CheckStateChangedEvent e) {
		Target checkedTarget = (Target)e.getElement();
		if (e.getChecked())
			selectedTargets.addElement(checkedTarget);
		else
			selectedTargets.removeElement(checkedTarget);
			
		labelProvider.setSelectedTargets(selectedTargets);
		listViewer.refresh();				// need to tell the wizard container to refresh his buttons		getWizard().getContainer().updateButtons();
	}
	
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(
		GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		
		new Label(composite,SWT.NONE).setText(Policy.bind("wizard.availableTargetsLabel"));
		
		listViewer = new CheckboxTableViewer(composite,SWT.BORDER | SWT.CHECK);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		listViewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer,Object o1,Object o2) {
				return ((Target)o1).getName().compareTo(((Target)o2).getName());
			}
		});
		
		listViewer.getTable().setLayoutData(data);
		listViewer.setLabelProvider(labelProvider);
		listViewer.setContentProvider(TargetsListContentProvider.getInstance());
		listViewer.setInput(project);
		
		new Label(composite,SWT.NONE).setText(Policy.bind("wizard.argumentsLabel"));
		argumentsField = new Text(composite,SWT.BORDER);
		argumentsField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		// adds a listener to tell the wizard when it can tell its container to refresh the buttons		argumentsField.addModifyListener( new ModifyListener() {			public void modifyText(ModifyEvent e) {				AntLaunchWizardPage.this.getWizard().getContainer().updateButtons();			}		});
		
		showLog = new Button(composite, SWT.CHECK);
		showLog.setText(Policy.bind("wizard.displayLogLabel"));		showLog.setSelection(((AntLaunchWizard) getWizard()).getWantToShowLogOnSuccess());

		restorePreviousSelectedTargets();
		listViewer.addCheckStateListener(this);
		listViewer.refresh();
		setControl(composite);
	}
	
	public Vector getSelectedTargets() {
		return (Vector)selectedTargets.clone();
	}
	
	protected void restorePreviousSelectedTargets() {
		if (initialTargetSelections == null)
			return;
		
		Vector result = new Vector();
		Object availableTargets[] = TargetsListContentProvider.getInstance().getElements(project);
		for (int i = 0; i < initialTargetSelections.length; i++) {
			String currentTargetName = initialTargetSelections[i];
			for (int j = 0; j < availableTargets.length; j++) {
				if (((Target)availableTargets[j]).getName().equals(currentTargetName)) {
					result.addElement(availableTargets[j]);
					listViewer.setChecked(availableTargets[j],true);
					continue;
				}
			}
		}
		
		selectedTargets = result;
		labelProvider.setSelectedTargets(selectedTargets);
	}

	public void setInitialTargetSelections(String value[]) {
		initialTargetSelections = value;
	}		/**	 * Returns the arguments that the user may have entered to run the ant file.	 * 	 * @return String the arguments	 */	public String getArgumentsFromField() {		return argumentsField.getText();	}		/**	 * 	 */	public boolean shouldLogMessages() {		return showLog.getSelection();	}
	
}
