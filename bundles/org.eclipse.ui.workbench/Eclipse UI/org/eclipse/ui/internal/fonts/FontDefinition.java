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
package org.eclipse.ui.internal.fonts;

import java.util.Collection;

import org.eclipse.core.runtime.Platform;


/**
 * The FontDefiniton is the representation of the fontDefinition
 * from the plugin.xml of a type.
 */
public class FontDefinition {

	private String label;
	private String id;
	private String defaultsTo;
	private String description;

	//The elements for use by the preference page
	public static FontDefinition[] definitions;

	/**
	 * Get the currently defined definitions for the workbench. Read them in if
	 * they are not set.
	 * @return FontDefinition[]
	 */
	public static FontDefinition[] getDefinitions() {
		if (definitions == null) {
			FontDefinitionReader reader = new FontDefinitionReader();
			Collection values =
				reader.readRegistry(Platform.getPluginRegistry());
			definitions = new FontDefinition[values.size()];
			values.toArray(definitions);
		}
		return definitions;
	}

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param fontName The name display
	 * ed in the preference page.
	 * @param uniqueId The id used to refer to this definition.
	 * @param defaultsId The id of the font this defaults to.
	 * @param fontDescription The description of the font in the preference page.
	 */
	FontDefinition(
		String fontName,
		String uniqueId,
		String defaultsId,
		String fontDescription) {
		this.label = fontName;
		this.id = uniqueId;
		this.defaultsTo = defaultsId;
		this.description = fontDescription;
	}

	/**
	 * Returns the defaultsTo. This is the id of the text font
	 * that this font defualts to.
	 * @return String or <pre>null</pre>.
	 */
	public String getDefaultsTo() {
		return defaultsTo;
	}

	/**
	 * Returns the description.
	 * @return String or <pre>null</pre>.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the label.
	 * @return String
	 */
	public String getLabel() {
		return label;
	}


	/**
	 * Returns the id.
	 * @return String
	 */
	public String getId() {
		return id;
	}


}
