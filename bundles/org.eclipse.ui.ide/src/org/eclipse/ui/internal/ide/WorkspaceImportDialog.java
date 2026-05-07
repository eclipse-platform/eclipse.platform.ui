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
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Import Workspaces"); // Window title //$NON-NLS-1$
	}

	@Override
	public void create() {
		super.create();
		setTitle("Import Workspaces"); //$NON-NLS-1$
		setMessage("Select workspaces to import."); //$NON-NLS-1$
	}

//	private void logInfo(String msg) {
//		System.out.println(msg); // or route to your logger
//	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		// FIX ORDER
		createLocationRow(container);
		createFilter(container);
		createTable(container);
		loadInitialInput();
		createButtons(container);
		return container;
	}

	private void createLocationRow(Composite parent) {
	    Composite row = new Composite(parent, SWT.NONE);
	    row.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	    row.setLayout(new GridLayout(3, false));

		new Label(row, SWT.NONE).setText("Workspace location:"); //$NON-NLS-1$
	    locationText = new Text(row, SWT.BORDER);
	    locationText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

	    if (baseInstallPath != null) {
	        locationText.setText(baseInstallPath);
	    }

	    Button browse = new Button(row, SWT.PUSH);
		browse.setText(IDEWorkbenchMessages.ChooseWorkspaceDialog_browseLabel);
		browse.addListener(SWT.Selection, e -> {
			DirectoryDialog dlg = new DirectoryDialog(parent.getShell());
			dlg.setText("Select Eclipse Installation"); //$NON-NLS-1$
			dlg.setMessage("Select installation directory"); //$NON-NLS-1$
			String selectedPath = dlg.open();
			if (selectedPath != null) {
				locationText.setText(selectedPath);
				// reload workspaces
				reloadWorkspaces(selectedPath);
			}
		});
	}

	private void createFilter(Composite parent) {
		// Add ICON_CANCEL
		Text filter = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		filter.setMessage("type filter text"); //$NON-NLS-1$
		filter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// FILTER LOGIC
		filter.addModifyListener(e -> {
			String raw = filter.getText();
			if (raw == null || raw.trim().isEmpty()) {
				viewer.resetFilters();
			} else {
				String txt = raw.toLowerCase().replace("*", ""); //$NON-NLS-1$ //$NON-NLS-2$
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
			// Restore check state AFTER refresh
			viewer.setCheckedElements(checkedElements.toArray());
		});

		// HANDLE CLEAR BUTTON CLICK
		filter.addListener(SWT.DefaultSelection, e -> {
			filter.setText(""); // triggers modify listener → resets filter //$NON-NLS-1$
		});
	}

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

		// SET INPUT
		viewer.setInput(filtered);

		// CHECK ALL by default
		checkedElements.clear();
		checkedElements.addAll(filtered);
		viewer.setCheckedElements(checkedElements.toArray());
		viewer.refresh();
	}

	private void createTable(Composite parent) {
		try {
			viewer = CheckboxTableViewer.newCheckList(parent,
					SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SHADOW_IN);

			Table table = viewer.getTable();
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.minimumHeight = 250;
			table.setLayoutData(gd);

			// Column 1
			TableViewerColumn col1 = new TableViewerColumn(viewer, SWT.NONE);
			col1.getColumn().setText("Workspace"); //$NON-NLS-1$
			col1.getColumn().setWidth(200);

			col1.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					String name = new File((String) element).getName();
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createButtons(Composite parent) {
		try {
			Composite bar = new Composite(parent, SWT.NONE);
			bar.setLayout(new GridLayout(2, false));

			// Select All (VISIBLE ONLY)
			Button selectAll = new Button(bar, SWT.PUSH);
			selectAll.setText("Select All"); //$NON-NLS-1$
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
			deselectAll.setText("Deselect All"); //$NON-NLS-1$
			deselectAll.addListener(SWT.Selection, e -> {
				if (viewer == null) {
					return;
				}

				TableItem[] items = viewer.getTable().getItems();
				for (TableItem item : items) {
					Object data = item.getData();
					if (data instanceof String) {
						viewer.setChecked(data, false);
						checkedElements.remove(data);
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
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
//		for (Object o : checked) {
//			selected.add((String) o);
//		}
		super.okPressed();
	}

	public List<String> getSelected() {
		return selected;
	}

	private void reloadWorkspaces(String newPath) {
		if (parentDialog == null) {
			return;
		}

		List<String> rawList = parentDialog.importWorkspaces(newPath);
		if (rawList == null) {
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

		viewer.setInput(filtered);
		// ALWAYS check all after browse
		checkedElements.clear();
		checkedElements.addAll(filtered);
		viewer.setCheckedElements(checkedElements.toArray());
		viewer.refresh();
	}
}