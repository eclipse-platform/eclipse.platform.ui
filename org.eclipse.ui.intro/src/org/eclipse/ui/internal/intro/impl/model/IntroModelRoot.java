/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.impl.model;

import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.util.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.intro.impl.model.loader.*;
import org.eclipse.ui.internal.intro.impl.util.*;
import org.osgi.framework.*;
import org.w3c.dom.*;

/**
 * The root class for the OOBE model. It loads the configuration into the
 * appropriate classes.
 * 
 * Model rules:
 * <ol>
 * <li>if an attribute is not included in the markup, its value will be null in
 * the model.</li>
 * <li>Children of a given parent (ie: model root, page, or div) *must* have
 * distinctive IDs otherwise resolving includes and extensions may fail.</li>
 * <li>Containers have the concept of loading children and resolving children.
 * At the model root lovel, resolving children means resolving ALL extensions of
 * model. At the container level, resolving children means resolving includes.
 * </li>
 * <li>Extensions are resolved before includes at the container level to avoid
 * race conditions. eg: if a page includes a shared div and an extension extends
 * this shared div, you want the include to get the extended div and not the
 * original div.</li>
 * <li>Resolving extensions should not resolve includes. No need to load other
 * models when we dont have to.</li>
 * <li>unresolved includes are left as children of the parent container.</li>
 * <li>Unresolved extensions are left as children of the targetted model.</li>
 * </ol>
 */
public class IntroModelRoot extends AbstractIntroContainer {

    /**
     * Model constants that fire property change event when they are changed in
     * the model.
     */
    public static final int CURRENT_PAGE_PROPERTY_ID = 1;


    private static final String ATT_CONTENT = "content"; //$NON-NLS-1$


    // False if there is no valid contribution to the
    // org.eclipse.ui.into.config extension point. Start off with true, and set
    // to false whenever something bad happens.
    private boolean hasValidConfig = true;

    private IntroPartPresentation introPartPresentation;
    private IntroHomePage homePage;
    private String currentPageId;

    // the config extensions for this model.
    private IConfigurationElement[] configExtensionElements;

    // maintain listener list for model changes.
    private ListenerList propChangeListeners = new ListenerList(2);

    /**
     * Model root. Takes a configElement that represents <config>in the
     * plugin.xml markup AND all the extension contributed to this model through
     * the configExtension point.
     */
    public IntroModelRoot(IConfigurationElement configElement,
            IConfigurationElement[] configExtensionElements) {
        // the config element that represents the correct model root.
        super(configElement);
        this.configExtensionElements = configExtensionElements;

    }

    public void loadModel() {
        getChildren();
    }

    /**
     * loads the full model. The children of a model root are the presentation,
     * followed by all pages, and all shared divs. Then if the model has
     * extension, its the unresolved container extensions, followed by all
     * extension pages and divs. The presentation is loaded from the
     * IConfiguration element representing the config. All else is loaded from
     * xml content file.
     *  
     */
    protected void loadChildren() {
        children = new Vector();

        Log.info("Loading Intro plugin model...."); //$NON-NLS-1$

        // load presentation first and create the model class for it. If there
        // is more than one presentation, load first one, and log rest.
        IConfigurationElement presentationElement = loadPresentation();
        if (presentationElement == null) {
            // no presentations at all, exit.
            setModelState(true, false);
            Log.warning("Could not find presentation element in intro config."); //$NON-NLS-1$
            return;
        }

        introPartPresentation = new IntroPartPresentation(presentationElement);
        children.add(introPartPresentation);
        // set parent.
        introPartPresentation.setParent(this);

        // now load all children of the config. There should only be pages and
        // divs here. And order is not important. These elements are loaded from
        // the content file DOM.
        Document document = loadDOM(getCfgElement());
        if (document == null) {
            // we failed to parse the content file. Intro Parser would have
            // logged the fact. Parser would also have checked to see if the
            // content file has the correct root tag.
            setModelState(true, false);
            return;
        }

        loadPages(document, getBundle());
        loadSharedDivs(document, getBundle());

        setModelState(true, true);
    }


    /**
     * Resolve each include in this container's children.
     */
    protected void resolveChildren() {
        // now handle config extension.
        resolveConfigExtensions();
        resolved = true;
    }

    private IConfigurationElement loadPresentation() {
        // If there is more than one presentation, load first one, and log
        // rest.
        IConfigurationElement[] presentationElements = getCfgElement()
                .getChildren(IntroPartPresentation.TAG_PRESENTATION);

        IConfigurationElement presentationElement = ModelLoaderUtil
                .validateSingleContribution(presentationElements,
                        IntroPartPresentation.ATT_HOME_PAGE_ID);
        return presentationElement;
    }



    /**
     * Loads all pages defined in this config from the xml content file.
     */
    private void loadPages(Document dom, Bundle bundle) {
        String homePageId = getPresentation().getHomePageId();
        Element[] pages = ModelLoaderUtil.getElementsByTagName(dom,
                IntroPage.TAG_PAGE);
        for (int i = 0; i < pages.length; i++) {
            Element pageElement = pages[i];
            if (pageElement.getAttribute(IntroPage.ATT_ID).equalsIgnoreCase(
                    homePageId)) {
                // Create the model class for the Root Page.
                homePage = new IntroHomePage(pageElement, bundle);
                homePage.setParent(this);
                currentPageId = homePage.getId();
                children.add(homePage);
            } else {
                // Create the model class for an intro Page.
                IntroPage page = new IntroPage(pageElement, bundle);
                page.setParent(this);
                children.add(page);
            }
        }
    }

    /**
     * Loads all shared divs defined in this config, from the DOM.
     */
    private void loadSharedDivs(Document dom, Bundle bundle) {
        Element[] divs = ModelLoaderUtil.getElementsByTagName(dom,
                IntroDiv.TAG_DIV);
        for (int i = 0; i < divs.length; i++) {
            IntroDiv div = new IntroDiv(divs[i], bundle);
            div.setParent(this);
            children.add(div);
        }
    }

    /**
     * Handles all the configExtensions to this current model.
     *  
     */
    private void resolveConfigExtensions() {
        for (int i = 0; i < configExtensionElements.length; i++) {
            // get the pd from the extensions since they are defined in other
            // plugins.
            Bundle bundle = ModelLoaderUtil
                    .getBundleFromConfigurationElement(configExtensionElements[i]);

            Document dom = loadDOM(configExtensionElements[i]);
            if (dom == null)
                    // we failed to parse the content file. Intro Parser would
                    // have logged the fact. Parser would also have checked to
                    // see if the content file has the correct root tag.
                    continue;

            // Find the target of this container extension, and add all its
            // children to target. Make sure to pass pd to propagate to all
            // children.
            Element extensionContentElement = loadExtensionContent(dom, bundle);
            if (extensionContentElement == null)
                    // no extension content defined.
                    continue;

            if (extensionContentElement.hasAttribute("failed")) { //$NON-NLS-1$
                // we failed to resolve this configExtension, add the extension
                // as a child of this model.
                children.add(new IntroExtensionContent(extensionContentElement,
                        bundle));
                continue;
            }

            // Now load all pages and shared divs from this config extension
            // only if we resolved this extension. No point adding pages that
            // will never be referenced.
            Element[] pages = ModelLoaderUtil.getElementsByTagName(dom,
                    IntroPage.TAG_PAGE);
            for (int j = 0; j < pages.length; j++) {
                // Create the model class for an intro Page.
                IntroPage page = new IntroPage(pages[i], bundle);
                page.setParent(this);
                children.add(page);
            }

            // load all shared divs from all configExtensions to this model.
            loadSharedDivs(dom, bundle);
        }
    }

    /**
     * load the extension content of this configExtension into model classes,
     * and insert them at target. A config extension can have only ONE extension
     * content. This is because if the extension fails, we need to be able to
     * not include the page and div contributions as part of the model.
     * 
     * @param
     * @return
     */
    private Element loadExtensionContent(Document dom, Bundle bundle) {
        Element[] extensionContents = ModelLoaderUtil.getElementsByTagName(dom,
                IntroExtensionContent.TAG_CONTAINER_EXTENSION);
        // There should only be one container extension.
        Element extensionContentElement = ModelLoaderUtil
                .validateSingleContribution(extensionContents,
                        IntroExtensionContent.ATT_PATH);
        if (extensionContentElement == null)
                // no extensionContent defined.
                return null;

        // Create the model class.
        IntroExtensionContent extensionContent = new IntroExtensionContent(
                extensionContentElement, bundle);
        // now resolve this extension.
        String path = extensionContent.getPath();
        AbstractIntroElement target = findTarget(this, path);
        if (target == null)
            // target could not be found. Signal failure.
            extensionContentElement.setAttribute("failed", "true"); //$NON-NLS-1$ //$NON-NLS-2$

        else if (target.isOfType(AbstractIntroElement.ABSTRACT_CONTAINER)) {
            // extensions are only for container (ie: divs and pages)
            AbstractIntroContainer targetContainer = (AbstractIntroContainer) target;
            // make sure you load the children of the target container
            // because we want the children vector to be initialized and
            // loaded with children for ordering.
            if (!targetContainer.isLoaded())
                    targetContainer.loadChildren();
            targetContainer.addChildren(extensionContent.getChildren(), bundle);
            handleExtensionStyleInheritence(extensionContent, targetContainer);
        }
        return extensionContentElement;
    }

    /**
     * Updates the inherited styles based on the merge-style attribute. If we
     * are including a shared div, or if we are including an element from the
     * same page, do nothing. For inherited alt-styles, we have to cache the pd
     * from which we inherited the styles to be able to access resources in that
     * plugin.
     * 
     * @param include
     * @param target
     */
    private void handleExtensionStyleInheritence(
            IntroExtensionContent extension,
            AbstractIntroElement targetContainer) {

        if (targetContainer.getType() == AbstractIntroElement.DIV
                && targetContainer.getParent().getType() == AbstractIntroElement.MODEL_ROOT)
                // if we are extending a shared div, defined under a config, we
                // can not include styles.
                return;

        // Update the parent page styles. skip style if it is null;
        String style = extension.getStyle();
        if (style != null)
                targetContainer.getParentPage().addStyle(style);

        // for alt-style cache pd for loading resources.
        style = extension.getAltStyle();
        if (style != null) {
            Bundle bundle = extension.getBundle();
            targetContainer.getParentPage().addAltStyle(style, bundle);
        }
    }

    /**
     * Sets the model state based on all the model classes.
     */
    private void setModelState(boolean loaded, boolean hasValidConfig) {
        this.loaded = loaded;
        this.hasValidConfig = hasValidConfig;
    }

    /**
     * Returns true if there is a valid contribution to
     * org.eclipse.ui.intro.config extension point, with a valid Presentation,
     * and pages.
     * 
     * @return Returns the hasValidConfig.
     */
    public boolean hasValidConfig() {
        return hasValidConfig;
    }

    /**
     * @return Returns the introPartPresentation.
     */
    public IntroPartPresentation getPresentation() {
        return introPartPresentation;
    }

    /**
     * @return Returns the rootPage.
     */
    public IntroHomePage getHomePage() {
        return homePage;
    }

    /**
     * @return all pages *excluding* the Home Page. If all pages are needed,
     *         call <code>(AbstractIntroPage[])
     *         getChildrenOfType(IntroElement.ABSTRACT_PAGE);</code>
     */
    public IntroPage[] getPages() {
        return (IntroPage[]) getChildrenOfType(AbstractIntroElement.PAGE);
    }

    /**
     * @return Returns the isLoaded.
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * @return Returns the currentPageId.
     */
    public String getCurrentPageId() {
        return currentPageId;
    }

    /**
     * @param currentPageId
     *            The currentPageId to set.
     */
    public void setCurrentPageId(String currentPageId) {
        this.currentPageId = currentPageId;
        firePropertyChange(CURRENT_PAGE_PROPERTY_ID);
    }

    public void addPropertyListener(IPropertyListener l) {
        propChangeListeners.add(l);
    }

    /**
     * Fires a property changed event.
     * 
     * @param propertyId
     *            the id of the property that changed
     */
    protected void firePropertyChange(final int propertyId) {
        Object[] array = propChangeListeners.getListeners();
        for (int nX = 0; nX < array.length; nX++) {
            final IPropertyListener l = (IPropertyListener) array[nX];
            Platform.run(new SafeRunnable() {

                public void run() {
                    l.propertyChanged(this, propertyId);
                }

                public void handleException(Throwable e) {
                    super.handleException(e);
                    //If an unexpected exception happens, remove it
                    //to make sure the workbench keeps running.
                    propChangeListeners.remove(l);
                }
            });
        }
    }

    public void removePropertyListener(IPropertyListener l) {
        propChangeListeners.remove(l);
    }

    /**
     * @return Returns the currentPage. return null if page is not found, or if
     *         we are not in a dynamic intro mode.
     */
    public AbstractIntroPage getCurrentPage() {
        if (!homePage.isDynamic())
                return null;

        AbstractIntroPage page = null;
        IntroPage[] pages = getPages();
        for (int i = 0; i < pages.length; i++) {
            if (pages[i].getId() != null
                    && pages[i].getId().equals(currentPageId))
                    page = pages[i];
        }
        if (page != null)
                return page;
        // not a page. Test for root page.
        if (homePage.getId().equals(currentPageId))
                return homePage;
        // return null if page is not found.
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.IntroElement#getType()
     */
    public int getType() {
        return AbstractIntroElement.MODEL_ROOT;
    }


    /**
     * Assumes that the passed config element has a "content" attribute. Reads
     * it and loads a DOM based on that attribute value.
     * 
     * @return
     */
    protected Document loadDOM(IConfigurationElement cfgElement) {
        String content = cfgElement.getAttribute(ATT_CONTENT);
        // Resolve.
        content = IntroModelRoot.getPluginLocation(content, cfgElement);
        Document document = new IntroContentParser(content).getDocument();
        return document;
    }



    /*
     * ======================= Util Methods for Model. =======================
     */

    /**
     * Checks to see if the passed string is a valid URL (has a protocol), if
     * yes, returns it as is. If no, treats it as a resource relative to the
     * declaring plugin. Return the plugin relative location, fully qualified.
     * Retruns null if the passed string itself is null.
     * 
     * @param resource
     * @param pluginDesc
     * @return returns the URL as is if it had a protocol.
     */
    protected static String resolveURL(String url, String pluginId) {
        Bundle bundle = null;
        if (pluginId != null)
                // if pluginId is not null, use it.
                bundle = Platform.getBundle(pluginId);
        return resolveURL(url, bundle);
    }

    /**
     * Checks to see if the passed string is a valid URL (has a protocol), if
     * yes, returns it as is. If no, treats it as a resource relative to the
     * declaring plugin. Return the plugin relative location, fully qualified.
     * Retruns null if the passed string itself is null.
     * 
     * @param resource
     * @param pluginDesc
     * @return returns the URL as is if it had a protocol.
     */
    protected static String resolveURL(String url, IConfigurationElement element) {
        Bundle bundle = ModelLoaderUtil
                .getBundleFromConfigurationElement(element);
        return resolveURL(url, bundle);
    }

    /**
     * @see resolveURL(String url, IConfigurationElement element)
     */
    protected static String resolveURL(String url, Bundle bundle) {
        // quick exit
        if (url == null)
                return null;
        IntroURLParser parser = new IntroURLParser(url);
        if (parser.hasProtocol())
            return url;
        else
            // make plugin relative url. Only now we need the pd.
            return getPluginLocation(url, bundle);
    }



    /**
     * Returns the fully qualified location of the passed resource string from
     * the declaring plugin. If the file could not be loaded from the plugin,
     * the resource is returned as is.
     * 
     * @param resource
     * @return
     */
    public static String getPluginLocation(String resource,
            IConfigurationElement element) {
        Bundle bundle = ModelLoaderUtil
                .getBundleFromConfigurationElement(element);
        return getPluginLocation(resource, bundle);
    }

    public static String getPluginLocation(String resource, Bundle bundle) {

        // quick exits.
        if (resource == null || !ModelLoaderUtil.bundleHasValidState(bundle))
                return null;

        URL localLocation = null;
        try {
            // we need to perform a 'resolve' on this URL.
            localLocation = Platform.find(bundle, new Path(resource));
            if (localLocation == null) {
                // localLocation can be null if the passed resource could not
                // be found relative to the plugin. log fact, return resource,
                // as is.
                String msg = StringUtil.concat("Could not find resource: ", //$NON-NLS-1$
                        resource, " in ", ModelLoaderUtil.getBundleHeader( //$NON-NLS-1$
                                bundle, Constants.BUNDLE_NAME)).toString();
                Log.warning(msg);
                return resource;
            }
            localLocation = Platform.asLocalURL(localLocation);
            return localLocation.toExternalForm();
        } catch (Exception e) {
            String msg = StringUtil.concat("Failed to load resource: ", //$NON-NLS-1$
                    resource, " from ", ModelLoaderUtil.getBundleHeader(bundle, //$NON-NLS-1$
                            Constants.BUNDLE_NAME)).toString();
            Log.error(msg, e);
            return resource;
        }
    }

    /**
     * Returns the fully qualified location of the passed resource string from
     * the passed plugin id. If the file could not be loaded from the plugin,
     * the resource is returned as is.
     * 
     * @param resource
     * @return
     */
    public static String getPluginLocation(String resource, String pluginId) {
        Bundle bundle = Platform.getBundle(pluginId);
        return getPluginLocation(resource, bundle);
    }



}