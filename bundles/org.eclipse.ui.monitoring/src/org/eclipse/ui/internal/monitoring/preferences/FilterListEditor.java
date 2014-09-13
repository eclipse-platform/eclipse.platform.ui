/*******************************************************************************
 * Copyright (C) 2014, Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcus Eng (Google) - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring.preferences;

import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

/**
 * Displays the list of traces to filter out and ignore.
 */
public class FilterListEditor extends ListEditor {
	FilterListEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
		getAddButton().setText(Messages.ListFieldEditor_add_filter_button_label);
		getUpButton().setVisible(false);
		getDownButton().setVisible(false);
	}

    @Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
    	super.doFillIntoGrid(parent, numColumns);
        List list = getListControl(parent);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, numColumns - 1, 1);
    	PixelConverter pixelConverter = new PixelConverter(parent);
        gd.widthHint = pixelConverter.convertWidthInCharsToPixels(65);
        list.setLayoutData(gd);
    }

	/**
	 * Handles parsing of defined traces to be filtered.
	 */
	@Override
	protected String createList(String[] items) {
		StringBuilder mergedItems = new StringBuilder();

		for (String item : items) {
			item.trim();
			if (mergedItems.length() != 0) {
				mergedItems.append(',');
			}
			mergedItems.append(item);
		}

		return mergedItems.toString();
	}

	@Override
	protected String getNewInputObject() {
		FilterInputDialog dialog = new FilterInputDialog(getShell());
		if (dialog.open() == Window.OK) {
			return dialog.getFilter();
		}
		return null;
	}

	@Override
	protected String[] parseString(String stringList) {
		return stringList.split(","); //$NON-NLS-1$
	}
}
