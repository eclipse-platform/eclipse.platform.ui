package org.eclipse.update.internal.ui.views;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.forms.ConfigurationPreviewForm;

public class ConfigurationPreview {
	private ConfigurationPreviewForm form;

	public ConfigurationPreview(NewConfigurationView configView) {
		form = new ConfigurationPreviewForm(configView);
	}
	public void createControl(Composite parent) {
		form.createControl(parent);
		form.initialize(null);
	}
	
	public Control getControl() {
		return form.getControl();
	}
	
	public Control getScrollingControl() {
		return form.getScrollingControl();
	}
	
	public void setSelection(IStructuredSelection selection) {
		Object obj = selection.getFirstElement();
		form.expandTo(obj);
	}
	
	public void dispose() {
		if (form!=null)
			form.dispose();
	}
}
