/*
 * Created on Dec 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IFormPart;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface IHelpPart extends IFormPart {
	void init(ReusableHelpPart parent, String id);
	Control getControl();
	String getId();
	void setVisible(boolean visible);
	boolean hasFocusControl(Control control);
	boolean fillContextMenu(IMenuManager manager);
}
