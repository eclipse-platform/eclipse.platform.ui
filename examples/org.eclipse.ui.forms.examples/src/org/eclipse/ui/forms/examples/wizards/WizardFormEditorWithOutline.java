
package org.eclipse.ui.forms.examples.wizards;

import org.eclipse.ui.internal.forms.WizardFormEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class WizardFormEditorWithOutline extends WizardFormEditor {
	private WizardFormEditorOutline outline;
	public Object getAdapter(Class key) {
		if (key.equals(IContentOutlinePage.class)) {
			return getContentOutline();
		}
		return super.getAdapter(key);
	}
	
	private IContentOutlinePage getContentOutline() {
		if (outline==null) {
			outline = new WizardFormEditorOutline(this);
		}
		return outline;
	}
	protected void update() {
		super.update();
		if (outline!=null)
			outline.update();
	}
}