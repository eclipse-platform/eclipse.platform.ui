/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.impl.model;

import java.util.*;

import org.eclipse.ui.internal.intro.impl.*;
import org.eclipse.ui.internal.intro.impl.model.loader.*;
import org.eclipse.ui.internal.intro.impl.util.*;
import org.osgi.framework.*;
import org.w3c.dom.*;

/**
 * Base class for all Intro pages, inlcuding HomePage. Adds page specific
 * support:
 * <ul>
 * <li>support for page styles, and style inheritance</li>
 * <li>support for XHTML via a DOM instance var. Resolving the page is also
 * handled here.</li>
 */
public abstract class AbstractIntroPage extends AbstractIntroContainer {

    protected static final String TAG_PAGE = "page"; //$NON-NLS-1$
    private static final String ATT_STYLE = "style"; //$NON-NLS-1$
    private static final String ATT_ALT_STYLE = "alt-style"; //$NON-NLS-1$
    private static final String ATT_CONTENT = "content"; //$NON-NLS-1$
    private static final String INVALID_CONTENT = "invalidPage.xhtml"; //$NON-NLS-1$

    private String style;
    private String altStyle;
    private IntroPageTitle title;
    private String content;

    // DOM representing XHTML content. DOM is only cached in the case of XHTML
    // content.
    private Document dom;
    // set when the content file is loaded (ie: loadChildren is called)
    private boolean isXHTMLPage;

    /**
     * The vectors to hold all inhertied styles and alt styles from included
     * elements. They are lazily created when children are resolved (ie:
     * includes are resolved) OR when extensions are resolved and styles need to
     * be added to the target page.
     * <p>
     * Style Rules:
     * <ul>
     * <li>For includes, merge-style controls wether or not the enclosing page
     * inherits the styles of the target.
     * <li>If a page is including a shared div, merging target styles into this
     * page is ignored. Shared divs do not have styles.</li>
     * <li>For extensions, if the style or alt-style is not defined, that means
     * that no style inheritence is needed, and the style of the target page are
     * not updated.
     * <li>If an extension is extending a shared div, merging the styles of
     * this extension into the target is ignored. Shared divs do not have
     * styles.</li>
     * <li>Shared hashtable has alt-styles as keys and bundles as values.</li>
     * </ul>
     */
    private Vector styles;
    private Hashtable altStyles;

    /**
     * @param element
     */
    AbstractIntroPage(Element element, Bundle bundle) {
        super(element, bundle);
        content = getAttribute(element, ATT_CONTENT);
        if (content == null)
            init(element, bundle);
        else
            // Content is not null. Resolve it. Other attributes will be loaded
            // when xml content file is loaded
            content = BundleUtil.getResolvedBundleLocation(content, bundle);
    }

    private void init(Element element, Bundle bundle) {
        style = getAttribute(element, ATT_STYLE);
        altStyle = getAttribute(element, ATT_ALT_STYLE);

        // Resolve.
        style = BundleUtil.getResolvedBundleLocation(style, bundle);
        altStyle = BundleUtil.getResolvedBundleLocation(altStyle, bundle);

    }


    /**
     * The page's title. Each page can have one title.
     * 
     * @return Returns the title of this page.
     */
    public String getTitle() {
        // title is a child of the page, and so we have to load children first.
        // We also have to resolve children because someone might be including a
        // title. Update title instance after all includes and extensions have
        // been resolved.
        getChildren();
        if (title == null) {
            // there should only be one title child per page. safe to cast.
            IntroPageTitle[] titles = (IntroPageTitle[]) getChildrenOfType(AbstractIntroElement.PAGE_TITLE);
            if (titles.length > 0)
                title = titles[0];
        }

        if (title == null)
            // still null. no title.
            return null;
        else
            return title.getTitle();
    }

    /**
     * @return Returns the style.
     */
    public String getStyle() {
        return style;
    }

    /**
     * @return Returns the alt_style.
     */
    public String getAltStyle() {
        return altStyle;
    }

    /**
     * Gets all the inherited styles of this page. Styles can be inherited from
     * includes or from configExtensions.
     * <p>
     * Note: this call needs to get all the children of this page, and so it
     * will resolve this page. might be expensive.
     * 
     * @return Returns all the inherited styles of this page. Returns an empty
     *               array if page is not expandable or does not have inherited
     *               styles.
     */
    public String[] getStyles() {
        // call get children first to resolve includes and populate styles
        // vector. Resolving children will initialize the style vectors.
        getChildren();
        String[] stylesArray = new String[styles.size()];
        styles.copyInto(stylesArray);
        return stylesArray;
    }

    /**
     * Gets all the inherited alt-styles of this page (ie: styles for the SWT
     * presentation). A hashtable is returned that has inhertied alt-styles as
     * keys, and plugin descriptors as values. This is needed to be able to load
     * resources from the inherited target plugin. Note: this call needs to get
     * all the children of this page, and so its will resolve this page. might
     * be expensive.
     * 
     * @return Returns all the inherited styles of this page. Returns an empty
     *               hashtable if page is not expandable, does not have any includes,
     *               or has includes that do not merge styles.
     */
    public Hashtable getAltStyles() {
        // call get children first to resolve includes and populate hashtable.
        // Resolving children will initialize the style vectors.
        getChildren();
        return altStyles;
    }

    /**
     * Adds the given style to the list. Style is not added if it already exists
     * in the list.
     * 
     * @param style
     */
    protected void addStyle(String style) {
        initStylesVectors();
        if (styles.contains(style))
            return;
        styles.add(style);
    }

    public void insertStyle(String style, int location) {
        initStylesVectors();
        if (styles.contains(style))
            return;
        styles.add(location, style);
    }

    /**
     * Adds the given style to the list.Style is not added if it already exists
     * in the list.
     * 
     * @param altStyle
     */
    protected void addAltStyle(String altStyle, Bundle bundle) {
        initStylesVectors();
        if (altStyles.containsKey(altStyle))
            return;
        altStyles.put(altStyle, bundle);
    }


    /**
     * Util method to add given styles to the list.
     *  
     */
    protected void addStyles(String[] styles) {
        for (int i = 0; i < styles.length; i++)
            addStyle(styles[i]);
    }

    /**
     * Util method to add map of altstyles to list.
     */
    protected void addAltStyles(Hashtable altStyles) {
        // 
        this.altStyles.putAll(altStyles);
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.IntroElement#getType()
     */
    public int getType() {
        return AbstractIntroElement.ABSTRACT_PAGE;
    }

    /*
     * Override parent behavior to lazily initialize styles vectors. This will
     * only be called once, if resolved == false. In the case of DOM model,
     * resolve this page's full DOM.
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroContainer#resolveChildren()
     */
    protected void resolveChildren() {
        initStylesVectors();
        // flag would be set
        if (isXHTMLPage)
            resolvePage();
        else
            super.resolveChildren();
    }

    private void initStylesVectors() {
        if (styles == null)
            // delay creation until needed.
            styles = new Vector();
        if (altStyles == null)
            // delay creation until needed.
            altStyles = new Hashtable();
    }

    /**
     * Override parent behavior to add support for HEAD & Title element in pages
     * only, and not in divs.
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroContainer#getModelChild(org.eclipse.core.runtime.IConfigurationElement)
     */
    protected AbstractIntroElement getModelChild(Element childElement,
            Bundle bundle) {
        AbstractIntroElement child = null;
        if (childElement.getNodeName().equalsIgnoreCase(IntroHead.TAG_HEAD)) {
            child = new IntroHead(childElement, bundle);
        } else if (childElement.getNodeName().equalsIgnoreCase(
                IntroPageTitle.TAG_TITLE)) {
            // if we have a title, only add it as a child if we did not load one
            // before. A page can only have one title.
            if (title == null) {
                child = new IntroPageTitle(childElement, bundle);
            }
        }
        if (child != null)
            return child;
        return super.getModelChild(childElement, bundle);
    }

    /**
     * Returns all head contributions in this page. There can be more than one
     * head contribution in the page;
     * 
     * @return
     */
    public IntroHead[] getHTMLHeads() {
        return (IntroHead[]) getChildrenOfType(AbstractIntroElement.HEAD);
    }


    /**
     * load the children of this container. Override parent behavior because we
     * want to support loading content from other xml files. The design is that
     * only the id and content from the existing page are honored. All other
     * attributes are what they are defined in the external page, for 3.0 style
     * content. For XHTML content, all info is in the xhtml page. If we fail to
     * load the page, display the Invalid Page page.
     *  
     */
    protected void loadChildren() {
        if (content == null) {
            // no content. do regular loading.
            super.loadChildren();
            return;
        }

        // content attribute is defined. It either points to an XHTML file, or a
        // introContent.xml file. Process each case. Assume it is an
        // introContent
        IntroContentParser parser = new IntroContentParser(content);
        Document dom = parser.getDocument();
        if (dom == null) {
            // bad xml. This could be bad XHTML or bad intro XML. Parser would
            // have logged fact. Load dom for invalid page.
            String invalidContentFilePath = BundleUtil
                    .getResolvedBundleLocation(INVALID_CONTENT, IntroPlugin
                            .getDefault().getBundle());
            parser = new IntroContentParser(invalidContentFilePath);
            dom = parser.getDocument();
            // make sure to override content attribute to resolve the Invalid
            // Page page correctly.
            content = invalidContentFilePath;
        }

        // parser content depending on type. Make sure to set the loaded flag
        // accordongly, otherwise content file will always be parsed.
        if (parser.hasXHTMLContent())
            loadXHTMLContent(dom);
        else
            // load the first page with correct id, from content xml file.
            loadXMLContent(dom);
    }

    /**
     * Load the xml content from the introContent.xml file pointed to by the
     * content attribute, and loaded into the passed DOM. Load the first page
     * with correct id from this content file.
     * 
     * @param dom
     */
    private void loadXMLContent(Document dom) {
        Element[] pages = ModelUtil.getElementsByTagName(dom,
                IntroPage.TAG_PAGE);
        if (pages.length == 0) {
            Log.warning("Content file has no pages."); //$NON-NLS-1$
            return;
        }
        // point the element of this page to the new element. Pick first page
        // with matching id. Make sure to disable loading of children of current
        // element if a matching page in the external content file is not found.
        boolean foundMatchingPage = false;
        for (int i = 0; i < pages.length; i++) {
            Element pageElement = pages[i];
            if (pageElement.getAttribute(IntroPage.ATT_ID).equals(getId())) {
                this.element = pageElement;
                // call init on the new element. the filtering and the style-id
                // are loaded by the parent class.
                init(pageElement, getBundle());
                // TODO: revisit. Special processing here should be made more
                // general. we know id is correct.
                style_id = element
                        .getAttribute(AbstractBaseIntroElement.ATT_STYLE_ID);
                filteredFrom = element
                        .getAttribute(AbstractBaseIntroElement.ATT_FILTERED_FROM);
                foundMatchingPage = true;
            }
        }
        if (foundMatchingPage)
            // now do children loading as usual.
            super.loadChildren();
        else {
            // page was not found in content file. Perform load actions, and log
            // fact. init the children vector.
            children = new Vector();
            loaded = true;
            // free DOM model for memory performance.
            element = null;
            Log.warning("Content file does not have page with id= " + getId()); //$NON-NLS-1$
        }
    }

    private void loadXHTMLContent(Document dom) {
        // no need to load any children since we use XSLT to print XHTML
        // content. Simply cache DOM.
        this.dom = dom;
        this.isXHTMLPage = true;
        // init empty children vector.
        children = new Vector();
        loaded = true;
    }

    /**
     * Returns the DOM representing an external XHTML file. May return null if
     * extension content is 3.0 format. The page is resolved before returning,
     * meaning includes are resolved, and the basde of the page is defined.
     * 
     * @return
     */
    public Document getResolvedDocument() {
        // we need to force a getChildren to resolve the page.
        getChildren();
        return dom;
    }


    /**
     * Returns the DOM representing an external XHTML file. May return null if
     * extension content is 3.0 format. The page is NOT resolved before
     * returning. It is retruned as given by the dom parser.
     * 
     * @return
     */
    public Document getDocument() {
        // we only need to load children here.
        if (!loaded)
            loadChildren();
        return dom;
    }


    /**
     * Returns whether or not we have an XHTML page as the content for this
     * page. The XHTML page is defined through the content attribute. This
     * method forces the content file to be parsed and loaded in memory.
     * 
     * @return
     */
    public boolean isXHTMLPage() {
        // we need to force loading of children since we need to determine
        // content type. Load the children without resolving (for performance),
        // this will set the XHTML flag at the page level.
        if (!loaded)
            loadChildren();
        return isXHTMLPage;
    }


    /**
     * Deep searches all children in this container's DOM for the first child
     * with the given id. The element retrieved must have the passed local name.
     * Note that in an XHTML file (aka DOM) elements should have a unique id
     * within the scope of a document. We use local name because this allows for
     * finding intro anchors, includes and dynamic content element regardless of
     * whether or not an xmlns was used in the xml. note: could not have used
     * inheritance from parent container because return type for parent is intro
     * legacy model.
     * 
     * @param id
     * @return
     */
    public Element findDomChild(String id, String localElementName) {
        if (!loaded)
            loadChildren();
        // using getElementById is tricky and we need to have intro XHTML
        // modules to properly use tis method.
        NodeList children = dom.getElementsByTagNameNS("*", localElementName); //$NON-NLS-1$
        for (int i = 0; i < children.getLength(); i++) {
            Element anchor = (Element) children.item(i);
            if (anchor.getAttribute("id").equals(id)) //$NON-NLS-1$
                return anchor;
        }
        // anchor not found.
        return null;
    }

    /**
     * Search for any element with the given id.
     * 
     * @param id
     * @return
     */
    public Element findDomChild(String id) {
        return findDomChild(id, "*"); //$NON-NLS-1$

    }



    /**
     * Resolves the full page. It is called just before the page needs to be
     * displayed.
     * <li>adds a BASE child to the DOM HEAD element, if one is not defined.
     * All intro documents must have a base defined to resolve all uris.</li>
     * <li>resolves all includes in the page. This means importing target DOM.
     * </li>
     * <li>resolves all XHTML attributes for resources, eg: src, href
     * attributes.</li>
     */
    protected void resolvePage() {
        // insert base meta-tag, and resolve includes.
        ModelUtil.insertBase(dom, ModelUtil.getFolderPath(content));
        resolveIncludes();
        // now remove all anchors from this page.
        ModelUtil.removeElement(dom, IntroAnchor.TAG_ANCHOR);
        resolved = true;
    }

    /**
     * Resolves all includes in this page. This means importing the DOM of the
     * target path into the current page DOM, and resolving XHTML attributes for
     * resources.
     */
    protected void resolveIncludes() {
        // get all includes elements in DOM.
        NodeList includes = dom.getElementsByTagNameNS("*", //$NON-NLS-1$
                IntroInclude.TAG_INCLUDE);
        // get the array version of the include nodelist to work around
        // replaceChild() DOM api design.
        Node[] nodes = ModelUtil.getArray(includes);
        for (int i = 0; i < nodes.length; i++) {
            Element includeElement = (Element) nodes[i];
            IntroInclude include = new IntroInclude(includeElement, getBundle());
            // result[0] is target parent page, result[1] is target element.
            Object[] results = findDOMIncludeTarget(include);
            Element targetElement = (Element) results[1];
            if (targetElement == null) {
                // INTRO: fix log strings.
                String message = "Could not resolve following include:  " //$NON-NLS-1$
                        + ModelLoaderUtil.getLogString(includeElement,
                                IntroInclude.ATT_PATH);
                Log.warning(message);
                return;
            }

            // insert the target element instead of the include.
            Node targetNode = dom.importNode(targetElement, true);
            // update the src attribute of this node, if defined by w3
            // specs.
            String targetContentFilePath = ((AbstractIntroPage) results[0])
                    .getContent();
            ModelUtil.updateResourceAttributes((Element) targetNode,
                    targetContentFilePath);
            // use of replace API to remove include element is tricky. It
            // confuses the NodeList used in the loop above. Removing an include
            // removes it from the NodeList. Used cloned Array instead.
            includeElement.getParentNode().replaceChild(targetNode,
                    includeElement);
        }
    }


    /**
     * Find the target Element pointed to by the path in the include. It is
     * assumed that configId always points to an external config, and not the
     * same config of the inlcude.
     * 
     * @param include
     * @param path
     * @return
     */
    private Object[] findDOMIncludeTarget(IntroInclude include) {
        String path = include.getPath();
        IntroModelRoot targetModelRoot = (IntroModelRoot) getParentPage()
                .getParent();
        String targetConfigID = include.getConfigId();
        if (targetConfigID != null)
            targetModelRoot = ExtensionPointManager.getInst().getModel(
                    targetConfigID);
        if (targetModelRoot == null)
            // if the target config was not found, skip this include.
            return null;
        return findDOMTarget(targetModelRoot, path);

    }



    /**
     * Finds the child element that corresponds to the given path in the passed
     * model.
     * 
     * @param model
     *                   model containing target path.
     * @param path
     *                   the path to look for
     * @param results
     *                   two object array that will return the target intro page as the
     *                   first result, and the actual target DOM Element as the second
     *                   result. It is gauranteed to not be null. Content may be null.
     * @return target DOM element
     */
    public Object[] findDOMTarget(IntroModelRoot model, String path) {
        Object[] results = new Object[2];
        // path must be pageId/anchorID in the case of of XHTML pages.
        // pages.
        String[] pathSegments = path.split("/"); //$NON-NLS-1$
        if (pathSegments.length != 2)
            // path does not have correct format. Return empty results.
            return results;

        // save to cast.
        AbstractIntroPage targetPage = (AbstractIntroPage) model.findChild(
                pathSegments[0], ABSTRACT_PAGE);

        if (targetPage != null) {
            results[0] = targetPage;
            Element targetElement = targetPage.findDomChild(pathSegments[1]);
            if (targetElement != null)
                results[1] = targetElement;
        }
        return results;
    }


    /**
     * @return Returns the content.
     */
    public String getContent() {
        return content;
    }



    /**
     * Deep copy since class has mutable objects.
     */
    public Object clone() throws CloneNotSupportedException {
        AbstractIntroPage clone = (AbstractIntroPage) super.clone();
        if (title != null) {
            IntroPageTitle clonedTitle = (IntroPageTitle) title.clone();
            clonedTitle.setParent(clone);
            clone.title = clonedTitle;
        }
        // styles are safe for a shallow copy.
        if (styles != null)
            clone.styles = (Vector) styles.clone();
        if (altStyles != null)
            clone.altStyles = (Hashtable) altStyles.clone();
        return clone;
    }

}



