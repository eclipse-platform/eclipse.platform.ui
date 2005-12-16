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

import org.eclipse.core.runtime.Assert;

import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

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

/**
 * Dialog to show the refactoring history.
 * 
 * @since 3.2
 */
public class RefactoringHistoryDialog extends Dialog {

	/** The height key */
	private static final String HEIGHT= "height"; //$NON-NLS-1$

	/** The width key */
	private static final String WIDTH= "width"; //$NON-NLS-1$

	/** The x coordinate key */
	private static final String X= "x"; //$NON-NLS-1$

	/** The y coordinate key */
	private static final String Y= "y"; //$NON-NLS-1$

	/** The commit button id */
	protected final int fButtonId;

	/** The dialog bounds, or <code>null</code> */
	private Rectangle fDialogBounds= null;

	/** The refactoring history dialog configuration to use */
	protected final RefactoringHistoryDialogConfiguration fDialogConfiguration;

	/** The dialog bounds key */
	private final String fDialogKey;

	/** The dialog settings, or <code>null</code> */
	private IDialogSettings fDialogSettings= null;

	/** The refactoring history control */
	protected RefactoringHistoryControl fHistoryControl= null;

	/** The refactoring history */
	protected final RefactoringHistory fRefactoringHistory;

	/**
	 * Creates a new refactoring history dialog.
	 * 
	 * @param parent
	 *            the parent shell
	 * @param configuration
	 *            the refactoring history dialog configuration to use
	 * @param history
	 *            the refactoring history to display
	 * @param id
	 *            the commit button's id
	 */
	public RefactoringHistoryDialog(final Shell parent, final RefactoringHistoryDialogConfiguration configuration, final RefactoringHistory history, final int id) {
		super(parent);
		Assert.isNotNull(configuration);
		Assert.isNotNull(history);
		fDialogConfiguration= configuration;
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
		Button button= createButton(parent, fButtonId, fDialogConfiguration.getButtonLabel(), true);
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
		getShell().setText(fDialogConfiguration.getDialogTitle());
		fHistoryControl= createHistoryControl(composite);
		fHistoryControl.createControl();
		fHistoryControl.setInput(fRefactoringHistory);
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
		return new RefactoringHistoryControl(parent, fDialogConfiguration);
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
			if (fDialogConfiguration != null) {
				width= fDialogConfiguration.getDefaultWidth();
				height= fDialogConfiguration.getDefaultHeight();
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
}
