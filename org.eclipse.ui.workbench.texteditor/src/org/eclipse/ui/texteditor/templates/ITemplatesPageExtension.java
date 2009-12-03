/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Piotr Maj <pm@jcake.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor.templates;

import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateStore;


/**
 * Extension interface for {@link org.eclipse.ui.texteditor.templates.ITemplatesPage}.
 * Adds the following functions:
 * <ul>
 * 	<li>access to currently selected items on page</li>
 *  <li>access to template store which drives this page</li>
 * </ul>
 * <p>
 * 	This interface may be implemented by clients.
 * </p>
 * @since 3.6
 */
public interface ITemplatesPageExtension {

	/**
	 * Returns template store associated with this page.
	 * 
	 * @return template store
	 */
	TemplateStore getTemplateStore();

	/**
	 * Returns currently selected templates.
	 *
	 * @return an array with the currently selected templates
	 */
	TemplatePersistenceData[] getSelectedTemplates();
}
