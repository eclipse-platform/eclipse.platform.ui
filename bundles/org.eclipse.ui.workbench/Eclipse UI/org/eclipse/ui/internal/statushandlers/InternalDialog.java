/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.statushandlers;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogTray;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.progress.ProgressManagerUtil;
import org.eclipse.ui.internal.progress.ProgressMessages;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.IViewDescriptor;


/**
 * Parent window actually does not use its Shell to build dialog on. The
 * window passes the shell to the InternalDialog, and it can do switching
 * modality and recreate the window silently.
 * 
 * @since 3.4
 */
public class InternalDialog extends TrayDialog {

	/**
	 * The id of the goto action button
	 */
	static final int GOTO_ACTION_ID = IDialogConstants.CLIENT_ID + 1;

	static final String LOG_VIEW_ID = "org.eclipse.pde.runtime.LogView"; //$NON-NLS-1$

	/**
	 * Preference used to indicate whether the user should be prompted to
	 * confirm the execution of the job's goto action
	 */
	static final String PREF_SKIP_GOTO_ACTION_PROMPT = "pref_skip_goto_action_prompt"; //$NON-NLS-1$

	/**
	 * This composite holds all components of the dialog.
	 */
	private Composite dialogArea;
	/**
	 * This composite is initially scrolled to the 0 x 0 size. When more
	 * than one status arrives, listArea is resized and a list is created on
	 * it to present statuses to the user.
	 */
	private Composite listArea;
	/**
	 * On this composite are presented additional elements for displaying
	 * single status. Currently it is the second label that displays the
	 * second most important message to the user.
	 */
	private Composite singleStatusDisplayArea;
	/**
	 * This label is used to display the second most important message to
	 * the user. It is placed on singleStatusDisplayArea.
	 */
	private Label singleStatusLabel;
	/**
	 * A list from which the user selects statuses. The list is placed on
	 * listArea.
	 */
	private TableViewer statusListViewer;
	/**
	 * Composite on the left bottom corner. Allows for opening support tray
	 * & Error Log.
	 */
	private Composite linkComposite;
	/**
	 * This item is used to launch support tray
	 */
	private Link launchTrayLink;
	/**
	 * This fields contains indicator if link to ErrorLog view should be
	 * present.
	 */
	private Link showErrorLogLink;
	/**
	 * Main dialog image holder.
	 */
	private Label titleImageLabel;
	/**
	 * Message in the header.
	 */
	private Label mainMessageLabel;
	/**
	 * Header area.
	 */
	private Composite titleArea;

	/**
	 * In this support tray status support providers are displayed.
	 */
	private SupportTray supportTray;

	private DetailsAreaManager detailsManager;

	private Map dialogState;

	/**
	 * @param dialogState
	 * @param modal
	 */
	public InternalDialog(final Map dialogState, boolean modal) {
		super(ProgressManagerUtil.getDefaultParent());
		this.dialogState = dialogState;
		supportTray = new SupportTray(dialogState, new Listener() {
			public void handleEvent(Event event) {
				dialogState.put(IStatusDialogConstants.TRAY_OPENED,
						Boolean.FALSE);
				// close the tray
				closeTray();
				// set focus back to shell
				getShell().setFocus();
			}
		});
		detailsManager = new DetailsAreaManager(dialogState);
		setShellStyle(SWT.RESIZE | SWT.MAX | SWT.MIN | getShellStyle());
		setBlockOnOpen(false);

		if (!modal) {
			setShellStyle(~SWT.APPLICATION_MODAL & getShellStyle());
		}
	}

	protected void buttonPressed(int id) {
		if (id == GOTO_ACTION_ID) {
			IAction gotoAction = getGotoAction();
			if (gotoAction != null) {
				if (isPromptToClose()) {
					okPressed(); // close the dialog
					gotoAction.run(); // run the goto action
				}
			}
		}
		if (id == IDialogConstants.DETAILS_ID) {
			// was the details button pressed?
			dialogState.put(IStatusDialogConstants.DETAILS_OPENED, new Boolean(
					toggleDetailsArea()));
		} else {
			super.buttonPressed(id);
		}
	}

	/*
	 * (non-Javadoc) Method declared in Window.
	 */
	final protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(getString(IStatusDialogConstants.TITLE));
	}
	
	/**
	 * Status dialog button should be aligned SWT.END. 
	 */
	protected void setButtonLayoutData(Button button) {
		GridData data = new GridData(SWT.END, SWT.CENTER, false, false);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = Math.max(widthHint, minSize.x);
		button.setLayoutData(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		createTitleArea(parent);
		createListArea(parent);
		dialogArea = parent;
		Dialog.applyDialogFont(dialogArea);
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	protected boolean isResizable() {
		return true;
	}

	/**
	 * Creates title area.
	 * 
	 * @param parent
	 *            A composite on which the title area should be created.
	 */
	private void createTitleArea(Composite parent) {
		titleArea = new Composite(parent, SWT.NONE);
		titleArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.horizontalSpacing = 10;
		layout.marginLeft = 10;
		layout.marginTop = 10;
		layout.marginBottom = 0;
		titleArea.setLayout(layout);

		titleImageLabel = new Label(titleArea, SWT.NONE);
		titleImageLabel.setImage(getLabelProviderWrapper()
				.getImage(getCurrentStatusAdapter()));
		GridData layoutData = new GridData();
		layoutData.verticalSpan = 2;
		layoutData.verticalAlignment = SWT.TOP;
		titleImageLabel.setLayoutData(layoutData);

		GridData messageData = new GridData(SWT.FILL, SWT.FILL, true, true);
		messageData.widthHint = convertWidthInCharsToPixels(50);
		mainMessageLabel = new Label(titleArea, SWT.WRAP);
		mainMessageLabel.setLayoutData(messageData);
		// main message set up early, to address bug 222391
		mainMessageLabel.setText(getLabelProviderWrapper()
				.getMainMessage(getCurrentStatusAdapter()));
		if (!isMulti()) {
			singleStatusDisplayArea = createSingleStatusDisplayArea(titleArea);
		}
	}

	/**
	 * Create an area which allows the user to view the status if only one
	 * is created or to select one of reported statuses when there are many.
	 * 
	 * @param parent
	 *            the parent composite on which all components should be
	 *            placed.
	 */
	private void createListArea(Composite parent) {
		listArea = new Composite(parent, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 0;
		layoutData.widthHint = 0;
		listArea.setLayoutData(layoutData);
		GridLayout layout = new GridLayout();
		listArea.setLayout(layout);
		if (isMulti()) {
			fillListArea(listArea);
		}
	}

	/**
	 * This function checks if the dialog is modal.
	 * 
	 * @return true if the dialog is modal, false otherwise
	 * 
	 */
	public boolean isModal() {
		return ((getShellStyle() & SWT.APPLICATION_MODAL) == SWT.APPLICATION_MODAL);
	}

	/**
	 * @return Returns the supportTray.
	 */
	public SupportTray getSupportTray() {
		return supportTray;
	}

	/**
	 * @param supportTray
	 *            The supportTray to set.
	 */
	public void setSupportTray(SupportTray supportTray) {
		this.supportTray = supportTray;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open() {
		boolean modalitySwitch = getBooleanValue(IStatusDialogConstants.MODALITY_SWITCH);
		int result = super.open();
		if (modalitySwitch) {
			if (getBooleanValue(IStatusDialogConstants.DETAILS_OPENED)) {
				showDetailsArea();
			}
			if (getBooleanValue(IStatusDialogConstants.TRAY_OPENED)) {
				openTray();
			}
		} else {
			if (getBooleanValue(IStatusDialogConstants.ANIMATION)) {
				Rectangle shellPosition = getShell().getBounds();
				ProgressManagerUtil.animateUp(shellPosition);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#closeTray()
	 */
	public void closeTray() throws IllegalStateException {
		if (getTray() != null) {
			super.closeTray();
		}
		//preserve state during modality switch
		if (!getBooleanValue(IStatusDialogConstants.MODALITY_SWITCH)) {
			dialogState.put(IStatusDialogConstants.TRAY_OPENED, Boolean.FALSE);
		}
		if (launchTrayLink != null && !launchTrayLink.isDisposed()) {
			launchTrayLink.setEnabled(providesSupport()
					&& !getBooleanValue(IStatusDialogConstants.TRAY_OPENED));
		}
	}
	
	/**
	 * Method which should be invoked when new errors become available for
	 * display.
	 */
	void refresh() {
		if (dialogArea == null || dialogArea.isDisposed()) {
			return;
		}
		updateTitleArea();
		updateListArea();
		updateEnablements();
		// adjust width if necessary
		Point currentSize = getShell().getSize();
		Point desiredSize = getShell()
				.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if (currentSize.x < desiredSize.x) {
			getShell().setSize(desiredSize.x, currentSize.y);
		} else {
			getShell().layout();
		}
	}

	void refreshDialogSize() {
		Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		getShell().setSize(newSize);
	}

	/**
	 * Show the details portion of the dialog if it is not already visible.
	 * This method will only work when it is invoked after the control of
	 * the dialog has been set. In other words, after the
	 * <code>createContents</code> method has been invoked and has returned
	 * the control for the content area of the dialog. Invoking the method
	 * before the content area has been set or after the dialog has been
	 * disposed will have no effect.
	 */
	private void showDetailsArea() {
		if (dialogArea != null && !dialogArea.isDisposed()) {
			if (detailsManager.isOpen()) {
				detailsManager.close();
				detailsManager.createDetailsArea(dialogArea,
						getCurrentStatusAdapter());
				dialogState.put(IStatusDialogConstants.DETAILS_OPENED,
						Boolean.TRUE);
			} else {
				toggleDetailsArea();
				dialogState.put(IStatusDialogConstants.DETAILS_OPENED,
						Boolean.TRUE);
			}
			dialogArea.layout();
		}
	}

	/**
	 * Toggles the unfolding of the details area. This is triggered by the
	 * user pressing the details button.
	 * 
	 */
	private boolean toggleDetailsArea() {
		boolean opened = false;
		Point windowSize = getShell().getSize();
		if (detailsManager.isOpen()) {
			detailsManager.close();
			getButton(IDialogConstants.DETAILS_ID).setText(
					IDialogConstants.SHOW_DETAILS_LABEL);
			opened = false;
		} else {
			detailsManager.createDetailsArea(dialogArea,
					getCurrentStatusAdapter());
			getButton(IDialogConstants.DETAILS_ID).setText(
					IDialogConstants.HIDE_DETAILS_LABEL);
			opened = true;
		}

		GridData listAreaGridData = (GridData) listArea.getLayoutData();
		// if there is only one status to display,
		// make sure that the list area is as small as possible
		if (!isMulti()) {
			listAreaGridData.heightHint = 0;
		}
		// allow listArea to grab space depending if details
		// are opened or not
		if (opened) {
			listAreaGridData.grabExcessVerticalSpace = false;
		} else {
			listAreaGridData.grabExcessVerticalSpace = true;
		}
		listArea.setLayoutData(listAreaGridData);

		Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int diffY = newSize.y - windowSize.y;
		// increase the dialog height if details were opened and such
		// increase is necessary
		// decrease the dialog height if details were closed and empty space
		// appeared
		if ((opened && diffY > 0) || (!opened && diffY < 0)) {
			getShell().setSize(
					new Point(windowSize.x, windowSize.y + (diffY)));
		}
		dialogArea.layout();
		return opened;
	}

	/**
	 * This method should initialize the dialog bounds.
	 */
	protected void initializeBounds() {
		super.initializeBounds();
		refreshDialogSize();
		boolean modalitySwitch = getBooleanValue(IStatusDialogConstants.MODALITY_SWITCH);
		if (modalitySwitch) {
			getShell().setBounds(getShellBounds());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#getInitialLocation(org.eclipse.swt.graphics
	 * .Point)
	 */
	public Point getInitialLocation(Point initialSize) {
		// TODO Auto-generated method stub
		return super.getInitialLocation(initialSize);
	}

	/**
	 * The selection in the multiple job list has changed. Update widget
	 * enablements, repopulate the list and show details.
	 */
	private void handleSelectionChange() {
		StatusAdapter newSelection = getSingleSelection();
		if (newSelection != null) {
			dialogState.put(IStatusDialogConstants.CURRENT_STATUS_ADAPTER,
					newSelection);
			showDetailsArea();
			refresh();
		}
	}

	/**
	 * This method creates display area for {@link StatusAdapter}s when more
	 * is available.
	 * 
	 * @param parent
	 *            A parent composite on which all components should be
	 *            placed.
	 */
	private void fillListArea(Composite parent) {
		// it is necessary to make list parent composite taller
		GridData listAreaGD = (GridData) parent.getLayoutData();
		listAreaGD.grabExcessHorizontalSpace = true;
		if (!detailsManager.isOpen()) {
			listAreaGD.grabExcessVerticalSpace = true;
		}
		listAreaGD.heightHint = SWT.DEFAULT;

		// create list viewer
		statusListViewer = new TableViewer(parent, SWT.SINGLE
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		statusListViewer.setComparator(getLabelProviderWrapper());
		Control control = statusListViewer.getControl();
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = convertHeightInCharsToPixels(5);
		control.setLayoutData(data);
		initContentProvider();
		initLabelProvider();
		statusListViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChange();
				if ((getTray() == null) && getBooleanValue(IStatusDialogConstants.TRAY_OPENED)
						&& providesSupport()) {
					silentTrayOpen();
					return;
				}
				if ((getTray() != null) && !providesSupport()) {
					silentTrayClose();
					return;
				}
				supportTray.selectionChanged(event);
			}
		});
		Dialog.applyDialogFont(parent);
	}

	/**
	 * closes the tray without changing any flag
	 */
	private void silentTrayClose() {
		super.closeTray();
	}

	/** opens the tray without changing any flag */
	private void silentTrayOpen() {
		if (getTray() == null)
			super.openTray(supportTray);
	}
	/**
	 * This methods switches StatusAdapters presentation depending if there
	 * is one status or more.
	 */
	private void updateListArea() {
		// take care about list area if there is more than one status
		if (isMulti()) {
			if (singleStatusDisplayArea != null) {
				singleStatusDisplayArea.dispose();
			}
			if (statusListViewer == null
					|| statusListViewer.getControl().isDisposed()) {
				fillListArea(listArea);
				listArea.layout();
				listArea.getParent().layout();
				getShell().setSize(
						getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
			refreshStatusListArea();
		}
	}

	/**
	 * Updated title area. Adjust title, title message and title image
	 * according to selected {@link StatusAdapter}.
	 */
	private void updateTitleArea() {
		Image image = getLabelProviderWrapper().getImage(
				getCurrentStatusAdapter());
		titleImageLabel.setImage(image);
		if (getCurrentStatusAdapter() != null) {
			mainMessageLabel.setText(getLabelProviderWrapper()
					.getMainMessage(getCurrentStatusAdapter()));
		}
		if (singleStatusDisplayArea != null) {
			if (isMulti()) {
				singleStatusDisplayArea.dispose();
			} else {
				refreshSingleStatusArea();
			}
		}
		titleArea.layout();
	}

	/**
	 * This method creates button bar that is available on the bottom of the
	 * dialog.
	 */
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));

		linkComposite = createLinkComposite(composite);

		// Add the buttons to the button bar.
		createButtonsForButtonBar(composite);

		composite.layout();
		return composite;
	}

	/**
	 * This method creates buttons that are placed on button bar.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		IAction gotoAction = getGotoAction();
		String text = null;
		if (gotoAction != null) {
			text = gotoAction.getText();
		}
		Button button = createButton(parent, GOTO_ACTION_ID,
				text == null ? "" : text, //$NON-NLS-1$
				false);
		if (text == null)
			hideButton(button, true);

		createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);

		createButton(parent, IDialogConstants.DETAILS_ID,
				IDialogConstants.SHOW_DETAILS_LABEL, false);
	}

	/**
	 * This method creates additional display area for {@link StatusAdapter}
	 * when only one is available.
	 * 
	 * It creates one label on a composite currently for secondary message.
	 * 
	 * @param parent
	 *            A parent composite on which all components should be
	 *            placed.
	 * @return composite the composite on which are all components for
	 *         displaying status when only one is available.
	 */
	private Composite createSingleStatusDisplayArea(Composite parent) {
		// secondary message is displayed on separate composite with no
		// margins
		Composite singleStatusParent = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		singleStatusParent.setLayout(gridLayout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		singleStatusParent.setLayoutData(gd);

		// label that wraps
		singleStatusLabel = new Label(singleStatusParent, SWT.WRAP);
		GridData labelLayoutData = new GridData(SWT.FILL, SWT.FILL, true,
				true);
		labelLayoutData.widthHint = convertWidthInCharsToPixels(50);
		singleStatusLabel.setLayoutData(labelLayoutData);
		// main message set up early, to address bug 222391
		singleStatusLabel.setText(getLabelProviderWrapper()
				.getColumnText(getCurrentStatusAdapter(), 0));

		singleStatusLabel.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				showDetailsArea();
			}

			public void mouseUp(MouseEvent e) {
			}
		});
		return singleStatusParent;
	}

	/**
	 * This method closes the dialog.
	 */
	public boolean close() {
		boolean modalitySwitch = getBooleanValue(IStatusDialogConstants.MODALITY_SWITCH);
		if (detailsManager.isOpen()) {
			dialogState.put(IStatusDialogConstants.DETAILS_OPENED, Boolean.TRUE);
			toggleDetailsArea();
		}
		if (getBooleanValue(IStatusDialogConstants.TRAY_OPENED)) {
			closeTray();
			if (modalitySwitch) {
				dialogState.put(IStatusDialogConstants.DETAILS_OPENED, Boolean.TRUE);
			}
		}
		dialogState.put(IStatusDialogConstants.SHELL_BOUNDS, getShell().getBounds());
		statusListViewer = null;
		boolean result = super.close();
		if (!modalitySwitch && getBooleanValue(IStatusDialogConstants.ANIMATION)) {
			ProgressManagerUtil.animateDown(getShellBounds());
		}
		return result;
	}

	/**
	 * Hide the button if hide is <code>true</code>.
	 * 
	 * @param button
	 * @param hide
	 */
	private void hideButton(Button button, boolean hide) {
		((GridData) button.getLayoutData()).exclude = hide;
		button.setVisible(!hide);
		button.setEnabled(!hide);
	}

	/**
	 * Update the button enablements
	 */
	private void updateEnablements() {
		Button details = getButton(IDialogConstants.DETAILS_ID);
		if (details != null) {
			details.setEnabled(true);
		}
		Button gotoButton = getButton(GOTO_ACTION_ID);
		if (gotoButton != null) {
			IAction gotoAction = getGotoAction();
			boolean hasValidGotoAction = (gotoAction != null)
					&& (gotoAction.getText() != null);
			if (hasValidGotoAction) {
				hideButton(gotoButton, false);
				gotoButton.setText(gotoAction.getText());

				((GridData) gotoButton.getLayoutData()).widthHint = gotoButton
						.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
				gotoButton.getParent().layout();
			} else
				hideButton(gotoButton, true);
		}
		// and tray enablement button
		if (providesSupport() && !getBooleanValue(IStatusDialogConstants.HIDE_SUPPORT_BUTTON)) {
			if (launchTrayLink == null || launchTrayLink.isDisposed()) {
				launchTrayLink = createGetSupportLink();
			}
			launchTrayLink
					.setEnabled(!getBooleanValue(IStatusDialogConstants.TRAY_OPENED));
		} else {
			if (launchTrayLink != null && !launchTrayLink.isDisposed()) {
				launchTrayLink.dispose();
				launchTrayLink = null;
			}
		}
		IViewDescriptor descriptor = shouldDisplayLinkToErrorLog();
		if (descriptor != null) {
			if (showErrorLogLink == null || showErrorLogLink.isDisposed()) {
				showErrorLogLink = createShowErrorLogLink();
			}
		} else {
			if (showErrorLogLink != null && !showErrorLogLink.isDisposed()) {
				showErrorLogLink.dispose();
			}
		}
		linkComposite.getParent().layout();
	}

	private IViewDescriptor shouldDisplayLinkToErrorLog() {
		/* no support for error log */
		if (!getBooleanValue(IStatusDialogConstants.ERRORLOG_LINK)) {
			return null;
		}
		/* check handling hint and display link if it is expected */
		boolean shouldDisplay = false;
		Iterator it = ((Collection) dialogState
				.get(IStatusDialogConstants.STATUS_ADAPTERS)).iterator();
		while (it.hasNext()) {
			StatusAdapter adapter = (StatusAdapter) it.next();
			Integer hint = (Integer) adapter.getProperty(WorkbenchStatusDialogManagerImpl.HINT);
			if (hint != null
					&& ((hint.intValue() & StatusManager.LOG) != 0)) {
				shouldDisplay |= true;
				break;
			}
		}
		if (!shouldDisplay) {
			return null;
		}
		/* view description */
		return Workbench.getInstance().getViewRegistry().find(LOG_VIEW_ID);
	}

	/**
	 * Opens the dialog tray (support area at the right side of the dialog)
	 */
	public void openTray(DialogTray tray) throws IllegalStateException,
			UnsupportedOperationException {
		if (launchTrayLink != null && !launchTrayLink.isDisposed()) {
			launchTrayLink.setEnabled(false);
		}
		if (providesSupport()) {
			super.openTray(tray);
		}
		dialogState.put(IStatusDialogConstants.TRAY_OPENED, Boolean.TRUE);
	}

	/**
	 * Refreshes the single status area. Is called only when there is one
	 * and only one error.
	 */
	private void refreshSingleStatusArea() {
		String description = getLabelProviderWrapper()
				.getColumnText(getCurrentStatusAdapter(), 0);
		if (description.equals(singleStatusLabel.getText()))
			singleStatusLabel.setText(" "); //$NON-NLS-1$
		singleStatusLabel.setText(description);
		singleStatusDisplayArea.layout();
		getShell().setText(getString(IStatusDialogConstants.TITLE));
	}

	/**
	 * Refresh the contents of the viewer.
	 */
	private void refreshStatusListArea() {
		if (statusListViewer != null
				&& !statusListViewer.getControl().isDisposed()) {
			statusListViewer.refresh();
			if (statusListViewer.getTable().getItemCount() > 1) {
				getShell()
						.setText(
								WorkbenchMessages.WorkbenchStatusDialog_MultipleProblemsHaveOccured);
			} else {
				getShell().setText(
						getString(IStatusDialogConstants.TITLE));
			}
		}
	}

	/**
	 * Sets the content provider for the viewer.
	 */
	private void initContentProvider() {
		IContentProvider provider = new IStructuredContentProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {
				// Nothing of interest here
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.jface.viewers.IStructuredContentProvider#getElements
			 * (java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return ((Collection) dialogState
						.get(IStatusDialogConstants.STATUS_ADAPTERS)).toArray();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org
			 * .eclipse.jface.viewers.Viewer, java.lang.Object,
			 * java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				if (newInput != null) {
					refreshStatusListArea();
				}
			}
		};
		statusListViewer.setContentProvider(provider);
		statusListViewer.setInput(this);
		statusListViewer.setSelection(new StructuredSelection(
				getCurrentStatusAdapter()));
	}

	/**
	 * Creates a new control that provides access to support providers.
	 * <p>
	 * The <code>WorkbenchStatusDialog</code> implementation of this method
	 * creates the control, registers it for selection events including
	 * selection, Note that the parent's layout is assumed to be a
	 * <code>GridLayout</code> and the number of columns in this layout is
	 * incremented. Subclasses may override.
	 * </p>
	 * 
	 * @param parent
	 *            A parent composite on which all components should be
	 *            placed.
	 * @return the report control
	 */
	private Composite createLinkComposite(Composite parent) {
		Composite linkArea = new Composite(parent, SWT.NONE) {

			// the composite should be as small as possible when there is no
			// additional controls on it
			public Point computeSize(int wHint, int hHint, boolean changed) {
				Point newSize = super.computeSize(wHint, hHint, changed);
				if (getChildren().length == 0) {
					newSize.x = 0;
					newSize.y = 0;
				}
				return newSize;
			}

		};
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		linkArea.setLayout(layout);

		((GridLayout) parent.getLayout()).numColumns++;

		GridData layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, true,
				false);
		linkArea.setLayoutData(layoutData);
		return linkArea;
	}

	/**
	 * Creates a button with a report image. This is only used if there is
	 * an image available.
	 */
	private Link createGetSupportLink() {
		// no support
		if (!providesSupport() || getBooleanValue(IStatusDialogConstants.HIDE_SUPPORT_BUTTON)) {
			return null;
		}

		Link link = new Link(linkComposite, SWT.NONE);
		link
				.setText(WorkbenchMessages.WorkbenchStatusDialog_SupportHyperlink);
		link
				.setToolTipText(WorkbenchMessages.WorkbenchStatusDialog_SupportTooltip);
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openTray();
			}
		});
		Dialog.applyDialogFont(link);
		return link;
	}

	private Link createShowErrorLogLink() {
		Link link = new Link(linkComposite, SWT.NONE);
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					Workbench.getInstance().getActiveWorkbenchWindow()
							.getActivePage().showView(LOG_VIEW_ID);
				} catch (CoreException ce) {
					StatusManager.getManager().handle(ce,
							WorkbenchPlugin.PI_WORKBENCH);
				}
			}
		});
		link.setText(WorkbenchMessages.ErrorLogUtil_ShowErrorLogHyperlink);
		link
				.setToolTipText(WorkbenchMessages.ErrorLogUtil_ShowErrorLogTooltip);
		Dialog.applyDialogFont(link);
		return link;
	}

	/**
	 * Sets initial label provider.
	 */
	private void initLabelProvider() {
		statusListViewer.setLabelProvider(getLabelProviderWrapper());
	}

	/**
	 * Returns {@link IAction} associated with selected StatusAdapter.
	 * 
	 * @return {@link IAction} that is set as {@link StatusAdapter} property
	 *         with Job.class key.
	 */
	private IAction getGotoAction() {
		Object property = null;

		Job job = (Job) (getCurrentStatusAdapter().getAdapter(Job.class));
		if (job != null) {
			property = job.getProperty(IProgressConstants.ACTION_PROPERTY);
		}

		if (property instanceof IAction) {
			return (IAction) property;
		}
		return null;
	}

	/**
	 * Get the single selection. Return null if the selection is not just
	 * one element.
	 * 
	 * @return StatusAdapter or <code>null</code>.
	 */
	private StatusAdapter getSingleSelection() {
		ISelection rawSelection = statusListViewer.getSelection();
		if (rawSelection != null
				&& rawSelection instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) rawSelection;
			if (selection.size() == 1) {
				return (StatusAdapter) selection.getFirstElement();
			}
		}
		return null;
	}

	/*
	 * Prompt to inform the user that the dialog will close and the errors
	 * cleared.
	 */
	private boolean isPromptToClose() {
		IPreferenceStore store = WorkbenchPlugin.getDefault()
				.getPreferenceStore();
		if (!store.contains(PREF_SKIP_GOTO_ACTION_PROMPT)
				|| !store.getString(PREF_SKIP_GOTO_ACTION_PROMPT).equals(
						MessageDialogWithToggle.ALWAYS)) {
			MessageDialogWithToggle dialog = MessageDialogWithToggle.open(
					MessageDialog.CONFIRM, getShell(),
					ProgressMessages.JobErrorDialog_CloseDialogTitle,
					ProgressMessages.JobErrorDialog_CloseDialogMessage,
					ProgressMessages.JobErrorDialog_DoNotShowAgainMessage,
					false, store, PREF_SKIP_GOTO_ACTION_PROMPT, SWT.SHEET);
			return dialog.getReturnCode() == Window.OK;
		}
		return true;
	}

	public void openTray() {
		openTray(supportTray);
	}

	public boolean providesSupport() {
		return supportTray.providesSupport(getCurrentStatusAdapter()) != null;
	}

	private String getString(Object key) {
		return (String) dialogState.get(key);
	}

	private StatusAdapter getCurrentStatusAdapter() {
		return (StatusAdapter) dialogState
				.get(IStatusDialogConstants.CURRENT_STATUS_ADAPTER);
	}

	private boolean getBooleanValue(Object key) {
		Boolean b = (Boolean) dialogState.get(key);
		if (b == null) {
			return false;
		}
		return b.booleanValue();
	}

	private Rectangle getShellBounds() {
		return (Rectangle) dialogState.get(IStatusDialogConstants.SHELL_BOUNDS);
	}

	private LabelProviderWrapper getLabelProviderWrapper() {
		return (LabelProviderWrapper) dialogState
				.get(IStatusDialogConstants.LABEL_PROVIDER);
	}

	private boolean isMulti() {
		return ((Collection) dialogState
				.get(IStatusDialogConstants.STATUS_ADAPTERS)).size() > 1;
	}
}