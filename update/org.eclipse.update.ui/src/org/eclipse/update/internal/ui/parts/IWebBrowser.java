package org.eclipse.update.internal.ui.parts;

import org.eclipse.swt.widgets.Control;

/**
 * @author dejan
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public interface IWebBrowser {
	Control getControl();
	boolean isBackwardEnabled();
	boolean isForwardEnabled();
	int navigate(String url);
	void stop();
	void refresh();
	int back();
	int forward();
	void dispose();

}
