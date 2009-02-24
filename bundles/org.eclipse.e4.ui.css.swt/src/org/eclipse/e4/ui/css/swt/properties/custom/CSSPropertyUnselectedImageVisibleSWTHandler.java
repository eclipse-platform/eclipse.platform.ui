package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyUnselectedImageVisibleSWTHandler extends AbstractCSSPropertySWTHandler{

	public static final ICSSPropertyHandler INSTANCE = new CSSPropertyUnselectedImageVisibleSWTHandler();
	
	public void applyCSSProperty(Control control, String property,
		    CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		boolean isUnselectedImage = (Boolean)engine.convert(value, Boolean.class, null);
		if (control instanceof CTabFolder) {
			CTabFolder folder = (CTabFolder) control;
			folder.setUnselectedImageVisible(isUnselectedImage);
		}
	}

	public String retrieveCSSProperty(Control control, String property,
			String pseudo, CSSEngine engine) throws Exception {
		CTabFolder folder = (CTabFolder)control;
		if (folder.getUnselectedImageVisible())
			return "true";
		else
			return "false";
	}


}
