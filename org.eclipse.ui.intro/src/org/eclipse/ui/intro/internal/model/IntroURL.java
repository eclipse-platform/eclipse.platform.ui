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

package org.eclipse.ui.intro.internal.model;

import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.*;
import org.eclipse.ui.intro.*;
import org.eclipse.ui.intro.internal.*;
import org.eclipse.ui.intro.internal.extensions.*;
import org.eclipse.ui.intro.internal.parts.*;
import org.eclipse.ui.intro.internal.presentations.*;
import org.eclipse.ui.intro.internal.util.*;

/**
 * An intro url. An intro URL is a valid http url, with org.eclipse.ui.intro as
 * a host. This class holds all logic to execute Intro URL commands, ie: an
 * Intro URL knows how to execute itself.
 */
public class IntroURL {

    /**
     * Intro URL constants.
     */
    public static final String INTRO_PROTOCOL = "http";
    public static final String INTRO_HOST_ID = "org.eclipse.ui.intro";

    /**
     * Constants that represent Intro URL actions.
     */
    public static final String SET_STANDBY_MODE = "setStandbyMode";
    public static final String SHOW_STANDBY = "showStandby";
    public static final String CLOSE = "close";
    public static final String SHOW_HELP_TOPIC = "showHelpTopic";
    public static final String SHOW_HELP = "showHelp";
    public static final String OPEN_BROWSER = "openBrowser";
    public static final String RUN_ACTION = "runAction";
    public static final String SHOW_PAGE = "showPage";
    public static final String SHOW_MESSAGE = "showMessage";

    /**
     * Constants that represent valid action keys.
     */
    public static final String KEY_ID = "id";
    public static final String KEY_PLUGIN_ID = "pluginId";
    public static final String KEY_CLASS = "class";
    public static final String KEY_STANDBY = "standby";
    public static final String KEY_PART_ID = "partId";
    public static final String KEY_INPUT = "input";
    public static final String KEY_MESSAGE = "message";

    private String action = null;
    private Properties parameters = null;

    /**
     * Prevent creation. Must be created through an IntroURLParser. This
     * constructor assumed we have a valid intro url.
     * 
     * @param url
     */
    IntroURL(String action, Properties parameters) {
        this.action = action;
        this.parameters = parameters;
    }

    /**
     * Executes whatever valid Intro action is embedded in this Intro URL.
     *  
     */
    public void execute() {
        Display display = Display.getCurrent();
        BusyIndicator.showWhile(display, new Runnable() {

            public void run() {
                doExecute();
            }
        });
    }

    private void doExecute() {
        // check to see if we have a custom action
        // if (action.indexOf("/") != -1)
        //   handleCustomAction();

        // check for all Intro actions.
        if (action.equals(CLOSE))
            closeIntro();

        /**
         * Sets the state of the intro part. Does not care about passing input
         * to the part.
         */
        else if (action.equals(SET_STANDBY_MODE))
            setStandbyState(getParameter(KEY_STANDBY));

        else if (action.equals(SHOW_STANDBY))
            handleStandbyStateChanged(getParameter(KEY_PART_ID),
                    getParameter(KEY_INPUT));

        else if (action.equals(SHOW_HELP))
            // display the full Help System.
            showHelp();

        else if (action.equals(SHOW_HELP_TOPIC))
            // display a Help System Topic.
            showHelpTopic(getParameter(KEY_ID));

        else if (action.equals(RUN_ACTION))
            // run an Intro action. Get the pluginId and the class keys.
            runAction(getParameter(KEY_PLUGIN_ID), getParameter(KEY_CLASS));

        else if (action.equals(SHOW_PAGE))
            // display an Intro Page.
            showPage(getParameter(KEY_ID));

        else if (action.equals(SHOW_MESSAGE))
            showMessage(getParameter(KEY_MESSAGE));
    }

    private void closeIntro() {
        // Relies on Workbench.
        PlatformUI.getWorkbench().closeIntro(
                PlatformUI.getWorkbench().getIntro());
    }

    /**
     * Sets the into part to standby, and shows the passed standby part, with
     * the given input.
     * 
     * @param partId
     * @param input
     */
    private void handleStandbyStateChanged(String partId, String input) {
        // set intro to standby mode. we know we have a customizable part.
        CustomizableIntroPart introPart = getCustomizableIntroPart(true);
        PlatformUI.getWorkbench().setIntroStandby(introPart, true);
        StandbyPart standbyPart = introPart.getStandbyPart();

        // Get the StandbyPartContent that maps to the given partId.
        StandbyPartContent standbyPartContent = ExtensionPointManager.getInst()
                .getStandbyPart(partId);

        if (standbyPartContent != null) {
            String standbyContentClassName = standbyPartContent.getClassName();
            String pluginId = standbyPartContent.getPluginId();

            Object standbyContentObject = createClassInstance(pluginId,
                    standbyContentClassName);
            if (standbyContentObject instanceof IStandbyContentPart) {
                IStandbyContentPart contentPart = (IStandbyContentPart) standbyContentObject;
                standbyPart.addStandbyContentPart(partId, contentPart);
                standbyPart.setTopControl(partId);
                standbyPart.setInput(input);
                return;
            }
        }

        // we do not have a valid partId or we failed to instantiate part, show
        // Context help part.
        standbyPart.setTopControl(IIntroConstants.HELP_CONTEXT_STANDBY_PART);
    }

    /**
     * Set the Workbench Intro Part state.
     * 
     * @param state
     */
    private void setStandbyState(String state) {
        boolean standby = state.equals("true") ? true : false;
        CustomizableIntroPart introPart = getCustomizableIntroPart(standby);
        // should rely on Workbench api. If the Intro part was not open when
        // this method was called, the following line simply resets the part
        // into standby.
        PlatformUI.getWorkbench().setIntroStandby(introPart, standby);
    }

    /**
     * Utility method to return the Intro part, if it is open. If it is not, then opens the 
     * Intro part with the given state. This is needed to avoid flicker if states need to be changed.
     * @param standby
     * @return
     * @todo Generated comment
     */
    private CustomizableIntroPart getCustomizableIntroPart(boolean standby) {
        // do not rely on model presentation to get part because Intro may be
        // closed.
        CustomizableIntroPart intro = (CustomizableIntroPart) IntroPlugin
                .getIntroPart();
        if (intro == null)
            intro = (CustomizableIntroPart) IntroPlugin.showIntroPart(standby);
        return intro;
    }

    /**
     * Run an action
     */
    private void runAction(String pluginId, String className) {

        Object actionObject = createClassInstance(pluginId, className);
        try {
            if (actionObject instanceof IIntroAction) {
                IIntroAction introAction = (IIntroAction) actionObject;
                IIntroSite site = IntroPlugin.getDefault().getIntroModelRoot()
                        .getPresentation().getIntroPart().getIntroSite();
                introAction.initialize(site, parameters);
                introAction.run();
            } else if (actionObject instanceof IAction) {
                IAction action = (IAction) actionObject;
                action.run();
            } else if (actionObject instanceof IActionDelegate) {
                final IActionDelegate delegate = (IActionDelegate) actionObject;
                if (delegate instanceof IWorkbenchWindowActionDelegate)
                    ((IWorkbenchWindowActionDelegate) delegate).init(PlatformUI
                            .getWorkbench().getActiveWorkbenchWindow());
                Action proxy = new Action(this.action) {

                    public void run() {
                        delegate.run(this);
                    }
                };
                proxy.run();
            }
        } catch (Exception e) {
            Logger.logError("Could not run action: " + className, e);
            return;
        }
    }

    private Object createClassInstance(String pluginId, String className) {
        if (pluginId == null | className == null)
            // quick exits.
            return null;

        IPluginDescriptor desc = Platform.getPluginRegistry()
                .getPluginDescriptor(pluginId);
        if (desc == null)
            // quick exit.
            return null;

        Class aClass;
        Object aObject;
        try {
            aClass = desc.getPluginClassLoader().loadClass(className);
            aObject = aClass.newInstance();
            return aObject;
        } catch (Exception e) {
            Logger.logError("Could not instantiate: " + className + " in "
                    + pluginId, e);
            return null;
        }
    }

    /**
     * Open a help topic.
     */
    private void showHelpTopic(String href) {
        // WorkbenchHelp takes care of error handling.
        WorkbenchHelp.displayHelpResource(href);
    }

    /**
     * Open the help system.
     */
    private void showHelp() {
        WorkbenchHelp.displayHelp();
    }

    private void showMessage(String message) {

        //TODO some of the actions run UI code yet they are in
        // model package.
        if (message == null)
            message = "";
        else
            message = URLDecoder.decode(message);
        MessageDialog.openInformation(null, "Introduction", message);
    }

    /**
     * Display an Intro Page.
     */
    private void showPage(String pageId) {
        // set the current page id in the model. This will triger a listener
        // event to the UI.
        IntroModelRoot modelRoot = IntroPlugin.getDefault().getIntroModelRoot();
        modelRoot.setCurrentPageId(pageId);
    }

    private void handleCustomAction() {
        // REVISIT:
    }

    /**
     * @return Returns the action imbedded in this URL.
     */
    public String getAction() {
        return action;
    }

    /**
     * Return a parameter defined in the Intro URL. Returns null if the
     * parameter is not defined.
     * 
     * @param parameterId
     * @return
     */
    public String getParameter(String parameterId) {
        return parameters.getProperty(parameterId);
    }

}