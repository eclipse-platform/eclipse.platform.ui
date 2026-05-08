/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
package org.eclipse.ui.internal.ide;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog for importing workspaces from a previous Eclipse installation.
 * Displays valid workspaces and allows users to select workspaces to import
 * into the launcher.
 */
public class WorkspaceImportDialog extends TitleAreaDialog {
	private static final String EMPTY = ""; //$NON-NLS-1$
	private List<String> input;
	private String baseInstallPath;
	private CheckboxTableViewer viewer;
	private List<String> selected = new ArrayList<>();
	private List<String> existingWorkspaces;
	private Text locationText;
	private ChooseWorkspaceDialog parentDialog;

	private Set<String> checkedElements = new LinkedHashSet<>();

	/**
	 * Creates a dialog for importing workspaces from a previous Eclipse
	 * installation.
	 *
	 * @param parentShell        the parent shell
	 * @param input              the list of valid workspaces
	 * @param baseInstallPath    the selected Eclipse installation path
	 * @param parentDialog       the parent workspace launcher dialog
	 * @param existingWorkspaces the list of already imported workspaces
	 */
	public WorkspaceImportDialog(Shell parentShell, List<String> input, String baseInstallPath,
			ChooseWorkspaceDialog parentDialog, List<String> existingWorkspaces) {
		super(parentShell);
		this.input = input;
		this.baseInstallPath = baseInstallPath;
		this.parentDialog = parentDialog;
		this.existingWorkspaces = existingWorkspaces;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(IDEWorkbenchMessages.WorkspaceImportDialog_dialogName);
	}

	@Override
	public void create() {
		super.create();
		setTitle(IDEWorkbenchMessages.WorkspaceImportDialog_dialogTitle);
		setMessage(IDEWorkbenchMessages.WorkspaceImportDialog_dialogMessage);
	}

	@Override
	protected Point getInitialSize() {
		Point size = super.getInitialSize();
		return new Point(Math.max(size.x, convertHorizontalDLUsToPixels(500)),
				Math.max(size.y, convertVerticalDLUsToPixels(350)));
	}

	/**
	 * Creates the main dialog area and initializes the workspace import UI
	 * components.
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));

		createLocationRow(container);
		createFilter(container);
		createTable(container);
		loadInitialInput();
		createButtons(container);

		return container;
	}

	/**
	 * Creates the installation path input row with browse support for selecting an
	 * application installation directory.
	 */
	private void createLocationRow(Composite parent) {
	    Composite row = new Composite(parent, SWT.NONE);
	    row.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	    row.setLayout(new GridLayout(3, false));

		new Label(row, SWT.NONE).setText(IDEWorkbenchMessages.WorkspaceImportDialog_installationPath);
	    locationText = new Text(row, SWT.BORDER);
	    locationText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		locationText.setText(baseInstallPath != null ? baseInstallPath : EMPTY);

	    Button browse = new Button(row, SWT.PUSH);
		browse.setText(IDEWorkbenchMessages.WorkspaceImportDialog_browseLabel);
		browse.setToolTipText(IDEWorkbenchMessages.WorkspaceImportDialog_browseTooltip);
		browse.addListener(SWT.Selection, e -> {
			DirectoryDialog dlg = new DirectoryDialog(parent.getShell());
			dlg.setText(IDEWorkbenchMessages.WorkspaceImportDialog_selectPreviousInstallationText);
			dlg.setMessage(IDEWorkbenchMessages.WorkspaceImportDialog_selectPreviousInstallationMessage);
			String selectedPath = dlg.open();
			if (selectedPath != null) {
				locationText.setText(selectedPath);
				// reload workspaces
				reloadWorkspaces(selectedPath);
			}
		});
	}

	/**
	 * Creates the workspace filter text field and applies filtering to the
	 * workspace table viewer.
	 */
	private void createFilter(Composite parent) {
		// Add ICON_CANCEL
		Text filter = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		filter.setMessage(IDEWorkbenchMessages.WorkspaceImportDialog_typeFilterText);
		filter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// FILTER LOGIC
		filter.addModifyListener(e -> {
			String raw = filter.getText();
			if (raw.trim().isEmpty()) {
				viewer.resetFilters();
			} else {
				String txt = raw.toLowerCase().replace("*", EMPTY); //$NON-NLS-1$
				viewer.setFilters(new ViewerFilter[] { new ViewerFilter() {
					@Override
					public boolean select(Viewer v, Object p, Object el) {
						String value = el.toString().toLowerCase();
						return value.contains(txt);
					}
				}
				});
			}
			viewer.refresh();
			// Restore check state after refresh
			viewer.setCheckedElements(checkedElements.toArray());
		});

		// Handle clear button click
		filter.addListener(SWT.Selection, e -> {
			if (e.detail == SWT.ICON_CANCEL) {
				filter.setText(EMPTY);
			}
		});
	}

	/**
	 * Loads and filters the initial workspace input into the table viewer and
	 * selects all entries by default.
	 */
	private void loadInitialInput() {
		if (input == null) {
			return;
		}

		List<String> filtered = new ArrayList<>();
		for (String ws : input) {
			if (ws == null || ws.trim().isEmpty()) {
				continue;
			}

			if (existingWorkspaces != null && existingWorkspaces.contains(ws)) {
				continue;
			}
			filtered.add(ws);
		}

		// Set input
		viewer.setInput(filtered);

		// CHECK ALL by default
		checkedElements.clear();
		checkedElements.addAll(filtered);
		viewer.setCheckedElements(checkedElements.toArray());
		viewer.refresh();
	}

	/**
	 * Creates the workspace table viewer with check-box support and initializes its
	 * columns and selection handling.
	 */
	private void createTable(Composite parent) {
		viewer = CheckboxTableViewer.newCheckList(parent,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SHADOW_IN);

		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.minimumHeight = 250;
		table.setLayoutData(gd);

		// Column 1 - workspace
		TableViewerColumn col1 = new TableViewerColumn(viewer, SWT.NONE);
		col1.getColumn().setText(IDEWorkbenchMessages.WorkspaceImportDialog_tableColumn1);
		col1.getColumn().setWidth(200);

		col1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return new File((String) element).getName();
			}
		});

		// Column 2 - path
		TableViewerColumn col2 = new TableViewerColumn(viewer, SWT.NONE);
		col2.getColumn().setText(IDEWorkbenchMessages.WorkspaceImportDialog_tableColumn2);
		col2.getColumn().setWidth(400);

		col2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return (String) element;
			}
		});

		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.addCheckStateListener(event -> {
			String element = (String) event.getElement();

			if (event.getChecked()) {
				checkedElements.add(element);
			} else {
				checkedElements.remove(element);
			}
		});
	}

	/**
	 * Creates the Select All and Deselect All buttons for managing workspace
	 * selections in the table viewer.
	 */
	private void createButtons(Composite parent) {
		Composite bar = new Composite(parent, SWT.NONE);
		bar.setLayout(new GridLayout(2, false));

		// Select All (VISIBLE ONLY)
		Button selectAll = new Button(bar, SWT.PUSH);
		selectAll.setText(IDEWorkbenchMessages.WorkspaceImportDialog_selectAllLabel);
		selectAll.addListener(SWT.Selection, e -> {
			if (viewer == null) {
				return;
			}

			TableItem[] items = viewer.getTable().getItems();
			for (TableItem item : items) {
				Object data = item.getData();
				if (data instanceof String) {
					viewer.setChecked(data, true);
					checkedElements.add((String) data);
				}
			}
		});

		// Deselect All (VISIBLE ONLY)
		Button deselectAll = new Button(bar, SWT.PUSH);
		deselectAll.setText(IDEWorkbenchMessages.WorkspaceImportDialog_deselectAllLabel);
		deselectAll.addListener(SWT.Selection, e -> {
			if (viewer == null) {
				return;
			}

			TableItem[] items = viewer.getTable().getItems();
			for (TableItem item : items) {
				Object data = item.getData();
				viewer.setChecked(data, false);
				checkedElements.remove(data);
			}
		});
	}

	@Override
	protected void okPressed() {
		if (viewer == null) {
			super.okPressed();
			return;
		}

		selected.clear();
		Object[] checked = viewer.getCheckedElements();
		for (Object o : checked) {
			selected.add((String) o);
		}
		super.okPressed();
	}

	public List<String> getSelected() {
		return selected;
	}

	/**
	 * Reloads and refreshes the workspace list for the selected Eclipse
	 * installation path.
	 * <p>
	 * Displays appropriate messages when:
	 * <ul>
	 * <li>no workspaces are found</li>
	 * <li>all valid workspaces are already imported</li>
	 * </ul>
	 * Updates the table viewer with newly detected workspaces and restores checked
	 * state.
	 *
	 * @param newPath the Eclipse installation path to scan for workspaces
	 */
	private void reloadWorkspaces(String newPath) {
		if (parentDialog == null) {
			return;
		}

		List<String> rawList = parentDialog.importWorkspaces(newPath);
		// No prefs file or no valid workspaces
		if (rawList.isEmpty()) {
			MessageDialog.openWarning(getShell(), IDEWorkbenchMessages.WorkspaceImportDialog_noWorkspaceFoundTitle,
					IDEWorkbenchMessages.WorkspaceImportDialog_noWorkspaceFoundMessage);
			viewer.setInput(Collections.emptyList());
			viewer.refresh();
			return;
		}

		List<String> filtered = new ArrayList<>();
		for (String ws : rawList) {
			if (ws == null || ws.trim().isEmpty()) {
				continue;
			}

			if (existingWorkspaces != null && existingWorkspaces.contains(ws)) {
				continue;
			}
			filtered.add(ws);
		}

		// All valid workspaces are already imported
		if (filtered.isEmpty()) {
			MessageDialog.openInformation(getShell(), IDEWorkbenchMessages.WorkspaceImportDialog_noNewWorkspacesTitle,
					IDEWorkbenchMessages.WorkspaceImportDialog_noNewWorkspacesMessage);
			viewer.setInput(Collections.emptyList());
			checkedElements.clear();
			viewer.refresh();
			return;
		}

		viewer.setInput(filtered);
		// ALWAYS check all after browse
		checkedElements.clear();
		checkedElements.addAll(filtered);
		viewer.setCheckedElements(checkedElements.toArray());
		viewer.refresh();
	}
}