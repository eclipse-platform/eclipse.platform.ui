/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.help;

import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

/**
 * Provides methods for accessing the help support system and for hooking
 * it in.
 * <p>
 * The help support system is optional, to allow some products to be configured
 * without one. The <code>getHelpSupport</code> method returns the help support
 * system if available.
 * </p>
 * <p>
 * The various <code>setHelp</code> methods allow help to be hooked in to SWT 
 * menus, menu items, and controls, and into JFace actions. This involves 
 * furnishing a help context id. When the user requests help for one of the 
 * established widgets (for instance, by hitting F1), the context id is retrieved
 * and passed to the help support system using <code>IHelp.displayHelp(helpContexts,position)</code>.
 * </p>
 * <p>
 * In cases more dynamic situations, clients may hook their own help listner then
 * call <code>displayHelp</code> with a context id or <code>IContext</code>.
 * </p>
 * <p>
 * This class provides static methods only; it is not intended to be instantiated
 * or subclassed.
 * </p>
 *
 * @see org.eclipse.help.IHelp
 * @see #getHelpSupport
  */
public class WorkbenchHelp {
	/**
	 * Key used for stashing help-related data on SWT widgets.
	 *
	 * @see org.eclipse.swt.Widget.getData(java.lang.String)
	 */	
	private static final String HELP_KEY = "org.eclipse.ui.help";//$NON-NLS-1$
	private static final String HELP_SYSTEM_EXTENSION_ID = "org.eclipse.help.support";//$NON-NLS-1$
	private static final String HELP_SYSTEM_CLASS_ATTRIBUTE = "class";//$NON-NLS-1$
	private static IHelp helpSupport;
	private static boolean isIntialized = false;
	private static HelpListener helpListener = null;
/**
 * This class is not intented to be instantiated
 */
private WorkbenchHelp() {
}
/**
 * Determines the location for the help popup shell given
 * the widget which orginated the request for help.
 *
 * @param display the display where the help will appear
 */
private static Point computePopUpLocation(Display display) {
	Point point = display.getCursorLocation();
	return new Point(point.x + 15, point.y);
}
/**
 * Calls the help support system to display the given help context
 *
 * @param helpContext the id of the context to display
 * @param point the location for the help popup
 */
private static void displayHelp(String helpContext, Point point) {
	IHelp helpSupport = getHelpSupport();
	if (helpSupport == null)
		return;

	helpSupport.displayContext(helpContext, point.x, point.y);
}
/**
 * Calls the help support system to display the given help context
 *
 * @param helpContext the context to display
 * @param point the location for the help popup
 */
private static void displayHelp(IContext helpContext, Point point) {
	IHelp helpSupport = getHelpSupport();
	if (helpSupport == null)
		return;

	helpSupport.displayContext(helpContext, point.x, point.y);
}
/**
 * Calls the help support system to display the given help context id.
 * <p>
 * May only be called from a UI thread.
 * <p>
 *
 * @param contextId the id of the context to display
 * @since 2.0
 */
public static void displayHelp(String contextId) {
	Point point = computePopUpLocation(Display.getCurrent());
	
	displayHelp(contextId, point);
}
/**
 * Calls the help support system to display the given help context.
 * <p>
 * May only be called from a UI thread.
 * <p>
 *
 * @param context the context to display
 * @since 2.0
 */
public static void displayHelp(IContext context) {
	Point point = computePopUpLocation(Display.getCurrent());
	
	displayHelp(context, point);
}
/**
 * Returns the help contexts on the given control.
 * <p>
 * Instances of <code>IContextComputer</code> may use this method
 * to obtain the prevviously registered help contexts of a control.
 * </p>
 *
 * @param control the control on which the contexts are registered
 * @return contexts the contexts to use when F1 help is invoked; a mixed-type
 *   array of context ids (type <code>String</code>) and/or help contexts (type
 *   <code>IContext</code>) or an <code>IContextComputer</code> or
 *   <code>null</code> if no contexts have been set.
 * @deprecated as context computers are no longer supported
 */
public static Object getHelp(Control control) {
	return control.getData(HELP_KEY);
}
/**
 * Returns the help contexts on the given menu.
 * <p>
 * Instances of <code>IContextComputer</code> may use this method
 * to obtain the prevviously registered help contexts of a menu.
 * </p>
 *
 * @param menu the menu on which the contexts are registered
 * @return contexts the contexts to use when F1 help is invoked; a mixed-type
 *   array of context ids (type <code>String</code>) and/or help contexts (type
 *   <code>IContext</code>) or an <code>IContextComputer</code> or
 *   <code>null</code> if no contexts have been set.
 * @deprecated as context computers are no longer supported
 */
public static Object getHelp(Menu menu) {
	return menu.getData(HELP_KEY);
}
/**
 * Returns the help contexts on the given menu item.
 * <p>
 * Instances of <code>IContextComputer</code> may use this method
 * to obtain the prevviously registered help contexts of a menu.
 * </p>
 *
 * @param menuItem the menu item on which the contexts are registered
 * @return contexts the contexts to use when F1 help is invoked; a mixed-type
 *   array of context ids (type <code>String</code>) and/or help contexts (type
 *   <code>IContext</code>) or an <code>IContextComputer</code> or
 *   <code>null</code> if no contexts have been set.
 * @deprecated as context computers are no longer supported
 */
public static Object getHelp(MenuItem menuItem) {
	return menuItem.getData(HELP_KEY);
}
/**
 * Returns the help listener which activates the help support system.
 *
 * @return the help listener
 */
private static HelpListener getHelpListener() {
	if (helpListener == null)
		initializeHelpListener();
	return helpListener;
}
/**
 * Returns the help support system for the platform, if available.
 *
 * @return the help support system, or <code>null</code> if none
 */
public static IHelp getHelpSupport() {
	if (!isIntialized) {
		isIntialized = true;
		initializeHelpSupport();
	}
	return helpSupport;
}

/**
 * Initializes the help listener.
 */
private static void initializeHelpListener() {
	helpListener = new HelpListener() {
		public void helpRequested(HelpEvent event) {
			if (getHelpSupport() == null)
				return;
			
			// get the help context from the widget
			Object object = event.widget.getData(HELP_KEY);

			// Since 2.0 we can expect that object is a String, however
			// for backward compatability we handle context computers and arrays.
			
			if (object instanceof String) {
				// determine a location in the upper right corner of the widget
				Point point = computePopUpLocation(event.widget.getDisplay());
				
				// display the help
				displayHelp((String)object, point);
				
				return;
			}
			
			
			Object[] helpContext = null;
			if (object instanceof IContextComputer) 
				// if it is a computed context, compute it now
				helpContext = ((IContextComputer)object).computeContexts(event);
			else if (object instanceof Object[])
				helpContext = (Object[])object;

			if (helpContext != null) {	
				// determine a location in the upper right corner of the widget
				Point point = computePopUpLocation(event.widget.getDisplay());
				
				// display the help
				displayHelp(helpContext, point);
			}
		}
	};
}

/**
 * Calls the help support system to display the given help context
 * 
 * @param helpContexts the contexts to display a mixed-type
 *   array of context ids (type <code>String</code>) and/or help contexts (type
 *   <code>IContext</code>)
 * @param point the location for point to help popup
 * @deprecated
 */
private static void displayHelp(Object[] helpContexts, Point point) {
	if (getHelpSupport() == null)
		return;
	
	// Since 2.0 the help support system no longer provides
	// API for an array of help contexts.
	// Therefore we only use the first context in the array.
	if (helpContexts[0] instanceof IContext) 
		getHelpSupport().displayContext((IContext)helpContexts[0], point.x, point.y);
	else
		getHelpSupport().displayContext((String)helpContexts[0], point.x, point.y);
}
/**
 * Initializes the help support system by getting an instance via the extension
 * point.
 */
private static void initializeHelpSupport() {
	BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
		public void run() {
			// get the help support extension from the system plugin registry	
			IPluginRegistry pluginRegistry = Platform.getPluginRegistry();
			IExtensionPoint point = pluginRegistry.getExtensionPoint(HELP_SYSTEM_EXTENSION_ID);
			if (point == null) return;
			IExtension[] extensions = point.getExtensions();
			if (extensions.length == 0) return;
			// There should only be one extension/config element so we just take the first
			IConfigurationElement[] elements = extensions[0].getConfigurationElements();
			if (elements.length == 0) return;
			// Instantiate the help support system
			try {
				helpSupport = (IHelp)WorkbenchPlugin.createExtension(elements[0],
					HELP_SYSTEM_CLASS_ATTRIBUTE);
			} catch (CoreException e) {
				WorkbenchPlugin.log("Unable to instantiate help support system" + e.getStatus());//$NON-NLS-1$
			}
		}
	});
}

/**
 * Returns <code>true</code> if the context-sensitive help
 * window is currently being displayed, <code>false</code> if not.
 * This avoid activating the help support if it is not already activated.
 */
public static boolean isContextHelpDisplayed() {
	return helpSupport != null && helpSupport.isContextHelpDisplayed();
}

/**
 * Sets the given help contexts on the given action.
 * <p>
 * Use this method when the list of help contexts is known in advance.
 * Help contexts can either supplied as a static list, or calculated with a
 * context computer (but not both).
 * </p>
 *
 * @param action the action on which to register the computer
 * @param contexts the contexts to use when F1 help is invoked; a mixed-type
 *   array of context ids (type <code>String</code>) and/or help contexts (type
 *   <code>IContext</code>)
 * @deprecated use setHelp with a single context id parameter
 */
public static void setHelp(IAction action, final Object[] contexts) {
	for (int i = 0; i < contexts.length; i++)
		Assert.isTrue(contexts[i] instanceof String || contexts[i] instanceof IContext);
	action.setHelpListener(new HelpListener() {
		public void helpRequested(HelpEvent event) {
			if (getHelpSupport() != null) {
				// determine a location in the upper right corner of the widget
				Point point = computePopUpLocation(event.widget.getDisplay());
	
				// display the help	
				displayHelp(contexts, point);
			}
		}
	});
}

/**
 * Sets the given help context computer on the given action.
 * <p>
 * Use this method when the help contexts cannot be computed in advance.
 * Help contexts can either supplied as a static list, or calculated with a
 * context computer (but not both).
 * </p>
 *
 * @param action the action on which to register the computer
 * @param computer the computer to determine the help contexts for the control
 *    when F1 help is invoked
 * @deprecated context computers are no longer supported, clients should implement
 *  their own help listener
 */
public static void setHelp(IAction action, final IContextComputer computer) {
	action.setHelpListener(new HelpListener() {
		public void helpRequested(HelpEvent event) {
			Object[] helpContext = computer.computeContexts(event);
			if (helpContext != null && getHelpSupport() != null) {
				// determine a location in the upper right corner of the widget
				Point point = computePopUpLocation(event.widget.getDisplay());
	
				// display the help	
				displayHelp(helpContext, point);
			}
		}
	});
}
/**
 * Sets the given help contexts on the given control.
 * <p>
 * Use this method when the list of help contexts is known in advance.
 * Help contexts can either supplied as a static list, or calculated with a
 * context computer (but not both).
 * </p>
 *
 * @param control the control on which to register the contexts
 * @param contexts the contexts to use when F1 help is invoked; a mixed-type
 *   array of context ids (type <code>String</code>) and/or help contexts (type
 *   <code>IContext</code>)
 * @deprecated use setHelp with single context id parameter
 */
public static void setHelp(Control control, Object[] contexts) {
	for (int i = 0; i < contexts.length; i++)
		Assert.isTrue(contexts[i] instanceof String || contexts[i] instanceof IContext);
	
	control.setData(HELP_KEY, contexts);
	// ensure that the listener is only registered once
	control.removeHelpListener(getHelpListener());
	control.addHelpListener(getHelpListener());
}
/**
 * Sets the given help context computer on the given control.
 * <p>
 * Use this method when the help contexts cannot be computed in advance.
 * Help contexts can either supplied as a static list, or calculated with a
 * context computer (but not both).
 * </p>
 *
 * @param control the control on which to register the computer
 * @param computer the computer to determine the help contexts for the control
 *    when F1 help is invoked
 * @deprecated context computers are no longer supported, clients should implement
 *  their own help listener
 */
public static void setHelp(Control control, IContextComputer computer) {
	control.setData(HELP_KEY, computer);
	// ensure that the listener is only registered once
	control.removeHelpListener(getHelpListener());
	control.addHelpListener(getHelpListener());
}
/**
 * Sets the given help contexts on the given menu.
 * <p>
 * Use this method when the list of help contexts is known in advance.
 * Help contexts can either supplied as a static list, or calculated with a
 * context computer (but not both).
 * </p>
 *
 * @param menu the menu on which to register the context
 * @param contexts the contexts to use when F1 help is invoked; a mixed-type
 *   array of context ids (type <code>String</code>) and/or help contexts (type
 *   <code>IContext</code>)
 * @deprecated use setHelp with single context id parameter
 */
public static void setHelp(Menu menu, Object[] contexts) {
	for (int i = 0; i < contexts.length; i++)
		Assert.isTrue(contexts[i] instanceof String || contexts[i] instanceof IContext);
	menu.setData(HELP_KEY, contexts);
	// ensure that the listener is only registered once
	menu.removeHelpListener(getHelpListener());
	menu.addHelpListener(getHelpListener());
}
/**
 * Sets the given help context computer on the given menu.
 * <p>
 * Use this method when the help contexts cannot be computed in advance.
 * Help contexts can either supplied as a static list, or calculated with a
 * context computer (but not both).
 * </p>
 *
 * @param menu the menu on which to register the computer
 * @param computer the computer to determine the help contexts for the control
 *    when F1 help is invoked
 * @deprecated context computers are no longer supported, clients should implement
 *  their own help listener
 */
public static void setHelp(Menu menu, IContextComputer computer) {
	menu.setData(HELP_KEY, computer);
	// ensure that the listener is only registered once
	menu.removeHelpListener(getHelpListener());
	menu.addHelpListener(getHelpListener());
}
/**
 * Sets the given help contexts on the given menu item.
 * <p>
 * Use this method when the list of help contexts is known in advance.
 * Help contexts can either supplied as a static list, or calculated with a
 * context computer (but not both).
 * </p>
 *
 * @param item the menu item on which to register the context
 * @param contexts the contexts to use when F1 help is invoked; a mixed-type
 *   array of context ids (type <code>String</code>) and/or help contexts (type
 *   <code>IContext</code>)
 * @deprecated use setHelp with single context id parameter
 */
public static void setHelp(MenuItem item, Object[] contexts) {
	for (int i = 0; i < contexts.length; i++)
		Assert.isTrue(contexts[i] instanceof String || contexts[i] instanceof IContext);
	item.setData(HELP_KEY, contexts);
	// ensure that the listener is only registered once
	item.removeHelpListener(getHelpListener());
	item.addHelpListener(getHelpListener());
}
/**
 * Sets the given help context computer on the given menu item.
 * <p>
 * Use this method when the help contexts cannot be computed in advance.
 * Help contexts can either supplied as a static list, or calculated with a
 * context computer (but not both).
 * </p>
 *
 * @param item the menu item on which to register the computer
 * @param computer the computer to determine the help contexts for the control
 *    when F1 help is invoked
 * @deprecated context computers are no longer supported, clients should implement
 *  their own help listener
 */
public static void setHelp(MenuItem item, IContextComputer computer) {
	item.setData(HELP_KEY, computer);
	// ensure that the listener is only registered once
	item.removeHelpListener(getHelpListener());
	item.addHelpListener(getHelpListener());
}
/**
 * Sets the given help context id on the given action.
 *
 * @param action the action on which to register the context id
 * @param contextId the context id to use when F1 help is invoked
 * @since 2.0
 */
public static void setHelp(IAction action, final String contextId) {
	action.setHelpListener(new HelpListener() {
		public void helpRequested(HelpEvent event) {
			if (getHelpSupport() != null) {
				// determine a location in the upper right corner of the widget
				Point point = computePopUpLocation(event.widget.getDisplay());
	
				// display the help	
				displayHelp(contextId, point);
			}
		}
	});
}
/**
 * Sets the given help context id on the given control.
 *
 * @param control the control on which to register the context id
 * @param contextId the context id to use when F1 help is invoked
 * @since 2.0
 */
public static void setHelp(Control control, String contextId) {
	control.setData(HELP_KEY, contextId);
	// ensure that the listener is only registered once
	control.removeHelpListener(getHelpListener());
	control.addHelpListener(getHelpListener());
}
/**
 * Sets the given help context id on the given menu.
 *
 * @param menu the menu on which to register the context id
 * @param contextId the context id to use when F1 help is invoked
 * @since 2.0
 */
public static void setHelp(Menu menu, String contextId) {
	menu.setData(HELP_KEY, contextId);
	// ensure that the listener is only registered once
	menu.removeHelpListener(getHelpListener());
	menu.addHelpListener(getHelpListener());
}
/**
 * Sets the given help context id on the given menu item.
 *
 * @param item the menu item on which to register the context id
 * @param contextId the context id to use when F1 help is invoked
 * @since 2.0
 */
public static void setHelp(MenuItem item, String contextId) {
	item.setData(HELP_KEY, contextId);
	// ensure that the listener is only registered once
	item.removeHelpListener(getHelpListener());
	item.addHelpListener(getHelpListener());
}
}
