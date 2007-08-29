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
package org.eclipse.ui.tests.views.properties.tabbed.override.folders;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.tests.views.properties.tabbed.model.Element;
import org.eclipse.ui.tests.views.properties.tabbed.override.items.IOverrideTestsItem;

/**
 * Interface for a TabFolder used by the properties view for the
 * TabFolder/TabItem example.
 * <p>
 * The OverrideTestsTabFolderPropertySheetPage example is a before look at the
 * properties view before the migration to the tabbed properties view and the
 * override tabs support. When elements are selected in the OverrideTestsView,
 * TabFolder/TabItem are displayed for the elements.
 * 
 * @author Anthony Hunter
 * @since 3.4
 */
public interface IOverrideTestsTabFolder {
	/**
	 * Add the listener from the item selection listeners.
	 * 
	 * @param listener
	 *            the item selection listener.
	 */
	public void addItemSelectionListener(
			IOverrideTestsTabItemSelectionListener listener);

	/**
	 * Determines if this folder applies to the element.
	 * 
	 * @param element
	 *            the element.
	 * @return <code>true</code> if this folder applies to the element.
	 */
	public boolean appliesTo(Element element);

	/**
	 * Creates the controls for the folder.
	 * 
	 * @param parent
	 *            the parent composite for the contents.
	 */
	public void createControls(Composite parent);

	/**
	 * Dispose the controls for the folder.
	 */
	public void dispose();

	/**
	 * Get the items for this folder.
	 * 
	 * @return the items for this folder.
	 */
	public IOverrideTestsItem[] getItem();

	/**
	 * Remove the listener from the item selection listeners.
	 * 
	 * @param listener
	 *            the item selection listener.
	 */
	public void removeItemSelectionListener(
			IOverrideTestsTabItemSelectionListener listener);

	/**
	 * Notifies the folder that the selected element has changed.
	 * 
	 * @param element
	 *            the selected element.
	 */
	public void selectionChanged(Element element);
}
