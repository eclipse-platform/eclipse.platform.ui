/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.intro.impl.presentations;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.swt.*;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.intro.impl.*;
import org.eclipse.ui.internal.intro.impl.html.*;
import org.eclipse.ui.internal.intro.impl.model.*;
import org.eclipse.ui.internal.intro.impl.util.*;

public class BrowserIntroPartImplementation extends
        AbstractIntroPartImplementation implements IPropertyListener {


    // the browser widget that will display the intro content
    private Browser browser = null;

    // the HTML generator used to generate dynamic content
    private IntroHTMLGenerator htmlGenerator = null;

    private BrowserIntroPartLocationListener urlListener = new BrowserIntroPartLocationListener(
            this);

    // Global actions
    private Action backAction = new Action() {

        {
            setToolTipText(IntroPlugin
                    .getString("Browser.backwardButton_tooltip")); //$NON-NLS-1$
            setImageDescriptor(ImageUtil
                    .createImageDescriptor("backward_nav.gif")); //$NON-NLS-1$
        }

        public void run() {
            // dynamic case. Uses navigation history.
            if (getModelRoot().getHomePage().isDynamic()) {
                if (canNavigateBackward()) {
                    navigateBackward();
                    if (locationIsURL()) {
                        // indicate navigation.
                        setNavigationState(true);
                        browser.setUrl(getCurrentLocation());
                    } else
                        // we need to regen HTML. Set current page, and this
                        // will triger regen.
                        getModelRoot().setCurrentPageId(getCurrentLocation());
                }
                return;
            }

            // static HTML case. use browser real Back.
            browser.back();
        }
    };

    private Action forwardAction = new Action() {

        {
            setToolTipText(IntroPlugin
                    .getString("Browser.forwardButton_tooltip")); //$NON-NLS-1$
            setImageDescriptor(ImageUtil
                    .createImageDescriptor("forward_nav.gif")); //$NON-NLS-1$
        }

        public void run() {
            // dynamic case. Uses navigation history.
            if (getModelRoot().getHomePage().isDynamic()) {
                if (canNavigateForward()) {
                    navigateForward();
                    if (locationIsURL()) {
                        // Note: browser.forward() will not work here.
                        // indicate navigation.
                        setNavigationState(true);
                        browser.setUrl(getCurrentLocation());
                    } else
                        // we need to regen HTML. Set current page, and this
                        // will triger regen.
                        getModelRoot().setCurrentPageId(getCurrentLocation());
                }
                return;
            }

            // static HTML case. use browser real Forward.
            browser.forward();
        }
    };

    private Action homeAction = new Action() {

        {
            setToolTipText(IntroPlugin.getString("Browser.homeButton_tooltip")); //$NON-NLS-1$
            setImageDescriptor(ImageUtil.createImageDescriptor("home_nav.gif")); //$NON-NLS-1$
        }

        public void run() {
            // Home is URL of root page in static case, and root page in
            // dynamic.
            IntroHomePage rootPage = getModelRoot().getHomePage();
            if (rootPage.isDynamic())
                getModelRoot().setCurrentPageId(rootPage.getId());
            else
                browser.setUrl(rootPage.getUrl());
        }
    };

    /**
     * create the browser and set it's contents
     */
    public void createPartControl(Composite parent) {

        browser = new Browser(parent, SWT.MULTI);

        // add a location listener on the browser so we can intercept
        // LocationEvents. Responsible for intercepting URLs and updating UI
        // with history.
        browser.addLocationListener(urlListener);

        addToolBarActions();

        if (!getModelRoot().hasValidConfig()) {
            browser.setText(IntroPlugin.getString("Browser.invalidConfig")); //$NON-NLS-1$
            return;
        }



        // the root page is the first page we want to display, if there is no
        // valid cached page. Its attributes decide if we have a static or
        // dynamic case.
        IntroHomePage rootPage = getModelRoot().getHomePage();

        // get cached page.
        String url = getCachedCurrentPage();

        if (rootPage.isDynamic())
            handleDynamicIntro();
        else
            handleStaticIntro();
    }



    private void handleDynamicIntro() {

        IntroHomePage homePage = getModelRoot().getHomePage();
        // check cache state.
        String cachedPage = getCachedCurrentPage();
        if (cachedPage != null) {
            // we have a cached state. handle appropriately
            if (isURL(cachedPage)) {
                // set the URL the browser should display
                boolean success = browser.setUrl(cachedPage);
                if (!success) {
                    Log
                            .error(
                                    "Unable to set the following ULR in browser: " + cachedPage, null); //$NON-NLS-1$
                    return;
                }
            } else {
                // Generate HTML for the cached page, and set it
                // on the browser.
                getModelRoot().setCurrentPageId(cachedPage);
                generateDynamicContentForPage(getModelRoot().getCurrentPage());
            }

        } else {
            // No cacched page. Generate HTML for the home page, and set it
            // on the browser.
            generateDynamicContentForPage(homePage);
        }

        // Add this presentation as a listener to model
        // only in dynamic case, for now.
        getModelRoot().addPropertyListener(this);

        // REVISIT: update the history here. The design of the history
        // navigation is that it has to be updated independant of the
        // property fired model events.
        updateHistory(homePage.getId());

    }

    private void handleStaticIntro() {
        // We have a static case. Set the url on the browser to be the url
        // defined in the root page. But first check memento if we can
        // restore last visited page.
        String url = getCachedCurrentPage();
        if (!isURL(url))
            // no cached state, or invalid state.
            url = getModelRoot().getHomePage().getUrl();

        if (url == null) {
            // We have no content to display. log an error
            Log.error("Url is null; no content to display in browser", //$NON-NLS-1$
                    null);
            return;
        }
        // set the URL the browser should display
        boolean success = browser.setUrl(url);
        if (!success) {
            Log.error(
                    "Unable to set the following ULR in browser: " + url, null); //$NON-NLS-1$
            return;
        }
    }


    /**
     * Generate dynamic HTML for the provided page, and set it in the browser
     * widget (Revisit this). This method also updates the navigation history.
     * 
     * @param page
     *            the page to generate HTML for
     */
    private void generateDynamicContentForPage(AbstractIntroPage page) {

        HTMLElement html = getHTMLGenerator().generateHTMLforPage(page);

        if (html == null) {
            // there was an error generating the html. log an error
            Log.error("Error generating HTML", null); //$NON-NLS-1$
            return;
        }
        // set the browser's HTML
        if (browser != null) {
            boolean success = browser.setText(html.toString());
            if (!success)
                Log.error("Unable to set HTML on the browser", null); //$NON-NLS-1$
        }
        // print the HTML if we are in debug mode and have tracing turned on
        if (IntroPlugin.getDefault().isDebugging()) {
            String printHtml = Platform
                    .getDebugOption("org.eclipse.ui.intro/trace/printHTML"); //$NON-NLS-1$
            if (printHtml != null && printHtml.equalsIgnoreCase("true")) { //$NON-NLS-1$
                System.out.println(html);
            }
        }
    }

    /**
     * Return the cached IntroHTMLGenerator
     * 
     * @return
     */
    private IntroHTMLGenerator getHTMLGenerator() {
        if (htmlGenerator == null)
            htmlGenerator = new IntroHTMLGenerator();

        return htmlGenerator;
    }

    protected void addToolBarActions() {
        // Handle menus:
        IActionBars actionBars = getIntroPart().getIntroSite().getActionBars();
        IToolBarManager toolBarManager = actionBars.getToolBarManager();
        toolBarManager.add(homeAction);
        toolBarManager.add(backAction);
        toolBarManager.add(forwardAction);
        toolBarManager.update(true);
        actionBars.updateActionBars();
    }

    /**
     * Handle model property changes. Property listeners are only added in the
     * dynamic case.
     * 
     * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object,
     *      int)
     */
    public void propertyChanged(Object source, int propId) {
        if (propId == IntroModelRoot.CURRENT_PAGE_PROPERTY_ID) {
            String pageId = getModelRoot().getCurrentPageId();
            if (pageId == null || pageId.equals("")) //$NON-NLS-1$
                // page ID was not set properly. exit.
                return;
            generateDynamicContentForPage(getModelRoot().getCurrentPage());
        }
    }

    public void setFocus() {
        browser.setFocus();
    }

    public void dispose() {
        browser.dispose();
    }

    /**
     * Override parent behavior to handle the case when we have a static page.
     * This can happen in both the static intro case, or in the dynamic when the
     * last visited page is the dynamic browser is an http: page, and not an
     * intro page.
     */
    protected void saveCurrentPage(IMemento memento) {
        if (memento == null)
            return;
        // Handle the case where we are on a static page.
        // browser.getURL() returns the empty string if there is no current URL
        // and returns "about:blank" if we are on a dynamic page
        if (browser != null && browser.getUrl() != null
                && browser.getUrl().length() > 0
                && !(browser.getUrl().equals("about:blank"))) { //$NON-NLS-1$
            String currentURL = browser.getUrl();
            if (currentURL != null) {
                memento.putString(IIntroConstants.MEMENTO_CURRENT_PAGE_ATT,
                        currentURL);
            }
        } else {
            super.saveCurrentPage(memento);
        }
    }

    private boolean isURL(String url) {
        IntroURLParser parser = new IntroURLParser(url);
        if (parser.hasProtocol())
            return true;
        return false;
    }


}