/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.intro.impl.presentations;

import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.intro.impl.IIntroConstants;
import org.eclipse.ui.internal.intro.impl.IntroPlugin;
import org.eclipse.ui.internal.intro.impl.html.HTMLElement;
import org.eclipse.ui.internal.intro.impl.html.IntroHTMLGenerator;
import org.eclipse.ui.internal.intro.impl.model.AbstractIntroElement;
import org.eclipse.ui.internal.intro.impl.model.AbstractIntroPage;
import org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation;
import org.eclipse.ui.internal.intro.impl.model.IntroContentProvider;
import org.eclipse.ui.internal.intro.impl.model.IntroHomePage;
import org.eclipse.ui.internal.intro.impl.model.IntroModelRoot;
import org.eclipse.ui.internal.intro.impl.model.ModelUtil;
import org.eclipse.ui.internal.intro.impl.model.loader.ContentProviderManager;
import org.eclipse.ui.internal.intro.impl.model.loader.IntroContentParser;
import org.eclipse.ui.internal.intro.impl.util.Log;
import org.eclipse.ui.intro.config.IIntroContentProvider;
import org.eclipse.ui.intro.config.IIntroContentProviderSite;
import org.eclipse.ui.intro.config.IIntroXHTMLContentProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BrowserIntroPartImplementation extends
        AbstractIntroPartImplementation implements IPropertyListener,
        IIntroContentProviderSite {


    // the browser widget that will display the intro content
    private Browser browser = null;

    // the HTML generator used to generate dynamic content
    private IntroHTMLGenerator htmlGenerator = null;

    // cache model instance for reuse.
    private IntroModelRoot model = getModel();

    private BrowserIntroPartLocationListener urlListener = new BrowserIntroPartLocationListener(
        this);


    protected void updateNavigationActionsState() {
        if (getModel().isDynamic()) {
            forwardAction.setEnabled(canNavigateForward());
            backAction.setEnabled(canNavigateBackward());
            return;
        }

        // in static html intro, use browser history.
        forwardAction.setEnabled(browser.isForwardEnabled());
        backAction.setEnabled(browser.isBackEnabled());
    }


    /**
     * create the browser and set it's contents
     */
    public void createPartControl(Composite parent) {
        browser = new Browser(parent, SWT.NONE);

        // add a location listener on the browser so we can intercept
        // LocationEvents. Responsible for intercepting URLs and updating UI
        // with history.
        browser.addLocationListener(urlListener);

        // add a location listener that will clear a flag at the end of any
        // navigation to a page. This is used in conjunction with the location
        // listener to filter out redundant navigations due to frames.
        browser.addProgressListener(new ProgressListener() {

            public void changed(ProgressEvent event) {
            }

            public void completed(ProgressEvent event) {
                browser.setData("frameNavigation", null); //$NON-NLS-1$
                if (!getModel().isDynamic())
                    updateNavigationActionsState();
            }
        });

        // Enable IE pop-up menu only in debug mode.
        browser.addListener(SWT.MenuDetect, new Listener() {

            public void handleEvent(Event event) {
                if (IntroPlugin.getDefault().isDebugging())
                    event.doit = true;
                else
                    event.doit = false;
            }
        });

        addToolBarActions();

        if (!getModel().hasValidConfig()) {
            browser.setText(IntroPlugin.getString("Browser.invalidConfig")); //$NON-NLS-1$
            return;
        }

        // root page is what decides if the model is dynamic or not.
        if (getModel().isDynamic())
            handleDynamicIntro();
        else
            handleStaticIntro();
    }



    private void handleDynamicIntro() {
        IntroHomePage homePage = getModel().getHomePage();
        // check cache state.
        String cachedPage = getCachedCurrentPage();
        if (cachedPage != null) {
            // we have a cached state. handle appropriately
            if (isURL(cachedPage)) {
                // set the URL the browser should display
                boolean success = browser.setUrl(cachedPage);
                if (!success) {
                    Log.error("Unable to set the following ULR in browser: " //$NON-NLS-1$
                            + cachedPage, null);
                    return;
                }
            } else {
                // Generate HTML for the cached page, and set it on the browser.
                getModel().setCurrentPageId(cachedPage, false);
                // generateDynamicContentForPage(getModel().getCurrentPage());
            }
            updateHistory(cachedPage);

        } else {
            // No cached page. Generate HTML for the home page, and set it
            // on the browser.
            // generateDynamicContentForPage(homePage);
            updateHistory(homePage.getId());
        }
        // INTRO: all setText calls above are commented out because calling
        // setText twice causes problems. revisit when swt bug is fixed.

        // Add this presentation as a listener to model
        // only in dynamic case, for now.
        getModel().addPropertyListener(this);
    }


    /**
     * Generate dynamic HTML for the provided page, and set it in the browser
     * widget. A cache is used for performance and for having a correct dynamic
     * content life cycle. This method also updates the navigation history.
     * 
     * @param page
     *                   the page to generate HTML for
     */
    private boolean generateDynamicContentForPage(AbstractIntroPage page) {
        String content = null;

        if (page.isXHTMLPage())
            content = generateXHTMLPage(page, this);
        else {
            HTMLElement html = getHTMLGenerator().generateHTMLforPage(page,
                this);
            if (html != null)
                content = html.toString();
        }


        if (content == null) {
            // there was an error generating the html. log an error
            Log.error("Error generating HTML content for page", null); //$NON-NLS-1$
            return false;
        }

        // set the browser's HTML.
        boolean success = false;
        if (browser != null) {
            success = browser.setText(content);
            if (!success)
                Log.error("Unable to set HTML on the browser", null); //$NON-NLS-1$
        }
        // print the HTML if we are in debug mode and have tracing turned on
        if (IntroPlugin.getDefault().isDebugging()) {
            String printHtml = Platform
                .getDebugOption("org.eclipse.ui.intro/trace/printHTML"); //$NON-NLS-1$
            if (printHtml != null && printHtml.equalsIgnoreCase("true")) { //$NON-NLS-1$
                System.out.println(content);
            }
        }
        return success;
    }

    /**
     * Use xslt to flatten the DOM for the page. Also create any dynamic content
     * providers if there are any.
     */
    private String generateXHTMLPage(AbstractIntroPage page,
            IIntroContentProviderSite site) {
        // if page is an XHTML page, DOM is cloned, manipulated by dynamic
        // content, passed threw XSLT tansform, and then discarded.
        // Note: Resolving dynamic content is done at the UI level, consistant
        // with SWT presentation.
        Document dom = resolveDynamicContent(page, site);
        return IntroContentParser.convertToString(dom);
    }

    /**
     * Resolve the dynamic content in the page. Resolving dynamic content will
     * alter the original page DOM. Dynamic content tags are removed once
     * resolved to have clean xhtml.
     */
    private Document resolveDynamicContent(AbstractIntroPage page,
            IIntroContentProviderSite site) {
        Document dom = page.getResolvedDocument();

        // get all content provider elements in DOM.
        NodeList contentProviders = dom.getElementsByTagNameNS("*", //$NON-NLS-1$
            IntroContentProvider.TAG_CONTENT_PROVIDER);

        // get the array version of the nodelist to work around DOM api design.
        Node[] nodes = ModelUtil.getArray(contentProviders);
        for (int i = 0; i < nodes.length; i++) {
            Element contentProviderElement = (Element) nodes[i];
            IntroContentProvider provider = new IntroContentProvider(
                contentProviderElement, page.getBundle());
            provider.setParent(page);
            // If we've already loaded the content provider for this element,
            // retrieve it, otherwise load the class.
            IIntroXHTMLContentProvider providerClass = (IIntroXHTMLContentProvider) ContentProviderManager
                .getInst().getContentProvider(provider);
            if (providerClass == null)
                // content provider never created before, create it.
                providerClass = (IIntroXHTMLContentProvider) ContentProviderManager
                    .getInst().createContentProvider(provider, site);

            if (providerClass != null) {
                // create the specialized content, and remove content tag.
                providerClass.createContent(provider.getId(),
                    (Element) contentProviderElement.getParentNode());
                contentProviderElement.getParentNode().removeChild(
                    contentProviderElement);
            } else {
                // we couldn't load the content provider, so add any alternate
                // text content if there is any.
                // INTRO: do it. 3.0 intro content style uses text element as
                // alt text. We can load XHTML content here.
            }
        }
        return dom;
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
        actionBars.setGlobalActionHandler(ActionFactory.FORWARD.getId(),
            forwardAction);
        actionBars.setGlobalActionHandler(ActionFactory.BACK.getId(),
            backAction);
        toolBarManager.add(homeAction);
        toolBarManager.add(backAction);
        toolBarManager.add(forwardAction);
        toolBarManager.update(true);
        actionBars.updateActionBars();
        updateNavigationActionsState();
    }

    public void dynamicStandbyStateChanged(boolean standby,
            boolean isStandbyPartNeeded) {

        if (isStandbyPartNeeded)
            // we have a standby part, nothing more to do in presentation.
            return;

        // presentation is shown here. toggle standby page. No need to update
        // history here.
        IntroHomePage homePage = getModel().getHomePage();
        IntroHomePage standbyPage = getModel().getStandbyPage();
        if (standbyPage == null)
            standbyPage = homePage;

        if (standby) {
            generateDynamicContentForPage(standbyPage);
        } else {
            // REVISIT: If cached page is the standby page and we are not
            // initially in standby mode, it means standby was forced on
            // intro view on close. react.
            if (getModel().getCurrentPage().equals(standbyPage.getId()))
                getModel().setCurrentPageId(getModel().getHomePage().getId());
            generateDynamicContentForPage(getModel().getCurrentPage());
        }
    }



    /**
     * Handle model property changes. Property listeners are only added in the
     * dynamic case.
     * 
     * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object,
     *           int)
     */
    public void propertyChanged(Object source, int propId) {
        if (propId == IntroModelRoot.CURRENT_PAGE_PROPERTY_ID) {
            String pageId = getModel().getCurrentPageId();
            if (pageId == null || pageId.equals("")) //$NON-NLS-1$
                // page ID was not set properly. exit.
                return;
            // update the presentation's content based on the model changes
            updateContent();
        }
    }

    public void setFocus() {
        browser.setFocus();
    }

    public void dispose() {
        browser.dispose();
    }

    /**
     * Regenerate the dynamic content for the current page
     */
    protected void updateContent() {
        generateDynamicContentForPage(getModel().getCurrentPage());
    }

    /**
     * Clear page cache for the page that contains this provider. Remove the
     * String cache from the HTML cache manager.This will force a call to
     * createContents on all content providers the next time this page needs to
     * be displayed.
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation#reflow()
     */
    public void reflow(IIntroContentProvider provider, boolean incremental) {
        updateContent();
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


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation#navigateBackward()
     */
    public boolean navigateBackward() {
        boolean success = false;
        if (getModel().isDynamic()) {
            // dynamic case. Uses navigation history.
            if (canNavigateBackward()) {
                navigateHistoryBackward();
                if (isURL(getCurrentLocation())) {
                    success = browser.setUrl(getCurrentLocation());
                } else {
                    // we need to regen HTML. We can not use setting current
                    // page to trigger regen for one case: navigating back from
                    // an url will trigger regen since current page would be the
                    // same.
                    AbstractIntroPage page = (AbstractIntroPage) getModel()
                        .findChild(getCurrentLocation(),
                            AbstractIntroElement.ABSTRACT_PAGE);
                    success = generateDynamicContentForPage(page);
                    getModel().setCurrentPageId(getCurrentLocation(), false);
                }
            } else
                success = false;
            // update history only in dynamic case.
            updateNavigationActionsState();
        } else
            // static HTML case. use browser real Back.
            success = browser.back();

        return success;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation#navigateForward()
     */
    public boolean navigateForward() {
        boolean success = false;
        if (getModel().isDynamic()) {
            // dynamic case. Uses navigation history.
            if (canNavigateForward()) {
                navigateHistoryForward();
                if (isURL(getCurrentLocation())) {
                    success = browser.setUrl(getCurrentLocation());
                } else {
                    AbstractIntroPage page = (AbstractIntroPage) getModel()
                        .findChild(getCurrentLocation(),
                            AbstractIntroElement.ABSTRACT_PAGE);
                    success = generateDynamicContentForPage(page);
                    getModel().setCurrentPageId(getCurrentLocation(), false);
                }
            } else
                success = false;
            // update history only in dynamic case.
            updateNavigationActionsState();
        } else
            // static HTML case. use browser real Forward.
            success = browser.forward();

        return success;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation#navigateHome()
     */
    public boolean navigateHome() {
        // Home is URL of root page in static case, and root page in
        // dynamic.
        IntroHomePage rootPage = getModel().getHomePage();
        String location = null;
        boolean success = false;
        if (getModel().isDynamic()) {
            location = rootPage.getId();
            success = getModel().setCurrentPageId(location);
        } else {
            location = rootPage.getUrl();
            success = browser.setUrl(location);
        }
        updateHistory(location);
        return success;
    }



    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation#handleRegistryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
     */
    protected void handleRegistryChanged(IRegistryChangeEvent event) {
        if (getModel().isDynamic()) {
            // null generator first.
            htmlGenerator = null;
            // Add this presentation as a listener to model only in dynamic
            // case.
            getModel().addPropertyListener(this);
            getModel().firePropertyChange(
                IntroModelRoot.CURRENT_PAGE_PROPERTY_ID);
        }
    }


    public void standbyStateChanged(boolean standby, boolean isStandbyPartNeeded) {
        // if we have a standby part, regardless if standby state, disable
        // actions. Same behavior for static html.
        if (isStandbyPartNeeded | standby) {
            homeAction.setEnabled(false);
            forwardAction.setEnabled(false);
            backAction.setEnabled(false);
        } else {
            homeAction.setEnabled(true);
            updateNavigationActionsState();
        }

        if (getModel().isDynamic())
            dynamicStandbyStateChanged(standby, isStandbyPartNeeded);
        else
            staticStandbyStateChanged(standby);
    }



    // ***************** Static Intro *****************
    private void handleStaticIntro() {
        // We have a static case. Set the url on the browser to be the url
        // defined in the root page. But first check memento if we can
        // restore last visited page.
        String url = getCachedCurrentPage();
        if (!isURL(url))
            // no cached state, or invalid state.
            url = getModel().getHomePage().getUrl();

        if (url == null) {
            // We have no content to display. log an error
            Log.error("Url is null; no content to display in browser", null); //$NON-NLS-1$
            return;
        }
        // set the URL the browser should display
        boolean success = browser.setUrl(url);
        if (!success) {
            Log.error("Unable to set the following ULR in browser: " + url, //$NON-NLS-1$
                null);
            return;
        }
    }

    public void staticStandbyStateChanged(boolean standby) {
        IntroHomePage homePage = getModel().getHomePage();
        IntroHomePage standbyPage = getModel().getStandbyPage();
        if (standbyPage == null)
            standbyPage = homePage;

        if (standby)
            browser.setUrl(standbyPage.getUrl());
        else
            browser.setUrl(homePage.getUrl());

    }


    public Browser getBrowser() {
        return browser;
    }
}