package org.eclipse.e4.ui.css.swt;

import org.eclipse.swt.widgets.Widget;

public class CSSSWT {
	//Keys are private and not in CSSSWTConstants since we don't want people using them directly

	private static String CSS_CLASS_NAME_KEY = "org.eclipse.e4.ui.css.CssClassNameKey";
	private static String CSS_ID_KEY = "org.eclipse.e4.ui.css.idKey";
		
	public static String getCSSClass(Widget widget) {
		return (String) widget.getData(CSS_CLASS_NAME_KEY);
	}

	public static String getID(Widget widget) {
		return (String) widget.getData(CSS_ID_KEY);
	}

	public static void setCSSClass(Widget widget, String className) {
		widget.setData(CSS_CLASS_NAME_KEY, className);
	}

	public static void setID(Widget widget, String id) {
		widget.setData(CSS_ID_KEY, id);
	}

}
