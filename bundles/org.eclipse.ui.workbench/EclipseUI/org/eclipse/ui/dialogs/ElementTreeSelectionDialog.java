/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   IBM Corporation - initial API and implementation
 *   Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 *     font should be activated and used by other components.
 *   Carsten Pfeiffer <carsten.pfeiffer@gebit.de> - Fix for bug 182354 -
 *     [Dialogs] API - make ElementTreeSelectionDialog usable with a
 *     FilteredTree
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import static org.eclipse.swt.events.SelectionListener.widgetDefaultSelectedAdapter;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
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
public class ElementTreeSelectionDialog extends SelectionStatusDialog {

	private TreeViewer fViewer;

	private IBaseLabelProvider fLabelProvider;

	private ITreeContentProvider fContentProvider;

	private ISelectionStatusValidator fValidator = null;

	private ViewerComparator fComparator;

	private boolean fAllowMultiple = true;

	private boolean fDoubleClickSelects = true;

	private String fEmptyListMessage = WorkbenchMessages.ElementTreeSelectionDialog_nothing_available;

	private IStatus fCurrStatus = new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$

	private List<ViewerFilter> fFilters;

	private Object fInput;

	private boolean fIsEmpty;

	private int fWidth = 60;

	private int fHeight = 18;

	/**
	 * Constructs an instance of <code>ElementTreeSelectionDialog</code>.
	 *
	 * @param parent          The parent shell for the dialog
	 * @param labelProvider   the label provider to render the entries
	 * @param contentProvider the content provider to evaluate the tree structure
	 */
	public ElementTreeSelectionDialog(Shell parent, ILabelProvider labelProvider,
			ITreeContentProvider contentProvider) {
		this(parent, (IBaseLabelProvider) labelProvider, contentProvider);
	}

	/**
	 * Constructs an instance of <code>ElementTreeSelectionDialog</code>.
	 *
	 * @param parent          The parent shell for the dialog
	 * @param labelProvider   the label provider to render the entries. It must be
	 *                        compatible with the Viewerreturned from
	 *                        {@link #doCreateTreeViewer(Composite, int)}
	 * @param contentProvider the content provider to evaluate the tree structure
	 * @since 3.106
	 */
	public ElementTreeSelectionDialog(Shell parent, IBaseLabelProvider labelProvider,
			ITreeContentProvider contentProvider) {
		super(parent);

		fLabelProvider = labelProvider;
		fContentProvider = contentProvider;

		setResult(new ArrayList<>(0));
		setStatusLineAboveButtons(true);
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
	 * Specifies if multiple selection is allowed.
	 *
	 * @param allowMultiple true if allowed.
	 */
	public void setAllowMultiple(boolean allowMultiple) {
		fAllowMultiple = allowMultiple;
	}

	/**
	 * Specifies if default selected events (double click) are created.
	 *
	 * @param doubleClickSelects true or false.
	 */
	public void setDoubleClickSelects(boolean doubleClickSelects) {
		fDoubleClickSelects = doubleClickSelects;
	}

	/**
	 * Sets the sorter used by the tree viewer.
	 *
	 * @param sorter the {@link ViewerSorter}
	 * @deprecated as of 3.3, use
	 *             {@link ElementTreeSelectionDialog#setComparator(ViewerComparator)}
	 *             instead
	 */
	@Deprecated
	public void setSorter(ViewerSorter sorter) {
		fComparator = sorter;
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
	 * Validate the receiver and update the ok status.
	 */
	protected void updateOKStatus() {
		if (!fIsEmpty) {
			if (fValidator != null) {
				fCurrStatus = fValidator.validate(getResult());
				updateStatus(fCurrStatus);
			} else {
				fCurrStatus = new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.OK, "", //$NON-NLS-1$
						null);
			}
		} else {
			fCurrStatus = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR, fEmptyListMessage, null);
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

	@Override
	protected void computeResult() {
		setResult(fViewer.getStructuredSelection().toList());
	}

	@Override
	public void create() {
		BusyIndicator.showWhile(null, () -> {
			access$superCreate();
			fViewer.setSelection(new StructuredSelection(getInitialElementSelections()), true);
			updateOKStatus();
		});
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		Label messageLabel = createMessageArea(composite);
		TreeViewer treeViewer = createTreeViewer(composite);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = convertWidthInCharsToPixels(fWidth);
		data.heightHint = convertHeightInCharsToPixels(fHeight);

		Tree treeWidget = treeViewer.getTree();
		treeWidget.setLayoutData(data);
		treeWidget.setFont(parent.getFont());

		if (fIsEmpty) {
			messageLabel.setEnabled(false);
			treeWidget.setEnabled(false);
		}

		return composite;
	}

	/**
	 * Creates and initializes the tree viewer.
	 *
	 * @param parent the parent composite
	 * @return the tree viewer
	 * @see #doCreateTreeViewer(Composite, int)
	 */
	protected TreeViewer createTreeViewer(Composite parent) {
		int style = SWT.BORDER | (fAllowMultiple ? SWT.MULTI : SWT.SINGLE);

		fViewer = doCreateTreeViewer(parent, style);
		fViewer.setContentProvider(fContentProvider);
		fViewer.setLabelProvider(fLabelProvider);
		fViewer.addSelectionChangedListener(event -> {
			access$setResult(event.getStructuredSelection().toList());
			updateOKStatus();
		});

		fViewer.setComparator(fComparator);
		if (fFilters != null) {
			for (int i = 0; i != fFilters.size(); i++) {
				fViewer.addFilter(fFilters.get(i));
			}
		}

		if (fDoubleClickSelects) {
			Tree tree = fViewer.getTree();
			tree.addSelectionListener(widgetDefaultSelectedAdapter(e -> {
				updateOKStatus();
				if (fCurrStatus.isOK()) {
					access$superButtonPressed(IDialogConstants.OK_ID);
				}
			}));
		}
		fViewer.addDoubleClickListener(event -> {
			updateOKStatus();

			// If it is not OK or if double click does not
			// select then expand
			if (!(fDoubleClickSelects && fCurrStatus.isOK())) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					Object item = ((IStructuredSelection) selection).getFirstElement();
					if (fViewer.getExpandedState(item)) {
						fViewer.collapseToLevel(item, 1);
					} else {
						fViewer.expandToLevel(item, 1);
					}
				}
			}
		});

		fViewer.setInput(fInput);

		return fViewer;
	}

	/**
	 * Creates the tree viewer.
	 *
	 * @param parent the parent composite
	 * @param style  the {@link SWT} style bits
	 * @return the tree viewer
	 * @since 3.4
	 */
	protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
		return new TreeViewer(new Tree(parent, style));
	}

	/**
	 * Returns the tree viewer.
	 *
	 * @return the tree viewer
	 */
	protected TreeViewer getTreeViewer() {
		return fViewer;
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

	/**
	 * Set the result using the super class implementation of buttonPressed.
	 *
	 * @param id the id of the button that was pressed (see IDialogConstants.*_ID
	 *           constants)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void access$superButtonPressed(int id) {
		super.buttonPressed(id);
	}

	/**
	 * Set the result using the super class implementation of setResult.
	 *
	 * @param result list of selected elements, or <code>null</code> if Cancel was
	 *               pressed
	 * @see SelectionStatusDialog#setResult(int, Object)
	 */
	protected void access$setResult(List result) {
		super.setResult(result);
	}

	@Override
	protected void handleShellCloseEvent() {
		super.handleShellCloseEvent();

		// Handle the closing of the shell by selecting the close icon
		if (getReturnCode() == CANCEL) {
			setResult(null);
		}
	}

}
