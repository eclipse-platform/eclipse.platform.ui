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

import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;

import org.eclipse.ui.internal.cheatsheets.*;

/**
 * 
 * A class to make a view a fast view only and follow the user as they switch perspectives.
 *
 */
/* package */ class FastViewHack {

	/**
	 * Ensure the given view is a fast view only and follows the
	 * user as they switch perspectives.
	 * 
	 * @param aView - the view to ensure is a following fast view only
	 */
	/* package */
	static final void enableFollowingFastViewOnly(IViewPart aView) {
		//System.out.println("enableFollowingFastViewOnly");
		FastViewHackPerspectiveListener listener = new FastViewHackPerspectiveListener(aView);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(listener);

		FastViewHackPartListener partListener = new FastViewHackPartListener(aView, listener);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(partListener);
	}


	/**
	 * Disables dragging of the shortcut bar icon for the cheat sheet fast view.
	 */
	/* package */
	static final void disableDragShortcutBarIcon() {
		// Disable dragging of the shortcut bar icon for the cheat sheet fast view
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				if(true) return;
				WorkbenchWindow window = (WorkbenchWindow)PlatformUI.getWorkbench().getActiveWorkbenchWindow();

				if( window == null ) {
					return;
				}

				try {
					String viewTitle = CheatSheetPlugin.getResourceString(ICheatSheetResource.CHEAT_SHEETS);

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
							toolItem.setData(ShowFastViewContribution.FAST_VIEW, null);
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	/**
	 * Removes the fastview button from the view's title bar.
	 * 
	 * @param viewPane - the view to remove the button from
	 */
	/* package */
	static void removeFastViewButton(ViewPane viewPane) {
		String fastViewToolTip = WorkbenchMessages.getString("ViewPane.pin"); //$NON-NLS-1$
		String fastViewDisabledToolTip = CheatSheetPlugin.getResourceString(ICheatSheetResource.FASTVIEW_DISABLED_FOR_CHEAT_SHEET);


		// Remove the Fast View push pin toolbar item from the cheat sheet view
		ViewForm viewForm = (ViewForm)viewPane.getControl();
		Control[] controls = viewForm.getChildren();
		boolean done = false;
		for(int i=0; i<controls.length && !done; ++i) {
			Control control = controls[i];

			if( control instanceof ToolBar ) {
				ToolBar toolBar = (ToolBar)control;

				ToolItem[] toolItems = toolBar.getItems();
				for(int j=0; j<toolItems.length; ++j) {
					ToolItem toolItem = toolItems[j];
					//System.out.println(toolItem.getToolTipText());
					if( toolItem.getToolTipText() !=null &&
						toolItem.getToolTipText().equalsIgnoreCase(fastViewToolTip) ) {
						//System.out.println("Found push pin ToolBar item for Fast View");
						
						try {
							Method method = toolBar.getClass().getDeclaredMethod("destroyItem", new Class[] { ToolItem.class }); //$NON-NLS-1$
							method.setAccessible(true);
							method.invoke(toolBar, new ToolItem[] { toolItem });
						} catch(Exception exc) {
							// Throws an exception on Linux when trying to remove "Fast view" button
							// from view toolbar. So if an exception occurs when trying to remove
							// the button just disable the button instead.
							toolItem.setToolTipText(fastViewDisabledToolTip);
							toolItem.setEnabled(false);
						}
						done = true;
						
						toolBar.getParent().layout(true);
						break;
					}
				}
			}
		}
	}


	/**
	 * Helper method used to debug methods available on a given object.
	 * @param object - the object to inspect
	 */
	/* package */
/*
	static final void debugMethods(Object object) {
		Method[] methods = object.getClass().getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			System.out.println("Method: " + method.getName());
			Class[] params = method.getParameterTypes();
			for (int j = 0; j < params.length; j++) {
				Class param = params[j];
				System.out.println("Param: " + param.getName());
			}
		}
	}	
*/

	/**
	 * Helper method used to debug fields available on a given object.
	 * @param object - the object to inspect
	 */
	/* package */
/*
	static final void debugFields(Object object) {
		Field[] fields = object.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			System.out.println("Field: " + field.getName());
			Class type = field.getType();
			System.out.println("Type: " + type.getName());
		}
	}	
*/
}
