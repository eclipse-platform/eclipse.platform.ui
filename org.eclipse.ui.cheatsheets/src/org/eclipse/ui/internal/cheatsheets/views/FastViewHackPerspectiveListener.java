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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;

import org.eclipse.ui.internal.cheatsheets.*;

/**
 * A class to handle perspective lifecycle events.
 * <p>
 * The following items are handled by the listener when
 * various perspective events occur:
 * <ul>
 *   <li>ensure the view is available within the current perspective
 *   </li>
 *   <li>removes the view in all perspectives except for the current one
 *   </li>
 *   <li>disable dragging of the shortcut bar icon
 *   </li>
 * </ul>
 */
/* package */
class FastViewHackPerspectiveListener implements IPerspectiveListener {
	private IViewPart view;

	/**
	 * Creates a new FastViewHackPerspectiveListener for the given view.
	 * 
	 * @param aView - the view to handle when perspective events occur
	 */
	/* package */
	FastViewHackPerspectiveListener(IViewPart aView) {
		//System.out.println("FastViewHackPerspectiveListener");
		view = aView;
	}

	/**
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(IWorkbenchPage, IPerspectiveDescriptor)
	 */
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		//System.out.print("perspectiveActivated: " + page.getLabel());
		//System.out.println(", " + perspective.getLabel());

		ensureViewIsAvailable(page, view, false);

		closeViewInPreviousPerspectives(page);

		updateViewToolBar(view);
	}

	/**
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(IWorkbenchPage, IPerspectiveDescriptor, String)
	 */
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
		//System.out.print("perspectiveChanged: " + page.getLabel());
		//System.out.print(", " + perspective.getLabel());
		//System.out.println(", " + changeId);

		if( changeId.equals(IWorkbenchPage.CHANGE_FAST_VIEW_REMOVE) ||
			changeId.equals(IWorkbenchPage.CHANGE_FAST_VIEW_ADD) ) {
			
			// Disable dragging of the shortcut bar icon for the cheat sheet fast view
			FastViewHack.disableDragShortcutBarIcon();
		}

	}


	/**
	 * 
	 * This closes the cheat view in all perspectives except for the current one,
	 * forcing there to be only one view at a time. Thus when the cheat sheet
	 * view is closed by the user there will be no more view references remaining
	 * and the view is closed.
	 * 
	 * @param page - the current workbenchpage
	 */
	private void closeViewInPreviousPerspectives(IWorkbenchPage page) {
		IWorkbenchWindow workbenchWindow = page.getWorkbenchWindow();
		if (workbenchWindow != null) {
			IWorkbenchPage[] pages = workbenchWindow.getPages();
			for (int j = 0; j < pages.length; ++j) {
				IWorkbenchPage currentPage = pages[j];

				// This closes the cheat view in all perspectives except for the current one,
				// forcing there to be only one view at a time. Thus when the cheat sheet
				// view is closed by the user there will be no more view references remaining
				// and the view is closed.
				Perspective activePerspective = null;
				try {
					Method method = currentPage.getClass().getDeclaredMethod("getActivePerspective", null); //$NON-NLS-1$
					method.setAccessible(true);
					activePerspective = (Perspective)method.invoke(page, null);
				} catch(Exception exc) {
					exc.printStackTrace();
				}
				
				if( activePerspective != null ) {
					Object perspectiveList = null;
					try {
						Field field = currentPage.getClass().getDeclaredField("perspList"); //$NON-NLS-1$
						field.setAccessible(true);
						perspectiveList = field.get(page);
					} catch(Exception exc) {
						exc.printStackTrace();
					}

					Perspective[] openPerspectives = null;
					try {
						Method method = perspectiveList.getClass().getDeclaredMethod("getOpenedPerspectives", null); //$NON-NLS-1$
						method.setAccessible(true);
						openPerspectives = (Perspective[])method.invoke(perspectiveList, null);
					} catch(Exception exc) {
						exc.printStackTrace();
					}

					if ( openPerspectives != null ) {
						for (int i = 0; i < openPerspectives.length; i++) {
							Perspective current = openPerspectives[i];
							if( current != activePerspective && activePerspective.containsView(view) ) {
								try {
									IViewReference viewRef = activePerspective.findView(view.getViewSite().getId());
									current.hideView(viewRef);
								} catch(Exception exc) {
									// Not much we can do, this exception occurs each time
									// we start the workbench with the cheat view still open
									// and multiple perspectives
									//exc.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
	}


	/**
	 * Ensures that given view is available within the current perspective.
	 * 
	 * @param page - the page to make the view available on
	 * @param view - the view to make available
	 * @param activate - true if the view should be activiated, and false otherwise 
	 */
	private void ensureViewIsAvailable(IWorkbenchPage page, IViewPart view, boolean activate) {
		//System.out.println("ensureViewIsAvailable");
		WorkbenchPage realPage = (WorkbenchPage)page;
		IViewPart viewPart = page.findView(view.getSite().getId());
		
		if(viewPart == null) {
			//the view doesn't exist in this perspective, so add it as a fast view.
			try {
				final String viewId = view.getSite().getId();
				viewPart = realPage.showView(viewId);
				
				final IViewReference[] viewRefs = realPage.getViewReferences();
				IViewReference viewRef = null;
				for (int i = 0; i < viewRefs.length; i++) {
					IViewReference reference = viewRefs[i];
					if( reference.getId().equals(viewId) ) {
						viewRef = reference;
						break;
					}
				}

				if( viewRef != null ) {
					//ensure that it's a fast view.
					//This only works on a WorkbenchPage.  It doesn't work on IWorkbenchPage.
					realPage.addFastView(viewRef);
				}
			} catch(PartInitException e) {
				MessageDialog.openError(realPage.getWorkbenchWindow().getShell(),
							CheatSheetPlugin.getResourceString(ICheatSheetResource.FASTVIEW_ONLY_ERROR),
							CheatSheetPlugin.getResourceString(ICheatSheetResource.FASTVIEW_ONLY_ERROR_MESSAGE) + e.getMessage());
				return;
			}
		}
		
		if(activate) {
			realPage.activate(viewPart);
		}
	}


	/**
	 * Update the given view's toolbar so the minimize button is available and
	 * the dock button is removed.
	 * 
	 * @param view - the view to have its toolbar updated
	 */
	private void updateViewToolBar(IViewPart view) {
		//System.out.println("updateViewToolBar");
		ViewSite viewSite = (ViewSite)view.getSite();
		ViewPane viewPane = (ViewPane)viewSite.getPane();
		viewPane.setFast(true);
		viewPane.getMenuManager().update(true);
		FastViewHack.removeFastViewButton(viewPane);
	}

}
