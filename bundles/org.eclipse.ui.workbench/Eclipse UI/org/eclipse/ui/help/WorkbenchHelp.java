/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.help;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IHelp;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.IToc;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.internal.WorkbenchPlugin;

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
 * @see org.eclipse.help.HelpSystem
 */
public class WorkbenchHelp {
    /**
     * Key used for stashing help-related data on SWT widgets.
     *
     * @see org.eclipse.swt.Widget.getData(java.lang.String)
     */
    private static final String HELP_KEY = "org.eclipse.ui.help";//$NON-NLS-1$

    /**
     * Id of extension point where the help UI is contributed.
     */
    private static final String HELP_SYSTEM_EXTENSION_ID = "org.eclipse.ui.helpSupport";//$NON-NLS-1$

    /**
     * Attribute id for class attribute of help UI extension point.
     */
    private static final String HELP_SYSTEM_CLASS_ATTRIBUTE = "class";//$NON-NLS-1$

    /**
     * Pluggable help UI, or <code>null</code> if none (or unknown).
     */
    private static AbstractHelpUI pluggableHelpUI = null;

    /**
     * Compatibility implementation of old IHelp interface.
     * WorkbenchHelp.getHelpSupport and IHelp were deprecated in 3.0.
     */
    private static class CompatibilityIHelpImplementation implements IHelp {

        /** @deprecated */
        public void displayHelp() {
            // real method - forward to help UI if available
            AbstractHelpUI helpUI = getHelpUI();
            if (helpUI != null) {
                helpUI.displayHelp();
            }
        }

        /** @deprecated */
        public void displayContext(IContext context, int x, int y) {
            // real method - forward to help UI if available
            AbstractHelpUI helpUI = getHelpUI();
            if (helpUI != null) {
                helpUI.displayContext(context, x, y);
            }
        }

        /** @deprecated */
        public void displayContext(String contextId, int x, int y) {
            // convenience method - funnel through the real method
            IContext context = HelpSystem.getContext(contextId);
            if (context != null) {
                displayContext(context, x, y);
            }
        }

        /** @deprecated */
        public void displayHelpResource(String href) {
            // real method - forward to help UI if available
            AbstractHelpUI helpUI = getHelpUI();
            if (helpUI != null) {
                helpUI.displayHelpResource(href);
            }
        }

        /** @deprecated */
        public void displayHelpResource(IHelpResource helpResource) {
            // convenience method - funnel through the real method
            displayHelpResource(helpResource.getHref());
        }

        /** @deprecated */
        public void displayHelp(String toc) {
            // deprecated method - funnel through the real method
            displayHelpResource(toc);
        }

        /** @deprecated */
        public void displayHelp(String toc, String selectedTopic) {
            // deprecated method - funnel through the real method
            displayHelpResource(selectedTopic);
        }

        /** @deprecated */
        public void displayHelp(String contextId, int x, int y) {
            // deprecated method - funnel through the real method
            displayContext(contextId, x, y);
        }

        /** @deprecated */
        public void displayHelp(IContext context, int x, int y) {
            // deprecated method - funnel through the real method
            displayContext(context, x, y);
        }

        /** @deprecated */
        public IContext getContext(String contextId) {
            // non-UI method - forward to HelpSystem
            return HelpSystem.getContext(contextId);
        }

        /** @deprecated */
        public IToc[] getTocs() {
            // non-UI method - forward to HelpSystem
            return HelpSystem.getTocs();
        }

        /** @deprecated */
        public boolean isContextHelpDisplayed() {
            // real method - forward to pluggedhelp UI
            return WorkbenchHelp.isContextHelpDisplayed();
        }
    }

    /**
     * Compatibility wrapper, or <code>null</code> if none.
     * Do not access directly; see getHelpSupport().
     */
    private static IHelp helpCompatibilityWrapper = null;

    /**
     * Indicates whether the pluggable help UI has been discovered and
     * initialized.
     */
    private static boolean isInitialized = false;

    /**
     * Internal help listener; lazily initialized.
     */
    private static HelpListener helpListener = null;

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
        AbstractHelpUI helpUI = getHelpUI();
        if (helpUI != null) {
            helpUI.displayHelp();
        }
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
        if (context == null) {
            throw new IllegalArgumentException();
        }
        AbstractHelpUI helpUI = getHelpUI();
        if (helpUI != null) {
            helpUI.displayContext(context, x, y);
        }
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
        if (href == null) {
            throw new IllegalArgumentException();
        }
        AbstractHelpUI helpUI = getHelpUI();
        if (helpUI != null) {
            helpUI.displayHelpResource(href);
        }
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
        // TODO Need a help ID from the context
        //final String contextId = command.getHelpId();
        final String contextId = ""; //$NON-NLS-1$
        return new HelpListener() {
            public void helpRequested(HelpEvent event) {
                if (getHelpUI() != null) {
                    IContext context = HelpSystem.getContext(contextId);
                    if (context != null) {
                        Point point = computePopUpLocation(event.widget
                                .getDisplay());
                        displayContext(context, point.x, point.y);
                    }
                }
            }
        };
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
        IContext context = HelpSystem.getContext(contextId);
        if (context != null) {
            Point point = computePopUpLocation(Display.getCurrent());
            displayContext(context, point.x, point.y);
        }
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
        Point point = computePopUpLocation(Display.getCurrent());
        AbstractHelpUI helpUI = getHelpUI();
        if (helpUI != null) {
            helpUI.displayContext(context, point.x, point.y);
        }
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
        return control.getData(HELP_KEY);
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
        return menu.getData(HELP_KEY);
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
        return menuItem.getData(HELP_KEY);
    }

    /**
     * Returns the help listener which activates the help support system.
     *
     * @return the help listener
     */
    private static HelpListener getHelpListener() {
        if (helpListener == null) {
            initializeHelpListener();
        }
        return helpListener;
    }

    /**
     * Initializes the help listener.
     */
    private static void initializeHelpListener() {
        helpListener = new HelpListener() {
            public void helpRequested(HelpEvent event) {
                if (getHelpUI() == null) {
                    return;
                }

                // get the help context from the widget
                Object object = event.widget.getData(HELP_KEY);

                // Since 2.0 we can expect that object is a String, however
                // for backward compatability we handle context computers and arrays.
                IContext context = null;
                if (object instanceof String) {
                    // context id - this is the norm
                    context = HelpSystem.getContext((String) object);
                } else if (object instanceof IContext) {
                    // already resolved context (pre 2.0)
                    context = (IContext) object;
                } else if (object instanceof IContextComputer) {
                    // a computed context (pre 2.0) - compute it now
                    Object[] helpContexts = ((IContextComputer) object)
                            .computeContexts(event);
                    // extract the first entry
                    if (helpContexts != null && helpContexts.length > 0) {
                        Object primaryEntry = helpContexts[0];
                        if (primaryEntry instanceof String) {
                            context = HelpSystem
                                    .getContext((String) primaryEntry);
                        } else if (primaryEntry instanceof IContext) {
                            context = (IContext) primaryEntry;
                        }
                    }
                } else if (object instanceof Object[]) {
                    // mixed array of String or IContext (pre 2.0) - extract the first entry
                    Object[] helpContexts = (Object[]) object;
                    // extract the first entry
                    if (helpContexts.length > 0) {
                        Object primaryEntry = helpContexts[0];
                        if (primaryEntry instanceof String) {
                            context = HelpSystem
                                    .getContext((String) primaryEntry);
                        } else if (primaryEntry instanceof IContext) {
                            context = (IContext) primaryEntry;
                        }
                    }
                }
                if (context != null) {
                    // determine a location in the upper right corner of the widget
                    Point point = computePopUpLocation(event.widget
                            .getDisplay());
                    // display the help
                    displayContext(context, point.x, point.y);
                }
            }
        };
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
        AbstractHelpUI helpUI = getHelpUI();
        if (helpUI != null && helpCompatibilityWrapper == null) {
            // create instance only once, and only if needed
            helpCompatibilityWrapper = new CompatibilityIHelpImplementation();
        }
        return helpCompatibilityWrapper;
    }

    /**
     * Returns the help UI for the platform, if available. This method will
     * initialize the help UI if necessary. 
     *
     * @return the help UI, or <code>null</code> if none
     */
    private static AbstractHelpUI getHelpUI() {
        if (!isInitialized) {
            initializePluggableHelpUI();
            isInitialized = true;
        }
        return pluggableHelpUI;
    }

    /**
     * Initializes the pluggable help UI by getting an instance via the
     * extension point.
     */
    private static void initializePluggableHelpUI() {
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
            public void run() {
                // get the help UI extension from the registry
                IExtensionPoint point = Platform.getExtensionRegistry()
                        .getExtensionPoint(HELP_SYSTEM_EXTENSION_ID);
                if (point == null) {
                    // our extension point is missing (!) - act like there was no help UI
                    return;
                }
                IExtension[] extensions = point.getExtensions();
                if (extensions.length == 0) {
                    // no help UI present
                    return;
                }
                // There should only be one extension/config element so we just take the first
                IConfigurationElement[] elements = extensions[0]
                        .getConfigurationElements();
                if (elements.length == 0) {
                    // help UI present but mangled - act like there was no help UI
                    return;
                }
                // Instantiate the help UI
                try {
                    pluggableHelpUI = (AbstractHelpUI) WorkbenchPlugin
                            .createExtension(elements[0],
                                    HELP_SYSTEM_CLASS_ATTRIBUTE);
                } catch (CoreException e) {
                    WorkbenchPlugin
                            .log("Unable to instantiate help UI" + e.getStatus());//$NON-NLS-1$
                }
            }
        });
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
        if (!isInitialized) {
            return false;
        }
        AbstractHelpUI helpUI = getHelpUI();
        return helpUI != null && helpUI.isContextHelpDisplayed();
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
        for (int i = 0; i < contexts.length; i++) {
            Assert.isTrue(contexts[i] instanceof String
                    || contexts[i] instanceof IContext);
        }
        action.setHelpListener(new HelpListener() {
            public void helpRequested(HelpEvent event) {
                if (contexts != null && contexts.length > 0
                        && getHelpUI() != null) {
                    // determine the context
                    IContext context = null;
                    if (contexts[0] instanceof String) {
                        context = HelpSystem.getContext((String) contexts[0]);
                    } else if (contexts[0] instanceof IContext) {
                        context = (IContext) contexts[0];
                    }
                    if (context != null) {
                        Point point = computePopUpLocation(event.widget
                                .getDisplay());
                        displayContext(context, point.x, point.y);
                    }
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
                Object[] helpContexts = computer.computeContexts(event);
                if (helpContexts != null && helpContexts.length > 0
                        && getHelpUI() != null) {
                    // determine the context
                    IContext context = null;
                    if (helpContexts[0] instanceof String) {
                        context = HelpSystem
                                .getContext((String) helpContexts[0]);
                    } else if (helpContexts[0] instanceof IContext) {
                        context = (IContext) helpContexts[0];
                    }
                    if (context != null) {
                        Point point = computePopUpLocation(event.widget
                                .getDisplay());
                        displayContext(context, point.x, point.y);
                    }
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
        for (int i = 0; i < contexts.length; i++) {
            Assert.isTrue(contexts[i] instanceof String
                    || contexts[i] instanceof IContext);
        }

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
        for (int i = 0; i < contexts.length; i++) {
            Assert.isTrue(contexts[i] instanceof String
                    || contexts[i] instanceof IContext);
        }
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
        for (int i = 0; i < contexts.length; i++) {
            Assert.isTrue(contexts[i] instanceof String
                    || contexts[i] instanceof IContext);
        }
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
                if (getHelpUI() != null) {
                    IContext context = HelpSystem.getContext(contextId);
                    if (context != null) {
                        Point point = computePopUpLocation(event.widget
                                .getDisplay());
                        displayContext(context, point.x, point.y);
                    }
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