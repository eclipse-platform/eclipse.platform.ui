package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Prompts the user for a multi-line comment for releasing to CVS.
 */
public class ReleaseCommentDialog extends Dialog {
	private static final int WIDTH_HINT = 350;
	private static final int HEIGHT_HINT = 50;
	
	private String comment = ""; //$NON-NLS-1$
	
	private Text text;
	
	/**
	 * ReleaseCommentDialog constructor.
	 * 
	 * @param parentShell  the parent of this dialog
	 */
	public ReleaseCommentDialog(Shell parentShell) {
		super(parentShell);
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
		return composite;
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
		super.okPressed();
	}
}
