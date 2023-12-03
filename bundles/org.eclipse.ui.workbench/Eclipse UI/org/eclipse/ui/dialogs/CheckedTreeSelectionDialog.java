/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 * 		Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 * 			activated and used by other components.
 *      Lubomir Marinov <lubomir.marinov@gmail.com> - Fix for bug 182122 -[Dialogs]
 *          CheckedTreeSelectionDialog#createSelectionButtons(Composite) fails to
 *          align the selection buttons to the right
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * A class to select elements out of a tree structure.
 *
 * @since 2.0
 */
public class CheckedTreeSelectionDialog extends SelectionStatusDialog {
	private CheckboxTreeViewer fViewer;

	private ILabelProvider fLabelProvider;

	private ITreeContentProvider fContentProvider;

	private ISelectionStatusValidator fValidator = null;

	private ViewerComparator fComparator;

	private String fEmptyListMessage = WorkbenchMessages.CheckedTreeSelectionDialog_nothing_available;

	private IStatus fCurrStatus = new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$

	private List<ViewerFilter> fFilters;

	private Object fInput;

	private boolean fIsEmpty;

	private int fWidth = 60;

	private int fHeight = 18;

	private boolean fContainerMode;

	private Object[] fExpandedElements;

	private int fStyle = SWT.BORDER;

	private final boolean fAllowEmptyTree;

	/**
	 * Constructs an instance of <code>ElementTreeSelectionDialog</code>.
	 *
	 * @param parent          The shell to parent from.
	 * @param labelProvider   the label provider to render the entries
	 * @param contentProvider the content provider to evaluate the tree structure
	 */
	public CheckedTreeSelectionDialog(Shell parent, ILabelProvider labelProvider,
			ITreeContentProvider contentProvider) {
		this(parent, labelProvider, contentProvider, SWT.BORDER);
	}

	/**
	 * Constructs an instance of <code>ElementTreeSelectionDialog</code>.
	 *
	 * @param parent             The shell to parent from.
	 * @param labelProvider      the label provider to render the entries
	 * @param contentProvider    the content provider to evaluate the tree structure
	 * @param allowEmptyTree <code>true</code> if an empty tree can be input, <code>false</code> if an empty tree must be treated as an error
	 * @since 3.131
	 */
	public CheckedTreeSelectionDialog(Shell parent, ILabelProvider labelProvider, ITreeContentProvider contentProvider,
			boolean allowEmptyTree) {
		this(parent, labelProvider, contentProvider, SWT.BORDER, allowEmptyTree);
	}

	/**
	 * Constructs an instance of <code>ElementTreeSelectionDialog</code>.
	 *
	 * @param parent          The shell to parent from.
	 * @param labelProvider   the label provider to render the entries
	 * @param contentProvider the content provider to evaluate the tree structure
	 * @param style           the style of the tree
	 * @since 3.105
	 */
	public CheckedTreeSelectionDialog(Shell parent, ILabelProvider labelProvider, ITreeContentProvider contentProvider,
			int style) {
		this(parent, labelProvider, contentProvider, style, false);
	}

	/**
	 * Constructs an instance of <code>ElementTreeSelectionDialog</code>.
	 *
	 * @param parent          The shell to parent from.
	 * @param labelProvider   the label provider to render the entries
	 * @param contentProvider the content provider to evaluate the tree structure
	 * @param style           the style of the tree
	 * @param allowEmptyTree  <code>true</code> if an empty tree can be input,
	 *                        <code>false</code> if an empty tree must be treated as
	 *                        an error
	 * @since 3.131
	 */
	public CheckedTreeSelectionDialog(Shell parent, ILabelProvider labelProvider, ITreeContentProvider contentProvider,
			int style, boolean allowEmptyTree) {
		super(parent);
		fLabelProvider = labelProvider;
		fContentProvider = contentProvider;
		setResult(new ArrayList<>(0));
		setStatusLineAboveButtons(true);
		fContainerMode = false;
		fExpandedElements = null;
		fStyle = style;
		fAllowEmptyTree = allowEmptyTree;
	}

	/**
	 * If set, the checked /gray state of containers (inner nodes) is derived from
	 * the checked state of its leaf nodes.
	 *
	 * @param containerMode The containerMode to set
	 */
	public void setContainerMode(boolean containerMode) {
		fContainerMode = containerMode;
	}

	/**
	 * Sets the initial selection. Convenience method.
	 *
	 * @param selection the initial selection.
	 */
	public void setInitialSelection(Object selection) {
		setInitialSelections(selection);
	}

	/**
	 * Sets the message to be displayed if the list is empty.
	 *
	 * @param message the message to be displayed.
	 */
	public void setEmptyListMessage(String message) {
		fEmptyListMessage = message;
	}

	/**
	 * Sets the sorter used by the tree viewer.
	 *
	 * @param sorter the sorter
	 * @deprecated since 3.3, use
	 *             {@link CheckedTreeSelectionDialog#setComparator(ViewerComparator)}
	 *             instead
	 */
	@Deprecated
	public void setSorter(ViewerSorter sorter) {
		fComparator = sorter;
	}

	/**
	 * Set the style used for the creation of the Tree. Changing this will only have
	 * an effect up to the time the Tree is created.
	 *
	 * @param style the style of the tree
	 * @since 3.105
	 */
	public void setStyle(int style) {
		fStyle = style;
	}

	/**
	 * Sets the comparator used by the tree viewer.
	 *
	 * @param comparator the {@link ViewerComparator}
	 * @since 3.3
	 */
	public void setComparator(ViewerComparator comparator) {
		fComparator = comparator;
	}

	/**
	 * Adds a filter to the tree viewer.
	 *
	 * @param filter a filter.
	 */
	public void addFilter(ViewerFilter filter) {
		if (fFilters == null) {
			fFilters = new ArrayList<>(4);
		}
		fFilters.add(filter);
	}

	/**
	 * Sets an optional validator to check if the selection is valid. The validator
	 * is invoked whenever the selection changes.
	 *
	 * @param validator the validator to validate the selection.
	 */
	public void setValidator(ISelectionStatusValidator validator) {
		fValidator = validator;
	}

	/**
	 * Sets the tree input.
	 *
	 * @param input the tree input.
	 */
	public void setInput(Object input) {
		fInput = input;
	}

	/**
	 * Expands elements in the tree.
	 *
	 * @param elements The elements that will be expanded.
	 */
	public void setExpandedElements(Object[] elements) {
		fExpandedElements = elements;
	}

	/**
	 * Sets the size of the tree in unit of characters.
	 *
	 * @param width  the width of the tree.
	 * @param height the height of the tree.
	 */
	public void setSize(int width, int height) {
		fWidth = width;
		fHeight = height;
	}

	/**
	 * Validate the receiver and update the status with the result.
	 */
	protected void updateOKStatus() {
		if (!fIsEmpty) {
			if (fValidator != null) {
				fCurrStatus = fValidator.validate(fViewer.getCheckedElements());
				updateStatus(fCurrStatus);
			} else if (!fCurrStatus.isOK()) {
				fCurrStatus = new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.OK, "", //$NON-NLS-1$
						null);
			}
		} else if (fAllowEmptyTree) {
			if (!fCurrStatus.isOK()) {
				fCurrStatus = new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.OK, "", //$NON-NLS-1$
						null);
			}
		} else {
			fCurrStatus = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, fEmptyListMessage, null);
		}
		updateStatus(fCurrStatus);
	}

	@Override
	public int open() {
		fIsEmpty = evaluateIfTreeEmpty(fInput);
		super.open();
		return getReturnCode();
	}

	private void access$superCreate() {
		super.create();
	}

	/**
	 * Handles cancel button pressed event.
	 */
	@Override
	protected void cancelPressed() {
		setResult(null);
		super.cancelPressed();
	}

	/*
	 * @see SelectionStatusDialog#computeResult()
	 */
	@Override
	protected void computeResult() {
		setResult(Arrays.asList(fViewer.getCheckedElements()));
	}

	@Override
	public void create() {
		BusyIndicator.showWhile(null, () -> {
			access$superCreate();
			fViewer.setCheckedElements(getInitialElementSelections().toArray());
			if (fExpandedElements != null) {
				fViewer.setExpandedElements(fExpandedElements);
			}
			updateOKStatus();
		});
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		Label messageLabel = createMessageArea(composite);
		CheckboxTreeViewer treeViewer = createTreeViewer(composite);
		Control buttonComposite = createSelectionButtons(composite);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = convertWidthInCharsToPixels(fWidth);
		data.heightHint = convertHeightInCharsToPixels(fHeight);
		Tree treeWidget = treeViewer.getTree();
		treeWidget.setLayoutData(data);
		treeWidget.setFont(parent.getFont());
		if (fIsEmpty) {
			messageLabel.setEnabled(false);
			treeWidget.setEnabled(false);
			buttonComposite.setEnabled(false);
		}
		return composite;
	}

	/**
	 * Creates the tree viewer.
	 *
	 * @param parent the parent composite
	 * @return the tree viewer
	 */
	protected CheckboxTreeViewer createTreeViewer(Composite parent) {
		if (fContainerMode) {
			fViewer = new ContainerCheckedTreeViewer(parent, fStyle);
		} else {
			fViewer = new CheckboxTreeViewer(parent, fStyle);
		}
		fViewer.setContentProvider(fContentProvider);
		fViewer.setLabelProvider(fLabelProvider);
		fViewer.addCheckStateListener(event -> updateOKStatus());
		fViewer.setComparator(fComparator);
		if (fFilters != null) {
			for (int i = 0; i != fFilters.size(); i++) {
				fViewer.addFilter(fFilters.get(i));
			}
		}
		fViewer.setInput(fInput);
		return fViewer;
	}

	/**
	 * Returns the tree viewer.
	 *
	 * @return the tree viewer
	 */
	protected CheckboxTreeViewer getTreeViewer() {
		return fViewer;
	}

	/**
	 * Adds the selection and deselection buttons to the dialog.
	 *
	 * @param composite the parent composite
	 * @return Composite the composite the buttons were created in.
	 */
	protected Composite createSelectionButtons(Composite composite) {
		Composite buttonComposite = new Composite(composite, SWT.RIGHT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		buttonComposite.setLayout(layout);
		buttonComposite.setFont(composite.getFont());
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		buttonComposite.setLayoutData(data);
		Button selectButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID,
				WorkbenchMessages.CheckedTreeSelectionDialog_select_all, false);
		SelectionListener listener = widgetSelectedAdapter(e -> {
			Object[] viewerElements = fContentProvider.getElements(fInput);
			for (Object viewerElement : viewerElements) {
				fViewer.setSubtreeChecked(viewerElement, true);
			}
			updateOKStatus();
		});
		selectButton.addSelectionListener(listener);
		Button deselectButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID,
				WorkbenchMessages.CheckedTreeSelectionDialog_deselect_all, false);
		listener = widgetSelectedAdapter(e -> {
			fViewer.setCheckedElements(new Object[0]);
			updateOKStatus();
		});
		deselectButton.addSelectionListener(listener);
		return buttonComposite;
	}

	private boolean evaluateIfTreeEmpty(Object input) {
		Object[] elements = fContentProvider.getElements(input);
		if (elements.length > 0) {
			if (fFilters != null) {
				for (ViewerFilter curr : fFilters) {
					elements = curr.filter(fViewer, input, elements);
				}
			}
		}
		return elements.length == 0;
	}
}
