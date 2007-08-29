/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed.override.items;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * Interface for an item used by the properties view for the override tabs
 * tests.
 * <p>
 * When the TabbedPropertySheetPage is used by the OverrideTestsView tests view,
 * each item is displayed in a ISection.
 * <p>
 * The OverrideTestsTabFolderPropertySheetPage example uses the items to display
 * in TabFolder/TabItem.
 * 
 * @author Anthony Hunter
 * @since 3.4
 */
public interface IOverrideTestsItem {

	/**
	 * Creates the controls for the item.
	 * 
	 * @param parent
	 *            the parent composite for the item.
	 * @param factory
	 *            the factory to create widgets for the item.
	 */
	public void createControls(Composite parent);

	/**
	 * Dispose the controls for the item.
	 */
	public void dispose();

	/**
	 * Get the root composite for the item.
	 * 
	 * @return the root composite for the item.
	 */
	public Composite getComposite();

	/**
	 * Get the kind of {@link Element} that this item applies to.
	 * 
	 * @return the kind of {@link Element} that this item applies to.
	 */
	public Class getElement();

	/**
	 * Get the icon image for the item.
	 * 
	 * @return the icon image for the item.
	 */
	public Image getImage();

	/**
	 * Get the text label for the item.
	 * 
	 * @return the text label for the item.
	 */
	public String getText();
}
