package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.model.WorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Prompts the user for a multi-line comment for releasing to CVS.
 */
public class ReleaseCommentDialog extends Dialog {
	private static final int WIDTH_HINT = 350;
	private static final int HEIGHT_HINT = 50;
	private final static int SELECTION_HEIGHT_HINT = 100;
	
	private String comment = ""; //$NON-NLS-1$
	
	private Text text;
	
	private IResource[] unaddedResources;
	
	private CheckboxTableViewer listViewer;

	private IResource[] resourcesToAdd;
	
	/**
	 * ReleaseCommentDialog constructor.
	 * 
	 * @param parentShell  the parent of this dialog
	 */
	public ReleaseCommentDialog(Shell parentShell) {
		super(parentShell);
	}
	
	public ReleaseCommentDialog(Shell parentShell, IResource[] unaddedResources) {
		super(parentShell);
		this.unaddedResources = unaddedResources;
	}
	
	/*
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		getShell().setText(Policy.bind("ReleaseCommentDialog.title")); //$NON-NLS-1$
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText(Policy.bind("ReleaseCommentDialog.enterComment")); //$NON-NLS-1$
		
		text = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = WIDTH_HINT;
		data.heightHint = HEIGHT_HINT;
		
		text.setLayoutData(data);
		text.setText(comment);
		text.selectAll();
		text.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN && (e.stateMask & SWT.CTRL) != 0) {
					e.doit = false;
					okPressed();
				}
			}
		});
		
		// if there are unadded resources, show them in a list
		if (hasUnaddedResources()) {
			addUnaddedResourcesArea(composite);
		}
		
		// set F1 help
		WorkbenchHelp.setHelp(composite, IHelpContextIds.RELEASE_COMMENT_DIALOG);	
		
		return composite;
	}

	private boolean hasUnaddedResources() {
		return unaddedResources != null && unaddedResources.length > 0;
	}

	/**
	 * Method addUnaddedResourcesArea.
	 * @param parent
	 */
	private void addUnaddedResourcesArea(Composite composite) {
		
		// add a description label
		Label label = new Label(composite, SWT.LEFT);
		label.setText(Policy.bind("ReleaseCommentDialog.unaddedResources")); 
	
		// add the selectable checkbox list
		listViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SELECTION_HEIGHT_HINT;
		data.widthHint = WIDTH_HINT;
		listViewer.getTable().setLayoutData(data);

		// set the contents of the list
		listViewer.setLabelProvider(new WorkbenchLabelProvider() {
			protected String decorateText(String input, Object element) {
				if (element instanceof IResource)
					return ((IResource)element).getFullPath().toString();
				else
					return input;
			}
		});
		listViewer.setContentProvider(new WorkbenchContentProvider());
		listViewer.setInput(new AdaptableResourceList(unaddedResources));
		listViewer.setAllChecked(true);
		addSelectionButtons(composite);
	}

	/**
	 * Add the selection and deselection buttons to the dialog.
	 * @param composite org.eclipse.swt.widgets.Composite
	 */
	private void addSelectionButtons(Composite composite) {
	
		Composite buttonComposite = new Composite(composite, SWT.RIGHT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonComposite.setLayout(layout);
		GridData data =
			new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		composite.setData(data);
	
		Button selectButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID, Policy.bind("ReleaseCommentDialog.selectAll"), false);
		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				listViewer.setAllChecked(true);
			}
		};
		selectButton.addSelectionListener(listener);
	
		Button deselectButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID, Policy.bind("ReleaseCommentDialog.deselectAll"), false);
		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				listViewer.setAllChecked(false);
	
			}
		};
		deselectButton.addSelectionListener(listener);
	}
	/**
	 * Return the entered comment
	 * 
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}
	/**
	 * Set the initial comment
	 * 
	 * @param comment  the initial comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	/*
	 * @see Dialog#okPressed
	 */
	protected void okPressed() {
		comment = text.getText();
		if (hasUnaddedResources()) {
			resourcesToAdd = getSelectedResources();
		} else {
			resourcesToAdd = new IResource[0];
		}
		super.okPressed();
	}

	/**
	 * Method getSelectedResources.
	 * @return IResource[]
	 */
	private IResource[] getSelectedResources() {
		// Build a list of selected resources.
		ArrayList list = new ArrayList();
		for (int i = 0; i < unaddedResources.length; ++i) {
			if (listViewer.getChecked(unaddedResources[i]))
				list.add(unaddedResources[i]);
		}
		return (IResource[]) list.toArray(new IResource[list.size()]);
	}
	
	/**
	 * Returns the resourcesToAdd.
	 * @return IResource[]
	 */
	public IResource[] getResourcesToAdd() {
		return resourcesToAdd;
	}

}
