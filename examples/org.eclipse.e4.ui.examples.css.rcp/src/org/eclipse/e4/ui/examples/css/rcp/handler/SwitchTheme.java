package org.eclipse.e4.ui.examples.css.rcp.handler;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.ui.examples.css.rcp.theme.ThemeHelper;


public class SwitchTheme extends AbstractHandler {

	private boolean reset = false;
	private final static String USER_CSS_DEFAULT = "org.eclipse.e4.ui.examples.css.rcp";
	private final static String USER_CSS_COLORFUL = "org.eclipse.e4.ui.examples.css.colorful";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String themeId = "";
		if (reset) { // toggle functionality of the menu item
			themeId = USER_CSS_DEFAULT;
		} else {
			themeId = USER_CSS_COLORFUL;
		}
		reset = !reset;
		ThemeHelper.getEngine().setTheme(themeId, true);
		return null;

	}





}
