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

package org.eclipse.ui.internal.intro.impl.model;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.intro.impl.model.loader.*;
import org.w3c.dom.*;

/**
 * An intro config component that is a container, ie: it can have children.
 */
public abstract class AbstractIntroContainer extends AbstractBaseIntroElement {

    // vector is lazily created when children are populated.
    protected Vector children;
    protected boolean loaded = false;
    protected boolean resolved = false;

    private Element element;

    /**
     * @param element
     */
    AbstractIntroContainer(IConfigurationElement element) {
        super(element);
    }

    /**
     * @param element
     */
    AbstractIntroContainer(Element element, IPluginDescriptor pd) {
        super(element, pd);
        this.element = element;
    }

    /**
     * Get the children of this container. Loading children and resolving
     * includes and extension is delayed until this method call.
     * 
     * @return Returns all the children of this container.
     */
    public AbstractIntroElement[] getChildren() {
        if (!loaded)
            loadChildren();

        if (!resolved)
            resolveChildren();

        AbstractIntroElement[] childrenElements = (AbstractIntroElement[]) convertToModelArray(
                children, AbstractIntroElement.ELEMENT);
        return childrenElements;
    }

    /**
     * Returns all the children of this container that are of the specified
     * type(s). <br>
     * An example of an element mask is as follows:
     * <p>
     * <code>
     * 		int elementMask = IntroElement.IMAGE | IntroElement.LINK;
     * 		int elementMask = IntroElement.ABSTRACT_CONTAINER; 
     * </code>
     * The return type is determined depending on the mask. If the mask is a
     * predefined constant in the IntroElement, and it does not correspond to an
     * abstract model class, then the object returned can be safely cast to an
     * array of the corresponding model class. For exmaple, the following code
     * gets all divs in the given page, in the same order they appear in the
     * plugin.xml markup:
     * <p>
     * <code>
     * 		IntroDiv[] divs  = (IntroDiv[])page.getChildrenOfType(IntroElement.DIV);
     * </code>
     * 
     * However, if the element mask is not homogenous (for example: LINKS | DIV)
     * then the returned array must be cast to an array of IntroElements.For
     * exmaple, the following code gets all images and links in the given page,
     * in the same order they appear in the plugin.xml markup:
     * <p>
     * <code>
     * 		int elementMask = IntroElement.IMAGE | IntroElement.LINK;
     * 		IntroElement[] imagesAndLinks  = 
     * 			(IntroElement[])page.getChildrenOfType(elementMask);
     * </code>
     * 
     * @return An array of elements of the right type. If the container has no
     *         children, or no children of the specified types, returns an empty
     *         array.
     */
    public Object[] getChildrenOfType(int elementMask) {

        AbstractIntroElement[] childrenElements = getChildren();
        // if we have no children, we still need to return an empty array of
        // the correct type.
        Vector typedChildren = new Vector();
        for (int i = 0; i < childrenElements.length; i++) {
            AbstractIntroElement element = childrenElements[i];
            if (element.isOfType(elementMask))
                typedChildren.addElement(element);
        }
        return convertToModelArray(typedChildren, elementMask);
    }

    /**
     * Utility method to convert all the content of a vector of
     * AbstractIntroElements into an array of IntroElements cast to the correct
     * class type. It is assumed that all elements in this vector are
     * IntroElement instances. If elementMask is a predefined model type (ie:
     * homogenous), then return array of corresponding type. Else, returns an
     * array of IntroElements.
     * 
     * @param vector
     */
    private Object[] convertToModelArray(Vector vector, int elementMask) {
        int size = vector.size();
        Object[] src = null;
        switch (elementMask) {
        // homogenous vector.
        case AbstractIntroElement.DIV:
            src = new IntroDiv[size];
            break;
        case AbstractIntroElement.LINK:
            src = new IntroLink[size];
            break;
        case AbstractIntroElement.TEXT:
            src = new IntroText[size];
            break;
        case AbstractIntroElement.IMAGE:
            src = new IntroImage[size];
            break;
        case AbstractIntroElement.HTML:
            src = new IntroHTML[size];
            break;
        case AbstractIntroElement.INCLUDE:
            src = new IntroInclude[size];
            break;
        case AbstractIntroElement.PAGE:
            src = new IntroPage[size];
            break;
        case AbstractIntroElement.ABSTRACT_PAGE:
            src = new AbstractIntroPage[size];
            break;
        case AbstractIntroElement.ABSTRACT_CONTAINER:
            src = new AbstractIntroContainer[size];
            break;
        case AbstractIntroElement.HEAD:
            src = new IntroHead[size];
            break;
        case AbstractIntroElement.PAGE_TITLE:
            src = new IntroPageTitle[size];
            break;

        default:
            // now handle left over abstract types. Vector is not homogenous.
            src = src = new AbstractIntroElement[size];
            break;
        }
        if (src == null)
            return new Object[0];
        else {
            vector.copyInto(src);
            return src;
        }
    }

    /**
     * Load all the children of this container. A container can have other
     * containers, links, htmls, text, image, include. Load them in the order
     * they appear in the xml content file.
     */
    protected void loadChildren() {
        // init the children vector.
        children = new Vector();

        NodeList nodeList = element.getChildNodes();
        Vector vector = new Vector();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE)
                vector.add(node);
        }
        Element[] filteredElements = new Element[vector.size()];
        vector.copyInto(filteredElements);

        addChildren(filteredElements, getPluginDesc());
        loaded = true;
    }

    /**
     * Adds children of a container (div and page) to this container.
     * 
     * @param childElements
     */
    protected void addChildren(Element[] childElements, IPluginDescriptor pd) {
        // loop through each child, in order as appearing in XML, and load the
        // model classes.
        for (int i = 0; i < childElements.length; i++) {
            Element childElement = childElements[i];
            AbstractIntroElement child = getModelChild(childElement, pd);
            if (child != null) {
                child.setParent(this);
                children.addElement(child);
            }
        }
    }

    /**
     * Adds a child to this container, depending on its type. Subclasses may
     * override if there is a child specific to the subclass.
     * 
     * @param childElements
     */
    protected AbstractIntroElement getModelChild(Element childElement,
            IPluginDescriptor pd) {

        AbstractIntroElement child = null;
        if (childElement.getNodeName().equalsIgnoreCase(IntroDiv.TAG_DIV))
            child = new IntroDiv(childElement, pd);
        else if (childElement.getNodeName()
                .equalsIgnoreCase(IntroLink.TAG_LINK))
            child = new IntroLink(childElement, pd);
        else if (childElement.getNodeName()
                .equalsIgnoreCase(IntroText.TAG_TEXT))
            child = new IntroText(childElement, pd);
        else if (childElement.getNodeName().equalsIgnoreCase(
                IntroImage.TAG_IMAGE))
            child = new IntroImage(childElement, pd);
        else if (childElement.getNodeName()
                .equalsIgnoreCase(IntroHTML.TAG_HTML))
            child = new IntroHTML(childElement, pd);
        else if (childElement.getNodeName().equalsIgnoreCase(
                IntroInclude.TAG_INCLUDE))
            child = new IntroInclude(childElement, pd);
        return child;
    }


    /**
     * Resolve each include in this container's children.
     */
    protected void resolveChildren() {
        for (int i = 0; i < children.size(); i++) {
            AbstractIntroElement child = (AbstractIntroElement) children
                    .elementAt(i);
            if (child.getType() == AbstractIntroElement.INCLUDE)
                resolveInclude((IntroInclude) child);
        }
        resolved = true;
    }

    /**
     * Resolves an include. Gets the intro element pointed to by the include,
     * and adds it as a child of this current container. If target is not a div,
     * or any element that can be included in a div, ignore this include.
     * 
     * @param include
     */
    private void resolveInclude(IntroInclude include) {
        AbstractIntroElement target = findIncludeTarget(include);
        if (target == null)
            // target could not be found.
            return;
        if (target.isOfType(AbstractIntroElement.DIV
                | AbstractIntroElement.ABSTRACT_TEXT
                | AbstractIntroElement.IMAGE | AbstractIntroElement.TEXT))
            insertTarget(include, target);
    }

    /**
     * Find the target element pointed to by the path in the include. It is
     * assumed that configId always points to an external config, and not the
     * same config of the inlcude.
     * 
     * @param include
     * @param path
     * @return
     */
    private AbstractIntroElement findIncludeTarget(IntroInclude include) {
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
        AbstractIntroElement target = findTarget(targetModelRoot, path);
        return target;
    }

    /**
     * Finds a path in a model.
     * 
     * @param model
     * @param path
     * @return
     */
    protected AbstractIntroElement findTarget(IntroModelRoot model, String path) {
        // extract path segments. Get first segment to start search.
        String[] pathSegments = path.split("/");
        if (model == null)
            // if the target config was not found, return.
            return null;

        AbstractIntroElement target = model.findChild(pathSegments[0]);
        if (target == null)
            // there is no element with the specified path.
            return null;

        // found parent segment. now find each child segment.
        for (int i = 1; i < pathSegments.length; i++) {
            if (!target.isOfType(AbstractIntroElement.ABSTRACT_CONTAINER))
                // parent is not a container, so no point going on.
                return null;
            String pathSegment = pathSegments[i];
            target = ((AbstractIntroContainer) target).findChild(pathSegment);
            if (target == null)
                // tried to find next segment and failed.
                return null;
        }
        return target;
    }


    /*
     * searches direct children for a given child with an id.
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.IntroElement#getType()
     */
    public AbstractIntroElement findChild(String elementId) {
        if (!loaded)
            loadChildren();

        for (int i = 0; i < children.size(); i++) {
            AbstractIntroElement aChild = (AbstractIntroElement) children
                    .elementAt(i);
            if (!aChild.isOfType(BASE_ELEMENT))
                // includes and heads do not have ids, and so can not be
                // referenced directly. This means that they can not be targets
                // for other includes. Skip, just in case someone adds an id to
                // it!
                continue;
            AbstractBaseIntroElement child = (AbstractBaseIntroElement) aChild;
            if (child.getId() != null && child.getId().equals(elementId))
                return child;
        }
        // no child found.
        return null;
    }

    private void insertTarget(IntroInclude include, AbstractIntroElement target) {
        int includeLocation = children.indexOf(include);
        if (includeLocation == -1)
            // should never be here.
            return;
        children.remove(includeLocation);
        // handle merging target styles first, before changing target parent.
        handleIncludeStyleInheritence(include, target);
        // set parent of target to this new container.
        target.setParent(this);
        children.insertElementAt(target, includeLocation);
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
    private void handleIncludeStyleInheritence(IntroInclude include,
            AbstractIntroElement target) {

        if (include.getMergeStyle() == false)
            // target styles are not needed. nothing to do.
            return;

        if (target.getParent().getType() == AbstractIntroElement.MODEL_ROOT
                || target.getParentPage().equals(include.getParentPage()))
            // If we are including from this same page ie: target is in the
            // same page, OR if we are including a shared div, defined
            // under a config, do not include styles.
            return;

        // Update the parent page styles. skip style if it is null;
        String style = target.getParentPage().getStyle();
        if (style != null)
            getParentPage().addStyle(style);

        // for alt-style cache pd for loading resources.
        style = target.getParentPage().getAltStyle();
        if (style != null) {
            IPluginDescriptor pd = target.getPluginDesc();
            getParentPage().addAltStyle(style, pd);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.IntroElement#getType()
     */
    public int getType() {
        return AbstractIntroElement.ABSTRACT_CONTAINER;
    }

    /**
     * @return Returns the loaded.
     */
    protected boolean isLoaded() {
        return loaded;
    }

    /**
     * @return Returns the class id.
     */
    public String getClassId() {
        return super.class_id;
    }

}