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

import org.eclipse.core.runtime.*;

/**
 * An intro config component that has an id attribute. All config components
 * can get to their configuration element.
 * <p>
 * Note: This is an abstract base class for all classes in the Intro Model.
 * <br>
 * Clients are not expected to implement/subclass this class, or any of its
 * subclasses.
 * </p>
 */
public abstract class IntroElement {

    /**
     * Type constant which identifies an IntroModelRoot element.
     */
    public static final int MODEL_ROOT = 1;

    /**
     * Type constant which identifies an IntroPartPresentation element.
     */
    public static final int PRESENTATION = 1 << 1;

    /**
     * Type constant which identifies an IntroHomePage element.
     */
    public static final int HOME_PAGE = 1 << 2;

    /**
     * Type constant which identifies the IntroPage element.
     */
    public static final int PAGE = 1 << 3;

    /**
     * Type constant which identifies the AbstractIntroPage element.
     */
    public static final int ABSTRACT_PAGE = HOME_PAGE | PAGE;

    /**
     * Type constant which identifies an IntroDiv element.
     */
    public static final int DIV = 1 << 4;

    /**
     * Type constant which identifies the AbstractIntroContainer element.
     */
    public static final int ABSTRACT_CONTAINER = ABSTRACT_PAGE | DIV
            | MODEL_ROOT;

    /**
     * Type constant which identifies the IntroHtml element.
     */
    public static final int HTML = 1 << 5;

    /**
     * Type constant which identifies the IntroLink element.
     */
    public static final int LINK = 1 << 6;

    /**
     * Type constant which identifies the AbstractText element.
     */
    public static final int ABSTRACT_TEXT = HTML | LINK;

    /**
     * Type constant which identifies the IntroImage element.
     */
    public static final int IMAGE = 1 << 7;

    /**
     * Type constant which identifies the IntroInclude element.
     */
    public static final int INCLUDE = 1 << 8;

    /**
     * Type constant which identifies the IntroText element.
     */
    public static final int TEXT = 1 << 9;

    /**
     * Type constant which identifies the IntroContainerExtension element.
     */
    public static final int CONTAINER_EXTENSION = 1 << 10;

    /**
     * Type constant which identifies the IntroHead element.
     */
    public static final int HEAD = 1 << 11;



    /**
     * Type constant which identifies any element in the Intro Model.
     */
    public static final int BASE_ELEMENT = ABSTRACT_CONTAINER | ABSTRACT_TEXT
            | IMAGE | INCLUDE | MODEL_ROOT | PRESENTATION | TEXT
            | CONTAINER_EXTENSION | HEAD;

    public static final String ID_ATTRIBUTE = "id";
    private IntroElement parent;

    // cached config element representing this component.
    private IConfigurationElement element;

    protected String id;

    IntroElement(IConfigurationElement element) {
        this.element = element;
        id = element.getAttribute(ID_ATTRIBUTE);
    }

    /**
     * @return Returns the element.
     */
    public IConfigurationElement getConfigurationElement() {
        return element;
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the specific model type of this intro element. To be implemented
     * by all subclasses.
     * 
     * @return returns one of the model class types defined in this class.
     */
    public abstract int getType();

    /**
     * Returns the parent of this intro element.
     * <p>
     * Rules:
     * <ul>
     * <li>For the model root, it retruns null.</li>
     * <li>For the introPart presentation it returns a model root.</li>
     * <li>For Pages, it returns an intro model root.</li>
     * <li>For all other elements, it retruns a subclass of abstract
     * container.</li>
     * <li>for divs that are children of configs (shared divs), it returns the
     * holding model root.</li>
     * <li>for Head elements that are children of Implementation elements
     * (shared Heads), it returns the holding prsentation element.</li>
     * </ul>
     * 
     * @return returns the parent of this intro element. Null only for model
     *         root.
     */
    public IntroElement getParent() {
        return parent;
    }

    /**
     * @param parent
     *            The parent to set.
     */
    protected void setParent(IntroElement parent) {
        this.parent = parent;
    }

    /**
     * Returns the parent page holding this intro element. For the model root
     * and the introPart presentation it returns null. For Pages, it returns
     * the page itself. For all other element, returns the holding page.
     * <p>
     * Exceptions:
     * <ul>
     * <li>for divs that are children of configs (shared divs), it returns
     * null.</li>
     * <li>for Head elements that are children of Implementation elements
     * (shared Heads), it returns null.</li>
     * </ul>
     */
    public AbstractIntroPage getParentPage() {
        // return yourself if you are a page.
        if (isOfType(IntroElement.ABSTRACT_PAGE))
            return (AbstractIntroPage) this;

        IntroElement parent = getParent();
        if (parent == null)
            return null;

        while (parent != null && parent.getParent() != null
                && !parent.isOfType(IntroElement.ABSTRACT_PAGE))
            parent = parent.getParent();
        if (parent.isOfType(ABSTRACT_PAGE))
            return (AbstractIntroPage) parent;
        else
            return null;
    }

    /**
     * Returns whether the element is among the specified element types.An
     * example of an element mask is as follows:
     * <p>
     * <code>
     *  	int elementMask = IntroElement.ABSTRACT_CONTAINER;
     * 		int elementMask = IntroElement.DIV | IntroElement.LINK;
     * </code>
     * 
     * @param elementMask
     *            element mask formed by bitwise OR of element type constants
     *            defined in this class.
     * @return <code>true</code> if this element has a matching type, and
     *         <code>false</code> otherwise.
     */
    public boolean isOfType(int elementMask) {
        return (getType() & elementMask) != 0;
    }

    /**
     * Returns whether the types of all the elements in the given array are
     * among the specified element types. <br>
     * An example of an element mask is as follows:
     * <p>
     * <code>
     * int elementMask = IntroElement.DIV | IntroElement.LINK;
     * </code>
     * 
     * @return <code>true</code> if all elements are of the right type, and
     *         <code>false</code> if the list is empty, or at least one
     *         element is not of the specified types.
     */
    public static final boolean allElementsAreOfType(IntroElement[] elements,
            int elementMask) {
        // if we have an empty list, no point going on.
        if (elements.length == 0)
            return false;

        for (int i = 0; i < elements.length; i++) {
            IntroElement element = elements[i];
            if (!element.isOfType(elementMask))
                return false;
        }
        return true;
    }

}
