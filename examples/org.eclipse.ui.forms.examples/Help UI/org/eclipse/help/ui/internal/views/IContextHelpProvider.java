/*
 * Created on Dec 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import org.eclipse.help.IContext;
import org.eclipse.swt.widgets.Widget;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface IContextHelpProvider {
	int SELECTION = 0x1;
	
	int getContextHelpChangeMask();
	IContext getHelpContext(Widget widget);
}