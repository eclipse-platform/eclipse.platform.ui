/*
 * Created on May 15, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.ui.views;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.forms.ConfigurationPreviewForm;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
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
