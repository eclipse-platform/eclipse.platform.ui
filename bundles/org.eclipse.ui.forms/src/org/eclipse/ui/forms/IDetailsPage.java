/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms;

import org.eclipse.swt.widgets.Composite;

/**
 * This interface should be implemented by clients providing
 * pages to handle object types in DetailsPart. Most of the
 * life cycle is the same as for the IFormPart. The page is
 * a part selection listener since selections in the master
 * part will be passed to the currently visible page.
 * 
 * @see DetailsPart
 * @see MasterDetailsBlock
 * @since 3.0
 */
public interface IDetailsPage extends IFormPart, IPartSelectionListener {
	/**
	 * Creates the contents of the page in the provided parent.
	 * @param parent the parent to create the page in
	 */
	void createContents(Composite parent);
}
