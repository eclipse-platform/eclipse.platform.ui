package org.eclipse.update.internal.ui.views;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.forms.ConfigurationPreviewForm;

public class ConfigurationPreview {
	private ConfigurationPreviewForm form;

	public ConfigurationPreview(NewConfigurationView configView) {
		form = new ConfigurationPreviewForm(configView);
	}
	public Control createControl(Composite parent) {
		form.createControl(parent);
		form.initialize(null);
		return form.getControl();
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
