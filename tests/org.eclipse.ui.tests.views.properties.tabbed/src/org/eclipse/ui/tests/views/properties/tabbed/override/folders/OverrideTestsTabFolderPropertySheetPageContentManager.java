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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.tests.views.properties.tabbed.model.Element;
import org.eclipse.ui.tests.views.properties.tabbed.override.OverrideTestsSelection;
import org.eclipse.ui.tests.views.properties.tabbed.override.OverrideTestsView;
import org.eclipse.ui.tests.views.properties.tabbed.override.items.IOverrideTestsItem;

/**
 * The content manager for the override tests property sheet page.
 * <p>
 * The OverrideTestsTabFolderPropertySheetPage example is a before look at the
 * properties view before the migration to the tabbed properties view and the
 * override tabs support. When elements are selected in the OverrideTestsView,
 * TabFolder/TabItem are displayed for the elements.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public class OverrideTestsTabFolderPropertySheetPageContentManager implements
		IOverrideTestsTabItemSelectionListener {

	private IOverrideTestsTabFolder activeFolder;

	private Composite composite;

	private IOverrideTestsTabFolder emptyFolder;

	private IOverrideTestsTabFolder[] folders;

	private OverrideTestsView overrideTestsView;

	public OverrideTestsTabFolderPropertySheetPageContentManager(
			Composite parent) {
		this.composite = parent;
		this.folders = new IOverrideTestsTabFolder[] { new BasicTabFolder(),
				new AdvancedTabFolder() };
		this.emptyFolder = new EmptyTabFolder();
	}

	@Override
	public void itemSelected(IOverrideTestsItem item) {
		overrideTestsView.setSelection(item.getElement());
	}

	/**
	 * Notifies the content manager that the selection has changed.
	 *
	 * @param part
	 *            the workbench part containing the selection
	 * @param selection
	 *            the current selection.
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		Assert.isTrue(part instanceof OverrideTestsView);
		this.overrideTestsView = (OverrideTestsView) part;
		Assert.isTrue(selection instanceof OverrideTestsSelection);
		Element element = ((OverrideTestsSelection) selection).getElement();
		IOverrideTestsTabFolder newFolder = null;

		if (element == null) {
			newFolder = emptyFolder;
		} else {
			for (int i = 0; i < folders.length; i++) {
				if (folders[i].appliesTo(element)) {
					newFolder = folders[i];
					break;
				}
			}
		}

		Assert.isTrue(newFolder != null);
		if (newFolder != activeFolder) {
			if (activeFolder != null) {
				activeFolder.removeItemSelectionListener(this);
				activeFolder.dispose();
			}
			activeFolder = newFolder;
			newFolder.createControls(composite);
			composite.layout(true);
			activeFolder.addItemSelectionListener(this);
		}
		if (element != null) {
			activeFolder.selectionChanged(element);
		}

	}
}
