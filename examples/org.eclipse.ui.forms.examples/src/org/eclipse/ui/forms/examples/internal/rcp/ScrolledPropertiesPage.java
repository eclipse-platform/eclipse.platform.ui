/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
package org.eclipse.ui.forms.examples.internal.rcp;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.*;
import org.eclipse.ui.forms.examples.internal.ExamplesPlugin;
import org.eclipse.ui.forms.widgets.*;
/**
 * @author dejan
 *
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ScrolledPropertiesPage extends FormPage {
	private final ScrolledPropertiesBlock block;
	public ScrolledPropertiesPage(FormEditor editor) {
		super(editor, "fourth", "Master Details");
		block = new ScrolledPropertiesBlock(this);
	}
	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		//FormToolkit toolkit = managedForm.getToolkit();
		form.setText("Form with scrolled sections");
		form.setBackgroundImage(ExamplesPlugin.getDefault().getImage(
				ExamplesPlugin.IMG_FORM_BG));
		block.createContent(managedForm);
	}
}
