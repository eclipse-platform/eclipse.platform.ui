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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.pages.*;
import org.eclipse.update.internal.ui.preferences.UpdateColors;
import org.eclipse.update.ui.forms.internal.*;

public abstract class UpdateSection extends FormSection {
	private UpdateFormPage page;
	
	public UpdateSection(UpdateFormPage page) {
		this.page = page;
		IPreferenceStore pstore = UpdateUI.getDefault().getPreferenceStore();
		pstore.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if (e.getProperty().equals(UpdateColors.P_TOPIC_COLOR))
					updateHeaderColor();
			}
		});
	}
	
	public UpdateFormPage getPage() {
		return page;
	}
	
	protected void updateHeaderColor() {
		header.setForeground(UpdateColors.getTopicColor(header.getDisplay()));
	}
}

