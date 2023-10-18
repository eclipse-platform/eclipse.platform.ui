/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.misc.CheckboxTreeAndListGroup;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

/**
 * A standard resource selection dialog which solicits a list of resources from
 * the user. The <code>getResult</code> method returns the selected resources.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * ResourceSelectionDialog dialog = new ResourceSelectionDialog(getShell(), rootResource, msg);
 * dialog.setInitialSelections(selectedResources);
 * dialog.open();
 * return dialog.getResult();
 * </pre>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ResourceSelectionDialog extends SelectionDialog {
	// the root element to populate the viewer with
	private IAdaptable root;

	// the visual selection widget group
	private CheckboxTreeAndListGroup selectionGroup;

	// constants
	private static final int SIZING_SELECTION_WIDGET_WIDTH = 400;

	private static final int SIZING_SELECTION_WIDGET_HEIGHT = 300;

	/**
	 * Creates a resource selection dialog rooted at the given element.
	 *
	 * @param parentShell the parent shell
	 * @param rootElement the root element to populate this dialog with
	 * @param message     the message to be displayed at the top of this dialog, or
	 *                    <code>null</code> to display a default message
	 */
	public ResourceSelectionDialog(Shell parentShell, IAdaptable rootElement, String message) {
		super(parentShell);
		setTitle(IDEWorkbenchMessages.ResourceSelectionDialog_title);
		root = rootElement;
		if (message != null) {
			setMessage(message);
		} else {
			setMessage(IDEWorkbenchMessages.ResourceSelectionDialog_message);
		}
		setShellStyle(getShellStyle() | SWT.SHEET);
	}

	/**
	 * Visually checks the previously-specified elements in the container (left)
	 * portion of this dialog's resource selection viewer.
	 */
	private void checkInitialSelections() {
		Iterator<?> itemsToCheck = getInitialElementSelections().iterator();

		while (itemsToCheck.hasNext()) {
			IResource currentElement = (IResource) itemsToCheck.next();

			if (currentElement.getType() == IResource.FILE) {
				selectionGroup.initialCheckListItem(currentElement);
			} else {
				selectionGroup.initialCheckTreeItem(currentElement);
			}
		}
	}

	/**
	 * @param event the event
	 */
	public void checkStateChanged(CheckStateChangedEvent event) {
		getOkButton().setEnabled(selectionGroup.getCheckedElementCount() > 0);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IIDEHelpContextIds.RESOURCE_SELECTION_DIALOG);
	}

	@Override
	public void create() {
		super.create();
		initializeDialog();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite = (Composite) super.createDialogArea(parent);

		// create the input element, which has the root resource
		// as its only child
		List<IAdaptable> input = new ArrayList<>();
		input.add(root);

		createMessageArea(composite);
		selectionGroup = new CheckboxTreeAndListGroup(composite, input,
				getResourceProvider(IResource.FOLDER | IResource.PROJECT | IResource.ROOT),
				WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(),
				new ResourceComparator(ResourceComparator.NAME), getResourceProvider(IResource.FILE),
				WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(),
				new ResourceComparator(ResourceComparator.NAME), SWT.NONE,
				// since this page has no other significantly-sized
				// widgets we need to hardcode the combined widget's
				// size, otherwise it will open too small
				SIZING_SELECTION_WIDGET_WIDTH, SIZING_SELECTION_WIDGET_HEIGHT);

		composite.addControlListener(new ControlListener() {
			@Override
			public void controlMoved(ControlEvent e) {
			}

			@Override
			public void controlResized(ControlEvent e) {
				// Also try and reset the size of the columns as appropriate
				TableColumn[] columns = selectionGroup.getListTable().getColumns();
				for (TableColumn column : columns) {
					column.pack();
				}
			}
		});

		return composite;
	}

	/**
	 * Returns a content provider for <code>IResource</code>s that returns only
	 * children of the given resource type.
	 */
	private ITreeContentProvider getResourceProvider(final int resourceType) {
		return new WorkbenchContentProvider() {
			@Override
			public Object[] getChildren(Object o) {
				if (o instanceof IContainer) {
					IResource[] members = null;
					try {
						members = ((IContainer) o).members();
					} catch (CoreException e) {
						// just return an empty set of children
						return new Object[0];
					}

					// filter out the desired resource types
					List<IResource> results = new ArrayList<>();
					for (IResource member : members) {
						// And the test bits with the resource types to see if they are what we want
						if ((member.getType() & resourceType) > 0) {
							results.add(member);
						}
					}
					return results.toArray();
				}
				// input element case
				if (o instanceof ArrayList) {
					return ((ArrayList<?>) o).toArray();
				}
				return new Object[0];
			}
		};
	}

	/**
	 * Initializes this dialog's controls.
	 */
	private void initializeDialog() {
		selectionGroup
				.addCheckStateListener(event -> getOkButton().setEnabled(selectionGroup.getCheckedElementCount() > 0));

		if (getInitialElementSelections().isEmpty()) {
			getOkButton().setEnabled(false);
		} else {
			checkInitialSelections();
		}
	}

	/**
	 * The <code>ResourceSelectionDialog</code> implementation of this
	 * <code>Dialog</code> method builds a list of the selected resources for later
	 * retrieval by the client and closes this dialog.
	 */
	@Override
	protected void okPressed() {
		Iterator<?> resultEnum = selectionGroup.getAllCheckedListItems();
		List<Object> list = new ArrayList<>();
		while (resultEnum.hasNext()) {
			list.add(resultEnum.next());
		}
		setResult(list);
		super.okPressed();
	}
}
