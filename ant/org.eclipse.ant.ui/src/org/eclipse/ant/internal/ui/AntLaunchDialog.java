package org.eclipse.ant.internal.ui;import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;import org.eclipse.swt.widgets.Listener;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class AntLaunchDialog extends Dialog {
	private IFile antFile;
	private IStructuredSelection selectedTargets;
	private ListViewer listViewer;
	
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
		
		listViewer = new ListViewer(composite,SWT.BORDER | SWT.CHECK);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		listViewer.getList().setLayoutData(data);
		listViewer.setLabelProvider(AntLaunchDialogLabelProvider.getInstance());
		listViewer.setContentProvider(AntLaunchDialogContentProvider.getInstance());
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
		selectedTargets = (IStructuredSelection)listViewer.getSelection();
		close();
	}
}
