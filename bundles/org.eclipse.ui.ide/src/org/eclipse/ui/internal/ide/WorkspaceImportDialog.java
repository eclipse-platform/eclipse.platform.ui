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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
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
 * @since 3.4
 *
 */
public class WorkspaceImportDialog extends TitleAreaDialog {

	private List<String> input;
	private String baseInstallPath;
	private CheckboxTableViewer viewer;
	private List<String> selected = new ArrayList<>();
	private List<String> existingWorkspaces;
	private Text locationText;
	private ChooseWorkspaceDialog parentDialog;

	private Set<String> checkedElements = new LinkedHashSet<>();

	public WorkspaceImportDialog(Shell parentShell, List<String> input, String baseInstallPath,
			ChooseWorkspaceDialog parentDialog, List<String> existingWorkspaces) {
		super(parentShell);
		this.input = input;
		this.baseInstallPath = baseInstallPath;
		this.parentDialog = parentDialog;
		this.existingWorkspaces = existingWorkspaces;
		logInfo("[WorkspaceImportDialog][ctor] Existing count: " + existingWorkspaces.size()); //$NON-NLS-1$
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Import Workspaces"); // Window title //$NON-NLS-1$
		logInfo("[WorkspaceImportDialog][configureShell] Title set"); //$NON-NLS-1$
	}

	@Override
	public void create() {
		super.create();
		setTitle("Import Workspaces"); //$NON-NLS-1$
		setMessage("Select workspaces to import."); //$NON-NLS-1$
		logInfo("[WorkspaceImportDialog][create] Title + Message set"); //$NON-NLS-1$
	}

	private void logInfo(String msg) {
		System.out.println(msg); // or route to your logger
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		logInfo("[WorkspaceImportDialog][createDialogArea] START"); //$NON-NLS-1$

		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));

		// FIX ORDER
		logInfo("[WorkspaceImportDialog] -> createLocationRow()"); //$NON-NLS-1$
		createLocationRow(container);

		logInfo("[WorkspaceImportDialog] -> createFilter()"); //$NON-NLS-1$
		createFilter(container);

		logInfo("[WorkspaceImportDialog] -> createTable()"); //$NON-NLS-1$
		createTable(container);

		logInfo("[WorkspaceImportDialog] -> loadInitialInput()"); //$NON-NLS-1$
		loadInitialInput();

		logInfo("[WorkspaceImportDialog] -> createButtons()"); //$NON-NLS-1$
		createButtons(container);

		logInfo("[WorkspaceImportDialog][createDialogArea] END"); //$NON-NLS-1$
		return container;
	}

	private void createLocationRow(Composite parent) {
		logInfo("[WorkspaceImportDialog][createLocationRow] START"); //$NON-NLS-1$

	    Composite row = new Composite(parent, SWT.NONE);
	    row.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	    row.setLayout(new GridLayout(3, false));

		new Label(row, SWT.NONE).setText("Workspace location:"); //$NON-NLS-1$

	    locationText = new Text(row, SWT.BORDER);
	    locationText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

	    if (baseInstallPath != null) {
	        locationText.setText(baseInstallPath);
			logInfo("[WorkspaceImportDialog] Using baseInstallPath: " + baseInstallPath); //$NON-NLS-1$
	    }

	    Button browse = new Button(row, SWT.PUSH);
		browse.setText("Browse..."); //$NON-NLS-1$
		browse.addListener(SWT.Selection, e -> {

			logInfo("[WorkspaceImportDialog][Browse] CLICKED"); //$NON-NLS-1$

			DirectoryDialog dlg = new DirectoryDialog(parent.getShell());
			dlg.setText("Select Eclipse Installation"); //$NON-NLS-1$
			dlg.setMessage("Select installation directory"); //$NON-NLS-1$

			String selected = dlg.open();

			logInfo("[WorkspaceImportDialog][Browse] Selected: " + selected); //$NON-NLS-1$

			if (selected != null) {
				locationText.setText(selected);

				// reload workspaces
				reloadWorkspaces(selected);
			}
		});
		logInfo("[WorkspaceImportDialog][createLocationRow] END"); //$NON-NLS-1$
	}

	private void createFilter(Composite parent) {
		logInfo("[WorkspaceImportDialog][createFilter] START"); //$NON-NLS-1$
		// Add ICON_CANCEL
		Text filter = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		filter.setMessage("type filter text"); //$NON-NLS-1$
		filter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		logInfo("[WorkspaceImportDialog][createFilter] Search field with clear icon created"); //$NON-NLS-1$

		// FILTER LOGIC
		filter.addModifyListener(e -> {
			String raw = filter.getText();
			logInfo("[WorkspaceImportDialog][createFilter] Raw: '" + raw + "'"); //$NON-NLS-1$ //$NON-NLS-2$

			if (raw == null || raw.trim().isEmpty()) {
				logInfo("[WorkspaceImportDialog][createFilter] Clearing filter"); //$NON-NLS-1$
				viewer.resetFilters();
			} else {
				String txt = raw.toLowerCase().replace("*", ""); //$NON-NLS-1$ //$NON-NLS-2$
				logInfo("[WorkspaceImportDialog][createFilter] Applying filter: " + txt); //$NON-NLS-1$
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
			// CRITICAL: restore check state AFTER refresh
			viewer.setCheckedElements(checkedElements.toArray());
			logInfo("[WorkspaceImportDialog][createFilter] Check state restored"); //$NON-NLS-1$
		});

		// HANDLE CLEAR BUTTON CLICK
		filter.addListener(SWT.DefaultSelection, e -> {
			logInfo("[WorkspaceImportDialog][createFilter] ❌ Clear (X) clicked"); //$NON-NLS-1$
			filter.setText(""); // triggers modify listener → resets filter //$NON-NLS-1$
		});
		logInfo("[WorkspaceImportDialog][createFilter] END"); //$NON-NLS-1$
	}

	private void loadInitialInput() {
		logInfo("[WorkspaceImportDialog][loadInitialInput] START"); //$NON-NLS-1$

		if (input == null) {
			logInfo("[WorkspaceImportDialog][loadInitialInput] ❌ input is NULL"); //$NON-NLS-1$
			return;
		}

		List<String> filtered = new ArrayList<>();
		for (String ws : input) {
			if (ws == null || ws.trim().isEmpty()) {
				logInfo("[WorkspaceImportDialog][loadInitialInput] Skipping NULL/empty"); //$NON-NLS-1$
				continue;
			}

			if (existingWorkspaces != null && existingWorkspaces.contains(ws)) {
				logInfo("[WorkspaceImportDialog][loadInitialInput] Skipping already existing: " + ws); //$NON-NLS-1$
				continue;
			}

			filtered.add(ws);
		}
		logInfo("[WorkspaceImportDialog][loadInitialInput] Final size: " + filtered.size()); //$NON-NLS-1$

		for (String ws : filtered) {
			logInfo("[WorkspaceImportDialog][loadInitialInput] Adding: " + ws); //$NON-NLS-1$
		}

		// SET INPUT
		viewer.setInput(filtered);

		// CHECK ALL by default
		checkedElements.clear();
		checkedElements.addAll(filtered);

		viewer.setCheckedElements(checkedElements.toArray());

		viewer.refresh();
		logInfo("[WorkspaceImportDialog][loadInitialInput] END"); //$NON-NLS-1$
	}

	private void createTable(Composite parent) {
		logInfo("[WorkspaceImportDialog][createTable] START"); //$NON-NLS-1$

		try {
			viewer = CheckboxTableViewer.newCheckList(parent,
					SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SHADOW_IN);
			logInfo("[WorkspaceImportDialog][createTable] Viewer created"); //$NON-NLS-1$

			Table table = viewer.getTable();

			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.minimumHeight = 250;
			table.setLayoutData(gd);
			logInfo("[WorkspaceImportDialog][createTable] GridData applied with min height"); //$NON-NLS-1$

			logInfo("[WorkspaceImportDialog][createTable] Table configured"); //$NON-NLS-1$

			// Column 1
			TableViewerColumn col1 = new TableViewerColumn(viewer, SWT.NONE);
			col1.getColumn().setText("Workspace"); //$NON-NLS-1$
			col1.getColumn().setWidth(200);

			col1.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					String name = new File((String) element).getName();
					logInfo("[WorkspaceImportDialog][col1] getText: " + name); //$NON-NLS-1$
					return name;
				}
			});

			// Column 2
			TableViewerColumn col2 = new TableViewerColumn(viewer, SWT.NONE);
			col2.getColumn().setText("Path"); //$NON-NLS-1$
			col2.getColumn().setWidth(400);

			col2.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					logInfo("[WorkspaceImportDialog][col2] path: " + element); //$NON-NLS-1$
					return (String) element;
				}
			});

			viewer.setContentProvider(ArrayContentProvider.getInstance());
			logInfo("[WorkspaceImportDialog][createTable] Content provider set"); //$NON-NLS-1$

			// INPUT LOGGING
			if (input == null) {
				logInfo("[WorkspaceImportDialog][createTable] ❌ input is NULL"); //$NON-NLS-1$
			} else {
				logInfo("[WorkspaceImportDialog][createTable] input size: " + input.size()); //$NON-NLS-1$
				for (String s : input) {
					logInfo("[WorkspaceImportDialog][createTable] input: " + s); //$NON-NLS-1$
				}
			}

//			// FILTER VALID
//			Set<String> valid = new LinkedHashSet<>();
//
//			if (input != null) {
//				for (String s : input) {
//
//					if (s == null) {
//						logInfo("[WorkspaceImportDialog][createTable] skipping NULL"); //$NON-NLS-1$
//						continue;
//					}
//
//					File f = new File(s);
//
//					if (!f.exists()) {
//						logInfo("[WorkspaceImportDialog][createTable] skipping not exists: " + s); //$NON-NLS-1$
//						continue;
//					}
//
//					if (!f.isDirectory()) {
//						logInfo("[WorkspaceImportDialog][createTable] skipping not dir: " + s); //$NON-NLS-1$
//						continue;
//					}
//
//					logInfo("[WorkspaceImportDialog][createTable] VALID: " + s); //$NON-NLS-1$
//					valid.add(s);
//				}
//			}
//			logInfo("[WorkspaceImportDialog][createTable] valid size: " + valid.size()); //$NON-NLS-1$
//
//			List<String> finalList = new ArrayList<>(valid);
//
//			for (String s : finalList) {
//				logInfo("[WorkspaceImportDialog][createTable] FINAL LIST: " + s); //$NON-NLS-1$
//			}

			viewer.addCheckStateListener(event -> {
				String element = (String) event.getElement();

				if (event.getChecked()) {
					checkedElements.add(element);
					logInfo("[CheckState] CHECKED: " + element); //$NON-NLS-1$
				} else {
					checkedElements.remove(element);
					logInfo("[CheckState] UNCHECKED: " + element); //$NON-NLS-1$
				}
			});
//			viewer.setInput(finalList);
//			logInfo("[WorkspaceImportDialog][createTable] viewer.setInput DONE"); //$NON-NLS-1$
//
//			viewer.refresh();
//			logInfo("[WorkspaceImportDialog][createTable] viewer.refresh DONE"); //$NON-NLS-1$
//
//			viewer.setAllChecked(true);
//			logInfo("[WorkspaceImportDialog][createTable] viewer.setAllChecked DONE"); //$NON-NLS-1$
		} catch (Exception e) {
			logInfo("[WorkspaceImportDialog][createTable] ❌ Exception: " + e.getMessage()); //$NON-NLS-1$
			e.printStackTrace();
		}
		logInfo("[WorkspaceImportDialog][createTable] END"); //$NON-NLS-1$
	}

	private void createButtons(Composite parent) {
		logInfo("[WorkspaceImportDialog][createButtons] START"); //$NON-NLS-1$
		try {
			Composite bar = new Composite(parent, SWT.NONE);
			bar.setLayout(new GridLayout(2, false));

			// Select All (VISIBLE ONLY)
			Button selectAll = new Button(bar, SWT.PUSH);
			selectAll.setText("Select All"); //$NON-NLS-1$
			selectAll.addListener(SWT.Selection, e -> {
				logInfo("[WorkspaceImportDialog][createButtons] Select All (VISIBLE) clicked"); //$NON-NLS-1$
				if (viewer == null) {
					logInfo("[WorkspaceImportDialog][createButtons] ❌ viewer NULL"); //$NON-NLS-1$
					return;
				}

				TableItem[] items = viewer.getTable().getItems();
				logInfo("[SelectAll] Visible items count: " + items.length); //$NON-NLS-1$
				for (TableItem item : items) {
					Object data = item.getData();
					if (data instanceof String) {
						viewer.setChecked(data, true);
						checkedElements.add((String) data);
						logInfo("[SelectAll] CHECKED: " + data); //$NON-NLS-1$
					}
				}
			});

			// Deselect All (VISIBLE ONLY)
			Button deselectAll = new Button(bar, SWT.PUSH);
			deselectAll.setText("Deselect All"); //$NON-NLS-1$
			deselectAll.addListener(SWT.Selection, e -> {
				logInfo("[WorkspaceImportDialog][createButtons] Deselect All (VISIBLE) clicked"); //$NON-NLS-1$
				if (viewer == null) {
					logInfo("[WorkspaceImportDialog][createButtons] ❌ viewer NULL"); //$NON-NLS-1$
					return;
				}

				TableItem[] items = viewer.getTable().getItems();
				logInfo("[DeselectAll] Visible items count: " + items.length); //$NON-NLS-1$
				for (TableItem item : items) {
					Object data = item.getData();
					if (data instanceof String) {
						viewer.setChecked(data, false);
						checkedElements.remove(data);
						logInfo("[DeselectAll] UNCHECKED: " + data); //$NON-NLS-1$
					}
				}
			});
		} catch (Exception e) {
			logInfo("[WorkspaceImportDialog][createButtons] ❌ Exception: " + e.getMessage()); //$NON-NLS-1$
			e.printStackTrace();
		}
		logInfo("[WorkspaceImportDialog][createButtons] END"); //$NON-NLS-1$
	}

	@Override
	protected void okPressed() {
		logInfo("[WorkspaceImportDialog][okPressed] START"); //$NON-NLS-1$

		if (viewer == null) {
			logInfo("[WorkspaceImportDialog][okPressed] ❌ viewer is NULL"); //$NON-NLS-1$
			super.okPressed();
			return;
		}

		Object[] checked = viewer.getCheckedElements();

		logInfo("[WorkspaceImportDialog][okPressed] checked count: " + checked.length); //$NON-NLS-1$

		for (Object o : checked) {
			logInfo("[WorkspaceImportDialog][okPressed] selected: " + o); //$NON-NLS-1$
			selected.add((String) o);
		}

		logInfo("[WorkspaceImportDialog][okPressed] END"); //$NON-NLS-1$

		super.okPressed();
	}

	public List<String> getSelected() {
		return selected;
	}

	private void reloadWorkspaces(String newPath) {
	    logInfo("[WorkspaceImportDialog][reloadWorkspaces] START: " + newPath); //$NON-NLS-1$

		if (parentDialog == null) {
			logInfo("[WorkspaceImportDialog][reloadWorkspaces] ❌ parentDialog NULL"); //$NON-NLS-1$
			return;
		}

		List<String> rawList = parentDialog.importWorkspaces(newPath);
		if (rawList == null) {
			logInfo("[WorkspaceImportDialog][reloadWorkspaces] ❌ rawList NULL"); //$NON-NLS-1$
	        return;
	    }

		List<String> filtered = new ArrayList<>();
		for (String ws : rawList) {

			if (ws == null || ws.trim().isEmpty()) {
				continue;
			}

			if (existingWorkspaces != null && existingWorkspaces.contains(ws)) {
				logInfo("[WorkspaceImportDialog][reloadWorkspaces] Skipping existing: " + ws); //$NON-NLS-1$
				continue;
			}

			filtered.add(ws);
		}

		logInfo("[WorkspaceImportDialog][reloadWorkspaces] Filtered size: " + filtered.size()); //$NON-NLS-1$
		viewer.setInput(filtered);

		// ALWAYS check all after browse
		checkedElements.clear();
		checkedElements.addAll(filtered);

		viewer.setCheckedElements(checkedElements.toArray());

		viewer.refresh();
	    logInfo("[WorkspaceImportDialog][reloadWorkspaces] END"); //$NON-NLS-1$
	}
}