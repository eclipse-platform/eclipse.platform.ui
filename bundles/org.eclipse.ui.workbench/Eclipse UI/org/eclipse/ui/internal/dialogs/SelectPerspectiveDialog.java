/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 *     activated and used by other components.
 *     Martin Karpisek <martin.karpisek@gmail.com> - Bug 106954
 *******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ITriggerPoint;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.activities.ws.ActivityMessages;
import org.eclipse.ui.internal.activities.ws.ActivityViewerFilter;
import org.eclipse.ui.internal.activities.ws.WorkbenchTriggerPoints;
import org.eclipse.ui.model.PerspectiveLabelProvider;

/**
 * A dialog for perspective creation
 */
public class SelectPerspectiveDialog extends Dialog implements
        ISelectionChangedListener {

    final private static int LIST_HEIGHT = 300;

    final private static int LIST_WIDTH = 300;

    private TableViewer list;

    private Button okButton;

    private IPerspectiveDescriptor perspDesc;

    private IPerspectiveRegistry perspReg;

    private ActivityViewerFilter activityViewerFilter = new ActivityViewerFilter();

	private Label descriptionHint;

    private Button showAllButton;

	private PopupDialog perspDescPopupDialog;

    /**
     * PerspectiveDialog constructor comment.
     *
     * @param parentShell the parent shell
     * @param perspReg the perspective registry
     */
    public SelectPerspectiveDialog(Shell parentShell,
            IPerspectiveRegistry perspReg) {
        super(parentShell);
        this.perspReg = perspReg;
		setShellStyle(getShellStyle() | SWT.SHEET);
    }

    @Override
	protected void cancelPressed() {
        perspDesc = null;
        super.cancelPressed();
    }

    @Override
	protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(WorkbenchMessages.SelectPerspective_shellTitle);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IWorkbenchHelpContextIds.SELECT_PERSPECTIVE_DIALOG);
    }

    /**
     * Adds buttons to this dialog's button bar.
     * <p>
     * The default implementation of this framework method adds standard ok and
     * cancel buttons using the <code>createButton</code> framework method.
     * Subclasses may override.
     * </p>
     *
     * @param parent the button bar composite
     */
    @Override
	protected void createButtonsForButtonBar(Composite parent) {
        okButton = createButton(parent, IDialogConstants.OK_ID,
				WorkbenchMessages.SelectPerspective_open_button_label, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
        updateButtons();
    }

    /**
     * Creates and returns the contents of the upper part of this dialog (above
     * the button bar).
     *
     * @param parent the parent composite to contain the dialog area
     * @return the dialog area control
     */
    @Override
	protected Control createDialogArea(Composite parent) {
        // Run super.
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setFont(parent.getFont());

        createViewer(composite);
        layoutTopControl(list.getControl());

		// Use F2... label
		descriptionHint = new Label(composite, SWT.WRAP);
		descriptionHint.setText(WorkbenchMessages.SelectPerspective_selectPerspectiveHelp);
		descriptionHint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		descriptionHint.setVisible(false);

        if (needsShowAllButton()) {
            createShowAllButton(composite);
        }
        // Return results.
        return composite;
    }

    /**
     * @return whether a show-all button is needed.  A show all button is needed only if the list contains filtered items.
     */
    private boolean needsShowAllButton() {
        return activityViewerFilter.getHasEncounteredFilteredItem();
    }

    /**
     * Create a show all button in the parent.
     *
     * @param parent the parent <code>Composite</code>.
     */
    private void createShowAllButton(Composite parent) {
        showAllButton = new Button(parent, SWT.CHECK);
        showAllButton
                .setText(ActivityMessages.Perspective_showAll);
        showAllButton.addSelectionListener(widgetSelectedAdapter(e -> {
		    if (showAllButton.getSelection()) {
		        list.resetFilters();
		    } else {
		        list.addFilter(activityViewerFilter);
		    }
		}));

    }

    /**
     * Create a new viewer in the parent.
     *
     * @param parent the parent <code>Composite</code>.
     */
    private void createViewer(Composite parent) {
        // Add perspective list.
        list = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.BORDER);
        list.getTable().setFont(parent.getFont());
		list.setLabelProvider(new PerspectiveLabelProvider());
        list.setContentProvider(new PerspContentProvider());
        list.addFilter(activityViewerFilter);
        list.setComparator(new ViewerComparator());
        list.setInput(perspReg);
        list.addSelectionChangedListener(this);
        list.addDoubleClickListener(event -> handleDoubleClickEvent());
		list.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				handleTableViewerKeyPressed(e);
			}
		});
    }

    /**
     * Returns the current selection.
     *
     * @return the current selection
     */
    public IPerspectiveDescriptor getSelection() {
        return perspDesc;
    }

    /**
     * Handle a double click event on the list
     */
    protected void handleDoubleClickEvent() {
        okPressed();
    }

    /**
     * Layout the top control.
     *
     * @param control the control.
     */
    private void layoutTopControl(Control control) {
        GridData spec = new GridData(GridData.FILL_BOTH);
        spec.widthHint = LIST_WIDTH;
        spec.heightHint = LIST_HEIGHT;
        control.setLayoutData(spec);
    }

    /**
     * Notifies that the selection has changed.
     *
     * @param event event object describing the change
     */
    @Override
	public void selectionChanged(SelectionChangedEvent event) {
        updateSelection(event);
        updateButtons();
		updateTooltip();
    }

    /**
     * Update the button enablement state.
     */
    protected void updateButtons() {
        okButton.setEnabled(getSelection() != null);
    }

    /**
     * Update the selection object.
     */
    protected void updateSelection(SelectionChangedEvent event) {
        perspDesc = null;
        IStructuredSelection sel = (IStructuredSelection) event.getSelection();
        if (!sel.isEmpty()) {
            Object obj = sel.getFirstElement();
            if (obj instanceof IPerspectiveDescriptor) {
				perspDesc = (IPerspectiveDescriptor) obj;
			}
        }
    }

    private void updateTooltip() {
		String tooltip = ""; //$NON-NLS-1$
		if (perspDesc != null) {
			tooltip = perspDesc.getDescription();
		}

		boolean hasTooltip = tooltip != null && tooltip.length() > 0;
		descriptionHint.setVisible(hasTooltip);

		if (perspDescPopupDialog != null) {
			perspDescPopupDialog.close();
			perspDescPopupDialog = null;
		}
	}

	@Override
	protected void okPressed() {
        ITriggerPoint triggerPoint = PlatformUI.getWorkbench()
                .getActivitySupport().getTriggerPointManager().getTriggerPoint(
                        WorkbenchTriggerPoints.OPEN_PERSPECITVE_DIALOG);
        if (WorkbenchActivityHelper.allowUseOf(triggerPoint, getSelection())) {
			super.okPressed();
		}
    }

    @Override
	protected boolean isResizable() {
    	return true;
    }

	private void handleTableViewerKeyPressed(KeyEvent event) {
		// popup the description for the selected perspective
		if (event.keyCode == SWT.F2 && event.stateMask == 0) {
			IStructuredSelection selection = list.getStructuredSelection();
			// only show description if one perspective is selected
			if (selection.size() == 1) {
				Object o = selection.getFirstElement();
				if (o instanceof IPerspectiveDescriptor) {
					String description = ((IPerspectiveDescriptor) o).getDescription();
					if (description.length() == 0) {
						description = WorkbenchMessages.SelectPerspective_noDesc;
					}
					popUp(description);
				}
			}
		}
	}

	private void popUp(final String description) {
		perspDescPopupDialog = new PopupDialog(getShell(), PopupDialog.HOVER_SHELLSTYLE, true, false, false, false, false, null, null) {
			private static final int CURSOR_SIZE = 15;

			@Override
			protected Point getInitialLocation(Point initialSize) {
				// show popup relative to cursor
				Display display = getShell().getDisplay();
				Point location = display.getCursorLocation();
				location.x += CURSOR_SIZE;
				location.y += CURSOR_SIZE;
				return location;
			}

			@Override
			protected Control createDialogArea(Composite parent) {
				Label label = new Label(parent, SWT.WRAP);
				label.setText(description);
				label.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent event) {
						close();
					}
				});
				// Use the compact margins employed by PopupDialog.
				GridData gd = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
				gd.horizontalIndent = PopupDialog.POPUP_HORIZONTALSPACING;
				gd.verticalIndent = PopupDialog.POPUP_VERTICALSPACING;
				label.setLayoutData(gd);
				return label;
			}
		};
		perspDescPopupDialog.open();
	}
}
