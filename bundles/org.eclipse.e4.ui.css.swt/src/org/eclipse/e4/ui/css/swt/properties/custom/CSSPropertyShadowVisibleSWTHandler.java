package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyShadowVisibleSWTHandler extends AbstractCSSPropertySWTHandler {

	
	public static final ICSSPropertyHandler INSTANCE = new CSSPropertyShadowVisibleSWTHandler();
	
	protected void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (!(control instanceof CTabFolder)) return;
		boolean shadowVisible = (Boolean) engine.convert(value, Boolean.class, null);
		
		CTabFolderRenderer renderer = ((CTabFolder) control).getRenderer();
		
		Object appContext = control.getDisplay().getData("org.eclipse.e4.ui.css.context");
		if (appContext != null && appContext instanceof IEclipseContext) {
			IEclipseContext context = (IEclipseContext) appContext;
			IEclipseContext childContext = context.createChild();
			childContext.set("shadowVisible", new Boolean(shadowVisible));
			ContextInjectionFactory.inject(renderer, childContext); 
		}
	}
	
	protected String retrieveCSSProperty(Control control, String property,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

}
