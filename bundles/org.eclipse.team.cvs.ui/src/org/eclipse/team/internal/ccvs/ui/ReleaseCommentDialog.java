package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Prompts the user for a multi-line comment for releasing to CVS.
 */
public class ReleaseCommentDialog extends Dialog {
	private static final int WIDTH_HINT = 350;
	private static final int HEIGHT_HINT = 50;
	private final static int SELECTION_HEIGHT_HINT = 100;
	
	private String[] comments = new String[0];
	private String comment = "";
	
	private Text text;
	
	private IResource[] unaddedResources;
	
	private CheckboxTableViewer listViewer;

	private IResource[] resourcesToAdd;
	
	private IProject mainProject;
	
	private Combo previousCommentsCombo;
	
	private static final int MAX_COMMENTS = 20;
	/**
	 * ReleaseCommentDialog constructor.
	 * 
	 * @param parentShell  the parent of this dialog
	 */
	public ReleaseCommentDialog(Shell parentShell, IResource[] resourcesToCommit, IResource[] unaddedResources) {
		super(parentShell);
		this.unaddedResources = unaddedResources;
		// this line is required for the CVS UI test framework
		this.resourcesToAdd = unaddedResources;
		// Get a project from which the commit template can be obtained
		if (resourcesToCommit.length > 0) 
			mainProject = resourcesToCommit[0].getProject();
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
		text.selectAll();
		text.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN && (e.stateMask & SWT.CTRL) != 0) {
					e.doit = false;
					okPressed();
				}
			}
		});
		
		previousCommentsCombo = new Combo(composite, SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		previousCommentsCombo.setLayoutData(data);
		previousCommentsCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = previousCommentsCombo.getSelectionIndex();
				if (index != -1)
					text.setText(previousCommentsCombo.getItem(index));
			}
		});
		
		Button clear = new Button(composite, SWT.PUSH);
		clear.setText(Policy.bind("ReleaseCommentDialog.clearTextArea")); //$NON-NLS-1$
		data = new GridData();
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, clear.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		data.horizontalAlignment = GridData.BEGINNING;
		clear.setLayoutData(data);
		clear.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ReleaseCommentDialog.this.clearCommitText();
			}
		});
		
		// if there are unadded resources, show them in a list
		if (hasUnaddedResources()) {
			addUnaddedResourcesArea(composite);
		}
		
		// set F1 help
		WorkbenchHelp.setHelp(composite, IHelpContextIds.RELEASE_COMMENT_DIALOG);	
		
		initializeValues();
		return composite;
	}

	/**
	 * Method initializeValues.
	 */
	private void initializeValues() {
		for (int i = comments.length - 1; i >= 0; --i) {
			previousCommentsCombo.add(comments[i]);
		}
		if (comments.length > 0)
			previousCommentsCombo.select(0);
		text.setText(getSelectedComment());
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
		label.setText(Policy.bind("ReleaseCommentDialog.unaddedResources"));  //$NON-NLS-1$
	
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
	
		Button selectButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID, Policy.bind("ReleaseCommentDialog.selectAll"), false); //$NON-NLS-1$
		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				listViewer.setAllChecked(true);
			}
		};
		selectButton.addSelectionListener(listener);
	
		Button deselectButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID, Policy.bind("ReleaseCommentDialog.deselectAll"), false); //$NON-NLS-1$
		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				listViewer.setAllChecked(false);
	
			}
		};
		deselectButton.addSelectionListener(listener);
	}

	/**
	 * Method getSelectedComment.
	 * @return String
	 */
	private String getSelectedComment() {
		if (comments.length == 0) {
			// There are no previous comments so use the template
			try {
				return getCommitTemplate();
			} catch (CVSException e) {
				// log the exception for now. 
				// The user can surface the problem by trying to reset the comment
				CVSUIPlugin.log(e);
			}
		} else {
			int index = previousCommentsCombo.getSelectionIndex();
			if (index != -1)
				return previousCommentsCombo.getItem(index);
		}
		return "";
	}

	/**
	 * Return the entered comment
	 * 
	 * @return the comment
	 */
	public String[] getComments() {
		return comments;
	}
	/**
	 * Set the initial comment
	 * 
	 * @param comment  the initial comment
	 */
	public void setComments(String[] comments) {
		if (comments == null) comments = new String[0];
		this.comments = comments;
		if (previousCommentsCombo != null) {
			previousCommentsCombo.removeAll();
			initializeValues();
		}
	}
	
	private void addComment(String comment) {
		// Only add the comment if the first entry isn't the same already
		this.comment = comment;
		if (comments.length > 0 && comments[0].equals(comment)) return;
		// Insert the comment as the first element
		String[] newComments = new String[Math.min(comments.length + 1, MAX_COMMENTS)];
		newComments[0] = comment;
		for (int i = 1; i < newComments.length; i++) {
			newComments[i] = comments[i-1];
		}
		comments = newComments;
	}
	
	/*
	 * @see Dialog#okPressed
	 */
	protected void okPressed() {
		addComment(text.getText());
		if (hasUnaddedResources()) {
			BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
				public void run() {
					resourcesToAdd = getSelectedResources();
				}
			});
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

	/**
	 * Method clearCommitText.
	 */
	private void clearCommitText() {
		try {
			text.setText(getCommitTemplate());
			previousCommentsCombo.deselectAll();
		} catch (CVSException e) {
			CVSUIPlugin.openError(getShell(), null, null, e, CVSUIPlugin.PERFORM_SYNC_EXEC);
		}
	}
	
	private String getCommitTemplate() throws CVSException {
		CVSTeamProvider provider = getProvider();
		if (provider == null) return "";
		String template = provider.getCommitTemplate();
		if (template == null) template = "";
		return template;
	}

	/**
	 * Method getProvider.
	 */
	private CVSTeamProvider getProvider() throws CVSException {
		if (mainProject == null) return null;
		return (CVSTeamProvider) RepositoryProvider.getProvider(mainProject, CVSProviderPlugin.getTypeId());
	}
	/**
	 * Returns the comment.
	 * @return String
	 */
	public String getComment() {
		return comment;
	}

}
