package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyCornerRadiusSWTHandler extends AbstractCSSPropertySWTHandler {

	
	public static final ICSSPropertyHandler INSTANCE = new CSSPropertyCornerRadiusSWTHandler();
	
	protected void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (!(control instanceof CTabFolder)) return;
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			int radiusValue = (int) ((CSSPrimitiveValue) value).getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
			
			CTabFolderRenderer renderer = ((CTabFolder) control).getRenderer();
					
			Object appContext = control.getDisplay().getData("org.eclipse.e4.ui.css.context");
			if (appContext != null && appContext instanceof IEclipseContext) {
				IEclipseContext context = (IEclipseContext) appContext;
				IEclipseContext childContext = EclipseContextFactory.create(context, null);
				childContext.set("radius", new Integer(radiusValue));
				ContextInjectionFactory.inject(renderer, childContext); 
			}
		}
	}
	
	protected String retrieveCSSProperty(Control control, String property,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

}
