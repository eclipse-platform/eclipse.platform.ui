package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.e4.ui.widgets.ETabFolder;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyETabFolderWebbyStyleHandler extends AbstractCSSPropertySWTHandler {

	public static final ICSSPropertyHandler INSTANCE = new CSSPropertyETabFolderWebbyStyleHandler();
	
	public void applyCSSProperty(Control control, String property,
		    CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		boolean bool = (Boolean)engine.convert(value, Boolean.class, null);
		if (control instanceof ETabFolder) {
			ETabFolder folder = (ETabFolder) control;
			folder.setWebbyStyle(bool);
		}
	}

	public String retrieveCSSProperty(Control control, String property,
			String pseudo, CSSEngine engine) throws Exception {
		if (control instanceof ETabFolder) {
			ETabFolder folder = (ETabFolder) control;
			return Boolean.toString( folder.getWebbyStyle());
		}
		return null;
	}


}
