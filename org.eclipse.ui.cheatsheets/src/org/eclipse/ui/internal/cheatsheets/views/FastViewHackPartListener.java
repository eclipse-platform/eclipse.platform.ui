/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.views;

import java.lang.reflect.*;

import org.eclipse.jface.action.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;

import org.eclipse.ui.internal.cheatsheets.*;

/**
 * A class to handle the fast view lifecycle events.
 * <p>
 * The following items are handled by the listener when
 * various part events occur:
 * <ul>
 *   <li>disable dragging of the view
 *   </li>
 *   <li>disable dragging of the shortcut bar icon
 *   </li>
 *   <li>disable shortcut bar fastview menu item
 *   </li>
 *   <li>ensure the shortcut bar icon has the correct selection
 *   </li>
 *   <li>remove the fastview button
 *   </li>
 *   <li>remove the fastview popup menu item
 *   </li>
 *   <li>ensure the view is a fastview 
 *   </li>
 *   <li>removes this listener and the perspective listener when the part is closed.
 *   </li>
 * </ul>
 */
/* package */ class FastViewHackPartListener implements IPartListener {
	private static IViewPart view;
	private final String fastViewMenuText;
	private final String viewTitle;
	private IPerspectiveListener perspectiveListener;

	private static boolean handledDisableShortcutBarMenu = false;
	private static boolean handledPartActivated = false;
//	The following does not appear to be needed after there was
//	similar code added to controlMoved method.
//	private static final Rectangle zeroSizeRectangle = new Rectangle(0,0,0,0);


	/**
	 * Creates a new FastViewHackPartListener for the given view.
	 * 
	 * @param aView - the view to handle the part lifecycle events for
	 * @param aListener - the perspectiveListener to remove when the part is closed
	 */
	/* package */ FastViewHackPartListener(IViewPart aView, IPerspectiveListener aListener) {
		//System.out.println("FastViewHackPartListener");
		view = aView;
		perspectiveListener = aListener;

		fastViewMenuText = WorkbenchMessages.getString("ViewPane.fastView"); //$NON-NLS-1$
		viewTitle = CheatSheetPlugin.getResourceString(ICheatSheetResource.CHEAT_SHEETS);
		

		ensureViewIsFastView();
	}

	/**
	 * @see org.eclipse.ui.IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		if (view == part) {
			//System.out.println("partActivated: " + part);

			PartPane partPane = ((ViewSite) part.getSite()).getPane();
			
			// This disables the view from being dragged. forcing the cheat sheet view
			// to remain a fastview.
			disableDragView(part, (ViewPane)partPane);

			// Set the selection for the shortcut bar icon for the cheat sheet fast view
			setFastViewIconSelection(true);

			if( handledPartActivated ) {
				return;
			}
			
			handledPartActivated = true;

			// Ensure the selection for the shortcut bar icon for the cheat sheet fast view is always correct
			ensureShortcutBarIconSelection(partPane);

			// Remove the Fast View push pin toolbar item from the cheat sheet view
			FastViewHack.removeFastViewButton((ViewPane)partPane);

			// Remove the Fast View menu item from the cheat sheet view title bar pop-up menu
			removeFastViewPopupMenuItem(partPane);
		}
	}

	/**
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
		/*
		if (view == part) {
			System.out.println("partBroughtToTop: " + part);
		}
		*/
	}

	/**
	 * Removes this listener and the perspective listener when the part is closed.
	 * 
	 * @see org.eclipse.ui.IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (view == part) {
			//System.out.println("partClosed: " + part);

			handledPartActivated = false;
			
			// Remove the Perspective and Part listeners.
			IWorkbench workbench = PlatformUI.getWorkbench();
			if( workbench != null ) {
				IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
				if( activeWorkbenchWindow != null ) {
					//System.out.println("PerspectiveListener removed");
					activeWorkbenchWindow.removePerspectiveListener(perspectiveListener);
					
					IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
					if( activePage != null ) {
						//System.out.println("PartListener removed");
						activePage.removePartListener(this);
					}
				}
			}
		}
	}

	/**
	 * Disable dragging of the shortcut bar icon each time a part is deactivated.
	 * 
	 * @see org.eclipse.ui.IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
		//System.out.println("partDeactivated: " + part);

		// Disable dragging of the shortcut bar icon for the cheat sheet fast view
		FastViewHack.disableDragShortcutBarIcon();

		if( view == part ) {
			// Set the selection for the shortcut bar icon for the cheat sheet fast view
			setFastViewIconSelection(false);
		}
	}

	/**
	 * @see org.eclipse.ui.IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
		if (view == part) {
			//System.out.println("partOpened: " + part);

			disableShortcutBarMenu();
		}
	}


	/**
	 * Disables the shortcut bar fastview menu item.
	 */
	private void disableShortcutBarMenu() {
		if( handledDisableShortcutBarMenu ) {
			return;
		}
		
		handledDisableShortcutBarMenu = true;

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ToolBarManager shortcutBar = null;
		try {		
			Field field = window.getClass().getDeclaredField("shortcutBar"); //$NON-NLS-1$
			field.setAccessible(true);
			shortcutBar = (ToolBarManager)field.get(window);
		} catch(Exception e) {
			e.printStackTrace();
		}

		ToolBar toolBar = shortcutBar.getControl();
		final Menu fastViewBarMenu = new Menu(toolBar);
		
		MenuItem closeItem = new MenuItem(fastViewBarMenu, SWT.NONE);
		closeItem.setText(WorkbenchMessages.getString("WorkbenchWindow.close")); //$NON-NLS-1$
		closeItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ToolItem toolItem = (ToolItem) fastViewBarMenu.getData();
				if (toolItem != null && !toolItem.isDisposed()) {
					IViewPart view = (IViewPart)toolItem.getData(ShowFastViewContribution.FAST_VIEW);

					if( view == null && toolItem.getToolTipText().equals(viewTitle) ) {
						view = FastViewHackPartListener.view;
					}
					//System.out.println("view: "+view);
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(view);
				}
			}
		});

		MenuItem restoreItem = restoreItem = new MenuItem(fastViewBarMenu, SWT.CHECK);
		restoreItem.setSelection(true);
		restoreItem.setText(WorkbenchMessages.getString("WorkbenchWindow.restore")); //$NON-NLS-1$
		restoreItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ToolItem toolItem = (ToolItem) fastViewBarMenu.getData();
				if (toolItem != null && !toolItem.isDisposed()) {
					IViewReference view = (IViewReference)toolItem.getData(ShowFastViewContribution.FAST_VIEW);
					((WorkbenchPage)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()).removeFastView(view);

				}
			}
		});

		fastViewBarMenu.addMenuListener(new MenuListener() {
			public void menuHidden(MenuEvent e) {
				//System.out.println("menuHidden");
				Menu menu = (Menu)e.widget;
				MenuItem restoreItem = menu.getItem(1);
				restoreItem.setEnabled(true);
/*
				// Following does not work correctly!
				//
				// It is suppose to add the Fast View menu item back to the menu but
				// when the menu is shown the menu item has no text but functions correctly.
				if( menu.getItemCount() != 2 ) {
					try {		
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						Field restoreItemField = window.getClass().getDeclaredField("restoreItem");
						restoreItemField.setAccessible(true);
						MenuItem restoreItem = (MenuItem)restoreItemField.get(window);
						
						Method method = menu.getClass().getDeclaredMethod("createItem", new Class[] { MenuItem.class, int.class });
						method.setAccessible(true);
						method.invoke(menu, new Object[] { restoreItem,  new Integer(1) });
					} catch(Exception exc) {
						exc.printStackTrace();
					}
				}
*/
			}
			public void menuShown(MenuEvent e) {
				//System.out.println("menuShown, widget:"+e.widget+"   source:"+e.getSource());
				Menu menu = (Menu)e.widget;
				ToolItem toolItem = (ToolItem)menu.getData();
				if( toolItem.getToolTipText() !=null &&
					toolItem.getToolTipText().equalsIgnoreCase(viewTitle) ) {
					MenuItem restoreItem = menu.getItem(1);
					restoreItem.setEnabled(false);

/*
					// Following does not work correctly!
					//
					// It is suppose to remove the Fast View menu item from the menu but
					// when the menu is updated to include the item again, it has no text
					// but functions correctly.
					if( menu.getItemCount() == 2 ) {
						MenuItem restoreItem = menu.getItem(1);
						restoreItem.setEnabled(false);

						try {
							Method method = menu.getClass().getDeclaredMethod("destroyItem", new Class[] { MenuItem.class });
							method.setAccessible(true);
							method.invoke(menu, new MenuItem[] { restoreItem });
						} catch(Exception exc) {
							exc.printStackTrace();
							restoreItem.setEnabled(false);
						}
					}
*/
				}
			}
		});

		try {		
			Field fastViewBarMenuField = window.getClass().getDeclaredField("fastViewBarMenu"); //$NON-NLS-1$
			fastViewBarMenuField.setAccessible(true);
			fastViewBarMenuField.set(window, fastViewBarMenu);

			Field restoreItemField = window.getClass().getDeclaredField("restoreItem"); //$NON-NLS-1$
			restoreItemField.setAccessible(true);
			restoreItemField.set(window, restoreItem);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Disables the dragging of the given view.
	 * 
	 * @param part - the view
	 * @param viewPane - the view's viewPane
	 */
	private void disableDragView(IWorkbenchPart part, ViewPane viewPane) {
		// This disables the view from being dragged. forcing the cheat sheet view
		// to remain a fastview.
		PerspectivePresentation perspectivePresentation = null;
		try {
			IWorkbenchPage page = part.getSite().getPage();
			Method method = page.getClass().getDeclaredMethod("getActivePerspective", null); //$NON-NLS-1$
			method.setAccessible(true);
			Perspective perspective = (Perspective)method.invoke(page, null);
			perspectivePresentation = perspective.getPresentation();
		} catch(Exception exc) {
			exc.printStackTrace();
		}
		
		if( perspectivePresentation != null ) {
			try {
				Method method = perspectivePresentation.getClass().getDeclaredMethod("disableDrag", new Class[] { ViewPane.class }); //$NON-NLS-1$
				method.setAccessible(true);
				method.invoke(perspectivePresentation, new ViewPane[] { viewPane });
			} catch(Exception exc) {
				exc.printStackTrace();
			}
		}
	}


	/**
	 * Ensures the shortcut bar icon for the given partpane has the correct selection.
	 * 
	 * @param partPane - the partpane to correct select/unselect in the shortcut bar
	 */
	private void ensureShortcutBarIconSelection(PartPane partPane) {
		// Ensure the selection for the shortcut bar icon for the cheat sheet fast view is always correct
		partPane.getControl().addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
				//System.out.println("controlMoved");
				//System.out.println(((ViewForm)e.widget).getBounds());
				if( ((ViewForm)e.widget).getBounds().x < 0 ) {
					setFastViewIconSelection(false);
				}
			}

			public void controlResized(ControlEvent e) {
/* The following is not needed after similar code added
 * to controlMoved method.
 */ 
//				//System.out.println("controlResized");
//
//				if( ((ViewForm)e.widget).getBounds().equals(zeroSizeRectangle) ) {
//					//System.out.println("cheat sheet minimized");
//					// Set the selection for the shortcut bar icon for the cheat sheet fast view
//					setFastViewIconSelection(false);
//				} else {
//					//System.out.println("cheat sheet NOT minimized");
//					// Set the selection for the shortcut bar icon for the cheat sheet fast view
//					setFastViewIconSelection(true);
//				}
			}
		});
	}


	/**
	 * Ensures the view is a fastview.
	 */
	private void ensureViewIsFastView() {
		// Ensure that the view is a fast view even when opened via the Window->Show View menu
		final WorkbenchPage realPage = (WorkbenchPage)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				
		final String viewId = view.getSite().getId();
		final IViewReference[] viewRefs = realPage.getViewReferences();
		IViewReference viewReference = null;
		for (int i = 0; i < viewRefs.length; i++) {
			IViewReference reference = viewRefs[i];
			if( reference.getId().equals(viewId) ) {
				viewReference = reference;
				break;
			}
		}

		final IViewReference viewRef = viewReference;
		if(view != null && !realPage.isFastView(viewRef) ) {
			Display.getCurrent().asyncExec(new Runnable() { 
				public void run() {
					// Double that view did not become a fast view while we were
					// waiting to execute.
					if( !realPage.isFastView(viewRef) ) {
						realPage.addFastView(viewRef);
						realPage.activate(view);
						
						FastViewHack.removeFastViewButton((ViewPane)((ViewSite) view.getSite()).getPane());
					}
				}
			});
		}
	}


	/**
	 * Remove the fastview popup menu item from the view's system pop-up menu.
	 * 
	 * @param partPane - the view to remove the menu item from
	 */
	private void removeFastViewPopupMenuItem(PartPane partPane) {
		// Remove the Fast View menu item from the cheat sheet view title bar pop-up menu
		try {
			Class[] innerClasses = PartPane.class.getDeclaredClasses();
			Class paneContributionClass = null;
			for(int i=0; i<innerClasses.length; ++i) {
				//System.out.println("inner class: "+innerClasses[i].getName());
				if( innerClasses[i].getName().equals("org.eclipse.ui.internal.PartPane$PaneContribution") ) { //$NON-NLS-1$
					paneContributionClass = innerClasses[i];
					break;
				}
			}
			
			if( paneContributionClass != null ) {
				Object paneContribution = null;
				try {
					Constructor constructor = paneContributionClass.getDeclaredConstructor(new Class[] { PartPane.class });
					constructor.setAccessible(true);
					paneContribution = constructor.newInstance(new Object[] { partPane });
				} catch(Exception exc) {
					exc.printStackTrace();
				}

				Field field = PartPane.class.getDeclaredField("paneMenuManager"); //$NON-NLS-1$
				field.setAccessible(true);
				MenuManager menuManager = new MenuManager();

				if( paneContribution != null ) {
					menuManager.add((IContributionItem)paneContribution);
					
					menuManager.add(new ContributionItem() {
						public boolean isDynamic() {
							return true;
						}
						public void fill(Menu menu, int index) {
							MenuItem[] items = menu.getItems();
							MenuItem item = null;
							for (int i = 0; i < items.length; i++) {
								if( items[i].getText().equals(fastViewMenuText) ) {
									item = items[i];
									break;
								}
							}
							
							if( item != null ) {
								try {
									Method method = menu.getClass().getDeclaredMethod("destroyItem", new Class[] { MenuItem.class }); //$NON-NLS-1$
									method.setAccessible(true);
									method.invoke(menu, new MenuItem[] { item });
								} catch(Exception exc) {
									// Throws an exception on Linux when trying to remove "Fast view" option
									// from view pop up menu. So if an exception occurs when trying to remove
									// the menu item just disable the menu item instead.
									item.setEnabled(false);
								}
							}
						}
					});
				}

				field.set(partPane, menuManager);
			}
		} catch(Exception exc) {
			exc.printStackTrace();
		}
	}


	/**
	 * Sets the selection for the shortcut bar icon representing the cheat sheet fast view.
	 */
	private void setFastViewIconSelection(boolean selected) {
		final boolean selection = selected;

		// Set the selection for the shortcut bar icon for the cheat sheet fast view
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				if(true) return;
				WorkbenchWindow window = (WorkbenchWindow)PlatformUI.getWorkbench().getActiveWorkbenchWindow();

				if( window == null ) {
					return;
				}

				try {
					Field field = window.getClass().getDeclaredField("shortcutBarPart"); //$NON-NLS-1$
					field.setAccessible(true);
//TODO: Port problem!					
//					ShortcutBarPart shortcutBarPart = (ShortcutBarPart)field.get(window);
//					
//					ToolBar toolBar = (ToolBar)shortcutBarPart.getControl();
					ToolBar toolBar = null;
					ToolItem[] toolItems = toolBar.getItems();
					for(int i=0; i<toolItems.length; ++i) {
						ToolItem toolItem = toolItems[i];
						//System.out.println("ShortcutBarPart item: "+toolItems[i].getToolTipText());
						if( toolItem.getToolTipText() !=null &&
							toolItem.getToolTipText().equalsIgnoreCase(viewTitle) ) {

							//System.out.println("Found ShortcutBarPart item for Cheat Sheets");
							toolItem.setSelection(selection);
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}

