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
package org.eclipse.ui.forms;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.widgets.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SectionPart implements IFormPart {
	private ManagedForm managedForm;
	private Section section;
	
	public SectionPart(Section section) {
		this.section = section;
		if ((section.getExpansionStyle()& Section.NONE)==0) {
			section.addExpansionListener(new ExpansionAdapter() {
				public void expansionStateChanging(ExpansionEvent e) {
					SectionPart.this.expansionStateChanging(e.getState());
				}
				public void expansionStateChanged(ExpansionEvent e) {
					SectionPart.this.expansionStateChanged(e.getState());
				}
			});
		}
	}
	
	protected void expansionStateChanging(boolean expanding) {
	}
	
	protected void expansionStateChanged(boolean expanded) {
	}
	
	public SectionPart(Composite parent, FormToolkit toolkit, int style) {
		this(toolkit.createSection(parent, style));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#initialize(org.eclipse.ui.forms.ManagedForm)
	 */
	public void initialize(ManagedForm form) {
		this.managedForm = form;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#commit(boolean)
	 */
	public void commit(boolean onSave) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#setFormInput(java.lang.Object)
	 */
	public void setFormInput(Object input) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#setFocus()
	 */
	public void setFocus() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#refresh()
	 */
	public void refresh() {
	}
}
