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
package org.eclipse.jface.dialogs;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;

/**
 * A <code>TrayDialog</code> is a specialized <code>Dialog</code> that can contain
 * a tray on its side. The tray's content is provided as a <code>DialogTray</code>.
 * <p>
 * It is recommended to subclass this class instead of <code>Dialog</code> in all
 * cases except where the dialog should never show a tray. For example, dialogs
 * which are very short, simple, and quick to dismiss (e.g. a message dialog with
 * an OK button) should subclass <code>Dialog</code>.
 * </p>
 * <p>
 * Note: Trays are not supported on dialogs that use a custom layout on the <code>
 * Shell</code> by overriding <code>Window#getLayout()</code>.
 * </p>
 * 
 * @see org.eclipse.jface.dialogs.DialogTray
 * @see org.eclipse.jface.window.Window#getLayout()
 * @since 3.2
 */
public abstract class TrayDialog extends Dialog {

	/*
	 * The dialog's tray (null if none).
	 */
	private DialogTray tray;

	/*
	 * The tray's control.
	 */
	private Control trayControl;
	
	/*
	 * The separator to the left of the sash.
	 */
	private Label leftSeparator;
	
	/*
	 * The separator to the right of the sash.
	 */
	private Label rightSeparator;
	
	/*
	 * The sash that allows the user to resize the tray.
	 */
	private Sash sash;

	/**
	 * Creates a tray dialog instance. Note that the window will have no visual
	 * representation (no widgets) until it is told to open.
	 * 
	 * @param shell the parent shell, or <code>null</code> to create a top-level shell
	 */
	protected TrayDialog(Shell shell) {
		super(shell);
	}
	
	/**
	 * Creates a tray dialog with the given parent.
	 * 
	 * @param parentShell the object that returns the current parent shell
	 */
	protected TrayDialog(IShellProvider parentShell) {
		super(parentShell);
	}

	/**
	 * Closes this dialog's tray, disposing its widgets.
	 * 
	 * @throws IllegalStateException if the tray was not open
	 */
	public void closeTray() throws IllegalStateException {
		if (getTray() == null) {
			throw new IllegalStateException("Tray was not open"); //$NON-NLS-1$
		}
		int trayWidth = trayControl.getSize().x + leftSeparator.getSize().x + sash.getSize().x + rightSeparator.getSize().x;
		trayControl.dispose();
		trayControl = null;
		tray = null;
		leftSeparator.dispose();
		leftSeparator = null;
		rightSeparator.dispose();
		rightSeparator = null;
		sash.dispose();
		sash = null;
		Shell shell = getShell();
		Rectangle bounds = shell.getBounds();
		shell.setBounds(bounds.x + ((getDefaultOrientation() == SWT.RIGHT_TO_LEFT) ? trayWidth : 0), bounds.y, bounds.width - trayWidth, bounds.height);
	}
	
	/**
	 * The tray dialog's default layout is a modified version of the default
	 * <code>Window</code> layout that can accomodate a tray, however it still
	 * conforms to the description of the <code>Window</code> default layout.
	 * <p>
	 * Note: Trays may not be supported with all custom layouts on the dialog's
	 * Shell. To avoid problems, use a single outer <code>Composite</code> for
	 * your dialog area, and set your custom layout on that <code>Composite</code>.
	 * </p>
	 * 
	 * @see org.eclipse.jface.window.Window#getLayout()
	 * @return a newly created layout or <code>null</code> for no layout
	 */
	protected Layout getLayout() {
		GridLayout layout = (GridLayout)super.getLayout();
		layout.numColumns = 5;
		layout.horizontalSpacing = 0;
		return layout;
	}
	
	/**
	 * Returns the tray currently shown in the dialog, or <code>null</code>
	 * if there is no tray.
	 * 
	 * @return the dialog's current tray, or <code>null</code> if there is none
	 */
	public DialogTray getTray() {
		return tray;
	}
	
	/**
	 * Constructs the tray's widgets and displays the tray in this dialog. The
	 * dialog's size will be adjusted to accomodate the tray.
	 * 
	 * @param tray the tray to show in this dialog
	 * @throws IllegalStateException if the dialog already has a tray open
	 * @throws UnsupportedOperationException if the dialog does not support trays,
	 *            for example if it uses a custom layout.
	 */
	public void openTray(DialogTray tray) throws IllegalStateException, UnsupportedOperationException {
		if (tray == null) {
			throw new NullPointerException("Tray was null"); //$NON-NLS-1$
		}
		if (getTray() != null) {
			throw new IllegalStateException("Tray was already open"); //$NON-NLS-1$
		}
		if (!isCompatibleLayout(getShell().getLayout())) {
			throw new UnsupportedOperationException("Trays not supported with custom layouts"); //$NON-NLS-1$
		}
		this.tray = tray;
		final Shell shell = getShell();
		leftSeparator = new Label(shell, SWT.SEPARATOR | SWT.VERTICAL);
		leftSeparator.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		sash = new Sash(shell, SWT.VERTICAL);
		sash.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		rightSeparator = new Label(shell, SWT.SEPARATOR | SWT.VERTICAL);
		rightSeparator.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		trayControl = tray.createContents(shell);
		Rectangle clientArea = shell.getClientArea();
		final GridData data = new GridData(GridData.FILL_VERTICAL);
		data.widthHint = trayControl.computeSize(SWT.DEFAULT, clientArea.height).x;
		trayControl.setLayoutData(data);
		int trayWidth = leftSeparator.computeSize(SWT.DEFAULT, clientArea.height).x + sash.computeSize(SWT.DEFAULT, clientArea.height).x + rightSeparator.computeSize(SWT.DEFAULT, clientArea.height).x + data.widthHint;
		Rectangle bounds = shell.getBounds();
		shell.setBounds(bounds.x - ((getDefaultOrientation() == SWT.RIGHT_TO_LEFT) ? trayWidth : 0), bounds.y, bounds.width + trayWidth, bounds.height);
		sash.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail != SWT.DRAG) {
					Rectangle clientArea = shell.getClientArea();
					int newWidth = clientArea.width - event.x - (sash.getSize().x + rightSeparator.getSize().x);
					if (newWidth != data.widthHint) {
						data.widthHint = newWidth;
						shell.layout();
					}
				}
			}
		});
	}
	
	/*
	 * Returns whether or not the given layout can support the addition of a tray.
	 */
	private boolean isCompatibleLayout(Layout layout) {
		if (layout != null && layout instanceof GridLayout) {
			GridLayout grid = (GridLayout)layout;
			return !grid.makeColumnsEqualWidth && (grid.horizontalSpacing == 0) &&
					(grid.marginWidth == 0) && (grid.marginHeight == 0) &&
					(grid.horizontalSpacing == 0) && (grid.numColumns == 5);
		}
		return false;
	}
}
