/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.editor.HockeyleagueEditor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * This is the property sheet page for the Hockeyleague model editor.
 * 
 * @author Anthony Hunter
 */
public class HockeyleaguePropertySheetPage
	extends TabbedPropertySheetPage {

	/**
	 * the hockey league model editor.
	 */
	protected HockeyleagueEditor editor;

	/**
	 * Contructor for this property sheet page.
	 * 
	 * @param tabbedPropertySheetPageContributor
	 *            the editor contributor of the property sheet page.
	 */
	public HockeyleaguePropertySheetPage(HockeyleagueEditor editor) {
		super(editor);
		this.editor = editor;
	}

	/**
	 * Get the EMF AdapterFactory for this editor.
	 * 
	 * @return the EMF AdapterFactory for this editor.
	 */
	public HockeyleagueEditor getEditor() {
		return editor;
	}

	/**
	 * Get the EMF AdapterFactory for this editor.
	 * 
	 * @return the EMF AdapterFactory for this editor.
	 */
	public AdapterFactory getAdapterFactory() {
		return editor.getAdapterFactory();
	}
}