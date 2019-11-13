/*******************************************************************************
 * Copyright (c) 2001, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.properties.tabbed;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.PropertySheetPage;

/**
 * An advanced section that is intended to show the original table format properties view
 * provided by base Eclipse.
 *
 * @author Anthony Hunter
 */
public class AdvancedPropertySection
	extends AbstractPropertySection {

	/**
	 * The Property Sheet Page.
	 */
	protected PropertySheetPage page;

	@Override
	public void createControls(Composite parent,
			final TabbedPropertySheetPage atabbedPropertySheetPage) {
		super.createControls(parent, atabbedPropertySheetPage);
		Composite composite = getWidgetFactory()
			.createFlatFormComposite(parent);
		page = new PropertySheetPage();

		page.createControl(composite);
		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		page.getControl().setLayoutData(data);

		page.getControl().addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent e) {
				atabbedPropertySheetPage.resizeScrolledComposite();
			}
		});
	}

	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		page.selectionChanged(part, selection);
	}

	@Override
	public void dispose() {
		super.dispose();

		if (page != null) {
			page.dispose();
			page = null;
		}

	}

	@Override
	public void refresh() {
		page.refresh();
	}

	@Override
	public boolean shouldUseExtraSpace() {
		return true;
	}
}
