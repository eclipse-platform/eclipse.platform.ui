/*******************************************************************************
 * Copyright (c) 2004, 2026 IBM Corporation and others.
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
 *     Jan-Ove Weichel <janove.weichel@vogella.com> - Bugs 411578, 486842, 487673
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 492918
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.BorderData;
import org.eclipse.swt.layout.BorderLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.osgi.framework.FrameworkUtil;

/**
 * A dialog that prompts for a directory to use as a workspace.
 */
public class ChooseWorkspaceDialog extends TitleAreaDialog {

	private static final String TILDE = "~"; //$NON-NLS-1$

	private static final String RECENT_WORKSPACES = "RECENT_WORKSPACES"; //$NON-NLS-1$

	private static final String OPEN_FOLDER_EMOJI = new String(
			new byte[] { (byte) 0xF0, (byte) 0x9F, (byte) 0x93, (byte) 0x82 }, StandardCharsets.UTF_8);

	private static final String DIALOG_SETTINGS_SECTION = "ChooseWorkspaceDialogSettings"; //$NON-NLS-1$

	private final ChooseWorkspaceData launchData;

	private Combo pathCombo;

	private boolean suppressAskAgain = false;

	private boolean centerOnMonitor = false;

	private Map<String, Link> recentWorkspacesLinks;

	private Composite recentWorkspacesForm;

	private Button defaultButton;

	//	private boolean hasShownImportPrompt = false;
	private static final String PLUGIN_ID = FrameworkUtil.getBundle(ChooseWorkspaceDialog.class).getSymbolicName();
	private void log(int severity, String message, Throwable t) {
		Platform.getLog(FrameworkUtil.getBundle(getClass())).log(new Status(severity, PLUGIN_ID, message, t));
	}
	private void logInfo(String msg) {
		log(IStatus.INFO, msg, null);
	}
	private void logError(String msg, Throwable t) {
		log(IStatus.ERROR, msg, t);
	}

	/**
	 * Create a modal dialog on the argument shell, using and updating the
	 * argument data object.
	 * @param parentShell the parent shell for this dialog
	 * @param launchData the launch data from past launches
	 *
	 * @param suppressAskAgain
	 *            true means the dialog will not have a "don't ask again" button
	 * @param centerOnMonitor indicates whether the dialog should be centered on
	 * the monitor or according to it's parent if there is one
	 */
	public ChooseWorkspaceDialog(Shell parentShell,
			ChooseWorkspaceData launchData, boolean suppressAskAgain, boolean centerOnMonitor) {
		super(parentShell);
		this.launchData = launchData;
		this.suppressAskAgain = suppressAskAgain;
		this.centerOnMonitor = centerOnMonitor;
	}

	/**
	 * Show the dialog to the user (if needed). When this method finishes,
	 * #getSelection will return the workspace that should be used (whether it
	 * was just selected by the user or some previous default has been used.
	 * The parameter can be used to override the users preference.  For example,
	 * this is important in cases where the default selection is already in use
	 * and the user is forced to choose a different one.
	 *
	 * @param force
	 *            true if the dialog should be opened regardless of the value of
	 *            the show dialog checkbox
	 */
	public void prompt(boolean force) {
		if (force || launchData.getShowDialog()) {
			open();

			// Bug 70576: Dialog gets dismissed via ESC and via the window's
			// close box. Make sure the launch doesn't continue with the default
			// workspace.
			if (getReturnCode() == CANCEL) {
				launchData.workspaceSelected(null);
			}

			return;
		}

		String[] recent = launchData.getRecentWorkspaces();

		// If the selection dialog was not used then the workspace to use is either the
		// most recent selection or the initialDefault (if there is no history).
		String workspace = null;
		if (recent != null && recent.length > 0) {
			workspace = recent[0];
		}
		if (workspace == null || workspace.isEmpty()) {
			workspace = launchData.getInitialDefault();
		}
		launchData.workspaceSelected(TextProcessor.deprocess(workspace));
	}

	/**
	 * Creates and returns the contents of the upper part of this dialog (above
	 * the button bar).
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method creates
	 * and returns a new <code>Composite</code> with no margins and spacing.
	 * </p>
	 *
	 * @param parent the parent composite to contain the dialog area
	 * @return the dialog area control
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		String productName = getWindowTitle();

		Composite composite = (Composite) super.createDialogArea(parent);
		setTitle(IDEWorkbenchMessages.ChooseWorkspaceDialog_dialogTitle);
		setMessage(NLS.bind(IDEWorkbenchMessages.ChooseWorkspaceDialog_dialogMessage, productName));

		// bug 59934: load title image for sizing, but set it non-visible so the
		//            white background is displayed
		if (getTitleImageLabel() != null) {
			getTitleImageLabel().setVisible(false);
		}

		// Will create Recent Workspaces Composite always.
		boolean createRecentWorkspacesComposite = true;
//		if (launchData.getRecentWorkspaces()[0] != null) {
//			createRecentWorkspacesComposite = true;
//		}
		createWorkspaceBrowseRow(composite);
		if (!suppressAskAgain) {
			createShowDialogButton(composite);
		}
		if (createRecentWorkspacesComposite) {
			createRecentWorkspacesComposite(composite);
		}

		Dialog.applyDialogFont(composite);
		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create "Import..." button first followed by Launch, Cancel
		Button importButton = createButton(parent, IDialogConstants.CLIENT_ID + 1,
				IDEWorkbenchMessages.ChooseWorkspaceDialog_importLabel, false);
		importButton.setToolTipText(IDEWorkbenchMessages.ChooseWorkspaceDialog_importTooltip);
		importButton.addListener(SWT.Selection, e -> {
			showImportWorkspacesDialog();
		});

		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, IDEWorkbenchMessages.ChooseWorkspaceDialog_launchLabel, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Returns the title that the dialog (or splash) should have.
	 *
	 * @return the window title
	 * @since 3.4
	 */
	public static String getWindowTitle() {
		String productName = null;
		IProduct product = Platform.getProduct();
		if (product != null) {
			productName = product.getName();
		}
		if (productName == null) {
			productName = IDEWorkbenchMessages.ChooseWorkspaceDialog_defaultProductName;
		}
		return productName;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(NLS.bind(IDEWorkbenchMessages.ChooseWorkspaceDialog_dialogName, getWindowTitle()));
		shell.addTraverseListener(e -> {
			// Bug 462707: [WorkbenchLauncher] dialog not closed on ESC.
			// The dialog doesn't always have a parent, so
			// Shell#traverseEscape() doesn't always close it for free.
			if (e.detail == SWT.TRAVERSE_ESCAPE) {
				e.detail = SWT.TRAVERSE_NONE;
				cancelPressed();
			}
		});
	}

	/**
	 * Notifies that the ok button of this dialog has been pressed.
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method sets
	 * this dialog's return code to <code>Window.OK</code>
	 * and closes the dialog. Subclasses may override.
	 * </p>
	 */
	@Override
	protected void okPressed() {
		workspaceSelected(getWorkspaceLocation());
	}

	/**
	 * Set the selected workspace to the given String and close the dialog
	 */
	private void workspaceSelected(String workspace) {
		launchData.workspaceSelected(TextProcessor.deprocess(workspace));
		super.okPressed();
	}

	/**
	 * Removes the workspace from RecentWorkspaces
	 */
	private void removeWorkspaceFromLauncher(String workspace, Combo combo) {
		// Remove Workspace from Properties
		List<String> recentWorkpaces = new ArrayList<>(Arrays.asList(launchData.getRecentWorkspaces()));
		recentWorkpaces.remove(workspace);
		launchData.setRecentWorkspaces(recentWorkpaces.toArray(new String[0]));
		launchData.writePersistedData();
		// Remove Workspace Composite
		recentWorkspacesLinks.get(workspace).dispose();
		recentWorkspacesLinks.remove(workspace);
		if (recentWorkspacesLinks.isEmpty()) {
			recentWorkspacesForm.dispose();
		}
		getShell().layout();
		initializeBounds();
		// Remove Workspace from combobox
		combo.remove(workspace);
		if (combo.getText().equals(workspace) || combo.getText().isEmpty()) {
			combo.setText(TextProcessor
					.process((combo.getItemCount() > 0 ? combo.getItem(0) : launchData.getInitialDefault())));
		}
	}

	/**
	 * Get the workspace location from the widget.
	 * @return String
	 */
	protected String getWorkspaceLocation() {
		return getCombo().getText();
	}

	@Override
	protected void cancelPressed() {
		launchData.workspaceSelected(null);
		super.cancelPressed();
	}

	/**
	 * The Recent Workspaces area of the dialog is only shown if Recent
	 * Workspaces are defined. It provides a faster way to launch a specific
	 * Workspace
	 */
	private void createRecentWorkspacesComposite(final Composite composite) {
		recentWorkspacesForm = new Composite(composite, SWT.NONE);
		recentWorkspacesForm.setBackground(composite.getBackground());
		recentWorkspacesForm.setLayout(new GridLayout());
		recentWorkspacesForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Composite header = new Composite(recentWorkspacesForm, SWT.NONE);
		header.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout headerLayout = new GridLayout(2, false);
		headerLayout.marginWidth = 0;
		headerLayout.marginHeight = 0;
		header.setLayout(headerLayout);
		header.setBackground(composite.getBackground());

		Label toggle = new Label(header, SWT.NONE);
		toggle.setBackground(composite.getBackground());
		toggle.setCursor(composite.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

		Label label = new Label(header, SWT.NONE);
		label.setText(IDEWorkbenchMessages.ChooseWorkspaceDialog_recentWorkspaces);
		label.setBackground(composite.getBackground());
		label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
		label.setCursor(composite.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

		Composite panel = new Composite(recentWorkspacesForm, SWT.NONE);
		panel.setBackground(composite.getBackground());
		GridData panelData = new GridData(SWT.FILL, SWT.FILL, true, false);
		panel.setLayoutData(panelData);

		RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.marginLeft = 14;
		layout.spacing = 6;
		panel.setLayout(layout);

		boolean expanded = launchData.isShowRecentWorkspaces();
		panel.setVisible(expanded);
		panelData.exclude = !expanded;
		toggle.setText(expanded ? "\u25BE" : "\u25B8"); //$NON-NLS-1$ //$NON-NLS-2$

		Listener toggleListener = e -> {
			boolean newState = !panel.getVisible();
			panel.setVisible(newState);
			((GridData) panel.getLayoutData()).exclude = !newState;
			toggle.setText(newState ? "\u25BE" : "\u25B8"); //$NON-NLS-1$ //$NON-NLS-2$
			launchData.setShowRecentWorkspaces(newState);
			recentWorkspacesForm.requestLayout();

			Point size = getInitialSize();
			Shell shell = getShell();
			shell.setBounds(getConstrainedShellBounds(
					new Rectangle(shell.getLocation().x, shell.getLocation().y, size.x, size.y)));
		};

		toggle.addListener(SWT.MouseDown, toggleListener);
		label.addListener(SWT.MouseDown, toggleListener);

		logInfo("[ChooseWorkspaceDialog][createRecentWorkspacesComposite] Before launchData size: " //$NON-NLS-1$
				+ (launchData.getRecentWorkspaces() != null ? launchData.getRecentWorkspaces().length : 0));
		String[] workspacesArray = launchData.getRecentWorkspaces();
		List<String> recentWorkspaces = (workspacesArray != null && workspacesArray.length > 0)
				? filterDuplicatedPaths(workspacesArray)
				: new ArrayList<>();

		// NEW CODE: Check if recent workspaces list is empty
//		if (recentWorkspaces == null || recentWorkspaces.isEmpty()) {
		// Show message in the panel
		if (recentWorkspaces == null || recentWorkspaces.isEmpty()) {
			Label emptyLabel = new Label(panel, SWT.WRAP);
			emptyLabel.setText(IDEWorkbenchMessages.ChooseWorkspaceDialog_noRecentWorkspaceFound);
			emptyLabel.setLayoutData(new RowData(SWT.DEFAULT, SWT.DEFAULT));
		} else {
			Label moreLabel = new Label(panel, SWT.WRAP);
			moreLabel.setText(IDEWorkbenchMessages.ChooseWorkspaceDialog_addMoreRecentWorkspaces);
			moreLabel.setLayoutData(new RowData(SWT.DEFAULT, SWT.DEFAULT));
		}
// link kind of logic for "Import..." starts
//		Link importLink = new Link(panel, SWT.WRAP);
//		importLink.setLayoutData(new RowData(SWT.DEFAULT, SWT.DEFAULT));
//		importLink.setText("<a>Import...</a>"); //$NON-NLS-1$
//		importLink.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				showImportWorkspacesDialog(); // ALWAYS allow manual import
//			}
//		});
//link kind of logic for "Import..." ends

// DO NOT DELETE
//		// Show a prompt dialog when the dialog opens, but only ONCE
//		Shell shell = getShell();
//		if (shell != null && !shell.isDisposed() && !hasShownImportPrompt) {
//			hasShownImportPrompt = true; // Set flag to prevent showing again
//			shell.getDisplay().asyncExec(() -> {
//				// Check again if still empty before showing prompt
//				// This prevents showing prompt if workspaces were already imported
//				List<String> currentWorkspaces = getRecentWorkspaces();
//				if (currentWorkspaces == null || currentWorkspaces.isEmpty()) {
//					showImportWorkspacesPromptDialog();
//				}
//			});
//		}
//DO NOT DELETE

		// Force layout BEFORE return (important)
//		panel.layout(true);
//		recentWorkspacesForm.layout(true, true);
//		recentWorkspacesLinks = new HashMap<>();
//		return;
//	}
		recentWorkspacesLinks = new HashMap<>(recentWorkspaces.size());
		Map<String, String> uniqueWorkspaceNames = createUniqueWorkspaceNameMap();

		List<Entry<String, String>> sortedList = uniqueWorkspaceNames.entrySet().stream().sorted((e1, e2) -> Integer
				.compare(recentWorkspaces.indexOf(e1.getValue()), recentWorkspaces.indexOf(e2.getValue())))
				.collect(Collectors.toList());

		for (Entry<String, String> uniqueWorkspaceEntry : sortedList) {
			final String recentWorkspace = uniqueWorkspaceEntry.getValue();

			Link link = new Link(panel, SWT.WRAP);
			link.setForeground(JFaceColors.getHyperlinkText(composite.getDisplay()));
			link.setLayoutData(new RowData(SWT.DEFAULT, SWT.DEFAULT));
			link.setText("<a>" + uniqueWorkspaceEntry.getKey() + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
			link.setToolTipText(recentWorkspace);
			link.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					workspaceSelected(recentWorkspace);
				}
			});

			recentWorkspacesLinks.put(recentWorkspace, link);

			Menu menu = new Menu(link);
			MenuItem forgetItem = new MenuItem(menu, SWT.PUSH);
			forgetItem.setText(IDEWorkbenchMessages.ChooseWorkspaceDialog_removeWorkspaceSelection);
			forgetItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					removeWorkspaceFromLauncher(recentWorkspace, getCombo());
				}
			});
			link.setMenu(menu);
		}
		logInfo("[ChooseWorkspaceDialog][createRecentWorkspacesComposite] Before recentWorkspaces size: " //$NON-NLS-1$
				+ recentWorkspaces.size());
		panel.layout(true);
		recentWorkspacesForm.layout(true, true);
	}

// DO NOT DELETE
//		/**
//		 * Shows a prompt dialog asking if user wants to import workspaces User can
//		 * choose to import or cancel and proceed with new workspace
//		 */
//		private void showImportWorkspacesPromptDialog() {
//			Shell shell = getShell();
//			if (shell == null || shell.isDisposed()) {
//				return;
//			}
//
//			// Double-check that workspaces are still empty before showing dialog
//			List<String> recentWorkspaces = getRecentWorkspaces();
//			if (recentWorkspaces != null && !recentWorkspaces.isEmpty()) {
//				// Workspaces were imported somehow, don't show the dialog
//				logInfo("[ChooseWorkspaceDialog][showImportWorkspacesPromptDialog] recentWorkspaces is empty so returning"); //$NON-NLS-1$
//				return;
//			}
//
//			// Create a confirmation dialog
//			MessageDialog dialog = new MessageDialog(shell, "No Recent Workspaces", // Title //$NON-NLS-1$
//					null, // Dialog title image
//					"No recent workspaces found.\n\nWould you like to import workspaces from a previous Eclipse installation?\n\n", //$NON-NLS-1$
//					// "No recent workspaces found.\n\nWould you like to import workspaces from a
//					// previous Eclipse installation?\n\n" / + "• Click 'Import' to browse and
//					// import workspaces from a previous Eclipse installation\n" / + "• Click
//					// 'Cancel' to proceed with creating a new workspace", // Message
//					MessageDialog.QUESTION, // Dialog type
//					new String[] { "Import...", "Cancel" }, // Button labels //$NON-NLS-1$ //$NON-NLS-2$
//					1 // Default button index (Cancel) - so Cancel is the default
//			) {
//				@Override
//				protected void buttonPressed(int buttonId) {
//					if (buttonId == 0) { // Import Workspaces button clicked
//						close();
//						// Open directory chooser to select previous Eclipse installation
//						DirectoryDialog dirDialog = new DirectoryDialog(shell);
//						dirDialog.setText("Select Previous Eclipse Installation"); //$NON-NLS-1$
//						dirDialog.setMessage(
//								"Select the directory of a previous Eclipse installation to import its workspaces:"); //$NON-NLS-1$
//						String selectedDir = dirDialog.open();
//
//						if (selectedDir != null) {
//							// Import workspaces and refresh
//							logInfo("[ChooseWorkspaceDialog][showImportWorkspacesPromptDialog] call from importAndRefreshWorkspaces()"); //$NON-NLS-1$
//							importAndRefreshWorkspaces(selectedDir);
//						}
//						// If user cancels the directory dialog, just close it and do nothing
//						// The main dialog remains open and user can proceed with new workspace
//					} else {
//						// Cancel button clicked - just close this dialog and let user proceed
//						close();
//					}
//				}
//			};
//			dialog.open();
//		}
//
//		/**
//		 * Imports workspaces from selected Eclipse installation and refreshes the UI
//		 */
//		private void importAndRefreshWorkspaces(String eclipseInstallPath) {
//			Shell shell = getShell();
//			if (shell == null || shell.isDisposed()) {
//				return;
//			}
//
//			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
//
//			try {
//				// Find and import workspaces from the selected Eclipse installation
//				List<String> importedWorkspaces = importWorkspacesFromEclipseInstallation(eclipseInstallPath);
//
//				System.out.println(
//						"Found " + (importedWorkspaces != null ? importedWorkspaces.size() : 0) + " workspaces to import"); //$NON-NLS-1$ //$NON-NLS-2$
//				logInfo("[ChooseWorkspaceDialog][importAndRefreshWorkspaces] Found " //$NON-NLS-1$
//						+ (importedWorkspaces != null ? importedWorkspaces.size() : 0) + " workspaces to import"); //$NON-NLS-1$
//
//				if (importedWorkspaces != null && !importedWorkspaces.isEmpty()) {
//					// Get current workspaces using getRecentWorkspaces() (reads from launchData)
//					List<String> currentWorkspaces = getRecentWorkspaces();
//					System.out.println("Current workspaces before merge: " //$NON-NLS-1$
//							+ (currentWorkspaces != null ? currentWorkspaces.size() : 0));
//					logInfo("[ChooseWorkspaceDialog][importAndRefreshWorkspaces] Current workspaces before merge: " //$NON-NLS-1$
//							+ (currentWorkspaces != null ? currentWorkspaces.size() : 0));
//
//					// Create a modifiable copy
//					List<String> mergedWorkspaces = new ArrayList<>();
//					if (currentWorkspaces != null && !currentWorkspaces.isEmpty()) {
//						mergedWorkspaces.addAll(currentWorkspaces);
//					}
//
//					// Add imported workspaces that aren't already present
//					for (String workspace : importedWorkspaces) {
//						if (!mergedWorkspaces.contains(workspace)) {
//							mergedWorkspaces.add(0, workspace); // Add at beginning to show as most recent
//							System.out.println("Added workspace: " + workspace); //$NON-NLS-1$
//							logInfo("[ChooseWorkspaceDialog][importAndRefreshWorkspaces] Added workspace: " + workspace); //$NON-NLS-1$
//						} else {
//							System.out.println("Workspace already exists: " + workspace); //$NON-NLS-1$
//							logInfo("[ChooseWorkspaceDialog][importAndRefreshWorkspaces] Workspace already exists: " //$NON-NLS-1$
//									+ workspace);
//						}
//					}
//
//					System.out.println("Merged workspaces count: " + mergedWorkspaces.size()); //$NON-NLS-1$
//					logInfo("[ChooseWorkspaceDialog][importAndRefreshWorkspaces] Merged workspaces count: " //$NON-NLS-1$
//							+ mergedWorkspaces.size());
//
//					// Update launchData directly with the string array
//					updateLaunchDataWorkspaces(mergedWorkspaces);
//
//					// Also save to preferences for persistence when Eclipse restarts
//					saveRecentWorkspacesToPreferences(mergedWorkspaces);
//
//					// Verify the update worked by checking getRecentWorkspaces() again
//					List<String> verifyWorkspaces = getRecentWorkspaces();
//					System.out.println("Verification - workspaces after update: " //$NON-NLS-1$
//							+ (verifyWorkspaces != null ? verifyWorkspaces.size() : 0));
//					logInfo("[ChooseWorkspaceDialog][importAndRefreshWorkspaces] Verification - workspaces after update: " //$NON-NLS-1$
//							+ (verifyWorkspaces != null ? verifyWorkspaces.size() : 0));
//					if (verifyWorkspaces != null) {
//						for (String ws : verifyWorkspaces) {
//							System.out.println("  Verified workspace: " + ws); //$NON-NLS-1$
//							logInfo("[ChooseWorkspaceDialog][importAndRefreshWorkspaces] Verified workspace: " + ws); //$NON-NLS-1$
//						}
//					}
//
//					// Refresh the UI
//					refreshRecentWorkspacesComposite();
//
//					// Show success message only if workspaces were actually imported
//					if (verifyWorkspaces != null && !verifyWorkspaces.isEmpty()) {
//						MessageDialog.openInformation(shell, "Workspaces Imported", String.format( //$NON-NLS-1$
//								"Successfully imported %d workspace(s) from the previous installation.\n\nYou can now select one of the imported workspaces or continue with a new workspace.", //$NON-NLS-1$
//								importedWorkspaces.size()));
//					} else {
//						MessageDialog.openWarning(shell, "Workspaces Not Saved", //$NON-NLS-1$
//								"Workspaces were found but could not be saved. Please check the error log."); //$NON-NLS-1$
//					}
//				} else {
//					// Show warning if no workspaces found
//					MessageDialog.openWarning(shell, "No Workspaces Found", //$NON-NLS-1$
//							"No workspaces could be found in the selected Eclipse installation.\n\nPlease select a valid Eclipse installation directory or proceed with creating a new workspace."); //$NON-NLS-1$
//				}
//			} finally {
//				shell.setCursor(null);
//			}
//		}
// DO NOT DELETE

	/**
	 * NEW METHOD: Show dialog to import workspaces from previous installation
	 */
// version 2
	private void showImportWorkspacesDialog() {
		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] START"); //$NON-NLS-1$

		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setText("Select Previous Eclipse Installation"); //$NON-NLS-1$
		dialog.setMessage("Select the directory of a previous Eclipse installation"); //$NON-NLS-1$

		String selectedDir = dialog.open();
		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Selected directory: " + selectedDir); //$NON-NLS-1$

		if (selectedDir == null) {
			logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] User cancelled directory selection"); //$NON-NLS-1$
			return;
		}

		// Step 1: Detect workspaces
		List<String> detected = importWorkspaces(selectedDir);

		if (detected == null) {
			logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] ❌ Detected list is NULL"); //$NON-NLS-1$
			return;
		}
		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Detected workspaces count: " + detected.size()); //$NON-NLS-1$

		for (String ws : detected) {
			logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Detected workspace: " + ws); //$NON-NLS-1$
		}

		if (detected.isEmpty()) {
			logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] ❌ No workspaces found after detection"); //$NON-NLS-1$
			MessageDialog.openWarning(getShell(), "No Workspaces Found", "No workspaces found in selected directory."); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		// Step 2: Open selection dialog
		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Opening WorkspaceImportDialog"); //$NON-NLS-1$
		WorkspaceImportDialog selectionDialog = new WorkspaceImportDialog(getShell(), detected, selectedDir, this,
				Arrays.asList(launchData.getRecentWorkspaces()));
		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Dialog created"); //$NON-NLS-1$

		int result = Window.CANCEL;
		try {
			logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] About to call open()"); //$NON-NLS-1$
			result = selectionDialog.open();
			logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Dialog open() returned: " + result); //$NON-NLS-1$
		} catch (Exception e) {
			logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] ❌ Exception while opening dialog: " //$NON-NLS-1$
					+ e.getMessage());
			e.printStackTrace();
		}
		if (result != Window.OK) {
			logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] User cancelled selection dialog"); //$NON-NLS-1$
			return;
		}

		List<String> selected = selectionDialog.getSelected();
		if (selected == null) {
			logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] ❌ Selected list is NULL"); //$NON-NLS-1$
			return;
		}
		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Selected count: " + selected.size()); //$NON-NLS-1$

		for (String ws : selected) {
			logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Selected workspace: " + ws); //$NON-NLS-1$
		}

		if (selected.isEmpty()) {
			logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] ❌ No workspaces selected"); //$NON-NLS-1$
			MessageDialog.openInformation(getShell(), "Nothing Selected", "No workspaces selected."); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		// Step 3: Merge
		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Merging selected workspaces"); //$NON-NLS-1$
		mergeSelectedWorkspaces(selected);
		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] END"); //$NON-NLS-1$
	}

	private void mergeSelectedWorkspaces(List<String> selected) {

		logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] START"); //$NON-NLS-1$

		if (selected == null) {
			logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] ❌ Selected list is NULL"); //$NON-NLS-1$
			return;
		}
		logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] Incoming selected size: " + selected.size()); //$NON-NLS-1$

		Set<String> merged = new LinkedHashSet<>();

		// Step 1: Existing workspaces
		String[] existing = launchData.getRecentWorkspaces();

		if (existing == null) {
			logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] Existing workspaces is NULL"); //$NON-NLS-1$
		} else {
			logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] Existing count: " + existing.length); //$NON-NLS-1$

			for (String ws : existing) {
				logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] Checking existing: " + ws); //$NON-NLS-1$
				if (ws == null) {
					logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] Skipping NULL existing"); //$NON-NLS-1$
					continue;
				}

				File f = new File(ws);
				if (!f.exists()) {
					logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] Skipping invalid existing: " + ws); //$NON-NLS-1$
					continue;
				}
				logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] Adding existing: " + ws); //$NON-NLS-1$
				merged.add(ws);
			}
		}

		// Step 2: Add selected
		for (String ws : selected) {
			logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] Processing selected: " + ws); //$NON-NLS-1$

			if (ws == null) {
				logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] Skipping NULL selected"); //$NON-NLS-1$
				continue;
			}

			File f = new File(ws);
			if (!f.exists()) {
				logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] Skipping invalid selected: " + ws); //$NON-NLS-1$
				continue;
			}

			if (merged.contains(ws)) {
				logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] Duplicate ignored: " + ws); //$NON-NLS-1$
			} else {
				logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] Adding selected: " + ws); //$NON-NLS-1$
				merged.add(ws);
			}
		}

		// Step 3: Final list
		List<String> result = new ArrayList<>(merged);
		logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] Final merged size: " + result.size()); //$NON-NLS-1$

		for (String ws : result) {
			logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] Final workspace: " + ws); //$NON-NLS-1$
		}

		// Step 4: Persist
		logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] Updating launchData"); //$NON-NLS-1$
		launchData.setRecentWorkspaces(result.toArray(new String[0]));

		logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] Saving to preferences"); //$NON-NLS-1$
		saveRecentWorkspacesToPreferences(result);

		logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] Refreshing UI"); //$NON-NLS-1$
		refreshRecentWorkspacesComposite();

		logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] Showing confirmation dialog"); //$NON-NLS-1$

		MessageDialog.openInformation(getShell(), "Workspaces Updated", //$NON-NLS-1$
				"Imported " + selected.size() + " workspace(s)."); //$NON-NLS-1$ //$NON-NLS-2$
		logInfo("[ChooseWorkspaceDialog][mergeSelectedWorkspaces] END"); //$NON-NLS-1$
	}
// version 2
// version 1 starts.
//	private void showImportWorkspacesDialog() {
//		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] === START ==="); //$NON-NLS-1$
//
//		DirectoryDialog dialog = new DirectoryDialog(getShell());
//		dialog.setText("Select Previous Eclipse Installation"); //$NON-NLS-1$
//		dialog.setMessage("Select the directory of a previous Eclipse installation to import its workspaces:"); //$NON-NLS-1$
//		String selectedDir = dialog.open();
//
//		if (selectedDir == null) {
//			logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] User cancelled directory selection"); //$NON-NLS-1$
//			return;
//		}
//
//		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Selected directory: " + selectedDir); //$NON-NLS-1$
//
//		// Find and import workspaces from the selected Eclipse installation
//		List<String> importedWorkspaces = importWorkspacesFromEclipseInstallation(selectedDir);
//
//		if (importedWorkspaces == null || importedWorkspaces.isEmpty()) {
//			logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] No workspaces found in selected installation"); //$NON-NLS-1$
//			MessageDialog.openWarning(getShell(), "No Workspaces Found", //$NON-NLS-1$
//					"No workspaces could be found in the selected Eclipse installation."); //$NON-NLS-1$
//			return;
//		}
//
//		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Imported workspaces count: " //$NON-NLS-1$
//				+ importedWorkspaces.size());
//
//		String[] currentWorkspacesArray = launchData.getRecentWorkspaces();
//		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Current workspaces count: " //$NON-NLS-1$
//				+ (currentWorkspacesArray != null ? currentWorkspacesArray.length : 0));
//
//		// Quick check if there's anything to add
//		boolean hasNewWorkspaces = false;
//		if (currentWorkspacesArray != null) {
//			for (String imported : importedWorkspaces) {
//				if (imported != null && imported.length() > 0) {
//					boolean exists = false;
//					for (String current : currentWorkspacesArray) {
//						if (imported.equals(current)) {
//							exists = true;
//							break;
//						}
//					}
//					if (!exists && new File(imported).exists()) {
//						hasNewWorkspaces = true;
//						logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] New workspace found: " + imported); //$NON-NLS-1$
//						break;
//					}
//				}
//			}
//		} else if (!importedWorkspaces.isEmpty()) {
//			hasNewWorkspaces = true;
//			logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] No current workspaces, all imported are new"); //$NON-NLS-1$
//		}
//
//		// Return early if nothing new to add
//		if (!hasNewWorkspaces) {
//			logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] No new workspaces to add, returning early"); //$NON-NLS-1$
//			MessageDialog.openInformation(getShell(), "No New Workspaces", //$NON-NLS-1$
//					"All workspaces from the previous installation are already in your recent workspaces list."); //$NON-NLS-1$
//			return;
//		}
//
//		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Proceeding with merge, has new workspaces: true"); //$NON-NLS-1$
//
//		// Only proceed with merge if there are new workspaces
//		int initialCapacity = (currentWorkspacesArray != null ? currentWorkspacesArray.length : 0)
//				+ importedWorkspaces.size();
//		Set<String> uniqueWorkspacesSet = new LinkedHashSet<>(initialCapacity);
//
//		// Add current workspaces
//		int removedCount = 0;
//		if (currentWorkspacesArray != null) {
//			for (int i = 0; i < currentWorkspacesArray.length; i++) {
//				String ws = currentWorkspacesArray[i];
//				if (ws != null && ws.length() > 0) {
//					File wsFile = new File(ws);
//					if (wsFile.exists()) {
//						uniqueWorkspacesSet.add(ws);
//					} else {
//						removedCount++;
//						logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Removing non-existent workspace: " //$NON-NLS-1$
//								+ ws);
//					}
//				}
//			}
//		}
//
//		// Add new imported workspaces
//		int addedCount = 0;
//		for (int i = 0; i < importedWorkspaces.size(); i++) {
//			String ws = importedWorkspaces.get(i);
//			if (ws != null && ws.length() > 0) {
//				File wsFile = new File(ws);
//				if (wsFile.exists() && uniqueWorkspacesSet.add(ws)) {
//					addedCount++;
//					logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Adding workspace: " + ws); //$NON-NLS-1$
//				}
//			}
//		}
//
//		List<String> mergedWorkspaces = new ArrayList<>(uniqueWorkspacesSet);
//		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] mergedWorkspaces.size(): " //$NON-NLS-1$
//				+ mergedWorkspaces.size());
//
//		if (mergedWorkspaces.isEmpty()) {
//			logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] No valid workspaces after merge"); //$NON-NLS-1$
//			MessageDialog.openWarning(getShell(), "No Valid Workspaces", "No valid workspaces found."); //$NON-NLS-1$ //$NON-NLS-2$
//			return;
//		}
//
//		// Update in-memory first
	//// try { / java.lang.reflect.Field field =
	/// launchData.getClass().getDeclaredField("recentWorkspaces"); //$NON-NLS-1$ /
	/// field.setAccessible(true); / field.set(launchData,
	/// mergedWorkspaces.toArray(new String[0])); /
	/// logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Updated
	/// launchData via reflection, launchData.getRecentWorkspaces().length: "
	/// //$NON-NLS-1$ / + launchData.getRecentWorkspaces().length); / } catch
	/// (Exception e) { /
	/// logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Reflection
	/// failed, using setter: " //$NON-NLS-1$ / + e.getMessage()); /
	/// launchData.setRecentWorkspaces(mergedWorkspaces.toArray(new String[0])); / }
//		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Before updating launchData without reflection, launchData.getRecentWorkspaces().length: " //$NON-NLS-1$
//				+ launchData.getRecentWorkspaces().length);
//		launchData.setRecentWorkspaces(mergedWorkspaces.toArray(new String[0])); // instead of reflection
//		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Updated launchData without reflection, launchData.getRecentWorkspaces().length: " //$NON-NLS-1$
//				+ launchData.getRecentWorkspaces().length);
//
//		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] About to save to preferences"); //$NON-NLS-1$
//		// Single file write
//		saveRecentWorkspacesToPreferences(mergedWorkspaces);
//		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Saved preferences"); //$NON-NLS-1$
//
//		// Update UI immediately
//		if (recentWorkspacesForm != null && !recentWorkspacesForm.isDisposed()) {
//			logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] Refreshing UI in normal thread"); //$NON-NLS-1$
//			refreshRecentWorkspacesComposite();
	//// getShell().getDisplay().asyncExec(() -> { / if (recentWorkspacesForm !=
	/// null && !recentWorkspacesForm.isDisposed()) { /
	/// logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] About to enter
	/// refreshRecentWorkspacesComposite() in UI thread"); //$NON-NLS-1$ /
	/// refreshRecentWorkspacesComposite(); / } / });
//		} else {
//			logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] recentWorkspacesForm is null or disposed, cannot refresh"); //$NON-NLS-1$
//		}
//
//		String message = String.format("Imported %d new workspace(s).", addedCount); //$NON-NLS-1$
//		if (removedCount > 0) {
//			message += String.format("\nRemoved %d non-existent workspace(s).", removedCount); //$NON-NLS-1$
//		}
//		logInfo("[ChooseWorkspaceDialog][showImportWorkspacesDialog] === END, " + message); //$NON-NLS-1$
//		MessageDialog.openInformation(getShell(), "Workspaces Updated", message); //$NON-NLS-1$
//	}
// version 1 ends

	private List<String> importWorkspaces(File directory) {
		logInfo("[ChooseWorkspaceDialog][importWorkspaces(File)] START: " + directory); //$NON-NLS-1$
		// capture return value
		List<String> result = importWorkspacesFromEclipseInstallation(directory.getAbsolutePath());
		if (result == null) {
			logInfo("[ChooseWorkspaceDialog][importWorkspaces(File)] ❌ result is NULL"); //$NON-NLS-1$
			result = new ArrayList<>();
		}
		logInfo("[ChooseWorkspaceDialog][importWorkspaces(File)] END size=" + result.size()); //$NON-NLS-1$
		return result;
	}

	public List<String> importWorkspaces(String path) {
		logInfo("[ChooseWorkspaceDialog][importWorkspaces(String)] path=" + path); //$NON-NLS-1$
		return importWorkspaces(new File(path)); // ✅ now VALID
	}

	/**
	 * NEW METHOD: Method to save workspaces to preferences
	 */
	private void saveRecentWorkspacesToPreferences(List<String> workspaces) {
/*		logInfo("[ChooseWorkspaceDialog][saveRecentWorkspacesToPreferences] Entered saveRecentWorkspacesToPreferences()"); //$NON-NLS-1$
		IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		if (workspaces == null || workspaces.isEmpty()) {
			store.setValue(RECENT_WORKSPACES, ""); //$NON-NLS-1$
			logInfo("[ChooseWorkspaceDialog][saveRecentWorkspacesToPreferences] returning..."); //$NON-NLS-1$
			return;
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < workspaces.size(); i++) {
			if (i > 0) {
				sb.append("\n"); //$NON-NLS-1$
			}
			sb.append(workspaces.get(i));
		}
		store.setValue(RECENT_WORKSPACES, sb.toString());
		logInfo("[ChooseWorkspaceDialog][saveRecentWorkspacesToPreferences] Properly exiting saveRecentWorkspacesToPreferences(), sb.toString(): " //$NON-NLS-1$
				+ sb.toString());
// original implementation.
*/

		logInfo("[ChooseWorkspaceDialog][saveRecentWorkspacesToPreferences] Entered saveRecentWorkspacesToPreferences()"); //$NON-NLS-1$
		IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		if (workspaces == null || workspaces.isEmpty()) {
			store.setValue(RECENT_WORKSPACES, ""); //$NON-NLS-1$
			logInfo("[ChooseWorkspaceDialog][saveRecentWorkspacesToPreferences] looks like workspaces is empty or null so returning"); //$NON-NLS-1$
			return;
		}
		logInfo("[ChooseWorkspaceDialog][saveRecentWorkspacesToPreferences] About to build sb"); //$NON-NLS-1$
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < mergedWorkspaces.size(); i++) {
//			if (i > 0) {
//				sb.append("\n"); //$NON-NLS-1$
//			}
//			sb.append(mergedWorkspaces.get(i));
//		}
//		store.setValue(RECENT_WORKSPACES, sb.toString());
		try {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < workspaces.size(); i++) {
				String ws = workspaces.get(i);
				logInfo("[ChooseWorkspaceDialog][saveRecentWorkspacesToPreferences] Processing workspace index " + i //$NON-NLS-1$
						+ ": " //$NON-NLS-1$
						+ ws);
				if (ws == null) {
					logInfo("[ChooseWorkspaceDialog][saveRecentWorkspacesToPreferences] NULL workspace found at index " //$NON-NLS-1$
							+ i);
					continue;
				}
				if (i > 0) {
					sb.append("\n"); //$NON-NLS-1$
				}
				sb.append(ws);
			}
//			store.setValue(RECENT_WORKSPACES, sb.toString()); // option 1
//			IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode("org.eclipse.ui.ide"); //$NON-NLS-1$
//			prefs.put(RECENT_WORKSPACES, sb.toString());
//			prefs.flush(); // option 2
			if (Platform.getInstanceLocation().isSet()) {
				store.setValue(RECENT_WORKSPACES, sb.toString());
				logInfo("[ChooseWorkspaceDialog][saveRecentWorkspacesToPreferences] references updated, sb.toString():  " //$NON-NLS-1$
						+ sb.toString());
			} else {
				logInfo("[ChooseWorkspaceDialog][saveRecentWorkspacesToPreferences] Instance location not set yet. Skipping preference save."); //$NON-NLS-1$
			} // option 3
			logInfo("[ChooseWorkspaceDialog][saveRecentWorkspacesToPreferences] references updated, sb.toString():  " //$NON-NLS-1$
					+ sb.toString());
		} catch (Exception e) {
			logInfo("[ChooseWorkspaceDialog][saveRecentWorkspacesToPreferences] EXCEPTION in saving preferences: " //$NON-NLS-1$
					+ e.getMessage());
			e.printStackTrace();
		}
		logInfo("[ChooseWorkspaceDialog][saveRecentWorkspacesToPreferences] Exiting from saveRecentWorkspacesToPreferences()"); //$NON-NLS-1$
	}

	/**
	 * NEW METHOD: Method to import workspaces from previous Eclipse installation
	 */
	private List<String> importWorkspacesFromEclipseInstallation(String eclipseInstallPath) {
		List<String> workspaces = new ArrayList<>();

		System.out.println("Looking for workspaces in: " + eclipseInstallPath); //$NON-NLS-1$
		logInfo("[ChooseWorkspaceDialog][importWorkspaces] Looking for workspaces in: " + eclipseInstallPath); //$NON-NLS-1$
		// Try to find workspaces from the previous installation
		// Look for the configuration/.settings/org.eclipse.ui.ide.prefs file
		File configDir = new File(eclipseInstallPath, "configuration"); //$NON-NLS-1$
		File settingsDir = new File(configDir, ".settings"); //$NON-NLS-1$
		File recentWorkspacesFile = new File(settingsDir, "org.eclipse.ui.ide.prefs"); //$NON-NLS-1$

		System.out.println("Looking for preferences file at: " + recentWorkspacesFile.getAbsolutePath()); //$NON-NLS-1$
		logInfo("[ChooseWorkspaceDialog][importWorkspaces] Looking for preferences file at: " //$NON-NLS-1$
				+ recentWorkspacesFile.getAbsolutePath());
		if (recentWorkspacesFile.exists()) {
			System.out.println("Found preferences file"); //$NON-NLS-1$
			logInfo("[ChooseWorkspaceDialog][importWorkspaces] Found preferences file"); //$NON-NLS-1$
			try {
				// Parse the preferences file to extract recent workspaces
				Properties props = new Properties();
				try (FileInputStream fis = new FileInputStream(recentWorkspacesFile)) {
					props.load(fis);
				}

				String recentWorkspacesValue = props.getProperty("RECENT_WORKSPACES"); //$NON-NLS-1$
				System.out.println("RECENT_WORKSPACES value: " + recentWorkspacesValue); //$NON-NLS-1$
				logInfo("[ChooseWorkspaceDialog][importWorkspaces] RECENT_WORKSPACES value: " + recentWorkspacesValue); //$NON-NLS-1$
				if (recentWorkspacesValue != null && !recentWorkspacesValue.isEmpty()) {
					// Parse the workspaces (format is paths separated by newlines)
					String[] workspacePaths = recentWorkspacesValue.split("\\n"); //$NON-NLS-1$
					for (String path : workspacePaths) {
						path = path.trim();
						if (!path.isEmpty()) {
							File wsFile = new File(path);
							if (wsFile.exists()) {
								workspaces.add(path);
								System.out.println("Found existing workspace: " + path); //$NON-NLS-1$
								logInfo("[ChooseWorkspaceDialog][importWorkspaces] Found existing workspace: " + path); //$NON-NLS-1$
							} else {
								System.out.println("Workspace path does not exist: " + path); //$NON-NLS-1$
								logInfo("[ChooseWorkspaceDialog][importWorkspaces] Workspace path does not exist: " //$NON-NLS-1$
										+ path);
							}
						}
					}
				} else {
					System.out.println("RECENT_WORKSPACES property is empty or null"); //$NON-NLS-1$
					logInfo("[ChooseWorkspaceDialog][importWorkspaces] RECENT_WORKSPACES property is empty or null"); //$NON-NLS-1$
				}
			} catch (IOException e) {
				System.err.println("Error reading preferences file: " + e.getMessage()); //$NON-NLS-1$
				logError("[ChooseWorkspaceDialog][importWorkspaces] Error reading preferences file: ", e); //$NON-NLS-1$
				e.printStackTrace();
			}
		} else {
			System.out.println("Preferences file not found at: " + recentWorkspacesFile.getAbsolutePath()); //$NON-NLS-1$
			logInfo("[ChooseWorkspaceDialog][importWorkspaces] Preferences file not found at: " //$NON-NLS-1$
					+ recentWorkspacesFile.getAbsolutePath());

			// Try alternative location
			File alternativePrefs = new File(eclipseInstallPath,
					".metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.ui.ide.prefs"); //$NON-NLS-1$
			System.out.println("Checking alternative location: " + alternativePrefs.getAbsolutePath()); //$NON-NLS-1$
			logInfo("[ChooseWorkspaceDialog][importWorkspaces] Checking alternative location: " //$NON-NLS-1$
					+ alternativePrefs.getAbsolutePath());
			if (alternativePrefs.exists()) {
				System.out.println("Found alternative preferences file"); //$NON-NLS-1$
				logInfo("[ChooseWorkspaceDialog][importWorkspaces] Found alternative preferences file"); //$NON-NLS-1$
				// Parse it similarly...
			}
		}

		System.out.println("Total workspaces found: " + workspaces.size()); //$NON-NLS-1$
		logInfo("[ChooseWorkspaceDialog][importWorkspaces] Total workspaces found: " + workspaces.size()); //$NON-NLS-1$
		return workspaces;
	}

	/**
	 * NEW METHOD: Method to refresh the recent workspaces composite after import
	 */
	private void refreshRecentWorkspacesComposite() {
		System.out.println("Entering refreshRecentWorkspacesComposite()"); //$NON-NLS-1$
		logInfo("[ChooseWorkspaceDialog][refreshRecentWorkspacesComposite] Entering refreshRecentWorkspacesComposite()"); //$NON-NLS-1$

		// Get the parent composite
		Composite parent = recentWorkspacesForm.getParent();
		if (parent != null && !parent.isDisposed()) {
			// Store current expansion state
			boolean wasExpanded = launchData.isShowRecentWorkspaces();

			// Dispose the old form
			recentWorkspacesForm.dispose();

			// Recreate the recent workspaces section
			createRecentWorkspacesComposite(parent);

			// Force layout update
			parent.layout(true);
			parent.getShell().layout(true);

			// Ensure expansion state is restored
			if (recentWorkspacesForm != null && !recentWorkspacesForm.isDisposed()) {
				// Find the expandable composite and set its state
				// Control[] children = recentWorkspacesForm.getBody().getChildren(); //
				// recentWorkspacesForm became composite instead of form
				Control[] children = recentWorkspacesForm.getChildren();
				for (Control child : children) {
					if (child instanceof ExpandableComposite) {
						((ExpandableComposite) child).setExpanded(wasExpanded);
						break;
					}
				}
			}

			Shell shell = parent.getShell();
			if (shell != null && !shell.isDisposed()) {
				shell.layout(true, true);
				shell.redraw();
				shell.update();
				// Resize dialog properly
				Point size = getInitialSize();
				shell.setBounds(getConstrainedShellBounds(
						new Rectangle(shell.getLocation().x, shell.getLocation().y, size.x, size.y)));
			}

			System.out.println("Refresh completed"); //$NON-NLS-1$
			logInfo("[ChooseWorkspaceDialog][refreshRecentWorkspacesComposite] Refresh completed"); //$NON-NLS-1$
		} else {
			System.out.println("Parent composite is null or disposed"); //$NON-NLS-1$
			logInfo("[ChooseWorkspaceDialog][refreshRecentWorkspacesComposite] Parent composite is null or disposed"); //$NON-NLS-1$
		}
		logInfo("[ChooseWorkspaceDialog][refreshRecentWorkspacesComposite] Exiting refreshRecentWorkspacesComposite()"); //$NON-NLS-1$
	}

	/**
	 * Creates a map with unique WorkspaceNames for the
	 * RecentWorkspacesComposite. The values are full absolute paths of recently
	 * used workspaces, the keys are unique segments somehow made from the
	 * values.
	 */
	private Map<String, String> createUniqueWorkspaceNameMap() {
		final String fileSeparator = File.separator;
		Map<String, String> uniqueWorkspaceNameMap = new HashMap<>();

		// Convert workspace paths to arrays of single path segments
		List<String[]> splittedWorkspaceNames = getRecentWorkspaces().stream()
				.filter(s -> s != null && !s.isEmpty()).map(s -> s.split(Pattern.quote(fileSeparator)))
				.collect(Collectors.toList());

		// bug 531611: prevent endless loops
		int maxSegmentsCount = 0;
		for (String[] strings : splittedWorkspaceNames) {
			maxSegmentsCount = Math.max(0, strings.length);
		}

		// create and collect unique workspace keys produced from arrays,
		// try to generate unique keys starting with the last segment of the
		// workspace path, increasing number of segments if no unique names
		// could be generated,
		// loop until all array values are removed from array list
		for (int i = 1; !splittedWorkspaceNames.isEmpty() && i <= maxSegmentsCount; i++) {
			final int c = i;

			// Function which flattens arrays to (hopefully unique) keys
			Function<String[], String> stringArraytoName = s -> String.join(fileSeparator,
					s.length < c ? s : Arrays.copyOfRange(s, s.length - c, s.length));

			// list of found unique keys
			List<String> uniqueNames = splittedWorkspaceNames.stream().map(stringArraytoName)
					.collect(Collectors.groupingBy(s -> s, Collectors.counting())).entrySet().stream()
					.filter(e -> e.getValue() == 1).map(Entry::getKey).collect(Collectors.toList());

			// remove paths for which we have found unique keys
			splittedWorkspaceNames.removeIf(a -> {
				String joined = stringArraytoName.apply(a);
				if (uniqueNames.contains(joined)) {
					uniqueWorkspaceNameMap.put(joined, String.join(fileSeparator, a));
					return true;
				}
				return false;
			});
		}
		return uniqueWorkspaceNameMap;
	}

	/**
	 * The main area of the dialog is just a row with the current selection
	 * information and a drop-down of the most recently used workspaces.
	 */
	protected Control createWorkspaceBrowseRow(Composite parent) {
		Composite panel = createBrowseComposite(parent);

		pathCombo = createPathCombo(panel);
		createBrowseButton(panel, pathCombo);
		setInitialTextValues(pathCombo);
		Label label = new Label(parent, SWT.LEAD);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		label.setLayoutData(gd);
		pathCombo.addModifyListener(e -> {
			String hint = getUnexpectedPathHint();
			label.setText(hint);
			boolean empty = hint.isEmpty();
			if (empty != gd.exclude) {
				label.setVisible(!empty);
				gd.exclude = empty;
				parent.layout();
			}
		});
		Listener[] listeners = pathCombo.getListeners(SWT.Modify);
		Event event = new Event();
		event.type = SWT.Modify;
		event.widget = pathCombo;
		for (Listener listener : listeners) {
			listener.handleEvent(event);
		}
		return panel;
	}

	protected String getUnexpectedPathHint() {
		String workspaceLocation = getWorkspaceLocation();
		if (!workspaceLocation.isBlank()) {
			File location = new File(workspaceLocation);
			Path path;
			try {
				path = location.getAbsoluteFile().toPath();
			} catch (InvalidPathException e) {
				return NLS.bind(IDEWorkbenchMessages.ChooseWorkspaceDialog_InvalidPathWarning, e.getReason());
			}
			String normalisedPath = path.normalize().toString();
			String normalisedPathWithSeperator = normalisedPath + File.separator;
			if (normalisedPathWithSeperator.contains(TILDE)) {
				return NLS.bind(IDEWorkbenchMessages.ChooseWorkspaceDialog_TildeNonExpandedWarning, normalisedPath);
			}
			if (!workspaceLocation.equalsIgnoreCase(normalisedPath)
					&& !workspaceLocation.equalsIgnoreCase(normalisedPathWithSeperator)) {
				return NLS.bind(IDEWorkbenchMessages.ChooseWorkspaceDialog_ResolvedAbsolutePath, normalisedPath);
			}
			if (!maybeWritable(path)) {
				return NLS.bind(IDEWorkbenchMessages.ChooseWorkspaceDialog_NotWriteablePathWarning, normalisedPath);
			}
		}
		return ""; //$NON-NLS-1$
	}

	/** the returned value may be wrong **/
	private boolean maybeWritable(Path path) {
		try {
			if (Files.exists(path)) {
				// both java.io.File.canWrite() and
				// java.nio.file.Files.isWritable(Path)
				// can not be trusted on windows. they may return wrong values.
				// for example JDK-8282720, JDK-8148211, JDK-8154915
				return Files.isWritable(path);
			}
			Path parent = path.getParent();
			if (parent == null) {
				return false;
			}
			return maybeWritable(parent);
		} catch (SecurityException se) {
			return false;
		}
	}

	protected Composite createBrowseComposite(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);

		BorderLayout layout = new BorderLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.spacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		panel.setFont(parent.getFont());
		return panel;
	}

	protected Combo createPathCombo(Composite panel) {
		Combo combo = new Combo(panel, SWT.BORDER | SWT.LEAD | SWT.DROP_DOWN);
		new DirectoryProposalContentAssist().apply(combo);
		combo.setTextDirection(SWT.AUTO_TEXT_DIRECTION);
		combo.setFocus();
		combo.setLayoutData(new BorderData(SWT.CENTER));
		combo.addModifyListener(e -> {
			Button okButton = getButton(Window.OK);
			if(okButton != null && !okButton.isDisposed()) {
				boolean nonWhitespaceFound = false;
				String characters = getWorkspaceLocation();
				for (int i = 0; !nonWhitespaceFound
						&& i < characters.length(); i++) {
					if (!Character.isWhitespace(characters.charAt(i))) {
						nonWhitespaceFound = true;
					}
				}
				okButton.setEnabled(nonWhitespaceFound && isValidPath(characters));
			}
		});
		return combo;
	}

	/**
	 * @param characters
	 * @return
	 */
	private boolean isValidPath(String path) {
		try {
			Path.of(path);
			return true;
		} catch (InvalidPathException e) {
			return false;
		}
	}

	protected Button createBrowseButton(Composite panel, Combo combo) {
		Button browseButton = new Button(panel, SWT.PUSH);
		browseButton.setText(IDEWorkbenchMessages.ChooseWorkspaceDialog_browseLabel);
		browseButton.setToolTipText(IDEWorkbenchMessages.ChooseWorkspaceDialog_browseTooltip);
		setButtonLayoutData(browseButton);
		browseButton.setLayoutData(new BorderData(SWT.RIGHT));
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SHEET);
				dialog.setText(IDEWorkbenchMessages.ChooseWorkspaceDialog_directoryBrowserTitle);
				dialog.setMessage(IDEWorkbenchMessages.ChooseWorkspaceDialog_directoryBrowserMessage);
				dialog.setFilterPath(getInitialBrowsePath());
				String dir = dialog.open();
				if (dir != null) {
					combo.setText(TextProcessor.process(dir));
				}
			}
		});
		int smallButtonLimit = browseButton.getFont().getFontData()[0].getHeight() * 40;
		panel.getParent().addControlListener(new ControlListener() {

			@Override
			public void controlResized(ControlEvent e) {
				// browseButton
				Point size = panel.getParent().getSize();
				if (size.x < smallButtonLimit) {
					browseButton.setText(OPEN_FOLDER_EMOJI);
				} else {
					browseButton.setText(IDEWorkbenchMessages.ChooseWorkspaceDialog_browseLabel);
				}
			}

			@Override
			public void controlMoved(ControlEvent e) {

			}
		});
		return browseButton;
	}

	/**
	 * Return a string containing the path that is closest to the current
	 * selection in the text widget. This starts with the current value and
	 * works toward the root until there is a directory for which File.exists
	 * returns true. Return the current working dir if the text box does not
	 * contain a valid path.
	 *
	 * @return closest parent that exists or an empty string
	 */
	protected String getInitialBrowsePath() {
		File dir = new File(getWorkspaceLocation());
		while (dir != null && !dir.exists()) {
			dir = dir.getParentFile();
		}

		return dir != null ? dir.getAbsolutePath() : System
				.getProperty("user.dir"); //$NON-NLS-1$
	}

	/*
	 * see org.eclipse.jface.Window.getInitialLocation()
	 */
	@Override
	protected Point getInitialLocation(Point initialSize) {
		Composite parent = getShell().getParent();

		if (!centerOnMonitor || parent == null) {
			return super.getInitialLocation(initialSize);
		}

		Monitor monitor = parent.getMonitor();
		Rectangle monitorBounds = monitor.getClientArea();
		Point centerPoint = Geometry.centerPoint(monitorBounds);

		return new Point(centerPoint.x - (initialSize.x / 2), Math.max(
				monitorBounds.y, Math.min(centerPoint.y
						- (initialSize.y * 2 / 3), monitorBounds.y
						+ monitorBounds.height - initialSize.y)));
	}

	/**
	 * The show dialog button allows the user to choose to neven be nagged again.
	 */
	private void createShowDialogButton(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		panel.setFont(parent.getFont());

		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		panel.setLayout(layout);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.verticalAlignment = GridData.END;
		panel.setLayoutData(data);

		defaultButton = new Button(panel, SWT.CHECK);
		defaultButton.setText(IDEWorkbenchMessages.ChooseWorkspaceDialog_useDefaultMessage);
		defaultButton.setSelection(!launchData.getShowDialog());
		defaultButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				launchData.toggleShowDialog();
			}
		});
	}

	private void setInitialTextValues(Combo text) {
		for (String recentWorkspace : getRecentWorkspaces()) {
			if (recentWorkspace != null) {
				text.add(recentWorkspace);
			}
		}

		text.setText(TextProcessor.process((text.getItemCount() > 0 ? text
				.getItem(0) : launchData.getInitialDefault())));
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		// If we were explicitly instructed to center on the monitor, then
		// do not provide any settings for retrieving a different location or, worse,
		// saving the centered location.
		if (centerOnMonitor) {
			return null;
		}

		IDialogSettings settings = PlatformUI
				.getDialogSettingsProvider(FrameworkUtil.getBundle(ChooseWorkspaceDialog.class)).getDialogSettings();
		IDialogSettings section = settings.getSection(DIALOG_SETTINGS_SECTION);
		if (section == null) {
			section = settings.addNewSection(DIALOG_SETTINGS_SECTION);
		}
		return section;
	}

	/**
	 * Get the "Workspace" path combo box or null if not initialized.
	 *
	 * @return Combo
	 */
	public Combo getCombo() {
		return pathCombo;
	}

	/**
	 * Get the "Recent Workspaces" form or null if not initialized.
	 *
	 * @return Composite
	 */
	public Composite getRecentWorkspacesForm() {
		return recentWorkspacesForm;
	}

	/**
	 * Get the "Use this as default..." check box or null if not initialized.
	 *
	 * @return Button
	 */
	public Button getDefaultButton() {
		return defaultButton;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private List<String> getRecentWorkspaces() {
		String[] workspaces = launchData.getRecentWorkspaces();
		return filterDuplicatedPaths(workspaces);
	}

	/**
	 * Filters out duplicates in the specified {@code paths}. Duplicated paths are
	 * paths that point to the same disk location, but have superfluous
	 * {@link File#separator} symbols.
	 *
	 * @param paths The set of paths to filter.
	 * @return The set of paths without duplicates.
	 */
	public static List<String> filterDuplicatedPaths(String[] paths) {
		if (paths == null || paths.length == 0) {
			return Collections.emptyList();
		}

		Set<String> normalizedPaths = new HashSet<>();
		List<String> recentWorkspaces = new ArrayList<>();
		for (String workspace : paths) {
			if (workspace != null && !workspace.isEmpty()) {
				String[] splitPath = workspace.split(Pattern.quote(File.separator));
				String normalizedPath = Arrays.stream(splitPath).filter(s -> !s.isEmpty()).collect(Collectors.joining(File.separator));
				if (workspace.startsWith(File.separator)) {
					normalizedPath = File.separator + normalizedPath;
				}
				boolean nonDuplicate = normalizedPaths.add(normalizedPath);
				if (nonDuplicate) {
					recentWorkspaces.add(workspace);
				}
			}
		}
		return Collections.unmodifiableList(recentWorkspaces);
	}
}
