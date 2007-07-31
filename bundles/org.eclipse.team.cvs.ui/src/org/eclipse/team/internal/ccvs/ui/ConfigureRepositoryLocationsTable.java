/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ccvs.ui;

import java.util.*;
import java.util.List;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.CVSProjectSetCapability;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;

public class ConfigureRepositoryLocationsTable implements ICellModifier,
		IStructuredContentProvider, ITableLabelProvider {

	private static final class AlternativeRepositoryComparator extends
			ViewerComparator {

		public AlternativeRepositoryComparator() {
		}

		private int getCategory(Object element) {
			if (element instanceof RepositoryLocationItem) {
				return 0;
			}
			return 2;
		}

		public int compare(Viewer viewer, Object e1, Object e2) {
			final int compare = getCategory(e1) - getCategory(e2);
			if (compare != 0)
				return compare;
			return super.compare(viewer, ((Item) e1).location,
					((Item) e2).location);
		}
	}

	public abstract static class Item implements Comparable {
		public final ICVSRepositoryLocation location;
		public List alternativeList;
		public int selected;

		public Item(ICVSRepositoryLocation name, List alternative) {
			this.location = name;
			this.alternativeList = alternative;
			this.selected = 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Object o) {
			return location.getLocation(false).compareTo(
					((Item) o).location.getLocation(false));
		}
	}

	public static class RepositoryLocationItem extends Item {
		public RepositoryLocationItem(
				ICVSRepositoryLocation projectSetRepositoryLocation,
				List suggestedRepositoryLocations) {
			super(projectSetRepositoryLocation, suggestedRepositoryLocations);
		}
	}

	protected static final String ITEM = "item"; //$NON-NLS-1$
	protected static final String PROPERTY_ALTERNATIVE_LIST = "alternativeList"; //$NON-NLS-1$

	private TableViewer fTableViewer;

	private CellEditor[] cellEditors;

	private TextCellEditor dummyAlternativeRepositoryEditor;

	private Table table;

	/**
	 * List of <code>AlternativeRepositoryTable.RepositoryLocationItem</code>
	 * used as an input to the table.
	 */
	private List fAlternatives;

	/**
	 * Indicates whether a connection method should be displayed in the first
	 * column - project set information.
	 */
	private boolean fShowConnectionMethod;

	private boolean fNoDuplicateRepositoryLocationFound;

	public ConfigureRepositoryLocationsTable(Map alternativesMap) {
		fAlternatives = new ArrayList();
		Set checkSet = new HashSet();
		for (Iterator iterator = alternativesMap.entrySet().iterator(); iterator
				.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			fAlternatives
					.add(new ConfigureRepositoryLocationsTable.RepositoryLocationItem(
							(ICVSRepositoryLocation) entry.getKey(),
							(List) entry.getValue()));
			fNoDuplicateRepositoryLocationFound = checkSet
					.add(excludeConnectionMethod((ICVSRepositoryLocation) entry
							.getKey()));
		}
		fShowConnectionMethod = !fNoDuplicateRepositoryLocationFound;
		// we won't need it anymore
		checkSet = null;
	}

	public Composite createControl(final Composite composite) {
		/**
		 * Create a table.
		 */
		table = new Table(composite, SWT.V_SCROLL | SWT.BORDER | SWT.MULTI
				| SWT.FULL_SELECTION);
		// table.setLayoutData(SWTUtils.createHVFillGridData());
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.addListener(SWT.MeasureItem, new Listener() {
			public void handleEvent(Event event) {
				// int clientWidth = table.getClientArea().width;
				event.height = event.gc.getFontMetrics().getHeight() + 5;
				// event.width = clientWidth * 2;
			}
		});

		/**
		 * The 'Project Set repository location' column
		 */
		final TableColumn projectSetRepositoryColumn = new TableColumn(table,
				SWT.NONE, 0);
		projectSetRepositoryColumn
				.setText(CVSUIMessages.ConfigureRepositoryLocationsWizard_column0);

		/**
		 * The 'Alternative repository locations' column
		 */
		final TableColumn alternativeRepositoryColums = new TableColumn(table,
				SWT.NONE, 1);
		alternativeRepositoryColums
				.setText(CVSUIMessages.ConfigureRepositoryLocationsWizard_column1);

		composite.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle area = composite.getClientArea();
				Point size = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				ScrollBar vBar = table.getVerticalBar();
				int width = area.width - table.computeTrim(0, 0, 0, 0).width
						- vBar.getSize().x;
				if (size.y > area.height + table.getHeaderHeight()) {
					// Subtract the scrollbar width from the total column width
					// if a vertical scrollbar will be required
					Point vBarSize = vBar.getSize();
					width -= vBarSize.x;
				}
				Point oldSize = table.getSize();
				if (oldSize.x > area.width) {
					// table is getting smaller so make the columns
					// smaller first and then resize the table to
					// match the client area width
					projectSetRepositoryColumn.setWidth(width / 2);
					alternativeRepositoryColums.setWidth(width
							- projectSetRepositoryColumn.getWidth());
					table.setSize(area.width, area.height);
				} else {
					// table is getting bigger so make the table
					// bigger first and then make the columns wider
					// to match the client area width
					table.setSize(area.width, area.height);
					projectSetRepositoryColumn.setWidth(width / 2);
					alternativeRepositoryColums.setWidth(width
							- projectSetRepositoryColumn.getWidth());
				}
			}
		});

		/**
		 * Create a viewer for the table.
		 */
		fTableViewer = new TableViewer(table);
		fTableViewer.setContentProvider(this);
		fTableViewer.setLabelProvider(this);
		fTableViewer.setComparator(new AlternativeRepositoryComparator());

		/**
		 * Add a cell editor in the 'Alternative repository locations' column
		 */
		new TableEditor(table);

		cellEditors = new CellEditor[2];
		cellEditors[0] = null;
		// to enable cell editing, create a dummy cell editor
		cellEditors[1] = dummyAlternativeRepositoryEditor = new TextCellEditor(
				table, SWT.READ_ONLY);

		fTableViewer.setCellEditors(cellEditors);
		fTableViewer.setColumnProperties(new String[] { ITEM,
				PROPERTY_ALTERNATIVE_LIST });
		fTableViewer.setCellModifier(this);
		fTableViewer.setInput(fAlternatives);

		return table;
	}

	public Object getValue(Object element, String property) {

		final Item item = (Item) element;

		if (PROPERTY_ALTERNATIVE_LIST.equals(property)) {
			return new Integer(item.selected);
		}
		return null;
	}

	public boolean canModify(Object element, String property) {
		// set the correct cell editor for this element
		cellEditors[1] = getCellEditor(element);
		// only allow modification for editable elements
		return PROPERTY_ALTERNATIVE_LIST.equals(property);
	}

	private CellEditor getCellEditor(Object element) {

		if (element instanceof RepositoryLocationItem) {

			// create combo-box list of alternative repositories
			List alternativeList = ((RepositoryLocationItem) element).alternativeList;
			String[] alternativeNames = new String[alternativeList.size()];
			int i = 0;
			for (Iterator iterator = alternativeList.iterator(); iterator
					.hasNext();) {
				CVSRepositoryLocation repo = (CVSRepositoryLocation) iterator
						.next();
				alternativeNames[i++] = repo.getLocation();
			}
			return new ComboBoxCellEditor(table, alternativeNames,
					SWT.READ_ONLY);
		}
		return dummyAlternativeRepositoryEditor;
	}

	public void modify(Object element, String property, Object value) {

		final IStructuredSelection selection = (IStructuredSelection) fTableViewer
				.getSelection();
		final Item item = (Item) selection.getFirstElement();
		if (item == null)
			return;

		final int comboIndex = ((Integer) value).intValue();

		if (PROPERTY_ALTERNATIVE_LIST.equals(property)) {
			item.selected = comboIndex;
		}
		fTableViewer.refresh(item);
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		final Item item = (Item) element;

		switch (columnIndex) {
		case 0:
			return fShowConnectionMethod ? item.location.getLocation(false)
					: excludeConnectionMethod(item.location);
		case 1:
			return ((CVSRepositoryLocation) item.alternativeList
					.get(item.selected)).getLocation();
		default:
			return null;
		}
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

	public Object[] getElements(Object inputElement) {
		return ((Collection) inputElement).toArray();
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public IStructuredSelection getSelection() {
		return (IStructuredSelection) fTableViewer.getSelection();
	}

	public TableViewer getViewer() {
		return fTableViewer;
	}

	/**
	 * @return a selected (in a combo box) alternative repository location for
	 *         the first selected element
	 */
	public CVSRepositoryLocation getSelectedAlternativeRepository() {
		RepositoryLocationItem firstElement = (RepositoryLocationItem) getSelection()
				.getFirstElement();
		return (CVSRepositoryLocation) firstElement.alternativeList
				.get(firstElement.selected);
	}

	public void addAlternativeRepositoryToSelection(
			ICVSRepositoryLocation location) {
		// add newly created repository location to all selected elements
		for (Iterator iterator = getSelection().iterator(); iterator.hasNext();) {
			RepositoryLocationItem selectedItem = (RepositoryLocationItem) iterator
					.next();
			selectedItem.alternativeList.add(0, location);
			selectedItem.selected = 0;
			// fTableViewer.refresh(selectedItem);
		}

		// add newly created repository location to not-selected elements
		// new location must be compatible with the one from the project set
		for (int i = 0; i < fTableViewer.getTable().getItemCount(); i++) {
			Object element = fTableViewer.getElementAt(i);
			if (!getSelection().toList().contains(element)) {
				RepositoryLocationItem locationItem = (RepositoryLocationItem) element;
				if (CVSProjectSetCapability.isCompatible(location,
						locationItem.location)) {
					locationItem.alternativeList.add(location);
				}
			}
		}

		// update labels because of the first loop - first item changed
		fTableViewer.refresh(true);
	}

	/**
	 * @return A map with repository location from the project set as a key and
	 *         selected repository location from a combo box as value.
	 */
	public Map getSelected() {
		Map map = new HashMap();
		for (Iterator iterator = fAlternatives.iterator(); iterator.hasNext();) {
			ConfigureRepositoryLocationsTable.RepositoryLocationItem rli = (ConfigureRepositoryLocationsTable.RepositoryLocationItem) iterator
					.next();
			map.put(rli.location, rli.alternativeList.get(rli.selected));
		}
		return map;
	}

	public void setShowConnectionMethod(boolean show) {
		fShowConnectionMethod = show;
		fTableViewer.refresh(true);
	}

	private String excludeConnectionMethod(ICVSRepositoryLocation location) {
		String user = location.getUsername();
		String host = location.getHost();
		int port = location.getPort();
		String root = location.getRootDirectory();

		return (user != null && !user.equals("") ? (user + CVSRepositoryLocation.HOST_SEPARATOR) //$NON-NLS-1$
				: "") + //$NON-NLS-1$ 
				host
				+ CVSRepositoryLocation.COLON
				+ ((port == CVSRepositoryLocation.USE_DEFAULT_PORT) ? "" : (new Integer(port).toString())) + //$NON-NLS-1$ 
				root;
	}

	public boolean noDuplicateRepositoryLocationFound() {
		return fNoDuplicateRepositoryLocationFound;
	}
}
