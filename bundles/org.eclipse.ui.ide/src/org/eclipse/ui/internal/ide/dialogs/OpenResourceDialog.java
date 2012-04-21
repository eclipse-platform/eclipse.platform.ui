/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.util.Collections;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.ShowInMenu;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.IViewDescriptor;


/**
 * Shows a list of resources to the user with a text entry field for a string
 * pattern used to filter the list of resources.
 * 
 * @since 2.1
 */
public class OpenResourceDialog extends FilteredResourcesSelectionDialog {

	private Button openWithButton;

	/**
	 * Creates a new instance of the class.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param container
	 *            the container
	 * @param typesMask
	 *            the types mask
	 */
	public OpenResourceDialog(Shell parentShell, IContainer container,
			int typesMask) {
		super(parentShell, true, container, typesMask);
		setTitle(IDEWorkbenchMessages.OpenResourceDialog_title);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 * @since 3.5
	 */
	protected void fillContextMenu(IMenuManager menuManager) {
		super.fillContextMenu(menuManager);

		final IStructuredSelection selectedItems = getSelectedItems();
		if (selectedItems.isEmpty()) {
			return;
		}
		
		IWorkbenchPage activePage = getActivePage();
		if (activePage == null) {
			return;
		}

		menuManager.add(new Separator());
		
		// Add 'Open' menu item
		OpenFileAction openFileAction = new OpenFileAction(activePage) {
			public void run() {
				okPressed();
			}
		};
		openFileAction.selectionChanged(selectedItems);
		if (openFileAction.isEnabled()) {
			menuManager.add(openFileAction);
			
			IAdaptable selectedAdaptable = getSelectedAdaptable();
			if (selectedAdaptable != null) {
				
				// Add 'Open With' sub-menu
				MenuManager subMenu = new MenuManager(IDEWorkbenchMessages.OpenResourceDialog_openWithMenu_label);
				OpenWithMenu openWithMenu = new OpenWithMenu(activePage, selectedAdaptable) {
					/*
					 * (non-Javadoc)
					 * @see org.eclipse.ui.actions.OpenWithMenu#openEditor(org.eclipse.ui.IEditorDescriptor, boolean)
					 */
					protected void openEditor(IEditorDescriptor editorDescriptor, boolean openUsingDescriptor) {
						computeResult();
						setResult(Collections.EMPTY_LIST);
						close();
						super.openEditor(editorDescriptor, openUsingDescriptor);
					}
				};
				subMenu.add(openWithMenu);
				menuManager.add(subMenu);
			}
		}
		
		
		// Add 'Show In' sub-menu
		MenuManager showInMenuManager = new MenuManager(IDEWorkbenchMessages.OpenResourceDialog_showInMenu_label);
		ShowInMenu showInMenu = new ShowInMenu() {
			/* (non-Javadoc)
			 * @see org.eclipse.ui.internal.ShowInMenu#getContext(org.eclipse.ui.IWorkbenchPart)
			 */
			protected ShowInContext getContext(IWorkbenchPart sourcePart) {
				return new ShowInContext(null, selectedItems);
			}
			/* (non-Javadoc)
			 * @see org.eclipse.ui.internal.ShowInMenu#getContributionItem(org.eclipse.ui.views.IViewDescriptor)
			 */
			protected IContributionItem getContributionItem(IViewDescriptor viewDescriptor) {
				final String targetId= viewDescriptor.getId();
				String label = '&' + viewDescriptor.getLabel();
				ImageDescriptor icon = viewDescriptor.getImageDescriptor();
				Action action = new Action(label, icon) {
					/* (non-Javadoc)
					 * @see org.eclipse.jface.action.Action#run()
					 */
					public void run() {
						computeResult();
						setResult(Collections.EMPTY_LIST);
						close();
						
						IWorkbenchPage page = getActivePage();
						IViewPart view;
						try {
							view = page.showView(targetId);
							IShowInTarget target = getShowInTarget(view);
							if (!(target != null && target.show(getContext(null)))) {
								page.getWorkbenchWindow().getShell().getDisplay().beep();
							}
						} catch (PartInitException e) {
							StatusManager.getManager().handle(new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
											IStatus.ERROR, "", e)); //$NON-NLS-1$
						}
					}
					private IShowInTarget getShowInTarget(IWorkbenchPart targetPart) {
						return (IShowInTarget) org.eclipse.ui.internal.util.Util.getAdapter(targetPart, IShowInTarget.class);
					}
				};
				action.setId(targetId);
				return new ActionContributionItem(action);
			}
		};
        showInMenu.setId(ContributionItemFactory.VIEWS_SHOW_IN.getId());
        showInMenu.initialize(activePage.getWorkbenchWindow());
		showInMenuManager.add(showInMenu);
		menuManager.add(showInMenuManager);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 * @since 3.5
	 */
	protected void createButtonsForButtonBar(final Composite parent) {
		// increment the number of columns in the button bar
		GridLayout parentLayout = (GridLayout)parent.getLayout();
		parentLayout.numColumns++;
		parentLayout.makeColumnsEqualWidth = false;
		
		final Composite openComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		openComposite.setLayout(layout);

		Button okButton = createButton(openComposite, IDialogConstants.OK_ID, IDEWorkbenchMessages.OpenResourceDialog_openButton_text, true);

		// Arrow down button for Open With menu
		((GridLayout)openComposite.getLayout()).numColumns++;
		openWithButton = new Button(openComposite, SWT.PUSH);
		openWithButton.setToolTipText(IDEWorkbenchMessages.OpenResourceDialog_openWithButton_toolTip);
		openWithButton.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_BUTTON_MENU));

		GridData data = new GridData(SWT.CENTER, SWT.FILL, false, true);
		openWithButton.setLayoutData(data);

		openWithButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				showOpenWithMenu(openComposite);
			}
		});
		openWithButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				showOpenWithMenu(openComposite);
			}
		});
		openWithButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent e) {
				if (e.childID == ACC.CHILDID_SELF)
					e.result= IDEWorkbenchMessages.OpenResourceDialog_openWithButton_toolTip;
			}
		});

		Button cancelButton = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		GridData cancelLayoutData = (GridData) cancelButton.getLayoutData();
		GridData okLayoutData = (GridData) okButton.getLayoutData();
		int buttonWidth = Math.max(cancelLayoutData.widthHint, okLayoutData.widthHint);
		cancelLayoutData.widthHint = buttonWidth;
		okLayoutData.widthHint = buttonWidth;
		
		if (openComposite.getDisplay().getDismissalAlignment() == SWT.RIGHT) {
			// Make the default button the right-most button.
			// See also special code in org.eclipse.jface.dialogs.Dialog#initializeBounds()
			openComposite.moveBelow(null);
			if (Util.isCarbon()) {
			okLayoutData.horizontalIndent = -10;
		}
	}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#initializeBounds()
	 * @since 3.5
	 */
	protected void initializeBounds() {
		super.initializeBounds();
		if (openWithButton.getDisplay().getDismissalAlignment() == SWT.RIGHT) {
			// Move the menu button back to the right of the default button.
			if (!Util.isMac()) {
				// On the Mac, the round buttons and the big padding would destroy the visual coherence of the split button.
				openWithButton.moveBelow(null);
				openWithButton.getParent().layout();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#updateButtonsEnableState(org.eclipse.core.runtime.IStatus)
	 * @since 3.5
	 */
	protected void updateButtonsEnableState(IStatus status) {
		super.updateButtonsEnableState(status);
		if (openWithButton != null && !openWithButton.isDisposed()) {
			openWithButton.setEnabled(!status.matches(IStatus.ERROR) && getSelectedItems().size() == 1);
		}
	}

	private IAdaptable getSelectedAdaptable() {
		IStructuredSelection s = getSelectedItems();
		if (s.size() != 1) {
			return null;
		}
		Object selectedElement = s.getFirstElement();
		if (selectedElement instanceof IAdaptable) {
			return (IAdaptable) selectedElement;
		}
		return null;
	}

	private IWorkbenchPage getActivePage() {
		IWorkbenchWindow activeWorkbenchWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null) {
			return null;
		}
		return activeWorkbenchWindow.getActivePage();
	}

	private void showOpenWithMenu(final Composite openComposite) {
		IWorkbenchPage activePage = getActivePage();
		if (activePage == null) {
			return;
		}
		IAdaptable selectedAdaptable = getSelectedAdaptable();
		if (selectedAdaptable == null) {
			return;
		}

		OpenWithMenu openWithMenu = new OpenWithMenu(activePage, selectedAdaptable) {
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.ui.actions.OpenWithMenu#openEditor(org.eclipse.ui.IEditorDescriptor, boolean)
			 */
			protected void openEditor(IEditorDescriptor editorDescriptor, boolean openUsingDescriptor) {
				computeResult();
				setResult(Collections.EMPTY_LIST);
				close();
				super.openEditor(editorDescriptor, openUsingDescriptor);
			}
		};

		Menu menu = new Menu(openComposite.getParent());
		Control c = openComposite;
		Point p = c.getLocation();
		p.y = p.y + c.getSize().y;
		p = c.getParent().toDisplay(p);

		menu.setLocation(p);
		openWithMenu.fill(menu, -1);
		menu.setVisible(true);
	}

}
