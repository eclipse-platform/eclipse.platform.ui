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

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.intro.*;
import org.eclipse.ui.intro.internal.extensions.*;
import org.eclipse.ui.intro.internal.presentations.*;
import org.eclipse.ui.intro.internal.util.*;

/**
 * This class models the presentation element contributed to a config extension
 * point. The Presentation class delegates UI creation to the actual
 * Implementation class, and passes the IntroPart along to this implementation
 * class.
 * <p>
 * Rules:
 * <ul>
 * <li>There is no model class for the "implementation" markup element. This
 * presentation class inherits information from the implementation class that is
 * picked (based on OS, ...).</li>
 * <li>ID attribute of this model class is the id of the picked implementation
 * element.</li>
 * <li>Style attribute in this model class is the style of the picked
 * implementation element.</li>
 * <li>HTMLHeadContent in this model class is the HEAD element under the picked
 * implementation element, only if the implementation element is a Browser
 * implmenetation.</li>
 * <ul>
 */
public class IntroPartPresentation extends IntroElement {

    protected static final String PRESENTATION_ELEMENT = "presentation";

    private static final String IMPLEMENTATION_ELEMENT = "implementation";

    private static final String TITLE_ATTRIBUTE = "title";

    private static final String STYLE_ATTRIBUTE = "style";

    private static final String OS_ATTRIBUTE = "os";

    private static final String WS_ATTRIBUTE = "ws";

    private static final String HOME_PAGE_ID_ATTRIBUTE = "home-page-id";

    private String title;

    private String style;

    private String homePageId;

    // The Head contributions to this preentation (inherited from child
    // implementation).
    private IntroHead head;

    private AbstractIntroPartImplementation implementation;

    // CustomizableIntroPart instance. Passed to the Implementation classes.
    private IIntroPart introPart;

    /**
     *  
     */
    IntroPartPresentation(IConfigurationElement element) {
        super(element);
        title = element.getAttribute(TITLE_ATTRIBUTE);
        homePageId = element.getAttribute(HOME_PAGE_ID_ATTRIBUTE);
    }

    private void updatePresentationAttributes(IConfigurationElement element) {
        if (element != null) {
            // reset (ie: inherit) id and style to be implementation id and
            // style. Then handle HEAD content in the case of HTML Browser.
            style = element.getAttribute(STYLE_ATTRIBUTE);
            id = element.getAttribute(ID_ATTRIBUTE);
            // get Head contribution, regardless of implementation class.
            // Implementation class is created lazily by UI.
            head = getHead(element);
            // Resolve.
            style = IntroModelRoot.resolveURL(style, element);
        }
    }

    /**
     * Returns the model class for the Head element under an implementation.
     * Returns null if there is no head contribution.
     * 
     * @param element
     * @return
     */
    private IntroHead getHead(IConfigurationElement element) {
        try {
            // There should only be one head element. Since elements where
            // obtained by name, no point validating name.
            IConfigurationElement[] headElements = element
                    .getChildren(IntroHead.HEAD_ELEMENT);
            if (headElements.length == 0)
                // no contributions. done.
                return null;
            IntroHead head = new IntroHead(headElements[0]);
            head.setParent(this);
            return head;
        } catch (Exception e) {
            Util.handleException(e.getMessage(), e);
            return null;
        }
    }

    /**
     * @param introPart
     * @throws PartInitException
     */
    public void init(IIntroPart introPart) throws PartInitException {
        // REVISIT: Called when the actual UI needs to be created. Incomplete
        // separation of model / UI. Will change later.
        // should not get here if there is no valid implementation.
        this.introPart = introPart;
    }

    /**
     * Returns the style associated with the Presentation. May be null if no
     * shared presentation style is needed, or in the case of static HTML OOBE.
     * 
     * @return Returns the style.
     */
    public String getStyle() {
        return style;
    }

    /**
     * Returns the title associated with the Presentation. May be null if no
     * title is defined
     * 
     * @return Returns the presentation title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Creates the UI based on the implementation class.
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        Vector validImplementations = getValidImplementationElements(getConfigurationElement());
        IConfigurationElement implementationElement = null;
        for (int i = 0; i < validImplementations.size(); i++) {
            implementationElement = (IConfigurationElement) validImplementations
                    .elementAt(i);
            // you want to pass primed model.
            updatePresentationAttributes(implementationElement);
            try {
                implementation = createIntroPartImplementation(implementationElement);
                if (implementation == null)
                    // failed to create executable.
                    continue;

                implementation.init(introPart);
                implementation.createPartControl(parent);
                Logger.logInfo("Loaded config implementation from: "
                        + ExtensionPointManager
                                .getLogString(implementationElement));
                break;
            } catch (Exception e) {
                Logger.logWarning("failed to create implementation from: "
                        + ExtensionPointManager
                                .getLogString(implementationElement));
                implementation = null;
                implementationElement = null;
            }
        }

        if (implementationElement == null) {
            // worst case scenario. We failed in all cases.
            implementation = new FormIntroPartImplementation();
            try {
                implementation.init(introPart);
            } catch (Exception e) {
                // should never be here.
                Logger.logError(e.getMessage(), e);
                return;
            }
            implementation.createPartControl(parent);
            Logger
                    .logWarning("Loaded UI Forms implementation as a default Welcome.");
        }
    }

    /**
     * Retruns a list of valid implementation elements of the config. Choose
     * correct implementation element based on os atrributes. Rules: get current
     * OS, choose first contributrion, with os that matches OS. Otherwise,
     * choose first contribution with no os. Returns null if no valid
     * implementation is found.
     */
    private Vector getValidImplementationElements(
            IConfigurationElement configElement) {

        Vector validList = new Vector();

        // There can be more than one implementation contribution. Add each
        // valid one. First start with OS, then WS then no OS.
        IConfigurationElement[] implementationElements = configElement
                .getChildren(IMPLEMENTATION_ELEMENT);
        IConfigurationElement implementationElement = null;

        if (implementationElements.length == 0)
            // no contributions. done.
            return validList;

        String currentOS = Platform.getOS();
        String currentWS = Platform.getWS();

        // first loop through all to find one with matching OS, with or
        // without WS.
        for (int i = 0; i < implementationElements.length; i++) {
            String os = implementationElements[i].getAttribute(OS_ATTRIBUTE);
            if (os == null)
                // no os, no match.
                continue;

            if (listValueHasValue(os, currentOS)) {
                // found implementation with correct OS. Now try if WS
                // matches.
                String ws = implementationElements[i]
                        .getAttribute(WS_ATTRIBUTE);
                if (ws == null) {
                    // good OS, and they do not care about WS. we have a
                    // match.
                    validList.add(implementationElements[i]);
                } else {
                    // good OS, and we have WS.
                    if (listValueHasValue(ws, currentWS))
                        validList.add(implementationElements[i]);
                }
            }
        }

        // now loop through all to find one with no OS defined, but with a
        // matching WS.
        for (int i = 0; i < implementationElements.length; i++) {
            String os = implementationElements[i].getAttribute(OS_ATTRIBUTE);
            if (os == null) {
                // found implementation with no OS. Now try if WS
                // matches.
                String ws = implementationElements[i]
                        .getAttribute(WS_ATTRIBUTE);
                if (ws == null) {
                    // no OS, and they do not care about WS. we have a
                    // match.
                    validList.add(implementationElements[i]);
                } else {
                    // no OS, and we have WS.
                    if (listValueHasValue(ws, currentWS))
                        validList.add(implementationElements[i]);
                }

            }
        }

        return validList;

    }

    /**
     * Util method that searches for the given value in a comma separated list
     * of values. The list is retrieved as an attribute value of OS, WS.
     *  
     */
    private boolean listValueHasValue(String stringValue, String value) {
        String[] attributeValues = stringValue.split(",");
        for (int i = 0; i < attributeValues.length; i++) {
            if (attributeValues[i].equalsIgnoreCase(value))
                return true;
        }
        return false;
    }

    /**
     * Creates the actual implementation class. Returns null on failure.
     * 
     * @return Returns the partConfigElement representing the single Intro
     *         config.
     */
    private AbstractIntroPartImplementation createIntroPartImplementation(
            IConfigurationElement configElement) {
        if (configElement == null)
            return null;
        AbstractIntroPartImplementation implementation = null;
        try {
            implementation = (AbstractIntroPartImplementation) configElement
                    .createExecutableExtension("class");
        } catch (Exception e) {
            Util.handleException("Could not instantiate implementation class "
                    + configElement.getAttribute("class"), e);
        }
        return implementation;
    }

    /**
     * @return Returns the introPart.
     */
    public IIntroPart getIntroPart() {
        return introPart;
    }

    public void setFocus() {
        if (implementation != null)
            implementation.setFocus();
    }

    /**
     * Called when the IntroPart is disposed. Forwards the call to the
     * implementation class.
     */
    public void dispose() {
        if (implementation != null)
            implementation.dispose();
    }

    /**
     * Forwards the call to the implementation class.
     */
    public void updateHistory(String pageId) {
        if (implementation != null)
            implementation.updateHistory(pageId);
    }

    /**
     * @return Returns the homePageId.
     */
    public String getHomePageId() {
        return homePageId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.internal.model.IntroElement#getType()
     */
    public int getType() {
        return IntroElement.PRESENTATION;
    }

    /**
     * @return Returns the HTML head conttent to be added to each dynamic html
     *         page in this presentation..
     */
    public IntroHead getHead() {
        return head;
    }
}