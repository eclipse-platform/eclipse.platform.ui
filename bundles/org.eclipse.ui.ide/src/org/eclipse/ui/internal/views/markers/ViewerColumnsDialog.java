/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * This was introduced as a fix to Bug 231081 and related, as an effort to
 * combine the columns and preference dialogs into one. It should be noted that
 * the class can be re-used or turned into a tool for column viewers in general,
 * but with some modifications. See example attached at the end of this class
 * 
 * @since 3.7
 * 
 * @author Hitesh Soliwal
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * 
 */
abstract class ViewerColumnsDialog extends ViewerSettingsAndStatusDialog {

	/** The list contains columns that are currently visible in viewer */
	private List visible;

	/** The list contains columns that are note shown in viewer */
	private List nonVisible;

	private TableViewer visibleViewer, nonVisibleViewer;

	private Button upButton, downButton;

	private Button toVisibleBtt, toNonVisibleBtt;

	private Label widthLabel;
	private Text widthText;

	private Point tableLabelSize;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param parentShell
	 */
	ViewerColumnsDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Initialize visible /non-visible columns.
	 * 
	 * @param columnObjs
	 */
	void setColumnsObjs(Object[] columnObjs) {
		IColumnInfoProvider columnInfo = doGetColumnInfoProvider();
		IColumnUpdater updater = doGetColumnUpdater();
		List visible = getVisible();
		List nonVisible = getNonVisible();
		visible.clear();
		nonVisible.clear();
		Object data = null;
		for (int i = 0; i < columnObjs.length; i++) {
			data = columnObjs[i];
			if (columnInfo.isColumnVisible(data)) {
				updater.setColumnVisible(data, true);
				updater.setColumnIndex(data, visible.size());
				visible.add(data);
			} else {
				updater.setColumnVisible(data, false);
				updater.setColumnIndex(data, nonVisible.size());
				nonVisible.add(data);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.ViewerSettingsAndStatusDialog#
	 * createDialogContentArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogContentArea(Composite dialogArea) {
		Composite composite = new Composite(dialogArea, SWT.NONE);
		GridLayout gridLayout = new GridLayout(4, false);
		gridLayout.marginHeight = 0;
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createInvisibleTable(composite);
		createMoveButtons(composite);
		createVisibleTable(composite);
		createUpDownBtt(composite);
		createWidthArea(composite);
		Object element = visibleViewer.getElementAt(0);
		if (element != null)
			visibleViewer.setSelection(new StructuredSelection(element));
		visibleViewer.getTable().setFocus();
		return composite;
	}

	/**
	 * The Up and Down button to change column ordering.
	 * 
	 * @param parent
	 */
	Control createUpDownBtt(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.marginHeight = 0;
		compositeLayout.marginWidth = 0;
		composite.setLayout(compositeLayout);
		composite.setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, true));
		
		Composite bttArea = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		bttArea.setLayout(layout);
		bttArea.setLayoutData(new GridData(SWT.NONE, SWT.CENTER, false, true));
		upButton = new Button(bttArea, SWT.PUSH);
		upButton.setText(JFaceResources.getString("ConfigureColumnsDialog_up")); //$NON-NLS-1$
		upButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				handleUpButton(event);
			}
		});
		setButtonLayoutData(upButton);
		((GridData)upButton.getLayoutData()).verticalIndent = tableLabelSize.y;
		upButton.setEnabled(false);

		downButton = new Button(bttArea, SWT.PUSH);
		downButton.setText(JFaceResources
				.getString("ConfigureColumnsDialog_down")); //$NON-NLS-1$
		downButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				handleDownButton(event);
			}
		});
		setButtonLayoutData(downButton);
		downButton.setEnabled(false);
		return bttArea;
	}

	/**
	 * Create the controls responsible to display/edit column widths.
	 * 
	 * @param parent
	 * @return {@link Control}
	 */
	Control createWidthArea(Composite parent) {

		Label dummy = new Label(parent, SWT.NONE);
		dummy.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 2, 1));
		
		Composite widthComposite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		widthComposite.setLayout(gridLayout);
		widthComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));
		
		widthLabel = new Label(widthComposite, SWT.NONE);
		widthLabel.setText(MarkerMessages.MarkerPreferences_WidthOfShownColumn);
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		widthLabel.setLayoutData(gridData);

		widthText = new Text(widthComposite, SWT.BORDER);
		widthText.addVerifyListener(new VerifyListener() {

			public void verifyText(VerifyEvent e) {
				if (e.character != 0 && e.keyCode != SWT.BS
						&& e.keyCode != SWT.DEL
						&& !Character.isDigit(e.character)) {
					e.doit = false;
				}
			}
		});

		gridData = new GridData();
		gridData.widthHint = convertWidthInCharsToPixels(5);
		widthText.setLayoutData(gridData);
		widthText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateWidth();
			}
		});
		setWidthEnabled(false);
		return widthText;
	}

	private void setWidthEnabled(boolean enabled) {
		widthLabel.setEnabled(enabled);
		widthText.setEnabled(enabled);
	}

	/**
	 * Creates the table that lists out visible columns in the viewer
	 * 
	 * @param parent
	 * @return {@link Control}
	 */
	Control createVisibleTable(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight =0;
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(composite, SWT.NONE);
		label.setText(MarkerMessages.MarkerPreferences_VisibleColumnsTitle);

		final Table table = new Table(composite, SWT.BORDER | SWT.MULTI);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = convertWidthInCharsToPixels(20);
		data.heightHint = table.getItemHeight() * 15;
		table.setLayoutData(data);

		final TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText(MarkerMessages.MarkerPreferences_VisibleColumnsTitle);
		Listener columnResize = new Listener() {
			public void handleEvent(Event event) {
				column.setWidth(table.getClientArea().width);
			}
		};
		table.addListener(SWT.Resize, columnResize);

		visibleViewer = new TableViewer(table);
		visibleViewer.setLabelProvider(doGetLabelProvider());
		visibleViewer.setContentProvider(ArrayContentProvider.getInstance());
		visibleViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						handleVisibleSelection(event.getSelection());
					}
				});
		table.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				handleToNonVisibleButton(event);
			}
		});
		visibleViewer.setInput(getVisible());
		return table;
	}

	/**
	 * Creates the table that lists out non-visible columns in the viewer
	 * 
	 * @param parent
	 * @return {@link Control}
	 */
	Control createInvisibleTable(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		composite.setLayout(gridLayout);

		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(composite, SWT.NONE);
		label.setText(MarkerMessages.MarkerPreferences_HiddenColumnsTitle);
		applyDialogFont(label);
		tableLabelSize = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		final Table table = new Table(composite, SWT.BORDER | SWT.MULTI);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = convertWidthInCharsToPixels(20);
		data.heightHint = table.getItemHeight() * 15;
		table.setLayoutData(data);

		final TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText(MarkerMessages.MarkerPreferences_HiddenColumnsTitle);
		Listener columnResize = new Listener() {
			public void handleEvent(Event event) {
				column.setWidth(table.getClientArea().width);
			}
		};
		table.addListener(SWT.Resize, columnResize);

		nonVisibleViewer = new TableViewer(table);
		nonVisibleViewer.setLabelProvider(doGetLabelProvider());
		nonVisibleViewer.setContentProvider(ArrayContentProvider.getInstance());
		nonVisibleViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						handleNonVisibleSelection(event.getSelection());
					}
				});
		table.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				handleToVisibleButton(event);
			}
		});
		nonVisibleViewer.setInput(getNonVisible());
		return table;
	}

	/**
	 * Creates buttons for moving columns from non-visible to visible and
	 * vice-versa
	 * 
	 * @param parent
	 * @return {@link Control}
	 */
	Control createMoveButtons(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.marginHeight = 0;
		compositeLayout.marginWidth = 0;
		composite.setLayout(compositeLayout);
		composite.setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, true));

		Composite bttArea = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		bttArea.setLayout(layout);
		bttArea.setLayoutData(new GridData(SWT.NONE, SWT.CENTER, false, true));

		toVisibleBtt = new Button(bttArea, SWT.PUSH);
		toVisibleBtt.setText(MarkerMessages.MarkerPreferences_MoveRight);
		setButtonLayoutData(toVisibleBtt);
		((GridData)toVisibleBtt.getLayoutData()).verticalIndent = tableLabelSize.y;
		toVisibleBtt.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				handleToVisibleButton(event);
			}
		});
		toVisibleBtt.setEnabled(false);

		toNonVisibleBtt = new Button(bttArea, SWT.PUSH);
		toNonVisibleBtt.setText(MarkerMessages.MarkerPreferences_MoveLeft);
		setButtonLayoutData(toNonVisibleBtt);

		toNonVisibleBtt.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				handleToNonVisibleButton(event);
			}
		});
		toNonVisibleBtt.setEnabled(false);

		return bttArea;
	}

	/**
	 * Handles a selection change in the viewer that lists out the non-visible
	 * columns
	 * 
	 * @param selection
	 */
	void handleNonVisibleSelection(ISelection selection) {
		Object[] nvKeys = ((IStructuredSelection) selection).toArray();
		toVisibleBtt.setEnabled(nvKeys.length > 0);
		if (visibleViewer.getControl().isFocusControl()
				&& getVisible().size() <= 1) {
			handleStatusUdpate(IStatus.INFO,
					MarkerMessages.MarkerPreferences_AtLeastOneVisibleColumn);
		} else {
			handleStatusUdpate(IStatus.INFO, getDefaultMessage());
		}
	}

	/**
	 * Handles a selection change in the viewer that lists out the visible
	 * columns. Takes care of various enablements.
	 * 
	 * @param selection
	 */
	void handleVisibleSelection(ISelection selection) {
		List selVCols = ((IStructuredSelection) selection).toList();
		List allVCols = getVisible();
		toNonVisibleBtt.setEnabled(selVCols.size() > 0
				&& allVCols.size() > selVCols.size());

		IColumnInfoProvider infoProvider = doGetColumnInfoProvider();
		boolean moveDown = !selVCols.isEmpty(), moveUp = !selVCols.isEmpty();
		Iterator iterator = selVCols.iterator();
		while (iterator.hasNext()) {
			Object columnObj = iterator.next();
			if (!infoProvider.isColumnMovable(columnObj)) {
				moveUp = false;
				moveDown = false;
				break;
			}
			int i = allVCols.indexOf(columnObj);
			if (i == 0) {
				moveUp = false;
				if (!moveDown) {
					break;
				}
			}
			if (i == (allVCols.size() - 1)) {
				moveDown = false;
				if (!moveUp) {
					break;
				}
			}
		}
		upButton.setEnabled(moveUp);
		downButton.setEnabled(moveDown);

		boolean edit = selVCols.size() == 1 ? infoProvider
				.isColumnResizable(selVCols.get(0)) : false;
		setWidthEnabled(edit);
		if (edit) {
			int width = infoProvider.getColumnWidth(selVCols.get(0));
			widthText.setText(Integer.toString(width));
		} else {
			widthText.setText(""); //$NON-NLS-1$
		}
	}

	/**
	 * Applies to visible columns, and handles the changes in the order of
	 * columns
	 * 
	 * @param e
	 *            event from the button click
	 */
	void handleDownButton(Event e) {
		IStructuredSelection selection = (IStructuredSelection) visibleViewer
				.getSelection();
		Object[] selVCols = selection.toArray();
		List allVCols = getVisible();
		IColumnUpdater updater = doGetColumnUpdater();
		for (int i = selVCols.length - 1; i >= 0; i--) {
			Object colObj = selVCols[i];
			int index = allVCols.indexOf(colObj);
			updater.setColumnIndex(colObj, index + 1);
			allVCols.remove(index);
			allVCols.add(index + 1, colObj);
		}
		visibleViewer.refresh();
		handleVisibleSelection(selection);
	}

	/**
	 * Applies to visible columns, and handles the changes in the order of
	 * columns
	 * 
	 * @param e
	 *            event from the button click
	 */
	void handleUpButton(Event e) {
		IStructuredSelection selection = (IStructuredSelection) visibleViewer
				.getSelection();
		Object[] selVCols = selection.toArray();
		List allVCols = getVisible();
		IColumnUpdater updater = doGetColumnUpdater();
		for (int i = 0; i < selVCols.length; i++) {
			Object colObj = selVCols[i];
			int index = allVCols.indexOf(colObj);
			updater.setColumnIndex(colObj, index - 1);
			allVCols.remove(index);
			allVCols.add(index - 1, colObj);
		}
		visibleViewer.refresh();
		handleVisibleSelection(selection);
	}

	/**
	 * Moves selected columns from non-visible to visible state
	 * 
	 * @param e
	 *            event from the button click
	 */
	void handleToVisibleButton(Event e) {
		IStructuredSelection selection = (IStructuredSelection) nonVisibleViewer
				.getSelection();
		List selVCols = selection.toList();
		List nonVisible = getNonVisible();
		nonVisible.removeAll(selVCols);

		List list = getVisible();
		list.addAll(selVCols);

		updateVisibility(selVCols, true);
		updateIndices(getVisible());
		updateIndices(getNonVisible());

		visibleViewer.refresh();
		visibleViewer.setSelection(selection);
		nonVisibleViewer.refresh();
		handleVisibleSelection(selection);
		handleNonVisibleSelection(nonVisibleViewer.getSelection());

	}

	/**
	 * Moves selected columns from visible to non-visible state
	 * 
	 * @param e
	 *            event from the button click
	 */
	protected void handleToNonVisibleButton(Event e) {
		if (visibleViewer.getControl().isFocusControl()
				&& getVisible().size() <= 1) {
			handleStatusUdpate(IStatus.INFO,
					MarkerMessages.MarkerPreferences_AtLeastOneVisibleColumn);
			return;
		}
		IStructuredSelection selection = (IStructuredSelection) visibleViewer
				.getSelection();
		List selVCols = selection.toList();
		getVisible().removeAll(selVCols);
		getNonVisible().addAll(selVCols);

		updateVisibility(selVCols, false);
		updateIndices(getVisible());
		updateIndices(getNonVisible());

		nonVisibleViewer.refresh();
		nonVisibleViewer.setSelection(selection);
		visibleViewer.refresh();
		handleVisibleSelection(visibleViewer.getSelection());
		handleNonVisibleSelection(nonVisibleViewer.getSelection());
	}

	void updateIndices(List list) {
		ListIterator iterator = list.listIterator();
		IColumnUpdater updater = doGetColumnUpdater();
		while (iterator.hasNext()) {
			updater.setColumnIndex(iterator.next(), iterator.previousIndex());
		}
	}

	void updateVisibility(List list, boolean visibility) {
		IColumnUpdater updater = doGetColumnUpdater();
		Iterator iterator = list.iterator();
		while (iterator.hasNext()) {
			updater.setColumnVisible(iterator.next(), visibility);
		}
	}
	
	protected void performDefaults() {
		refreshViewers();
		super.performDefaults();
	}

	/**
	 * Updates the UI based on values of the variable
	 */
	void refreshViewers() {
		if (nonVisibleViewer != null) {
			nonVisibleViewer.refresh();
		}
		if (visibleViewer != null) {
			visibleViewer.refresh();
		}
	}

	/**
	 * @return List of visible columns
	 */
	public List getVisible() {
		if (visible == null) {
			visible = new ArrayList();
		}
		return visible;
	}

	/**
	 * @return List of non-visible columns
	 */
	public List getNonVisible() {
		if (nonVisible == null) {
			nonVisible = new ArrayList();
		}
		return nonVisible;
	}

	/**
	 * An adapter class to {@link ITableLabelProvider}
	 * 
	 */
	class TableLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			return getText(element);
		}
	}

	/**
	 * Internal helper to @see {@link ViewerColumnsDialog#getLabelProvider()}
	 */
	ITableLabelProvider doGetLabelProvider() {
		return getLabelProvider();
	}

	/**
	 * The tables-columns need to be displayed appropriately. The supplied
	 * column objects are adapted to text and image as dictacted by this
	 * {@link ITableLabelProvider}
	 */
	protected abstract ITableLabelProvider getLabelProvider();

	/**
	 * Internal helper to @see
	 * {@link ViewerColumnsDialog#getColumnInfoProvider()}
	 */
	IColumnInfoProvider doGetColumnInfoProvider() {
		return getColumnInfoProvider();
	}

	/**
	 * To configure the columns we need further information. The supplied column
	 * objects are adapted for its properties via {@link IColumnInfoProvider}
	 */
	protected abstract IColumnInfoProvider getColumnInfoProvider();

	/**
	 * Internal helper to @see {@link ViewerColumnsDialog#getColumnUpdater()}
	 */
	IColumnUpdater doGetColumnUpdater() {
		return getColumnUpdater();
	}

	/**
	 * To configure properties/order of the columns is achieved via
	 * {@link IColumnUpdater}
	 */
	protected abstract IColumnUpdater getColumnUpdater();

	/**
	 * 
	 */
	private void updateWidth() {
		try {
			int width = Integer.parseInt(widthText.getText());
			Object data = ((IStructuredSelection) visibleViewer.getSelection())
					.getFirstElement();
			if (data != null) {
				IColumnUpdater updater = getColumnUpdater();
				updater.setColumnWidth(data, width);
			}
		} catch (NumberFormatException ex) {
			// ignore
		}
	}

	/**
	 * Update various aspects of a columns from a viewer such
	 * {@link TableViewer}
	 */
	public interface IColumnInfoProvider {

		/**
		 * Get corresponding index for the column
		 * 
		 * @param columnObj
		 */
		public int getColumnIndex(Object columnObj);

		/**
		 * Get the width of the column
		 * 
		 * @param columnObj
		 */
		public int getColumnWidth(Object columnObj);

		/**
		 * Returns true if the column represented by parameters is showing in
		 * the viewer
		 * 
		 * @param columnObj
		 */
		public boolean isColumnVisible(Object columnObj);

		/**
		 * Returns true if the column represented by parameters is configured as
		 * movable
		 * 
		 * @param columnObj
		 */
		public boolean isColumnMovable(Object columnObj);

		/**
		 * Returns true if the column represented by parameters is configured as
		 * resizable
		 * 
		 * @param columnObj
		 */
		public boolean isColumnResizable(Object columnObj);

	}

	/**
	 * Update various aspects of a columns from a viewer such
	 * {@link TableViewer}
	 */
	public interface IColumnUpdater {

		/**
		 * Set the column represented by parameters as visible
		 * 
		 * @param columnObj
		 * @param visible
		 */
		public void setColumnVisible(Object columnObj, boolean visible);

		/**
		 * Dummy method - more a result of symmetry
		 * 
		 * @param columnObj
		 * @param movable
		 */
		public void setColumnMovable(Object columnObj, boolean movable);

		/**
		 * Call back to notify change in the index of the column represented by
		 * columnObj
		 * 
		 * @param columnObj
		 * @param index
		 */
		public void setColumnIndex(Object columnObj, int index);

		/**
		 * Dummy method - more a result of symmetry
		 * 
		 * @param columnObj
		 * @param resizable
		 */
		public void setColumnResizable(Object columnObj, boolean resizable);

		/**
		 * Call back to notify change in the width of the column represented by
		 * columnObj
		 * 
		 * @param columnObj
		 * @param newWidth
		 */
		public void setColumnWidth(Object columnObj, int newWidth);

	}

	// //////////////////////////////////////////////////////////////////////////////////
	/**
	 * Ignore the class below as it is simply meant to test the above. I intend
	 * to retain this for a while.
	 */
	static class TestData {

		final Object key;

		final int keyIndex;

		int newIndex, width;

		boolean visibility, movable, resizable;

		TestData(Object key, int currIndex) {
			this.key = key;
			this.keyIndex = currIndex;
		}

		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + keyIndex;
			return result;
		}

		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof TestData)) {
				return false;
			}
			TestData other = (TestData) obj;
			if (key == null) {
				if (other.key != null) {
					return false;
				}
			} else if (!key.equals(other.key)) {
				return false;
			}
			if (keyIndex != other.keyIndex) {
				return false;
			}
			return true;
		}

		public String toString() {
			return key.toString();
		}

		private static ViewerColumnsDialog getColumnsDialog(Shell shell,
				final TestData[] colums) {
			ViewerColumnsDialog dialog = new ViewerColumnsDialog(shell) {

				protected IColumnInfoProvider getColumnInfoProvider() {
					return getInfoProvider(colums);
				}

				protected ITableLabelProvider getLabelProvider() {
					return new TableLabelProvider();
				}

				protected IColumnUpdater getColumnUpdater() {
					return getUpdater(colums);
				}
			};
			dialog.setColumnsObjs(colums);
			return dialog;
		}

		private static IColumnUpdater getUpdater(final TestData[] data) {
			return new IColumnUpdater() {

				public void setColumnWidth(Object columnObj, int newWidth) {
					((TestData) columnObj).width = newWidth;
				}

				public void setColumnVisible(Object columnObj, boolean visible) {
					((TestData) columnObj).visibility = visible;
				}

				public void setColumnResizable(Object columnObj,
						boolean resizable) {

				}

				public void setColumnMovable(Object columnObj, boolean movable) {
					((TestData) columnObj).movable = movable;

				}

				public void setColumnIndex(Object columnObj, int index) {
					((TestData) columnObj).newIndex = index;
				}
			};
		}

		private static IColumnInfoProvider getInfoProvider(
				final TestData[] colData) {
			return new IColumnInfoProvider() {

				public boolean isColumnVisible(Object columnObj) {
					return ((TestData) columnObj).visibility;
				}

				public boolean isColumnResizable(Object columnObj) {
					return ((TestData) columnObj).resizable;
				}

				public boolean isColumnMovable(Object columnObj) {
					return ((TestData) columnObj).movable;
				}

				public int getColumnWidth(Object columnObj) {
					return ((TestData) columnObj).width;
				}

				public int getColumnIndex(Object columnObj) {
					return ((TestData) columnObj).newIndex;
				}
			};
		}

		private static TestData[] genData(int count) {
			String[] cols = new String[count];
			for (int i = 0; i < cols.length; i++) {
				cols[i] = new String("Column-" + (i + 1)); //$NON-NLS-1$
			}
			Random random = new Random();

			boolean[] visibility = new boolean[cols.length];
			Arrays.fill(visibility, true);
			int ranInt = random.nextInt() % cols.length;
			for (int i = 0; i < ranInt; i++) {
				visibility[random.nextInt(ranInt)] = false;
			}

			boolean[] resizable = new boolean[cols.length];
			Arrays.fill(resizable, true);
			ranInt = random.nextInt() % cols.length;
			for (int i = 0; i < ranInt; i++) {
				resizable[random.nextInt(ranInt)] = false;
			}

			boolean[] movable = new boolean[cols.length];
			Arrays.fill(movable, true);
			ranInt = random.nextInt() % cols.length;
			for (int i = 0; i < ranInt; i++) {
				movable[random.nextInt(ranInt)] = false;
			}

			int[] widths = new int[cols.length];
			Arrays.fill(widths, 100);
			return TestData.generateColumnsData(cols, visibility, resizable,
					movable, widths);
		}

		public static TestData[] generateColumnsData(Object[] keys,
				boolean[] visibility, boolean[] resizable, boolean[] movable,
				int[] widths) {
			TestData[] colData = new TestData[keys.length];
			int m = 0, n = 0;
			for (int i = 0; i < colData.length; i++) {
				TestData data = new TestData(keys[i], i);
				data.visibility = visibility[i];
				data.resizable = resizable[i];
				data.movable = movable[i];
				data.width = widths[i];
				if (data.visibility) {
					data.newIndex = m++;
				} else {
					data.newIndex = n++;
				}
				colData[i] = data;
			}
			return colData;
		}

		/**
		 * Demo
		 * 
		 * @param args
		 */
		public static void main(String[] args) {
			Display display = new Display();
			final Shell shell = new Shell(display);
			shell.setLayout(new FillLayout());
			ViewerColumnsDialog dialog = getColumnsDialog(shell, genData(100));
			dialog.open();
			shell.dispose();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			display.dispose();

		}

	}

}
