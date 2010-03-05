/*******************************************************************************
 * Copyright (c) 2008, 2009 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - [252996] initial API and implementation
 *     IBM Corporation - ongoing implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFilterMatcherDescriptor;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.FindReplaceDocumentAdapterContentProposalProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.ide.dialogs.UIResourceFilterDescription;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * A widget group that displays resource filters. Includes buttons to edit,
 * remove existing filters and create new ones.
 */
public class ResourceFilterGroup {

	private Button addButton = null;
	private Button removeButton = null;
	private Button upButton = null;
	private Button downButton = null;
	private Button editButton = null;

	private TreeViewer filterView;
	private Filters filters;
	private UIResourceFilterDescription[] initialFilters = new UIResourceFilterDescription[0];
	private LabelProvider labelProvider;
	private Image checkIcon = null;

	private boolean tableViewCellEditorAdequatlyUsable = false;
	private boolean allowReordering = false;

	// parent shell
	private Shell shell;

	private IContainer nonExistantResource = getNonExistantResource();
	private IContainer resource = nonExistantResource;

	/**
	 * 
	 */
	public ResourceFilterGroup() {
		ImageDescriptor descriptor = AbstractUIPlugin
				.imageDescriptorFromPlugin(IDEWorkbenchPlugin.IDE_WORKBENCH,
						"$nl$/icons/full/obj16/header_complete.gif"); //$NON-NLS-1$
		if (descriptor != null)
			checkIcon = descriptor.createImage();
	}

	/**
	 * Set the IContainer resource to edit
	 * 
	 * @param res
	 *            the container resource
	 */

	public void setContainer(IContainer res) {
		resource = res;
	}

	private IContainer getNonExistantResource() {
		String projectName = "nonExistantProject_"; //$NON-NLS-1$
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(
				projectName);
		int iteration = 0;
		while (p.exists()) {
			p = ResourcesPlugin.getWorkspace().getRoot().getProject(
					projectName + iteration);
			iteration++;
		}
		return p;
	}

	class Filters extends FilterCopy {
		public Filters(IContainer resource) {
			try {
				IResourceFilterDescription[] tmp = resource.getFilters();
				children = new LinkedList();
				for (int i = 0; i < tmp.length; i++)
					addChild(new FilterCopy(UIResourceFilterDescription.wrap(tmp[i])));
			} catch (CoreException e) {
				ErrorDialog.openError(shell, NLS.bind(
						IDEWorkbenchMessages.InternalError, null), e
						.getLocalizedMessage(), e.getStatus());
			}
		}

		public Filters(IResourceFilterDescription filters[]) {
			children = new LinkedList();
			if (filters != null) {
				for (int i = 0; i < filters.length; i++)
					addChild(new FilterCopy(UIResourceFilterDescription.wrap(filters[i])));
			}
		}

		public Filters(UIResourceFilterDescription filters[]) {
			children = new LinkedList();
			if (filters != null) {
				for (int i = 0; i < filters.length; i++)
					addChild(new FilterCopy(filters[i]));
			}
		}

		boolean changed = false;
		public LinkedList/* <IResourceFilterDescription> */trash = new LinkedList/*
																	 * <IResourceFilterDescription
																	 * >
																	 */();

		public void add(FilterCopy newFilter) {
			super.addChild(newFilter);
			changed = true;
		}

		public void remove(FilterCopy filter) {
			super.removeChild(filter);
			if (filter.original != null)
				trash.add(filter);
			changed = true;
		}

		public void moveUp(UIResourceFilterDescription filter) {
			FilterCopy[] content = getChildren();
			for (int i = 1; i < content.length; i++) {
				if (content[i] == filter) {
					FilterCopy tmp = content[i - 1];
					content[i - 1] = content[i];
					content[i] = tmp;
				}
			}
			children = new LinkedList(Arrays.asList(content));
			changed = true;
		}

		public void moveDown(UIResourceFilterDescription filter) {
			FilterCopy[] content = getChildren();
			for (int i = 0; i < (content.length - 1); i++) {
				if (content[i] == filter) {
					FilterCopy tmp = content[i + 1];
					content[i + 1] = content[i];
					content[i] = tmp;
				}
			}
			children = new LinkedList(Arrays.asList(content));
			changed = true;
		}

		public int getChildrenLimit() {
			return Integer.MAX_VALUE;
		}

		protected void argumentsChanged() {
			changed = true;
		}

		public boolean hasChanged() {
			if (changed)
				return true;
			Iterator it = children.iterator();
			while (it.hasNext()) {
				FilterCopy filter = (FilterCopy) it.next();
				if (filter.hasChanged())
					return true;
			}
			return false;
		}

		public boolean isFirst(FilterCopy o) {
			if (children.size() > 0)
				return children.getFirst().equals(o);
			return false;
		}

		public boolean isLast(FilterCopy o) {
			if (children.size() > 0)
				return children.getLast().equals(o);
			return false;
		}

		public void removeAll() {
			if (children.size() > 0) {
				super.removeAll();
				changed = true;
			}
		}
	}

	class TreeContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof FilterCopy)
				return ((FilterCopy) parentElement).getChildren();
			return null;
		}

		public Object getParent(Object element) {
			if (element instanceof FilterCopy) {
				if (((FilterCopy) element).getParent() != null)
					return ((FilterCopy) element).getParent();
				return filters;
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof FilterCopy) {
				FilterCopy[] children = ((FilterCopy) element).getChildren();
				return children != null && children.length > 0;
			}
			return false;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class LabelProvider implements ITableLabelProvider {
		FilterTypeUtil util;

		public LabelProvider() {
			util = new FilterTypeUtil();
		}

		String getColumnID(int index) {
			return (String) filterView.getColumnProperties()[index];
		}

		public boolean isPartialFilter(Object element) {
			FilterCopy copy = (FilterCopy) element;
			return copy.isUnderAGroupFilter();
		}

		public Image getColumnImage(Object element, int columnIndex) {
			if (!isPartialFilter(element)) {
				if (getColumnID(columnIndex).equals(FilterTypeUtil.ARGUMENTS)) {
					Object index = FilterTypeUtil.getValue(
							(FilterCopy) element, FilterTypeUtil.TARGET);
					return util.getImage(FilterTypeUtil.TARGET,
							((Integer) index).intValue());
				}
				if (getColumnID(columnIndex).equals(FilterTypeUtil.MODE)) {
					Object index = FilterTypeUtil.getValue(
							(FilterCopy) element, FilterTypeUtil.MODE);
					return util.getImage(FilterTypeUtil.MODE, ((Integer) index)
							.intValue());
				}
				if (getColumnID(columnIndex).equals(FilterTypeUtil.INHERITABLE)) {
					Object condition = FilterTypeUtil.getValue(
							(FilterCopy) element, FilterTypeUtil.INHERITABLE);
					if (((Boolean) condition).booleanValue())
						return checkIcon;
				}
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			FilterCopy filter = ((FilterCopy) element);
			String column = getColumnID(columnIndex);
			return getValue(filter, column);
		}

		private String getValue(FilterCopy filter, String column) {
			if (column.equals(FilterTypeUtil.ID)) {
				String id = filter.getId();
				IFilterMatcherDescriptor descriptor = FilterTypeUtil.getDescriptor(id);
				if (descriptor != null)
					return descriptor.getName();
			}
			if (column.equals(FilterTypeUtil.MODE)) {
				if (!isPartialFilter(filter)) {
					if ((filter.getType() & IResourceFilterDescription.INCLUDE_ONLY) != 0)
						return NLS
								.bind(
										IDEWorkbenchMessages.ResourceFilterPage_includeOnlyColumn,
										null);
					return NLS
							.bind(
									IDEWorkbenchMessages.ResourceFilterPage_excludeAllColumn,
									null);
				}
				return getFilterTypeName(filter);
			}
			if (column.equals(FilterTypeUtil.TARGET)) {
				boolean includeFiles = (filter.getType() & IResourceFilterDescription.FILES) != 0;
				boolean includeFolders = (filter.getType() & IResourceFilterDescription.FOLDERS) != 0;
				if (includeFiles && includeFolders)
					return NLS
							.bind(
									IDEWorkbenchMessages.ResourceFilterPage_filesAndFoldersColumn,
									null);
				if (includeFiles)
					return NLS
							.bind(
									IDEWorkbenchMessages.ResourceFilterPage_filesColumn,
									null);
				if (includeFolders)
					return NLS
							.bind(
									IDEWorkbenchMessages.ResourceFilterPage_foldersColumn,
									null);
			}
			if (column.equals(FilterTypeUtil.ARGUMENTS)) {
				if (filter.hasStringArguments())
					return filter.getArguments() != null ? filter
							.getArguments().toString() : ""; //$NON-NLS-1$
				if ((filter.getChildrenLimit() > 0)
						&& !filter.isUnderAGroupFilter())
					return "< " + getFilterTypeName(filter) + " >"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			return null;
		}

		private String getFilterTypeName(FilterCopy filter) {
			IFilterMatcherDescriptor desc = FilterTypeUtil.getDescriptor(filter
					.getId());
			if (desc != null)
				return desc.getName();
			return ""; //$NON-NLS-1$
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
	}

	class CellModifier implements ICellModifier {
		public boolean canModify(Object element, String property) {
			FilterCopy filter = (FilterCopy) element;
			if (property.equals(FilterTypeUtil.ARGUMENTS)
					&& !filter.hasStringArguments())
				return false;
			return true;
		}

		public Object getValue(Object element, String property) {
			FilterCopy filter = (FilterCopy) element;
			return FilterTypeUtil.getValue(filter, property);
		}

		public void modify(Object element, String property, Object value) {
			FilterCopy filter = (FilterCopy) ((TableItem) element).getData();
			FilterTypeUtil.setValue(filter, property, value);
			filterView.refresh(filter);
		}
	}

	/**
	 * Creates the widget group. Callers must call <code>dispose</code> when the
	 * group is no longer needed.
	 * 
	 * @param parent
	 *            the widget parent
	 * @return container of the widgets
	 */
	public Control createContents(Composite parent) {

        Font font = parent.getFont();
		shell = parent.getShell();

		if (resource == null) {
			Label label = new Label(parent, SWT.NONE);
			label.setText(NLS.bind(
					IDEWorkbenchMessages.ResourceFilterPage_noResource, null));
	        label.setFont(font);
			return label;
		}

		if (resource == nonExistantResource)
			filters = new Filters(initialFilters);
		else
			filters = new Filters(resource);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);
		composite.setFont(font);

		Label label = new Label(composite, 0);
		label.setText(NLS.bind(IDEWorkbenchMessages.ResourceFilterPage_title,
				null));
		data = new GridData(GridData.FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setFont(font);
		
		createViewerGroup(composite);
		createButtonGroup(composite);

		refreshEnablement();
		return composite;
	}

	private void createViewerGroup(Composite parent) {
		filterView = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.BORDER
				| SWT.H_SCROLL);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		filterView.getTree().setLayoutData(data);
		filterView.setColumnProperties(FilterTypeUtil.columnNames);

		filterView.setContentProvider(new TreeContentProvider());
		filterView.setInput(filters);
		filterView.getTree().setFont(parent.getFont());

		filterView.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				refreshEnablement();
			}
		});

		TreeColumn column = new TreeColumn(filterView.getTree(), 0);
		column
				.setText(NLS
						.bind(
								IDEWorkbenchMessages.ResourceFilterPage_columnFilterMode,
								null));
		column.setData(FilterTypeUtil.MODE);
		column.setResizable(true);
		column.setMoveable(false);
		column.setWidth(getMinimumColumnWidth(column, 130));

		column = new TreeColumn(filterView.getTree(), 0);
		column.setText(NLS.bind(
				IDEWorkbenchMessages.ResourceFilterPage_columnFilterArguments,
				null));
		column.setData(FilterTypeUtil.ARGUMENTS);
		column.setResizable(true);
		column.setMoveable(true);
		column.setWidth(getMinimumColumnWidth(column, 120));

		column = new TreeColumn(filterView.getTree(), 0);
		column
				.setText(NLS
						.bind(
								IDEWorkbenchMessages.ResourceFilterPage_columnFilterInheritable,
								null));
		column.setData(FilterTypeUtil.INHERITABLE);
		column.setResizable(true);
		column.setMoveable(false);
		column.setAlignment(SWT.CENTER);
		column.setWidth(getMinimumColumnWidth(column, 70));

		filterView.getTree().setHeaderVisible(true);
		filterView.getTree().showColumn(filterView.getTree().getColumn(0));
		labelProvider = new LabelProvider();
		filterView.setLabelProvider(labelProvider);

		CellEditor[] editors = new CellEditor[5];
		editors[0] = new ComboBoxCellEditor(filterView.getTree(),
				FilterTypeUtil.getFilterNames(false), SWT.READ_ONLY);
		editors[1] = new ComboBoxCellEditor(filterView.getTree(),
				FilterTypeUtil.getModes(), SWT.READ_ONLY);
		editors[2] = new ComboBoxCellEditor(filterView.getTree(),
				FilterTypeUtil.getTargets(), SWT.READ_ONLY);
		editors[3] = new CheckboxCellEditor(filterView.getTree());
		editors[4] = new TextCellEditor(filterView.getTree());

		if (tableViewCellEditorAdequatlyUsable) {
			filterView.setCellEditors(editors);
			filterView.setCellModifier(new CellModifier());
		}

		filterView.getTree().addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
				handleEdit();
			}

			public void mouseDown(MouseEvent e) {
			}

			public void mouseUp(MouseEvent e) {
			}
		});
		FilterCopyDrag drag = new FilterCopyDrag();
		int ops = DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { filterCopyTransfer };
		filterView.addDragSupport(ops, transfers, drag);
		filterView.addDropSupport(ops, transfers,
				new FilterCopyDrop(filterView));

		filterView.getTree().addMenuDetectListener(new MenuDetectListener() {
			public void menuDetected(MenuDetectEvent e) {
				MenuManager mgr = new MenuManager();
				mgr.add(addSubFilterAction);
				filterView.getControl().setMenu(
						mgr.createContextMenu(filterView.getControl()));
			}
		});
	}

	Action addSubFilterAction = new AddSubFilterAction();

	class AddSubFilterAction extends Action {

		public AddSubFilterAction() {
			setText(NLS
					.bind(
							IDEWorkbenchMessages.ResourceFilterPage_addSubFilterActionLabel,
							null));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.Action#run()
		 */
		public void run() {
			ISelection selection = filterView.getSelection();
			if (selection instanceof IStructuredSelection) {
				FilterCopy filter = (FilterCopy) ((IStructuredSelection) selection)
						.getFirstElement();
				if (filter.getChildrenLimit() > 0) {
					FilterCopy newFilter = new FilterCopy();
					newFilter.setParent(filter);
					FilterEditDialog dialog = new FilterEditDialog(shell,
							newFilter);
					if (dialog.open() == Window.OK) {
						filter.addChild(newFilter);
						filterView.refresh();
					}
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.Action#isEnabled()
		 */
		public boolean isEnabled() {
			ISelection selection = filterView.getSelection();
			if (selection instanceof IStructuredSelection) {
				FilterCopy filter = (FilterCopy) ((IStructuredSelection) selection)
						.getFirstElement();
				return filter.getChildrenLimit() > 0;
			}
			return false;
		}
	}

	class FilterCopyDrop extends ViewerDropAdapter {
		protected FilterCopyDrop(Viewer viewer) {
			super(viewer);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang
		 * .Object)
		 */
		public boolean performDrop(Object data) {
			Object target = getCurrentTarget();
			if (target == null)
				target = filters;
			FilterCopy[] toDrop = (FilterCopy[]) data;

			if (target instanceof FilterCopy) {
				for (int i = 0; i < toDrop.length; i++)
					if (toDrop[i].equals(target)
							|| ((FilterCopy) target).hasParent(toDrop[i]))
						return false;
			}

			for (int i = 0; i < toDrop.length; i++) {
				if (target instanceof Filters)
					filters.add(toDrop[i]);
				if (target instanceof FilterCopy)
					((FilterCopy) target).addChild(toDrop[i]);
				filterView.refresh();
				filterView.reveal(toDrop[i]);
			}
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang
		 * .Object, int, org.eclipse.swt.dnd.TransferData)
		 */
		public boolean validateDrop(Object target, int operation,
				TransferData transferType) {
			if (filterCopyTransfer.isSupportedType(transferType)) {
				if (target instanceof FilterCopy)
					return ((FilterCopy) target).canAcceptDrop();
				return true;
			}
			return false;
		}
	}

	class FilterCopyDrag implements DragSourceListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse.swt
		 * .dnd.DragSourceEvent)
		 */
		public void dragFinished(DragSourceEvent event) {
			if (event.detail == DND.DROP_MOVE) {
				// nothing
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt
		 * .dnd.DragSourceEvent)
		 */
		public void dragSetData(DragSourceEvent event) {
			if (filterCopyTransfer.isSupportedType(event.dataType)) {
				event.data = getFilterCopySelection();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.dnd.DragSourceListener#dragStart(org.eclipse.swt.
		 * dnd.DragSourceEvent)
		 */
		public void dragStart(DragSourceEvent event) {
			if (getFilterCopySelection().length == 0)
				event.doit = false;
		}
	}

	private static int getMinimumColumnWidth(TreeColumn column, int hint) {
		Assert.isNotNull(column);
		FontMetrics fontMetrics;
		GC gc = new GC(column.getParent());
		try {
			gc.setFont(column.getParent().getFont());
			fontMetrics = gc.getFontMetrics();
		} finally {
			gc.dispose();
		}
		return Math.max(hint, fontMetrics.getAverageCharWidth()
				* column.getText().length());
	}

	private void createButtonGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		composite.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());

		addButton = new Button(composite, SWT.PUSH);
		addButton.setText(NLS.bind(
				IDEWorkbenchMessages.ResourceFilterPage_addButtonLabel, null));
		data = new GridData(SWT.FILL, SWT.FILL, false, false);
		addButton.setLayoutData(data);
		setButtonDimensionHint(addButton);
		addButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				handleAdd();
			}
		});

		editButton = new Button(composite, SWT.PUSH);
		editButton.setText(NLS.bind(
				IDEWorkbenchMessages.ResourceFilterPage_editButtonLabel, null));
		data = new GridData(SWT.FILL, SWT.FILL, false, false);
		editButton.setLayoutData(data);
		setButtonDimensionHint(editButton);
		editButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				handleEdit();
			}
		});

		removeButton = new Button(composite, SWT.PUSH);
		removeButton
				.setText(NLS
						.bind(
								IDEWorkbenchMessages.ResourceFilterPage_removeButtonLabel,
								null));
		data = new GridData(SWT.FILL, SWT.FILL, false, false);
		removeButton.setLayoutData(data);
		setButtonDimensionHint(removeButton);
		removeButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				handleRemove();
			}
		});

		if (allowReordering) {
			upButton = new Button(composite, SWT.PUSH);
			upButton
					.setText(NLS
							.bind(
									IDEWorkbenchMessages.ResourceFilterPage_upButtonLabel,
									null));
			data = new GridData(SWT.FILL, SWT.FILL, false, false);
			upButton.setLayoutData(data);
			setButtonDimensionHint(upButton);
			upButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				public void widgetSelected(SelectionEvent e) {
					handleUp();
				}
			});

			downButton = new Button(composite, SWT.PUSH);
			downButton.setText(NLS.bind(
					IDEWorkbenchMessages.ResourceFilterPage_downButtonLabel,
					null));
			data = new GridData(SWT.FILL, SWT.FILL, false, false);
			downButton.setLayoutData(data);
			setButtonDimensionHint(downButton);
			downButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				public void widgetSelected(SelectionEvent e) {
					handleDown();
				}
			});
		}
	}

	private void refreshEnablement() {
		addButton.setEnabled(true);
		ISelection selection = filterView.getSelection();
		IStructuredSelection structuredSelection = null;
		if (selection instanceof IStructuredSelection)
			structuredSelection = ((IStructuredSelection) selection);
		removeButton.setEnabled(structuredSelection != null
				&& structuredSelection.size() > 0);
		editButton.setEnabled(structuredSelection != null
				&& structuredSelection.size() == 1);
		if (upButton != null)
			upButton.setEnabled(structuredSelection != null
					&& (structuredSelection.size() > 0)
					&& !isFirst(structuredSelection.getFirstElement()));
		if (downButton != null)
			downButton.setEnabled(structuredSelection != null
					&& (structuredSelection.size() > 0)
					&& !isLast(structuredSelection.getFirstElement()));
	}

	private boolean isFirst(Object o) {
		return filters.isFirst((FilterCopy) o);
	}

	private boolean isLast(Object o) {
		return filters.isLast((FilterCopy) o);
	}

	private void handleAdd() {
		FilterCopy newFilter = new FilterCopy();
		FilterEditDialog dialog = new FilterEditDialog(shell, newFilter);
		if (dialog.open() == Window.OK) {
			filters.add(newFilter);
			filterView.refresh();
		}
	}

	private void handleEdit() {
		ISelection selection = filterView.getSelection();
		if (selection instanceof IStructuredSelection) {
			FilterCopy filter = (FilterCopy) ((IStructuredSelection) selection)
					.getFirstElement();
			FilterCopy copy = new FilterCopy(filter);
			copy.setParent(filter.getParent());
			FilterEditDialog dialog = new FilterEditDialog(shell, copy);
			if (dialog.open() == Window.OK) {
				if (copy.hasChanged()) {
					filter.copy(copy);
					filterView.refresh();
				}
			}
		}
	}

	private FilterCopy[] getFilterCopySelection() {
		ISelection selection = filterView.getSelection();
		IStructuredSelection structuredSelection = null;
		if (selection instanceof IStructuredSelection) {
			structuredSelection = ((IStructuredSelection) selection);
			FilterCopy[] tmp = new FilterCopy[structuredSelection.size()];
			System.arraycopy(structuredSelection.toArray(), 0, tmp, 0,
					tmp.length);
			return tmp;
		}
		return new FilterCopy[0];
	}

	private void handleRemove() {
		ISelection selection = filterView.getSelection();
		IStructuredSelection structuredSelection = null;
		if (selection instanceof IStructuredSelection) {
			structuredSelection = ((IStructuredSelection) selection);
			Iterator it = structuredSelection.iterator();
			while (it.hasNext()) {
				FilterCopy filter = (FilterCopy) it.next();
				filter.getParent().removeChild(filter);
			}
			filterView.refresh();
		}
	}

	private void handleUp() {
		ISelection selection = filterView.getSelection();
		if (selection instanceof IStructuredSelection) {
			FilterCopy filter = (FilterCopy) ((IStructuredSelection) selection)
					.getFirstElement();
			filters.moveUp(filter);
		}
		filterView.refresh();
		refreshEnablement();
	}

	private void handleDown() {
		ISelection selection = filterView.getSelection();
		if (selection instanceof IStructuredSelection) {
			FilterCopy filter = (FilterCopy) ((IStructuredSelection) selection)
					.getFirstElement();
			filters.moveDown(filter);
		}
		filterView.refresh();
		refreshEnablement();
	}

	private static void setButtonDimensionHint(Button button) {
		Assert.isNotNull(button);
		Object gd = button.getLayoutData();
		if (gd instanceof GridData) {
			((GridData) gd).widthHint = getButtonWidthHint(button);
			((GridData) gd).horizontalAlignment = GridData.FILL;
		}
	}

	private static int getButtonWidthHint(Button button) {
		button.setFont(JFaceResources.getDialogFont());
		FontMetrics fontMetrics;
		GC gc = new GC(button);
		try {
			gc.setFont(button.getFont());
			fontMetrics = gc.getFontMetrics();
		} finally {
			gc.dispose();
		}
		int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics,
				IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT,
				true).x);
	}

	/**
	 * Apply the default state of the resource.
	 */
	public void performDefaults() {
		if (resource == null)
			return;
		filters = new Filters(resource);
		filters.removeAll();
		filterView.setInput(filters);
		filterView.refresh();
	}

	/**
	 * @return the filters that were configured on this resource
	 */
	public UIResourceFilterDescription[] getFilters() {
		FilterCopy[] newFilters = filters.getChildren();
		UIResourceFilterDescription[] result = new UIResourceFilterDescription[newFilters.length];
		for (int i = 0; i < newFilters.length; i++) {
			result[i] = newFilters[i];
		}
		return result;
	}

	/**
	 * @param filters
	 */
	public void setFilters(IResourceFilterDescription[] filters) {
		initialFilters = new UIResourceFilterDescription[filters.length];
		for (int i = 0; i < filters.length; i++)
			initialFilters[i] = UIResourceFilterDescription.wrap(filters[i]);
	}

	/**
	 * @param filters
	 */
	public void setFilters(UIResourceFilterDescription[] filters) {
		initialFilters = filters;
	}

	/**
	 * Apply the read only state and the encoding to the resource.
	 * 
	 * @return true if the filters changed
	 */
	public boolean performOk() {

		if (filters.hasChanged()) {
			if (resource == null)
				return true;

			try {
				if (resource != nonExistantResource) {
					IResourceFilterDescription[] oldFilters = resource.getFilters();
					for (int i = 0; i < oldFilters.length; i++) {
						resource.removeFilter(oldFilters[i],
								IResource.BACKGROUND_REFRESH,
								new NullProgressMonitor());
					}
					FilterCopy[] newFilters = filters.getChildren();
					for (int i = 0; i < newFilters.length; i++) {
						resource.createFilter(newFilters[i].getType(), 
								newFilters[i].getFileInfoMatcherDescription(),
								IResource.BACKGROUND_REFRESH,
								new NullProgressMonitor());
					}
				}
			} catch (CoreException e) {
				ErrorDialog.openError(shell, NLS.bind(
						IDEWorkbenchMessages.InternalError, null), e
						.getLocalizedMessage(), e.getStatus());
			}
		}
		return true;
	}

	/**
	 * Disposes the group's resources.
	 */
	public void dispose() {
		if (checkIcon != null) {
			checkIcon.dispose();
			checkIcon = null;
		}
	}

	private FilterCopyTransfer filterCopyTransfer = new FilterCopyTransfer();

	class FilterCopyTransfer extends ByteArrayTransfer {

		private FilterCopyTransfer() {
		}

		public void javaToNative(Object object, TransferData transferData) {
			if (object == null || !(object instanceof FilterCopy[]))
				return;
			if (isSupportedType(transferData)) {
				FilterCopy[] myTypes = (FilterCopy[]) object;
				try {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					DataOutputStream writeOut = new DataOutputStream(out);
					writeOut.writeInt(myTypes.length);
					for (int i = 0; i < myTypes.length; i++)
						writeOut.writeInt(myTypes[i].getSerialNumber());
					byte[] buffer = out.toByteArray();
					writeOut.close();
					super.javaToNative(buffer, transferData);
				} catch (IOException e) {
				}
			}
		}

		public Object nativeToJava(TransferData transferData) {
			if (isSupportedType(transferData)) {
				byte[] buffer = (byte[]) super.nativeToJava(transferData);
				if (buffer == null)
					return null;
				FilterCopy[] myData = new FilterCopy[0];
				try {
					ByteArrayInputStream in = new ByteArrayInputStream(buffer);
					DataInputStream readIn = new DataInputStream(in);
					int size = readIn.readInt();

					LinkedList droppedFilters = new LinkedList();
					for (int i = 0; i < size; i++) {
						int serialNumber = readIn.readInt();
						FilterCopy tmp = filters
								.findBySerialNumber(serialNumber);
						if (tmp != null)
							droppedFilters.add(tmp);
					}
					myData = (FilterCopy[]) droppedFilters
							.toArray(new FilterCopy[0]);
					readIn.close();
				} catch (IOException ex) {
					return null;
				}
				return myData;
			}

			return null;
		}

		private final String MYTYPENAME = "org.eclipse.ui.ide.internal.filterCopy"; //$NON-NLS-1$
		private final int MYTYPEID = registerType(MYTYPENAME);

		protected String[] getTypeNames() {
			return new String[] { MYTYPENAME };
		}

		protected int[] getTypeIds() {
			return new int[] { MYTYPEID };
		}
	}
}

class FilterTypeUtil {
	static String ID = "id"; //$NON-NLS-1$
	static String TARGET = "target"; //$NON-NLS-1$
	static String MODE = "mode"; //$NON-NLS-1$
	static String ARGUMENTS = "arguments"; //$NON-NLS-1$
	static String INHERITABLE = "inheritable"; //$NON-NLS-1$

	static String[] columnNames = new String[] { MODE, ARGUMENTS, INHERITABLE };

	static String[] getModes() {
		return new String[] {
				NLS.bind(IDEWorkbenchMessages.ResourceFilterPage_includeOnly,
						null),
				NLS.bind(IDEWorkbenchMessages.ResourceFilterPage_excludeAll,
						null) };
	}

	public static void setValue(FilterCopy filter, String property, Object value) {
		if (property.equals(FilterTypeUtil.ID)) {
			IFilterMatcherDescriptor descriptor;
			if (value instanceof Integer) {
				int selection = ((Integer) value).intValue();
				descriptor = FilterTypeUtil.getDescriptorFromIndex(selection);
			} else
				descriptor = FilterTypeUtil.getDescriptorByName((String) value);
			if (descriptor != null)
				filter.setId(descriptor.getId());
		}
		if (property.equals(FilterTypeUtil.MODE)) {
			int selection = ((Integer) value).intValue();
			int type = filter.getType()
					& ~(IResourceFilterDescription.INCLUDE_ONLY | IResourceFilterDescription.EXCLUDE_ALL);
			if (selection == 0)
				filter.setType(type | IResourceFilterDescription.INCLUDE_ONLY);
			else
				filter.setType(type | IResourceFilterDescription.EXCLUDE_ALL);
		}
		if (property.equals(FilterTypeUtil.TARGET)) {
			int selection = ((Integer) value).intValue();
			int type = filter.getType()
					& ~(IResourceFilterDescription.FILES | IResourceFilterDescription.FOLDERS);
			if (selection == 0)
				filter.setType(type | IResourceFilterDescription.FILES);
			if (selection == 1)
				filter.setType(type | IResourceFilterDescription.FOLDERS);
			if (selection == 2)
				filter.setType(type | IResourceFilterDescription.FILES
						| IResourceFilterDescription.FOLDERS);
		}
		if (property.equals(FilterTypeUtil.INHERITABLE)) {
			int type = filter.getType() & ~IResourceFilterDescription.INHERITABLE;
			if (((Boolean) value).booleanValue())
				filter.setType(type | IResourceFilterDescription.INHERITABLE);
			else
				filter.setType(type);
		}
		if (property.equals(FilterTypeUtil.ARGUMENTS)) {
			filter.setArguments(value.equals("") ? null : value); //$NON-NLS-1$
		}
	}

	static IFilterMatcherDescriptor getDescriptor(String id) {
		IFilterMatcherDescriptor[] descriptors = ResourcesPlugin.getWorkspace()
				.getFilterMatcherDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			if (descriptors[i].getId().equals(id))
				return descriptors[i];
		}
		return null;
	}

	static int getDescriptorIndex(String id) {
		IFilterMatcherDescriptor descriptors[] = ResourcesPlugin.getWorkspace()
				.getFilterMatcherDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			if (descriptors[i].getId().equals(id))
				return i;
		}
		return -1;
	}

	static Object getValue(UIResourceFilterDescription filter, String property) {
		if (property.equals(ID)) {
			String id = filter.getFileInfoMatcherDescription().getId();
			int index = getDescriptorIndex(id);
			return new Integer(index);
		}
		if (property.equals(MODE)) {
			if ((filter.getType() & IResourceFilterDescription.INCLUDE_ONLY) != 0)
				return new Integer(0);
			return new Integer(1);
		}
		if (property.equals(TARGET)) {
			boolean includeFiles = (filter.getType() & IResourceFilterDescription.FILES) != 0;
			boolean includeFolders = (filter.getType() & IResourceFilterDescription.FOLDERS) != 0;
			if (includeFiles && includeFolders)
				return new Integer(2);
			if (includeFiles)
				return new Integer(0);
			if (includeFolders)
				return new Integer(1);
		}
		if (property.equals(INHERITABLE))
			return new Boolean(
					(filter.getType() & IResourceFilterDescription.INHERITABLE) != 0);

		if (property.equals(ARGUMENTS))
			return filter.getFileInfoMatcherDescription().getArguments() != null ? filter.getFileInfoMatcherDescription().getArguments() : ""; //$NON-NLS-1$
		return null;
	}

	static String[] getTargets() {
		return new String[] {
				NLS.bind(IDEWorkbenchMessages.ResourceFilterPage_files, null),
				NLS.bind(IDEWorkbenchMessages.ResourceFilterPage_folders, null),
				NLS
						.bind(
								IDEWorkbenchMessages.ResourceFilterPage_filesAndFolders,
								null) };
	}

	static String[] getFilterNames(boolean childrenOnly) {
		IFilterMatcherDescriptor[] descriptors = ResourcesPlugin.getWorkspace()
				.getFilterMatcherDescriptors();
		LinkedList names = new LinkedList();
		for (int i = 0; i < descriptors.length; i++) {
			if (!childrenOnly
					|| descriptors[i].getArgumentType().equals(
							IFilterMatcherDescriptor.ARGUMENT_TYPE_FILTER_MATCHER)
					|| descriptors[i].getArgumentType().equals(
							IFilterMatcherDescriptor.ARGUMENT_TYPE_FILTER_MATCHERS))
				names.add(descriptors[i].getName());
		}
		return (String[]) names.toArray(new String[0]);
	}

	static String getDefaultFilterID() {
		IFilterMatcherDescriptor descriptors[] = ResourcesPlugin.getWorkspace()
				.getFilterMatcherDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			if (descriptors[i].getArgumentType().equals(
					IFilterMatcherDescriptor.ARGUMENT_TYPE_STRING))
				return descriptors[i].getId();
		}
		return descriptors[0].getId();
	}

	static IFilterMatcherDescriptor getDescriptorFromIndex(int index) {
		IFilterMatcherDescriptor descriptors[] = ResourcesPlugin.getWorkspace()
				.getFilterMatcherDescriptors();
		return descriptors[index];
	}

	static IFilterMatcherDescriptor getDescriptorByName(String name) {
		IFilterMatcherDescriptor[] descriptors = ResourcesPlugin.getWorkspace()
				.getFilterMatcherDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			if (descriptors[i].getName().equals(name))
				return descriptors[i];
		}
		return null;
	}

	private Image fileIcon = null;
	private Image folderIcon = null;
	private Image fileFolderIcon = null;
	private Image includeIcon = null;
	private Image excludeIcon = null;
	private Image inheritableIcon = null;

	FilterTypeUtil() {
		ImageDescriptor descriptor = AbstractUIPlugin
				.imageDescriptorFromPlugin(IDEWorkbenchPlugin.IDE_WORKBENCH,
						"$nl$/icons/full/obj16/fileType_filter.gif"); //$NON-NLS-1$
		if (descriptor != null)
			fileIcon = descriptor.createImage();

		descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
				IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/obj16/folderType_filter.gif"); //$NON-NLS-1$
		if (descriptor != null)
			folderIcon = descriptor.createImage();

		descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
				IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/obj16/fileFolderType_filter.gif"); //$NON-NLS-1$
		if (descriptor != null)
			fileFolderIcon = descriptor.createImage();

		descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
				IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/obj16/includeMode_filter.gif"); //$NON-NLS-1$
		if (descriptor != null)
			includeIcon = descriptor.createImage();

		descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
				IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/obj16/excludeMode_filter.gif"); //$NON-NLS-1$
		if (descriptor != null)
			excludeIcon = descriptor.createImage();
		
		descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
				IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/obj16/inheritable_filter.gif"); //$NON-NLS-1$
		if (descriptor != null)
			inheritableIcon = descriptor.createImage();
	}

	Image getImage(String string, int i) {
		if (string.equals(MODE))
			return new Image[] { includeIcon, excludeIcon, inheritableIcon }[i];
		if (string.equals(TARGET))
			return new Image[] { fileIcon, folderIcon, fileFolderIcon }[i];
		return null;
	}
}

class FilterCopy extends UIResourceFilterDescription {
	Object arguments = null;
	String id = null;
	IPath path = null;
	IProject project = null;
	int type = 0;
	FilterCopy parent = null;
	LinkedList children = null;
	UIResourceFilterDescription original = null;
	int serialNumber = ++lastSerialNumber;
	static private int lastSerialNumber = 0;

	public FilterCopy(UIResourceFilterDescription filter) {
		internalCopy(filter);
		original = filter;
	}

	public void removeAll() {
		initializeChildren();
		Iterator it = children.iterator();
		while (it.hasNext()) {
			FilterCopy child = (FilterCopy) it.next();
			if (child.parent == this)
				child.parent = null;
		}
		children.clear();
		serializeChildren();
	}

	public void setParent(FilterCopy parent) {
		this.parent = parent;
	}

	public boolean canAcceptDrop() {
		int limit = getChildrenLimit();
		if (limit > 0) {
			FilterCopy[] tmp = getChildren();
			return (tmp == null) || (tmp.length < limit);
		}
		return false;
	}

	public boolean hasParent(FilterCopy filterCopy) {
		FilterCopy filter = this;
		do {
			if (filter.equals(filterCopy))
				return true;
			filter = filter.getParent();
		} while (filter != null);
		return false;
	}

	public FilterCopy getParent() {
		return parent;
	}

	public void copy(UIResourceFilterDescription filter) {
		internalCopy(filter);
		argumentsChanged();
	}

	private void internalCopy(UIResourceFilterDescription filter) {
		children = null;
		id = filter.getFileInfoMatcherDescription().getId();
		path = filter.getPath();
		project = filter.getProject();
		type = filter.getType();
		arguments = filter.getFileInfoMatcherDescription().getArguments();
		if (arguments instanceof FileInfoMatcherDescription[]) {
			FileInfoMatcherDescription[] descs = (FileInfoMatcherDescription[]) arguments;
			FilterCopy [] tmp = new FilterCopy[descs.length];
			for (int i = 0; i < tmp.length; i++)
				tmp[i] = new FilterCopy(this, descs[i]);
			arguments = tmp;
		}
	}

	public boolean hasChanged() {
		if (original != null) {
			return !((arguments == null ? (original.getFileInfoMatcherDescription().getArguments() == null)
					: arguments.equals(original.getFileInfoMatcherDescription().getArguments()))
					&& id.equals(original.getFileInfoMatcherDescription().getId()) && type == original
					.getType());
		}
		return true;
	}

	public FilterCopy() {
		path = null;
		project = null;
		type = IResourceFilterDescription.FILES | IResourceFilterDescription.INCLUDE_ONLY;
		id = FilterTypeUtil.getDefaultFilterID();
	}

	/**
	 * @param parent 
	 * @param description
	 */
	public FilterCopy(FilterCopy parent, FileInfoMatcherDescription description) {
		children = null;
		id = description.getId();
		path = parent.getPath();
		project = parent.getProject();
		type = parent.getType();
		arguments = description.getArguments();
		if (arguments instanceof FileInfoMatcherDescription[]) {
			FileInfoMatcherDescription[] descs = (FileInfoMatcherDescription[]) arguments;
			FilterCopy [] tmp = new FilterCopy[descs.length];
			for (int i = 0; i < tmp.length; i++)
				tmp[i] = new FilterCopy(parent, descs[i]);
			arguments = tmp;
		}
	}

	public Object getArguments() {
		return arguments;
	}

	public String getId() {
		return id;
	}

	public IPath getPath() {
		return path;
	}

	public IProject getProject() {
		return project;
	}

	public int getType() {
		return type;
	}

	public void setArguments(Object arguments) {
		this.arguments = arguments;
		argumentsChanged();
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setPath(IPath path) {
		this.path = path;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean hasStringArguments() {
		IFilterMatcherDescriptor descriptor = FilterTypeUtil.getDescriptor(id);
		if (descriptor != null)
			return descriptor.getArgumentType().equals(
					IFilterMatcherDescriptor.ARGUMENT_TYPE_STRING);
		return false;
	}

	public int getChildrenLimit() {
		IFilterMatcherDescriptor descriptor = FilterTypeUtil.getDescriptor(id);
		if (descriptor != null) {
			if (descriptor.getArgumentType().equals(
					IFilterMatcherDescriptor.ARGUMENT_TYPE_FILTER_MATCHER))
				return 1;
			if (descriptor.getArgumentType().equals(
					IFilterMatcherDescriptor.ARGUMENT_TYPE_FILTER_MATCHERS))
				return Integer.MAX_VALUE;
		}
		return 0;
	}

	public boolean equals(Object o) {
		if (!(o instanceof FilterCopy))
			return false;
		FilterCopy filter = (FilterCopy) o;
		return serialNumber == filter.serialNumber;
	}

	public int getSerialNumber() {
		return serialNumber;
	}

	public FilterCopy findBySerialNumber(int number) {
		LinkedList pending = new LinkedList();
		pending.add(this);
		while (!pending.isEmpty()) {
			FilterCopy filter = (FilterCopy) pending.getFirst();
			pending.removeFirst();
			if (filter.serialNumber == number)
				return filter;
			FilterCopy[] tmp = filter.getChildren();
			if (tmp != null)
				pending.addAll(Arrays.asList(tmp));
		}
		return null;
	}

	public FilterCopy[] getChildren() {
		if (getChildrenLimit() > 0) {
			initializeChildren();
			return (FilterCopy[]) children.toArray(new FilterCopy[0]);
		}
		return null;
	}

	protected void initializeChildren() {
		if (children == null) {
			if (getChildrenLimit() > 0) {
				children = new LinkedList();
				Object arguments = getArguments();
				if (arguments instanceof IResourceFilterDescription[]) {
					IResourceFilterDescription[] filters = (IResourceFilterDescription[]) arguments;
					if (filters != null)
						for (int i = 0; i < filters.length; i++) {
							FilterCopy child = new FilterCopy(UIResourceFilterDescription.wrap(filters[i]));
							child.parent = this;
							children.add(child);
						}
				}
			}
		}
	}

	public void addChild(FilterCopy child) {
		initializeChildren();
		if (child.getParent() != null)
			child.getParent().removeChild(child);
		children.add(child);
		child.parent = this;
		serializeChildren();
	}

	public void removeChild(FilterCopy child) {
		initializeChildren();
		children.remove(child);
		if (child.parent == this)
			child.parent = null;
		serializeChildren();
	}

	protected void serializeChildren() {
		initializeChildren();
		argumentsChanged();
	}

	protected void argumentsChanged() {
		initializeChildren();
		if (children != null)
			arguments = children.toArray(new FilterCopy[0]);
		FilterCopy up = parent;
		while (up != null) {
			up.serializeChildren();
			up = up.parent;
		}
	}

	public boolean isUnderAGroupFilter() {
		// a partial filter is a filter that is located under a group, but not
		// the root group
		if (parent != null) {
			if ((parent.getChildrenLimit() > 0) && (parent.getParent() != null))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceFilterDescription#getFileInfoMatcherDescription()
	 */
	public FileInfoMatcherDescription getFileInfoMatcherDescription() {
		
		
		Object arg = FilterCopy.this.getArguments();
		if (arg instanceof FilterCopy []) {
			FilterCopy [] filterCopies = (FilterCopy []) arg;
			FileInfoMatcherDescription[] descriptions = new FileInfoMatcherDescription[filterCopies.length];
			for (int i = 0; i < descriptions.length; i++)
				descriptions[i] = filterCopies[i].getFileInfoMatcherDescription();
			arg = descriptions;
		}
		
		FileInfoMatcherDescription desc = new FileInfoMatcherDescription(getId(), arg);
		return desc;
	}
}

class FilterEditDialog extends TrayDialog {

	private FilterCopy filter;

	protected Button includeButton;
	protected Button excludeButton;
	protected Button filesButton;
	protected Button foldersButton;
	protected Button filesAndFoldersButton;
	protected Combo idCombo;
	protected Button inherited;
	protected Text arguments;
	protected Label argumentsLabel;
	protected Label description;
	protected FilterTypeUtil util;

	private static final String REGEX_FILTER_ID = "org.eclipse.core.resources.regexFilter"; //$NON-NLS-1$
	
	/**
	 * Find and replace command adapters.
	 * @since 3.3
	 */
	private ContentAssistCommandAdapter fContentAssistField;

	/**
	 * Constructor for FilterEditDialog.
	 * 
	 * @param parentShell
	 * @param filter
	 */
	public FilterEditDialog(Shell parentShell, FilterCopy filter) {
		super(parentShell);
		this.filter = filter;
		util = new FilterTypeUtil();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		parent.setLayoutData(data);

		Font font = parent.getFont();
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(data);
		composite.setFont(font);

		Dialog.applyDialogFont(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(),
				IIDEHelpContextIds.EDIT_RESOURCE_FILTER_PROPERTY_PAGE);

		if (!filter.isUnderAGroupFilter()) {
			Composite topComposite = new Composite(composite, SWT.NONE);
			layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
			layout.marginWidth = 0;
			layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
			layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
			topComposite.setLayout(layout);
			data = new GridData(SWT.FILL, SWT.FILL, true, false);
			topComposite.setLayoutData(data);
			topComposite.setFont(font);

			createModeArea(font, topComposite);
			createTargetArea(font, topComposite);
		}
		createIdArea(font, composite);

		return composite;
	}

	/**
	 * @param font
	 * @param composite
	 */
	private void createInheritableArea(Font font, Composite composite) {
		GridData data;
		inherited = new Button(composite, SWT.CHECK);
		inherited
				.setText(NLS
						.bind(
								IDEWorkbenchMessages.ResourceFilterPage_columnFilterInheritable,
								null));
		inherited.setImage(util.getImage(FilterTypeUtil.MODE, 2));
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 1;
		inherited.setLayoutData(data);
		inherited.setFont(font);
		inherited.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				FilterTypeUtil.setValue(filter, FilterTypeUtil.INHERITABLE,
						new Boolean(inherited.getSelection()));
			}
		});
		inherited.setSelection((((Boolean) FilterTypeUtil.getValue(filter,
				FilterTypeUtil.INHERITABLE)).booleanValue()));
	}

	/**
	 * @param font
	 * @param composite
	 */
	private void createArgumentsArea(Font font, Composite composite) {
		GridData data;
		argumentsLabel = addLabel(composite, NLS.bind(
				IDEWorkbenchMessages.ResourceFilterPage_columnFilterArguments,
				null));
		arguments = new Text(composite, SWT.SINGLE | SWT.BORDER);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		arguments.setLayoutData(data);
		arguments.setFont(font);
		arguments.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				FilterTypeUtil.setValue(filter, FilterTypeUtil.ARGUMENTS,
						arguments.getText());
			}
		});
		if (filter.hasStringArguments())
			arguments.setText((String) FilterTypeUtil.getValue(filter,
					FilterTypeUtil.ARGUMENTS));
		arguments.setEnabled(filter.hasStringArguments());
		setArgumentLabelEnabled();

		TextContentAdapter contentAdapter= new TextContentAdapter();
		FindReplaceDocumentAdapterContentProposalProvider findProposer= new FindReplaceDocumentAdapterContentProposalProvider(true);
		fContentAssistField= new ContentAssistCommandAdapter(
				arguments,
				contentAdapter,
				findProposer, 
				null,
				new char[] {'\\', '[', '('},
				true);
	}

	/**
	 * 
	 */
	private void setArgumentLabelEnabled() {
		Color color = argumentsLabel.getDisplay().getSystemColor(
				filter.hasStringArguments() ? SWT.COLOR_BLACK : SWT.COLOR_GRAY);
		argumentsLabel.setForeground(color);
	}

	/**
	 * @param font
	 * @param composite
	 */
	private void createDescriptionArea(Font font, Composite composite) {
		GridData data;
		description = new Label(composite, SWT.LEFT | SWT.WRAP);
		description.setText(FilterTypeUtil.getDescriptor(filter.getId())
				.getDescription());
		data = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
		data.widthHint = 300;
		data.heightHint = 40;
		description.setLayoutData(data);
		description.setFont(font);
	}

	/**
	 * @param font
	 * @param composite
	 */
	private void createIdArea(Font font, Composite composite) {
		GridData data;
		Group idComposite = createGroup(font, composite, NLS.bind(
				IDEWorkbenchMessages.ResourceFilterPage_columnFilterID, null),
				true);
		idCombo = new Combo(idComposite, SWT.READ_ONLY);
		idCombo.setItems(FilterTypeUtil.getFilterNames(filter
				.getChildrenLimit() > 0));
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		idCombo.setLayoutData(data);
		idCombo.setFont(font);
		idCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				FilterTypeUtil.setValue(filter, FilterTypeUtil.ID, idCombo
						.getItem(idCombo.getSelectionIndex()));
				arguments.setEnabled(filter.hasStringArguments());
				setArgumentLabelEnabled();
				description.setText(FilterTypeUtil
						.getDescriptor(filter.getId()).getDescription());
				fContentAssistField.setEnabled(filter.getId().equals(REGEX_FILTER_ID));
			}
		});
		selectComboItem(filter.getId());
		Composite argumentComposite = new Composite(idComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		argumentComposite.setLayout(layout);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		argumentComposite.setLayoutData(data);
		argumentComposite.setFont(font);
		createArgumentsArea(font, argumentComposite);

		createDescriptionArea(font, idComposite);
		fContentAssistField.setEnabled(filter.getId().equals(REGEX_FILTER_ID));
	}

	/**
	 * 
	 */
	private void selectComboItem(String filterID) {
		IFilterMatcherDescriptor descriptor = ResourcesPlugin.getWorkspace()
		.getFilterMatcherDescriptor(filterID);
		if (descriptor != null) {
			String [] items = idCombo.getItems();
			for (int i = 0; i < items.length; i++) {
				if (items[i].equals(descriptor.getName())) {
					idCombo.select(i);
					break;
				}
			}
		}
	}

	/**
	 * @param font
	 * @param composite
	 */
	private void createModeArea(Font font, Composite composite) {
		GridData data;
		Group modeComposite = createGroup(font, composite, NLS.bind(
				IDEWorkbenchMessages.ResourceFilterPage_columnFilterMode,
				null), false);
		String[] modes = FilterTypeUtil.getModes();
		includeButton = new Button(modeComposite, SWT.RADIO);
		includeButton.setText(modes[0]);
		includeButton.setImage(util.getImage(FilterTypeUtil.MODE, 0));
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		includeButton.setLayoutData(data);
		includeButton.setFont(font);
		includeButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				FilterTypeUtil.setValue(filter, FilterTypeUtil.MODE,
						new Integer(0));
			}
		});
		includeButton.setSelection(((Integer) FilterTypeUtil.getValue(
				filter, FilterTypeUtil.MODE)).intValue() == 0);
		excludeButton = new Button(modeComposite, SWT.RADIO);
		excludeButton.setText(modes[1]);
		excludeButton.setImage(util.getImage(FilterTypeUtil.MODE, 1));
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		excludeButton.setLayoutData(data);
		excludeButton.setFont(font);
		excludeButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				FilterTypeUtil.setValue(filter, FilterTypeUtil.MODE,
						new Integer(1));
			}
		});
		excludeButton.setSelection(((Integer) FilterTypeUtil.getValue(
				filter, FilterTypeUtil.MODE)).intValue() == 1);
		createInheritableArea(font, modeComposite);
	}

	/**
	 * @param font
	 * @param composite
	 * @return the group
	 */
	private Group createGroup(Font font, Composite composite, String text,
			boolean grabExcessVerticalSpace) {
		GridLayout layout;
		GridData data;
		Group modeComposite = new Group(composite, SWT.NONE);
		modeComposite.setText(text);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		modeComposite.setLayout(layout);
		data = new GridData(SWT.FILL, SWT.FILL, true, grabExcessVerticalSpace);
		modeComposite.setLayoutData(data);
		modeComposite.setFont(font);
		return modeComposite;
	}

	/**
	 * @param font
	 * @param composite
	 */
	private void createTargetArea(Font font, Composite composite) {
		GridData data;
		Group targetComposite = createGroup(font, composite, NLS.bind(
				IDEWorkbenchMessages.ResourceFilterPage_columnFilterTargets,
				null), false);

		String[] targets = FilterTypeUtil.getTargets();
		filesButton = new Button(targetComposite, SWT.RADIO);
		filesButton.setText(targets[0]);
		filesButton.setImage(util.getImage(FilterTypeUtil.TARGET, 0));
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		filesButton.setLayoutData(data);
		filesButton.setFont(font);

		foldersButton = new Button(targetComposite, SWT.RADIO);
		foldersButton.setText(targets[1]);
		foldersButton.setImage(util.getImage(FilterTypeUtil.TARGET, 1));
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		foldersButton.setLayoutData(data);
		foldersButton.setFont(font);

		filesAndFoldersButton = new Button(targetComposite, SWT.RADIO);
		filesAndFoldersButton.setText(targets[2]);
		filesAndFoldersButton.setImage(util.getImage(FilterTypeUtil.TARGET, 2));
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		filesAndFoldersButton.setLayoutData(data);
		filesAndFoldersButton.setFont(font);

		filesButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				FilterTypeUtil.setValue(filter, FilterTypeUtil.TARGET,
						new Integer(0));
			}
		});
		foldersButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				FilterTypeUtil.setValue(filter, FilterTypeUtil.TARGET,
						new Integer(1));
			}
		});
		filesAndFoldersButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				FilterTypeUtil.setValue(filter, FilterTypeUtil.TARGET,
						new Integer(2));
			}
		});
		filesButton.setSelection(((Integer) FilterTypeUtil.getValue(filter,
				FilterTypeUtil.TARGET)).intValue() == 0);
		foldersButton.setSelection(((Integer) FilterTypeUtil.getValue(filter,
				FilterTypeUtil.TARGET)).intValue() == 1);
		filesAndFoldersButton.setSelection(((Integer) FilterTypeUtil.getValue(
				filter, FilterTypeUtil.TARGET)).intValue() == 2);
	}

	Label addLabel(Composite composite, String text) {
		String delimiter = ":"; //$NON-NLS-1$

		Font font = composite.getFont();
		Label label = new Label(composite, SWT.LEFT);
		label.setText(text + delimiter);
		GridData data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		label.setLayoutData(data);
		label.setFont(font);
		return label;
	}

	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		initialize();
		update();
		return control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	protected boolean isResizable() {
		return true;
	}

	protected void configureShell(Shell newShell) {
		newShell.setText(NLS.bind(
				IDEWorkbenchMessages.ResourceFilterPage_editFilterDialogTitle,
				null));
		super.configureShell(newShell);
	}

	private void initialize() {
	}

	protected void update() {
	}

	protected void okPressed() {
		super.okPressed();
	}
}