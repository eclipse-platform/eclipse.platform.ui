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
 * <p>This interface is used for extending steps in the cheat sheet.
 * Each cheat sheet has a set of steps, and each of these steps potentially has a 
 * help link.  The help link is displayed in the top right hand corner of the step in the 
 * cheat sheet.  The cheatSheetItemExtension extension point may be used to add
 * graphical items to an item to the left of the help icon for the step.</p>
 * 
 * <p>To add graphical elements to the left of the help icon, the extension cheatSheetItemExtension is used.
 * An extra attribute tag is added to the "item" element in the cheat sheet content file.  This attribute's
 * name corresponds to the itemAttribute name specified in the cheatSheetItemExtension.  
 * The class name in the extension must specify a  class that implements ICheatSheetItemExtensionElement.</p>
 * 
 * <p>When the cheat sheet content file is initially read, if an attribute is found for an Item element that is not known
 * by the cheat sheets framework, the name of the attribute is checked against the itemAttribute name in 
 * all cheatSheetItemExtension extensions.  If the attribute matches the name specified in an extension,
 * an instance of the class specified in the extension is created.  The attribute specified in the cheat sheet content 
 * file that is unknown to the cheat sheets framework is then passed to the instance of ICheatSheetItemExtensionElement
 * using the handleAttribute(Node) method.  It is up to the instance of ICheatSheetItemExtensionElement to handle
 * parsing and storing of the attribute data from the cheat sheet content file.</p>
 * 
 * <p>When the attribute has been handled by the created instance of the interface,
 * the cheat sheet continues to load.  At the moment when the cheat sheet framework begins to generate the 
 * graphical components for the steps in the cheat sheet, the createControl method is called on the 
 * ICheatSheetItemExtensionElement and the implementation of this interface may add graphical components to the
 * Composite that is passed to it.</p>
 * 
 * @since 3.0
 */
public abstract class AbstractItemExtensionElement {

	/**
	 * this method is called by the cheat sheet framework when the step is being grahically 
	 * generated.  Extra graphical elements may be added to this composite here and will be 
	 * displayed to the left of the help icon for the step containing the unknown attribute in it's item
	 * element of the cheat sheet content file.  The developer must set the background of their graphics 
	 * to the color of the cheat sheet passed to them.
	 * @param c the composite to add graphical compontents to  
	 * @param color the color to set the backgrount of the graphical component to to make it look integrated
	 */
	public void createControl(Composite c, Color color) {
	}

	/**
	 * This method is called by the cheat sheet framework when an attribute is found for an item element in the 
	 * cheat sheet content file that the framework is unaware of, and hence does not know how to handle it.
	 * It is only called if the name of the attribute found matches the itemAttribute specified in the cheatSheetItemExtension
	 * extension.  If it matches, an instance of the class specified in the extension implementing this 
	 * ICheatSheetItemExtensionElement interface is created and this method is called with a Node 
	 * containing the attribute from the cheat sheet content file.
	 * @param n is a node containing the attribute parsed from the cheat sheet content file.  The attribute name 
	 * matches the itemAttribute specified in the cheatSheetItemExtension
	 */
	public void handleAttribute(Node n) {
	}

	/**
	 * This method sets the name of the attribute to match if an unknown attribute name is found in the item
	 * tag of the cheat sheet content file during parsing.  
	 * @param name the name of the attribute to match if an unknown attribute is found
	 */
	public void setAttributeName(String name) {
	}

	/**
	 * This method returns the name of the attribute to match in the cheat sheet content file.
	 * If an unknown attribute appears in the cheat sheet item tag, the name of that attribute will
	 * be compared to all the objects implementing ICheatSheetItemExtensionElement registered in the 
	 * cheatsheetItemExtension extension points.  If the attribute name matches, that object will handle the parsing
	 * of the attribute by a call to handleAttribute(Node n).
	 * @return the name of the attribute to match in the cheat sheet content file
	 */
	public String getAttributeName() {
		return null;
	}

}
