/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
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

	private final class ResourceOpenWithMenu extends OpenWithMenu {
		private ResourceOpenWithMenu(IWorkbenchPage page, IAdaptable file) {
			super(page, file);
		}

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
	}

	private final class ResourceShowInMenu extends ShowInMenu {
		private final IStructuredSelection selectedItems;

		private ResourceShowInMenu(IStructuredSelection selectedItems, IWorkbenchWindow workbenchWindow) {
			this.selectedItems = selectedItems;
			setId(ContributionItemFactory.VIEWS_SHOW_IN.getId());
			initialize(workbenchWindow);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.ShowInMenu#getContext(org.eclipse.ui.IWorkbenchPart)
		 */
		protected ShowInContext getContext(IWorkbenchPart sourcePart) {
			return new ShowInContext(null, selectedItems);
		}

		protected IWorkbenchPart getSourcePart() {
			return null;
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
	}

	private static final int OPEN_WITH_ID = IDialogConstants.CLIENT_ID + 1;
	private static final int SHOW_IN_ID = IDialogConstants.CLIENT_ID + 2;
	
	private Button showInButton;
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
				OpenWithMenu openWithMenu = new ResourceOpenWithMenu(activePage, selectedAdaptable);
				subMenu.add(openWithMenu);
				menuManager.add(subMenu);
			}
		}
		
		
		// Add 'Show In' sub-menu
		MenuManager showInMenuManager = new MenuManager(IDEWorkbenchMessages.OpenResourceDialog_showInMenu_label);
		ShowInMenu showInMenu = new ResourceShowInMenu(selectedItems, activePage.getWorkbenchWindow());
		showInMenuManager.add(showInMenu);
		menuManager.add(showInMenuManager);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 * @since 3.5
	 */
	protected void createButtonsForButtonBar(final Composite parent) {
		GridLayout parentLayout = (GridLayout)parent.getLayout();
		parentLayout.makeColumnsEqualWidth = false;

		showInButton = createDropdownButton(parent, SHOW_IN_ID, IDEWorkbenchMessages.OpenResourceDialog_showInButton_text,
				new MouseAdapter() {
					public void mouseDown(MouseEvent e) {
						showShowInMenu();
					}
				});
		setButtonLayoutData(showInButton);
		
		openWithButton = createDropdownButton(parent, OPEN_WITH_ID, IDEWorkbenchMessages.OpenResourceDialog_openWithButton_text,
				new MouseAdapter() {
					public void mouseDown(MouseEvent e) {
						showOpenWithMenu();
					}
				});
		setButtonLayoutData(openWithButton);
		
		GridData showInLayoutData = (GridData) showInButton.getLayoutData();
		GridData openWithLayoutData = (GridData) openWithButton.getLayoutData();
		int buttonWidth = Math.max(showInLayoutData.widthHint, openWithLayoutData.widthHint);
		showInLayoutData.widthHint = buttonWidth;
		openWithLayoutData.widthHint = buttonWidth;
		
		new Label(parent, SWT.NONE).setLayoutData(new GridData(5, 0));
		parentLayout.numColumns++;
		
		Button okButton = createButton(parent, IDialogConstants.OK_ID, IDEWorkbenchMessages.OpenResourceDialog_openButton_text, true);
		Button cancelButton = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		GridData cancelLayoutData = (GridData) cancelButton.getLayoutData();
		GridData okLayoutData = (GridData) okButton.getLayoutData();
		buttonWidth = Math.max(cancelLayoutData.widthHint, okLayoutData.widthHint);
		cancelLayoutData.widthHint = buttonWidth;
		okLayoutData.widthHint = buttonWidth;
	}

	private Button createDropdownButton(final Composite parent, int id, String label, MouseListener mouseListener) {
		char textEmbedding = parent.getOrientation() == SWT.LEFT_TO_RIGHT ? '\u202a' : '\u202b';
		Button button = createButton(parent, id, textEmbedding + label + '\u202c', false);
		if (Util.isMac()) {
			// Button#setOrientation(int) is a no-op on the Mac. Use a Unicode BLACK DOWN-POINTING SMALL TRIANGLE.
			button.setText(button.getText() + " \u25BE"); //$NON-NLS-1$
		} else {
			int dropDownOrientation = parent.getOrientation() == SWT.LEFT_TO_RIGHT ? SWT.RIGHT_TO_LEFT : SWT.LEFT_TO_RIGHT;
			button.setOrientation(dropDownOrientation);
			button.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_BUTTON_MENU));
			button.addMouseListener(mouseListener);
		}
		return button;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case OPEN_WITH_ID:
			showOpenWithMenu();
			break;
		case SHOW_IN_ID:
			showShowInMenu();
			break;
		default:
			super.buttonPressed(buttonId);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#updateButtonsEnableState(org.eclipse.core.runtime.IStatus)
	 * @since 3.5
	 */
	protected void updateButtonsEnableState(IStatus status) {
		super.updateButtonsEnableState(status);
		if (showInButton != null && !showInButton.isDisposed()
				&& openWithButton != null && !openWithButton.isDisposed()) {
			openWithButton.setEnabled(!status.matches(IStatus.ERROR) && getSelectedItems().size() == 1);
			showInButton.setEnabled(!status.matches(IStatus.ERROR) && getSelectedItems().size() > 0);
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

	private void showOpenWithMenu() {
		IWorkbenchPage activePage = getActivePage();
		if (activePage == null) {
			return;
		}
		IAdaptable selectedAdaptable = getSelectedAdaptable();
		if (selectedAdaptable == null) {
			return;
		}

		ResourceOpenWithMenu openWithMenu = new ResourceOpenWithMenu(activePage, selectedAdaptable);
		showMenu(openWithButton, openWithMenu);
	}

	private void showShowInMenu() {
		IWorkbenchPage activePage = getActivePage();
		if (activePage == null) {
			return;
		}
		IStructuredSelection selectedItems = getSelectedItems();
		if (selectedItems.isEmpty()) {
			return;
		}
		
		ShowInMenu showInMenu = new ResourceShowInMenu(selectedItems, activePage.getWorkbenchWindow());
		showMenu(showInButton, showInMenu);
	}

	private void showMenu(Button button, IContributionItem menuContribution) {
		Menu menu = new Menu(button);
		Point p = button.getLocation();
		p.y = p.y + button.getSize().y;
		p = button.getParent().toDisplay(p);

		menu.setLocation(p);
		menuContribution.fill(menu, 0);
		menu.setVisible(true);
	}
}
