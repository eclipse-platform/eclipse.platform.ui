package org.eclipse.update.internal.ui.forms;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.update.internal.ui.UpdateUIPluginImages;
import org.eclipse.update.internal.ui.pages.IUpdateFormPage;
import org.eclipse.update.ui.forms.internal.WebForm;

public class UpdateWebForm extends WebForm implements IUpdateForm {
	private IUpdateFormPage page;

	public UpdateWebForm(IUpdateFormPage page) {
		this.page = page;
	}

	public IUpdateFormPage getPage() {
		return page;
	}

	public void initialize(Object model) {
		super.initialize(model);
		if (isWhiteBackground()) {
			setHeadingImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_BANNER));
			setHeadingUnderlineImage(
				UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_UNDERLINE));
		}
		refreshSize();
	}
	private boolean isWhiteBackground() {
		Color color = getFactory().getBackgroundColor();
		return (
			color.getRed() == 255 && color.getGreen() == 255 && color.getBlue() == 255);
	}
	protected void refreshSize() {
		((Composite) getControl()).layout();
		updateSize();
	}
}