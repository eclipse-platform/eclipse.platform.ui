/*******************************************************************************
 * Copyright (c) 2005, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.dialogs;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

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

	private final class ResizeListener extends ControlAdapter {

		private final GridData data;
		private final Shell shell;
		private static final int TRAY_RATIO = 100; // Percentage of extra width devoted to tray when resizing
		private int remainder = 0; // Used to prevent rounding errors from accumulating

		private ResizeListener(GridData data, Shell shell) {
			this.data = data;
			this.shell = shell;
		}

		@Override
		public void controlResized (ControlEvent event) {
			int newWidth = shell.getSize().x;
			if (newWidth != shellWidth) {
				int shellWidthIncrease = newWidth - shellWidth;
				int trayWidthIncreaseTimes100 = (shellWidthIncrease * TRAY_RATIO) + remainder;
				int trayWidthIncrease = trayWidthIncreaseTimes100/100;
				remainder = trayWidthIncreaseTimes100 - (100 * trayWidthIncrease);
				data.widthHint = data.widthHint + trayWidthIncrease;
				shellWidth = newWidth;
				if (!shell.isDisposed()) {
					shell.layout();
				}
			}
		}
	}

	private static boolean dialogHelpAvailable;

	/**
	 * The dialog's tray (null if none).
	 */
	private DialogTray tray;

	/**
	 * The tray's control (null if none).
	 */
	private Control trayControl;

	/**
	 * The control that had focus before the tray was opened (null if none).
	 */
	private Control nonTrayFocusControl;

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

	/*
	 * Whether or not help is available for this dialog.
	 */
	private boolean helpAvailable = isDialogHelpAvailable();

	private int shellWidth;

	private ControlAdapter resizeListener;

	/**
	 * The help button (null if none).
	 */
	private ToolItem fHelpButton;

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
		Shell shell = getShell();
		Control focusControl = shell.getDisplay().getFocusControl();
		if (focusControl!= null && isContained(trayControl, focusControl)) {
			if (nonTrayFocusControl!= null && !nonTrayFocusControl.isDisposed()) {
				nonTrayFocusControl.setFocus();
			} else {
				shell.setFocus();
			}
		}
		nonTrayFocusControl= null;
		shell.removeControlListener (resizeListener);
		resizeListener = null;
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
		Rectangle bounds = shell.getBounds();
		shell.setBounds(bounds.x + ((getDefaultOrientation() == SWT.RIGHT_TO_LEFT) ? trayWidth : 0), bounds.y, bounds.width - trayWidth, bounds.height);
		if (fHelpButton != null) {
			fHelpButton.setSelection(false);
		}
	}

	/**
	 * Returns true if the given Control is a direct or indirect child of
	 * container.
	 *
	 * @param container
	 *            the potential parent
	 * @param control
	 * @return boolean <code>true</code> if control is a child of container
	 */
	private boolean isContained(Control container, Control control) {
		Composite parent;
		while ((parent = control.getParent()) != null) {
			if (parent == container) {
				return true;
			}
			control = parent;
		}
		return false;
	}

	@Override
	protected void handleShellCloseEvent() {
		/*
		 * Close the tray to ensure that those dialogs that remember their
		 * size do not store the tray size.
		 */
		if (getTray() != null) {
			closeTray();
		}

		super.handleShellCloseEvent();
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		composite.setFont(parent.getFont());

		// create help control if needed
		if (isHelpAvailable()) {
			Control helpControl = createHelpControl(composite);
			((GridData) helpControl.getLayoutData()).horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		}
		Control buttonSection = super.createButtonBar(composite);
		((GridData) buttonSection.getLayoutData()).grabExcessHorizontalSpace = true;
		return composite;
	}

	/**
	 * Creates a new help control that provides access to context help.
	 * <p>
	 * The <code>TrayDialog</code> implementation of this method creates
	 * the control, registers it for selection events including selection,
	 * Note that the parent's layout is assumed to be a <code>GridLayout</code>
	 * and the number of columns in this layout is incremented. Subclasses may
	 * override.
	 * </p>
	 *
	 * @param parent the parent composite
	 * @return the help control
	 */
	protected Control createHelpControl(Composite parent) {
		Image helpImage = JFaceResources.getImage(DLG_IMG_HELP);
		if (helpImage != null) {
			return createHelpImageButton(parent, helpImage);
		}
		return createHelpLink(parent);
	}

	/*
	 * Creates a button with a help image. This is only used if there
	 * is an image available.
	 */
	private ToolBar createHelpImageButton(Composite parent, Image image) {
		ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.NO_FOCUS);
		((GridLayout) parent.getLayout()).numColumns++;
		toolBar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		toolBar.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		fHelpButton = new ToolItem(toolBar, SWT.CHECK);
		fHelpButton.setImage(image);
		fHelpButton.setToolTipText(JFaceResources.getString("helpToolTip")); //$NON-NLS-1$
		fHelpButton.addSelectionListener(widgetSelectedAdapter(e -> helpPressed()));
		return toolBar;
	}

	/*
	 * Creates a help link. This is used when there is no help image
	 * available.
	 */
	private Link createHelpLink(Composite parent) {
		Link link = new Link(parent, SWT.WRAP | SWT.NO_FOCUS);
		((GridLayout) parent.getLayout()).numColumns++;
		link.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		link.setText("<a>"+IDialogConstants.HELP_LABEL+"</a>"); //$NON-NLS-1$ //$NON-NLS-2$
		link.setToolTipText(IDialogConstants.HELP_LABEL);
		link.addSelectionListener(widgetSelectedAdapter(e -> helpPressed()));
		return link;
	}

	/*
	 * Returns whether or not the given layout can support the addition of a tray.
	 */
	private boolean isCompatibleLayout(Layout layout) {
		if (layout != null && layout instanceof GridLayout) {
			GridLayout grid = (GridLayout)layout;
			return !grid.makeColumnsEqualWidth && (grid.horizontalSpacing == 0) &&
					(grid.marginWidth == 0) && (grid.marginHeight == 0) &&
					(grid.numColumns == 5);
		}
		return false;
	}

	/**
	 * Returns whether or not context help is available for this dialog. This
	 * can affect whether or not the dialog will display additional help
	 * mechanisms such as a help control in the button bar.
	 *
	 * @return whether or not context help is available for this dialog
	 */
	public boolean isHelpAvailable() {
		return helpAvailable;
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
	@Override
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

	/*
	 * Called when the help control is invoked. This emulates the keyboard
	 * context help behavior (e.g. F1 on Windows). It traverses the widget
	 * tree upward until it finds a widget that has a help listener on it,
	 * then invokes a help event on that widget.
	 * If the help tray is already open, it closes it and doesn't invoke
	 * any help listener.
	 */
	private void helpPressed() {
		if (fHelpButton != null && fHelpButton.getSelection()) {
			DialogTray tray = getTray();
			if (tray != null) {
				// help is selected, but another tray is already visible. Close it now so that
				// help can be displayed.
				closeTray();
			}
		}
		if (tray == null ||
				fHelpButton != null && fHelpButton.getSelection()) { // help button was not selected before
			if (getShell() != null) {
				Control c = getShell().getDisplay().getFocusControl();
				while (c != null) {
					if (c.isListening(SWT.Help)) {
						c.notifyListeners(SWT.Help, new Event());
						break;
					}
					c = c.getParent();
				}
				if (fHelpButton != null) {
					if (getTray() != null)
						fHelpButton.setSelection(true);
					else
						fHelpButton.setSelection(false); // tray with help content was not created, disabled selection
				}
			}
		} else {
			closeTray();
		}
	}

	/**
	 * Constructs the tray's widgets and displays the tray in this dialog. The
	 * dialog's size will be adjusted to accommodate the tray.
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
		final Shell shell = getShell();
		Control focusControl = shell.getDisplay().getFocusControl();
		if (focusControl != null && isContained(shell, focusControl)) {
			nonTrayFocusControl = focusControl;
		}
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
		sash.addListener(SWT.Selection, event -> {
			if (event.detail != SWT.DRAG) {
				Rectangle clientArea1 = shell.getClientArea();
				int newWidth = clientArea1.width - event.x - (sash.getSize().x + rightSeparator.getSize().x);
				if (newWidth != data.widthHint) {
					data.widthHint = newWidth;
					shell.layout();
				}
			}
		});
		shellWidth = shell.getSize().x;

		resizeListener = new ResizeListener(data, shell);
		shell.addControlListener (resizeListener);

		this.tray = tray;
	}

	/**
	 * Sets whether or not context help is available for this dialog. This
	 * can affect whether or not the dialog will display additional help
	 * mechanisms such as a help control in the button bar.
	 *
	 * @param helpAvailable whether or not context help is available for the dialog
	 */
	public void setHelpAvailable(boolean helpAvailable) {
		this.helpAvailable = helpAvailable;
	}

	/**
	 * Tests if dialogs that have help control should show it
	 * all the time or only when explicitly requested for
	 * each dialog instance.
	 *
	 * @return <code>true</code> if dialogs that support help
	 * control should show it by default, <code>false</code> otherwise.
	 * @since 3.2
	 */
	public static boolean isDialogHelpAvailable() {
		return dialogHelpAvailable;
	}

	/**
	 * Sets whether JFace dialogs that support help control should
	 * show the control by default. If set to <code>false</code>,
	 * help control can still be shown on a per-dialog basis.
	 *
	 * @param helpAvailable <code>true</code> to show the help
	 * control, <code>false</code> otherwise.
	 * @since 3.2
	 */
	public static void setDialogHelpAvailable(boolean helpAvailable) {
		dialogHelpAvailable = helpAvailable;
	}
}
