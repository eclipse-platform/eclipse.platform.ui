/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 * CSS stylable element interface to wrap native widget.
 */
public interface CSSStylableElement extends Element {

	/**
	 * Return the native widget wrapped (SWT widget, Swing Component...)
	 * 
	 * @return
	 */
	public Object getNativeWidget();

	/**
	 * Return the id of the native widget. This method is used to manage CSS
	 * style like this input#MyId{....}.
	 * 
	 * @return
	 */
	public String getCSSId();

	/**
	 * Return the class of the native widget. This method is used to manage CSS
	 * style like this .blueClass {...}.
	 * 
	 * @return
	 */
	public String getCSSClass();

	/**
	 * Return the inline style of the native widget.
	 * 
	 * @return
	 */
	public String getCSSStyle();

	/**
	 * Return the default {@link CSSStyleDeclaration} of the native widget.
	 * 
	 * @return
	 */
	public CSSStyleDeclaration getDefaultStyleDeclaration(String pseudoE);

	/**
	 * Copy all default style declarations defined into
	 * <code>stylableElement</code>.
	 * 
	 * @param stylableElement
	 */
	public void copyDefaultStyleDeclarations(CSSStylableElement stylableElement);

	/**
	 * Set the default {@link CSSStyleDeclaration} of the native widget.
	 * 
	 * @param defaultStyleDeclaration
	 */
	public void setDefaultStyleDeclaration(String pseudoE,
			CSSStyleDeclaration defaultStyleDeclaration);

	/**
	 * Return true if <code>s</code> is pseudo instance (ex :focus) and false
	 * otherwise.
	 * 
	 * @param s
	 * @return
	 */
	public abstract boolean isPseudoInstanceOf(String s);

	/**
	 * Return all static pseudo instances. Static pseudo instance is used for
	 * widget which define method which can update property (Color,
	 * BackgroundColor) for a special state (without manage listener like
	 * focus). For instance SWT CTabFolder#setSelectionBackground (Color color)
	 * is method which set background Color when a CTabItem is selected.
	 * 
	 * @return
	 */
	public String[] getStaticPseudoInstances();

	/**
	 * Return true if <code>s</code> is static pseudo instance (ex
	 * :CTabFolder:selected) and false otherwise. Static pseudo instance is used
	 * for widget which define method which can update property (Color,
	 * BackgroundColor) for a special state (without manage listener like
	 * focus). For instance SWT CTabFolder#setSelectionBackground (Color color)
	 * is method which set background color when a CTabItem is selected.
	 * 
	 * @param s
	 * @return
	 */
	public boolean isStaticPseudoInstance(String s);

	/**
	 * Call-back method called when styles are applied to the all children nodes
	 * of the native widget
	 * 
	 * @param nodes
	 */
	public void onStylesApplied(NodeList nodes);

	/**
	 * Return the {@link CSSExtendedProperties} of the native widget.
	 * 
	 * @return
	 */
	public CSSExtendedProperties getStyle();

	/**
	 * Method called after the {@link CSSStylableElement} was linked to the
	 * native widget. You can add UI listener into this method.
	 */
	void initialize();

	/**
	 * Method called when {@link CSSEngine#dispose()} is called. You can
	 * remove UI listener into this method.
	 */
	public void dispose();
}
