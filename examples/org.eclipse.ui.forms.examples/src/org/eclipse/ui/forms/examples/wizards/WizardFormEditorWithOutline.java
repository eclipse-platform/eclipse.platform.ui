
package org.eclipse.ui.forms.examples.wizards;

import org.eclipse.ui.internal.forms.WizardFormEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
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
			outline.update();
		}
		return outline;
	}
	protected void update() {
		super.update();
		if (outline!=null)
			outline.update();
	}
}