/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids - bug 57208
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ui.dialogs.DialogArea;

/**
 * This area provides the widgets for providing the CVS commit comment
 */
public class CommitCommentArea extends DialogArea {

	private static final int WIDTH_HINT = 350;
	private static final int HEIGHT_HINT = 50;
	
	private Text text;
	private Combo previousCommentsCombo;
	private IProject mainProject;
	private String[] comments = new String[0];
	private String comment = ""; //$NON-NLS-1$
	
	public static final String OK_REQUESTED = "OkRequested";//$NON-NLS-1$
	public static final String COMMENT_MODIFIED = "CommentModified";//$NON-NLS-1$
    private String proposedComment;
	
	/**
	 * Constructor for CommitCommentArea.
	 * @param parentDialog
	 * @param settings
	 */
	public CommitCommentArea() {
		comments = CVSUIPlugin.getPlugin().getRepositoryManager().getPreviousComments();
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.DialogArea#createArea(org.eclipse.swt.widgets.Composite)
	 */
	public void createArea(Composite parent) {
        Dialog.applyDialogFont(parent);
		Composite composite = createGrabbingComposite(parent, 1);
		initializeDialogUnits(composite);
						
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
					CommitCommentArea.this.signalCtrlEnter();
				}
			}
		});
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
			    String oldComment = comment;
				comment = text.getText();
				CommitCommentArea.this.signalCommentModified(oldComment, comment);
			}
		});
		
		
		label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText(Policy.bind("ReleaseCommentDialog.choosePrevious")); //$NON-NLS-1$
		
		previousCommentsCombo = new Combo(composite, SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		previousCommentsCombo.setLayoutData(data);
		
		// Initialize the values before we register any listeners so
		// we don't get any platform specific selection behavior
		// (see bug 32078: http://bugs.eclipse.org/bugs/show_bug.cgi?id=32078)
		initializeValues();
		
		previousCommentsCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = previousCommentsCombo.getSelectionIndex();
				if (index != -1)
					text.setText(comments[index]);
			}
		});
	}

    /**
	 * Method initializeValues.
	 */
	private void initializeValues() {
		
		// populate the previous comment list
		for (int i = 0; i < comments.length; i++) {
			previousCommentsCombo.add(HistoryView.flattenText(comments[i]));
		}
		
		// We don't want to have an initial selection
		// (see bug 32078: http://bugs.eclipse.org/bugs/show_bug.cgi?id=32078)
		previousCommentsCombo.setText(""); //$NON-NLS-1$
		
		// determine the initial comment text
		String initialComment = proposedComment;
		if (initialComment == null) {
			try {
				initialComment = getCommitTemplate();
			} catch (CVSException e) {
				CVSUIPlugin.log(e);
			}
		}
		if (initialComment != null && initialComment.length() != 0) {
			text.setText(initialComment);
		}
	}

	/**
	 * Method signalCtrlEnter.
	 */
	private void signalCtrlEnter() {
		firePropertyChangeChange(OK_REQUESTED, null, null);
	}
	
    protected void signalCommentModified(String oldValue, String comment) {
        firePropertyChangeChange(COMMENT_MODIFIED, oldValue, comment);
    }

	private String getCommitTemplate() throws CVSException {
		CVSTeamProvider provider = getProvider();
		if (provider == null) return ""; //$NON-NLS-1$
		String template = provider.getCommitTemplate();
		if (template == null) template = ""; //$NON-NLS-1$
		return template;
	}
	
	/**
	 * Method getProvider.
	 */
	private CVSTeamProvider getProvider() {
		if (mainProject == null) return null;
		return (CVSTeamProvider) RepositoryProvider.getProvider(mainProject, CVSProviderPlugin.getTypeId());
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
	 * Returns the comment.
	 * @return String
	 */
	public String getComment() {
		if (comment != null && comment.length() > 0) finished();
		return comment;
	}

	/**
	 * Method setProject.
	 * @param iProject
	 */
	public void setProject(IProject iProject) {
		this.mainProject = iProject;
	}
	
	private void finished() {
		// strip template from the comment entered
		try {
			String commitTemplate = getCommitTemplate();
			if (comment.startsWith(commitTemplate)) {
				comment = comment.substring(commitTemplate.length());
			} else if (comment.endsWith(commitTemplate)) {
				comment = comment.substring(0, comment.length() - commitTemplate.length());
			}
		} catch (CVSException e) {
			// we couldn't get the commit template. Log the error and continue
			CVSUIPlugin.log(e);
		}
		// if there is still a comment, remember it
		if (comment.length() > 0) {
			CVSUIPlugin.getPlugin().getRepositoryManager().addComment(comment);
		}
	}
	
	public void setFocus() {
		if (text != null) {
			text.setFocus();
		}
	}

    public void setProposedComment(String proposedComment) {
        this.proposedComment = proposedComment;
    }

    public boolean hasCommitTemplate() {
        try {
             String commitTemplate = getCommitTemplate();
            return commitTemplate != null && commitTemplate.length() > 0;
        } catch (CVSException e) {
            CVSUIPlugin.log(e);
            return false;
        }
    }

    public void setEnabled(boolean b) {
        text.setEnabled(b);
        previousCommentsCombo.setEnabled(b);
    }
}
