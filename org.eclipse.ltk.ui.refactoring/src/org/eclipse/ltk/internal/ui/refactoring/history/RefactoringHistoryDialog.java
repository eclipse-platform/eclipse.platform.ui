/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;

import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.ui.refactoring.Assert;
import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryContentProvider;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryLabelProvider;

/**
 * Dialog to show the refactoring history.
 * 
 * @since 3.2
 */
public class RefactoringHistoryDialog extends Dialog {

	/** The button label key */
	private static final String BUTTON_LABEL= "buttonLabel"; //$NON-NLS-1$

	/** The dialog title key */
	private static final String DIALOG_TITLE= "title"; //$NON-NLS-1$

	/** The height key */
	private static final String HEIGHT= "height"; //$NON-NLS-1$

	/** The width key */
	private static final String WIDTH= "width"; //$NON-NLS-1$

	/** The x coordinate key */
	private static final String X= "x"; //$NON-NLS-1$

	/** The y coordinate key */
	private static final String Y= "y"; //$NON-NLS-1$

	/** The button id */
	protected final int fButtonId;

	/** The dialog bounds, or <code>null</code> */
	private Rectangle fDialogBounds= null;

	/** The dialog bounds key */
	private final String fDialogKey;

	/** The dialog settings, or <code>null</code> */
	private IDialogSettings fDialogSettings= null;

	/** The refactoring history control */
	protected RefactoringHistoryControl fHistoryControl= null;

	/** The refactoring history */
	protected final RefactoringHistory fRefactoringHistory;

	/** The resource bundle to use */
	protected final ResourceBundle fResourceBundle;

	/**
	 * Creates a new refactoring history dialog.
	 * 
	 * @param parent
	 *            the parent shell
	 * @param bundle
	 *            the resource bundle to use
	 * @param history
	 *            the refactoring history to display
	 * @param id
	 *            the ID of the dialog button
	 */
	public RefactoringHistoryDialog(final Shell parent, final ResourceBundle bundle, final RefactoringHistory history, final int id) {
		super(parent);
		Assert.isNotNull(bundle);
		Assert.isNotNull(history);
		fResourceBundle= bundle;
		fRefactoringHistory= history;
		fButtonId= id;
		fDialogSettings= RefactoringUIPlugin.getDefault().getDialogSettings();
		fDialogKey= getClass().getName();
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean close() {
		final boolean result= super.close();
		if (result && fDialogBounds != null) {
			IDialogSettings settings= fDialogSettings.getSection(fDialogKey);
			if (settings == null) {
				settings= new DialogSettings(fDialogKey);
				fDialogSettings.addSection(settings);
			}
			settings.put(X, fDialogBounds.x);
			settings.put(Y, fDialogBounds.y);
			settings.put(WIDTH, fDialogBounds.width);
			settings.put(HEIGHT, fDialogBounds.height);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IRefactoringHelpContextIds.REFACTORING_HISTORY_DIALOG);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void createButtonsForButtonBar(final Composite parent) {
		Button button= createButton(parent, fButtonId, fResourceBundle.getString(BUTTON_LABEL), true);
		button.setFocus();
		final SelectionAdapter adapter= new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				close();
			}
		};
		button.addSelectionListener(adapter);
		button= createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		button.addSelectionListener(adapter);
	}

	/**
	 * {@inheritDoc}
	 */
	protected Control createDialogArea(final Composite parent) {
		final Composite composite= (Composite) super.createDialogArea(parent);
		getShell().setText(fResourceBundle.getString(DIALOG_TITLE));
		fHistoryControl= createHistoryControl(composite);
		fHistoryControl.setContentProvider(new RefactoringHistoryContentProvider());
		fHistoryControl.setLabelProvider(new RefactoringHistoryLabelProvider(fResourceBundle));
		fHistoryControl.createControl();
		fHistoryControl.setRefactoringHistory(fRefactoringHistory);
		applyDialogFont(parent);
		return composite;
	}

	/**
	 * Creates the history control for this dialog
	 * 
	 * @param parent
	 *            the parent control
	 * @return the history control
	 */
	protected RefactoringHistoryControl createHistoryControl(final Composite parent) {
		return new RefactoringHistoryControl(parent, fResourceBundle);
	}

	/**
	 * {@inheritDoc}
	 */
	protected final Point getInitialLocation(final Point size) {
		final Point location= super.getInitialLocation(size);
		final IDialogSettings settings= fDialogSettings.getSection(fDialogKey);
		if (settings != null) {
			try {
				location.x= settings.getInt(X);
			} catch (NumberFormatException event) {
				// Do nothing
			}
			try {
				location.y= settings.getInt(Y);
			} catch (NumberFormatException event) {
				// Do nothing
			}
		}
		return location;
	}

	/**
	 * {@inheritDoc}
	 */
	protected final Point getInitialSize() {
		int width= 0;
		int height= 0;
		final Shell shell= getShell();
		if (shell != null) {
			shell.addControlListener(new ControlListener() {

				public void controlMoved(final ControlEvent event) {
					fDialogBounds= shell.getBounds();
				}

				public void controlResized(final ControlEvent event) {
					fDialogBounds= shell.getBounds();
				}
			});
		}
		final IDialogSettings settings= fDialogSettings.getSection(fDialogKey);
		if (settings == null) {
			if (fResourceBundle != null) {
				try {
					String string= fResourceBundle.getString(WIDTH);
					if (string != null)
						width= Integer.parseInt(string);
					string= fResourceBundle.getString(HEIGHT);
					if (string != null)
						height= Integer.parseInt(string);
				} catch (NumberFormatException exception) {
					// Do nothing
				}
				final Shell parent= getParentShell();
				if (parent != null) {
					final Point size= parent.getSize();
					if (width <= 0)
						width= size.x - 300;
					if (height <= 0)
						height= size.y - 200;
				}
			} else {
				final Shell parent= getParentShell();
				if (parent != null) {
					final Point size= parent.getSize();
					width= size.x - 100;
					height= size.y - 100;
				}
			}
			if (width < 600)
				width= 600;
			if (height < 500)
				height= 500;
		} else {
			try {
				width= settings.getInt(WIDTH);
			} catch (NumberFormatException exception) {
				width= 600;
			}
			try {
				height= settings.getInt(HEIGHT);
			} catch (NumberFormatException exception) {
				height= 500;
			}
		}
		return new Point(width, height);
	}

	/**
	 * Determines whether time information should be displayed.
	 * <p>
	 * Note: the default value is <code>true</code>.
	 * </p>
	 * 
	 * @param display
	 *            <code>true</code> to display time information,
	 *            <code>false</code> otherwise
	 */
	public final void setDisplayTime(final boolean display) {
		fHistoryControl.setDisplayTime(display);
	}

	/**
	 * Sets the message to display below the refactoring tree.
	 * 
	 * @param message
	 *            the message to display, or <code>null</code> for none
	 */
	public final void setMessage(final String message) {
		fHistoryControl.setMessage(message);
	}

	/**
	 * Sets the project which the history belongs to.
	 * <p>
	 * Note: the project does not have to exist.
	 * </p>
	 * 
	 * @param project
	 *            the project, or <code>null</code> for the workspace
	 */
	public final void setProject(final IProject project) {
		fHistoryControl.setProject(project);
	}
}
