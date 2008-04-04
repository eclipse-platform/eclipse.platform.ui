/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.help;

import org.eclipse.help.IContext;
import org.eclipse.help.IHelp;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.internal.help.WorkbenchHelpSystem;

/**
 * Provides methods for accessing the help UI.
 * <p>
 * The help UI is optional, to allow applications to be configured
 * without one.
 * </p>
 * <p>
 * The various <code>setHelp</code> methods allow context help to be hooked in
 * to SWT menus, menu items, and controls, and into JFace actions. This involves 
 * furnishing a help context id. When the user requests help for one of the 
 * established widgets (for instance, by hitting F1), the context id is
 * retrieved and passed to the help UI using 
 * <code>WorkbenchHelp.displayContext(helpContext, xposition, yposition)</code>.
 * </p>
 * <p>
 * In cases more dynamic situations, clients may hook their own help listener
 * and call <code>WorkbenchHelp.displayContext</code> with an 
 * <code>IContext</code>.
 * </p>
 * <p>
 * This class provides static methods only; it is not intended to be instantiated
 * or subclassed.
 * </p>
 *
 * @deprecated clients should use <code>IWorkbenchHelpSystem</code> instead
 * 
 * @see org.eclipse.help.HelpSystem
 * @see org.eclipse.ui.help.IWorkbenchHelpSystem
 * @see org.eclipse.ui.IWorkbench#getHelpSystem()
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class WorkbenchHelp {

    /**
     * This class is not intented to be instantiated
     */
    private WorkbenchHelp() {
    }

    /**
     * Displays the entire help bookshelf.
     * <p>
     * Ignored if no help UI is available.
     * </p>
     * 
     * @since 3.0
     */
    public static void displayHelp() {
    	PlatformUI.getWorkbench().getHelpSystem().displayHelp();
    }

    /**
     * Displays context-sensitive help for the given context.
     * <p>
     * (x,y) coordinates specify the location where the context sensitive 
     * help UI will be presented. These coordinates are screen-relative 
     * (ie: (0,0) is the top left-most screen corner).
     * The platform is responsible for calling this method and supplying the 
     * appropriate location.
     * </p>
     * <p>
     * Ignored if no help UI is available.
     * </p>
     * 
     * @param context the context to display
     * @param x horizontal position
     * @param y verifical position
     * @since 3.0
     */
    public static void displayContext(IContext context, int x, int y) {
    	PlatformUI.getWorkbench().getHelpSystem().displayContext(context, x, y);
    }

    /**
     * Displays help content for the help resource with the given URL.
     * <p>
     * This method is called by the platform to launch the help system UI, displaying
     * the documentation identified by the <code>href</code> parameter.
     * </p> 
     * <p>
     * The help system makes no guarantee that all the help resources can be displayed or how they are displayed.
     * </p>
     * <p>
     * Ignored if no help UI is available.
     * </p>
     * 
     * @param href the URL of the help resource.
     * <p>Valid href are as described in 
     * 	{@link  org.eclipse.help.IHelpResource#getHref() IHelpResource.getHref()}
     * </p>
     * @since 3.0
     */
    public static void displayHelpResource(String href) {
    	PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(href);
    }

    /**
     * Creates a new help listener for the given command. This retrieves the
     * help context ID from the command, and creates an appropriate listener
     * based on this.
     * 
     * @param command
     *            The command for which the listener should be created; must
     *            not be <code>null</code>.
     * @return A help listener; never <code>null</code>.
     */
    public static HelpListener createHelpListener(ICommand command) {
    	return WorkbenchHelpSystem.getInstance().createHelpListener(command);
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
    	PlatformUI.getWorkbench().getHelpSystem().displayHelp(contextId);    	
    }

    /**
     * Displays context-sensitive help for the given context.
     * <p>
     * May only be called from a UI thread.
     * <p>
     *
     * @param context the context to display
     * @since 2.0
     */
    public static void displayHelp(IContext context) {
    	PlatformUI.getWorkbench().getHelpSystem().displayHelp(context);
    }

    /**
     * Returns the help contexts on the given control.
     * <p>
     * Instances of <code>IContextComputer</code> may use this method
     * to obtain the previously registered help contexts of a control.
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
        return control.getData(WorkbenchHelpSystem.HELP_KEY);
    }

    /**
     * Returns the help contexts on the given menu.
     * <p>
     * Instances of <code>IContextComputer</code> may use this method
     * to obtain the previously registered help contexts of a menu.
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
        return menu.getData(WorkbenchHelpSystem.HELP_KEY);
    }

    /**
     * Returns the help contexts on the given menu item.
     * <p>
     * Instances of <code>IContextComputer</code> may use this method
     * to obtain the previously registered help contexts of a menu.
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
        return menuItem.getData(WorkbenchHelpSystem.HELP_KEY);
    }

    /**
     * Returns the help support system for the platform, if available.
     *
     * @return the help support system, or <code>null</code> if none
     * @deprecated Use the static methods on this class and on
     * {@link org.eclipse.help.HelpSystem HelpSystem} instead of the IHelp methods
     * on the object returned by this method.
     */
    public static IHelp getHelpSupport() {
    	return WorkbenchHelpSystem.getInstance().getHelpSupport();
    }

    
    /**
     * Returns whether the context-sensitive help window is currently being
     * displayed. Returns <code>false</code> if the help UI has not been
     * activated yet.
     * 
     * @return <code>true</code> if the context-sensitive help
     * window is currently being displayed, <code>false</code> otherwise
     */
    public static boolean isContextHelpDisplayed() {
    	return PlatformUI.getWorkbench().getHelpSystem().isContextHelpDisplayed();
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
    	WorkbenchHelpSystem.getInstance().setHelp(action, contexts);
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
    	WorkbenchHelpSystem.getInstance().setHelp(action, computer);
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
    	WorkbenchHelpSystem.getInstance().setHelp(control, contexts);
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
    	WorkbenchHelpSystem.getInstance().setHelp(control, computer);
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
    	WorkbenchHelpSystem.getInstance().setHelp(menu, contexts);
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
    	WorkbenchHelpSystem.getInstance().setHelp(menu, computer);
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
    	WorkbenchHelpSystem.getInstance().setHelp(item, contexts);
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
    	WorkbenchHelpSystem.getInstance().setHelp(item, computer);
    }

    /**
     * Sets the given help context id on the given action.
     *
     * @param action the action on which to register the context id
     * @param contextId the context id to use when F1 help is invoked
     * @since 2.0
     */
    public static void setHelp(IAction action, final String contextId) {
    	PlatformUI.getWorkbench().getHelpSystem().setHelp(action, contextId);
    }

    /**
     * Sets the given help context id on the given control.
     *
     * @param control the control on which to register the context id
     * @param contextId the context id to use when F1 help is invoked
     * @since 2.0
     */
    public static void setHelp(Control control, String contextId) {
    	PlatformUI.getWorkbench().getHelpSystem().setHelp(control, contextId);    	
    }

    /**
     * Sets the given help context id on the given menu.
     *
     * @param menu the menu on which to register the context id
     * @param contextId the context id to use when F1 help is invoked
     * @since 2.0
     */
    public static void setHelp(Menu menu, String contextId) {
    	PlatformUI.getWorkbench().getHelpSystem().setHelp(menu, contextId);
    }

    /**
     * Sets the given help context id on the given menu item.
     *
     * @param item the menu item on which to register the context id
     * @param contextId the context id to use when F1 help is invoked
     * @since 2.0
     */
    public static void setHelp(MenuItem item, String contextId) {
    	PlatformUI.getWorkbench().getHelpSystem().setHelp(item, contextId);
    }
}
