/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.ViewSettingsDialog;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * This was introduced as a fix to Bug , as an effort to combine the columns and
 * preference dialogs into one. It should be noted that the class can be re-used
 * or turned into a tool for column viewers in general, but with some
 * modifications. See example attached at the end of this class
 * 
 * @since 3.7
 * 
 * @author Hitesh Soliwal
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * 
 */
abstract class ViewerColumnsDialog extends ViewSettingsDialog {

	/** The list contains columns that are currently visible in viewer */
	private List visible;

	/** The list contains columns that are note shown in viewer */
	private List nonVisible;

	/**
	 * The number of elements to show at Max. A zero value may indicate
	 * disablement of limit
	 */
	private int limitValue;

	/** The message area */
	private CLabel messageLabel;

	private TableViewer visibleViewer, nonVisibleViewer;

	private Button upButton, downButton;

	private Button toVisibleBtt, toNonVisibleBtt;

	private Text widthText, limitEditor;

	/**
	 * A listener to validate positive integer numbers only
	 */
	Listener postivIntTextListener = new Listener() {

		private String intialValue;

		public void handleEvent(Event event) {
			intialValue = intialValue != null ? intialValue : Integer
					.toString(0);
			intialValue = handleIntegerFieldChange(event, intialValue);
		}
	};

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
	 * @see
	 * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets
	 * .Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(JFaceResources
				.getString("ConfigureColumnsDialog_Title")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#getShellStyle()
	 */
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	protected Control createDialogArea(Composite parent) {

		Composite dialogArea = (Composite) super.createDialogArea(parent);

		dialogArea.setLayout(new GridLayout(1, true));

		initializeDialogUnits(dialogArea);

		createMessageArea(dialogArea);

		createLimitArea(dialogArea);

		createColumnsArea(dialogArea);

		applyDialogFont(dialogArea);

		initializeDlg();

		return dialogArea;
	}

	/**
	 * 
	 */
	void initializeDlg() {
		handleStatusUdpate(IStatus.INFO, getDefaultMessage());
	}

	/**
	 * Create message area.
	 * 
	 * @param parent
	 */
	void createMessageArea(Composite parent) {
		messageLabel = new CLabel(parent, SWT.NONE);
		messageLabel.setImage(JFaceResources
				.getImage(Dialog.DLG_IMG_MESSAGE_INFO));
		messageLabel
				.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
	}

	/**
	 * Create element limit area.
	 * 
	 * @param parent
	 */
	void createLimitArea(Composite parent) {
		Composite composite = new Group(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		CLabel cLabel = new CLabel(composite, SWT.NONE);
		cLabel.setText(MarkerMessages.MarkerPreferences_VisibleItems);
		cLabel.setLayoutData(new GridData());

		limitEditor = new Text(composite, SWT.BORDER);
		limitEditor.setText(Integer.toString(getLimitValue()));
		limitEditor.setLayoutData(new GridData());
		limitEditor.addListener(SWT.FocusOut, postivIntTextListener);
		limitEditor.addListener(SWT.KeyDown, postivIntTextListener);
		limitEditor.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				int limit = 0;
				try {
					limit = Integer.parseInt(limitEditor.getText().trim());
				} catch (Exception e) {
					return;// ignore this one
				}
				setLimitValue(limit);
			}
		});
		limitEditor.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	/**
	 * Create the controls to configure columns.
	 * 
	 * @param dialogArea
	 */
	void createColumnsArea(Composite dialogArea) {
		Group columnArea = new Group(dialogArea, SWT.NONE);
		columnArea.setLayout(new GridLayout(4, false));
		GridData gData = new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		columnArea.setLayoutData(gData);
		columnArea.setText(MarkerMessages.MarkerPreferences_ColumnGroupTitle);

		createInvisibleTable(columnArea);
		createMoveButtons(columnArea);
		createVisibleTable(columnArea);
		createUpDownBtt(columnArea);
		createWidthArea(columnArea);
	}

	/**
	 * The Up and Down button to change column ordering.
	 * 
	 * @param parent
	 */
	void createUpDownBtt(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.horizontalSpacing = 0;
		layout.marginRight = -1;
		layout.marginLeft = -1;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_END));
		upButton = new Button(composite, SWT.PUSH);
		upButton.setText(JFaceResources.getString("ConfigureColumnsDialog_up")); //$NON-NLS-1$
		upButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				handleUpButton(event);
			}
		});
		upButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		upButton.setEnabled(false);

		downButton = new Button(composite, SWT.PUSH);
		downButton.setText(JFaceResources
				.getString("ConfigureColumnsDialog_down")); //$NON-NLS-1$
		downButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				handleDownButton(event);
			}
		});
		downButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		downButton.setEnabled(false);
	}

	/**
	 * Create the controls responsible to display/edit column widths.
	 * 
	 * @param parent
	 */
	void createWidthArea(Composite parent) {
		Label widthLabel = new Label(parent, SWT.NONE);
		widthLabel.setText(JFaceResources
				.getString("ConfigureColumnsDialog_WidthOfSelectedColumn")); //$NON-NLS-1$
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_END);
		gridData.horizontalSpan = 3;
		widthLabel.setLayoutData(gridData);

		widthText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		widthText.setText(Integer.toString(0));
		gridData = new GridData(GridData.FILL_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_CENTER);
		gridData.widthHint = convertWidthInCharsToPixels(5);
		widthText.setLayoutData(gridData);
		widthText.addListener(SWT.KeyDown, postivIntTextListener);
		widthText.addListener(SWT.FocusOut, postivIntTextListener);
		widthText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				if (widthText.isEnabled()) {
					int width = 0;
					try {
						width = Integer.parseInt(widthText.getText().trim());
					} catch (Exception e) {
						return;// ignore this one
					}
					Object data = ((IStructuredSelection) visibleViewer
							.getSelection()).getFirstElement();
					if (data != null) {
						IColumnUpdater updater = getColumnUpdater();
						updater.setColumnWidth(data, width);
					}
				}
			}
		});
		widthText.setText(MarkerSupportInternalUtilities.EMPTY_STRING);
		widthText.setEditable(false);
	}

	/**
	 * Adapter to {@link IStructuredContentProvider}
	 */
	abstract class ContentProviderAdapter implements IStructuredContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/**
	 * Creates the table that lists out visible columns in the viewer
	 * 
	 * @param parent
	 */
	void createVisibleTable(Composite parent) {
		final Table table = new Table(parent, SWT.BORDER | SWT.MULTI);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = convertWidthInCharsToPixels(30);
		data.heightHint = table.getItemHeight() * 15;
		table.setLayoutData(data);
		table.setHeaderVisible(true);

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
		visibleViewer.setContentProvider(new ContentProviderAdapter() {
			public Object[] getElements(Object inputElement) {
				return getVisible().toArray();
			}
		});
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
		visibleViewer.setInput(this);
	}

	/**
	 * Creates the table that lists out non-visible columns in the viewer
	 * 
	 * @param parent
	 */
	void createInvisibleTable(Composite parent) {
		final Table table = new Table(parent, SWT.BORDER | SWT.MULTI);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = convertWidthInCharsToPixels(30);
		data.heightHint = table.getItemHeight() * 15;
		table.setLayoutData(data);
		table.setHeaderVisible(true);

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
		nonVisibleViewer.setContentProvider(new ContentProviderAdapter() {
			public Object[] getElements(Object inputElement) {
				return getNonVisible().toArray();
			}
		});
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
		nonVisibleViewer.setInput(this);
	}

	/**
	 * Creates buttons for moving columns from non-visible to visible and
	 * vice-versa
	 * 
	 * @param parent
	 */
	void createMoveButtons(Composite parent) {
		Composite bttArea = new Composite(parent, SWT.NONE);
		bttArea.setLayout(new GridLayout(1, true));
		bttArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		toNonVisibleBtt = new Button(bttArea, SWT.PUSH);
		toNonVisibleBtt
				.setText(getDefaultOrientation() == SWT.RIGHT_TO_LEFT ? MarkerMessages.MarkerPreferences_MoveRight
						: MarkerMessages.MarkerPreferences_MoveLeft);
		toNonVisibleBtt.setLayoutData(new GridData());

		toNonVisibleBtt.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				handleToNonVisibleButton(event);
			}
		});
		toNonVisibleBtt.setEnabled(false);

		toVisibleBtt = new Button(bttArea, SWT.PUSH);
		toVisibleBtt
				.setText(getDefaultOrientation() == SWT.RIGHT_TO_LEFT ? MarkerMessages.MarkerPreferences_MoveLeft
						: MarkerMessages.MarkerPreferences_MoveRight);
		toVisibleBtt.setLayoutData(new GridData());
		toVisibleBtt.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				handleToVisibleButton(event);
			}
		});
		toVisibleBtt.setEnabled(false);
	}

	/**
	 * Display the error message and an appropriate icon.
	 * 
	 * @param messgage
	 * @param severity
	 */
	protected void handleStatusUdpate(int severity, String messgage) {
		Image image = null;
		switch (severity) {
		case IStatus.ERROR: {
			if (messgage == null) {
				messgage = getErrorMessage();
			}
			image = getErrorImage();
			break;
		}
		case IStatus.WARNING: {
			image = getWarningImage();
			break;
		}
		case IStatus.OK:
		case IStatus.INFO:
		default:
			image = getInfoImage();
		}
		messageLabel.setImage(image);
		if (messgage == null) {
			messgage = getDefaultMessage();
		}
		if (messgage == null) {
			messgage = MarkerSupportInternalUtilities.EMPTY_STRING;
		}
		messageLabel.setText(messgage);
	}

	/**
	 * Return the message to display when dialog is opened.
	 */
	protected String getDefaultMessage() {
		return MarkerMessages.MarkerPreferences_ZeroOrBlankValueCanBeUsedToDisableTheLimit;
	}

	/**
	 * @return Returns the error message to display for a wrong limit value.
	 */
	protected String getErrorMessage() {
		return JFaceResources.getString("IntegerFieldEditor.errorMessage"); //$NON-NLS-1$
	}

	/**
	 */
	protected Image getInfoImage() {
		return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
	}

	/**
	 */
	protected Image getWarningImage() {
		return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
	}

	/**
	 */
	protected Image getErrorImage() {
		return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.preferences.ViewSettingsDialog#performDefaults()
	 */
	protected void performDefaults() {
		refreshViewers();
		super.performDefaults();
	}

	/**
	 * 
	 * @param event
	 * @param intialvalue
	 * @return new integer value
	 */
	String handleIntegerFieldChange(Event event, String intialvalue) {
		Text text = (Text) event.widget;
		String value = text.getText().trim();
		switch (event.type) {
		case SWT.KeyDown:
			if (!Character.isDigit(event.character)) {
				if (!Character.isISOControl(event.character)) {
					handleStatusUdpate(IStatus.ERROR, getErrorMessage());
					event.doit = false;
					return intialvalue;
				}
			}
		case SWT.FocusOut:
			if (value.length() == 0) {
				value = Integer.toString(0);
				text.setText(value);
				text.selectAll();
				handleStatusUdpate(IStatus.INFO, null);
			} else {
				try {
					Integer.parseInt(value);
					handleStatusUdpate(IStatus.INFO, null);
				} catch (Exception e) {
					value = intialvalue;
					text.setText(value);
					text.selectAll();
					handleStatusUdpate(IStatus.ERROR, getErrorMessage());
					event.doit = false;
				}
			}
		}
		return value;
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
		if (edit) {
			widthText.setText(Integer.toString(infoProvider
					.getColumnWidth(selVCols.get(0))));
		} else {
			widthText.setText(Integer.toString(0));
		}
		widthText.setEditable(edit);
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
		if (getVisible().size() <= 1) {
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
		handleStatusUdpate(IStatus.INFO, getDefaultMessage());
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

	/**
	 * Updates the UI based on values of the variable
	 */
	void refreshViewers() {
		if (limitEditor != null) {
			limitEditor.setText(Integer.toString(getLimitValue()));
		}
		if (nonVisibleViewer != null) {
			nonVisibleViewer.refresh();
		}
		if (visibleViewer != null) {
			visibleViewer.refresh();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	protected boolean isResizable() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		super.okPressed();
	}

	/**
	 * @return Returns the limitValue.
	 */
	public int getLimitValue() {
		return limitValue;
	}

	/**
	 * @param limitValue
	 *            The limitValue to set.
	 */
	void setLimitValue(int limitValue) {
		this.limitValue = limitValue;
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

	// //////////////////////////////////////////////////////////////////////////////////
}
