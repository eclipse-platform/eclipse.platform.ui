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
package org.eclipse.update.internal.ui.forms;


import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.pages.IUpdateFormPage;
import org.eclipse.update.internal.ui.preferences.UpdateColors;

public class PropertyWebForm extends UpdateWebForm {
	private ArrayList headings = new ArrayList();

	/**
	 * Constructor for PropertyWebForm.
	 * @param page
	 */
	public PropertyWebForm(IUpdateFormPage page) {
		super(page);
		IPreferenceStore pstore =
			UpdateUI.getDefault().getPreferenceStore();
		pstore.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if (e.getProperty().equals(UpdateColors.P_TOPIC_COLOR))
					updateHeadings();
			}
		});
	}

	protected void updateHeadings() {
		for (int i = 0; i < headings.size(); i++) {
			Control c = (Control) headings.get(i);
			c.setForeground(UpdateColors.getTopicColor(c.getDisplay()));
		}
	}

	protected Label createProperty(Composite parent, String name) {
		createHeading(parent, name);
		Label label = factory.createLabel(parent, null);
		label.setText("");
		label.setLayoutData(createPropertyLayoutData());
		return label;
	}

	protected Object createPropertyLayoutData() {
		GridData gd = new GridData();
		gd.horizontalIndent = 10;
		return gd;
	}

	protected Label createHeading(Composite parent, String text) {
		Label l = factory.createHeadingLabel(parent, text);
		Color hc = UpdateColors.getTopicColor(parent.getDisplay());
		l.setForeground(hc);
		headings.add(l);
		return l;
	}

}
