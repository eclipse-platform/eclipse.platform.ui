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
package org.eclipse.ui.cheatsheets;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Node;

/**
 * Base class for adding controls to cheat sheet items.
 * <p>
 * Subclasses are used in conjunction with the
 * <code>org.eclipse.ui.cheatsheet.cheatSheetItemExtension</code> extension
 * point. Subclasses must be public and have a public 1-arg constructor 
 * that takes the name of the attribute type <code>String</code>. The
 * extension point specifies the name of the subclass and the name of the XML
 * attribute that it can handle. When the cheat sheet framework encounters an
 * item (or subitem) element in the cheat sheet content file with an attribute
 * with a matching name, an instance of the corresponding item extension subclass
 * is created. It is up to this instance to parse the XML attribute and remember
 * any useful information. Later, when creating the visual controls for the item
 * are being created, the instance is given the opportunity to add extra controls.
 * </p>
 * 
 * @since 3.0 TODO (lorne) - replaced setAttributeName by constructor; made other methods abstract
 */
public abstract class AbstractItemExtensionElement {
	
	/**
	 * Name of the XML attribute that this item extension handles.
	 */
	private String attributeName;
	
	/**
	 * Creates a new item element extension for handling the
	 * XML attributes of the given name.
	 * 
	 * @param attributeName the name of the attribute that this item extension handles
	 * @exception IllegalArgumentException if <code>attributeName</code>
	 * is <code>null</code>
	 */
	public AbstractItemExtensionElement(String attributeName) {
		if (attributeName == null) {
			throw new IllegalArgumentException();
		}
		this.attributeName = attributeName;
	}

	/**
	 * Returns the name of the XML attribute that this item extension handles.
	 * 
	 * @return the name of the attribute that this item extension handles
	 */
	public String getAttributeName() {
		return this.attributeName;
	}
	
	/**
	 * Called by the cheat sheet framework to parse and extract information
	 * from the given XML attribute node (and whose name matches
	 * <code>getAttributeName()</code>.
	 * 
	 * @param node a node containing the attribute parsed from the cheat sheet content file
	 */
	public abstract void handleAttribute(Node node);

	/**
	 * Called by the cheat sheet framework when creating the visual
	 * representation of a step. This method should add small controls (buttons,
	 * images) to the given composite to decorate the step. The background of
	 * the new controls should be set to the given color so that they are
	 * visually integrated.
	 * 
	 * @param composite the composite to add extra controls to
	 * @param color the suggested background color
	 * TODO (lorne) - rather than pass in a color, could client not ask the composite for its bg color?
	 */
	public abstract void createControl(Composite composite, Color color);

}
