package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyTabRendererSWTHandler extends AbstractCSSPropertySWTHandler {

	
	public static final ICSSPropertyHandler INSTANCE = new CSSPropertyTabRendererSWTHandler();
	
	protected void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (!(control instanceof CTabFolder)) return;
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			if (((CSSPrimitiveValue) value).getPrimitiveType() == CSSPrimitiveValue.CSS_URI) {
				String rendURL = ((CSSPrimitiveValue) value).getStringValue();
			
				Object appContext = control.getDisplay().getData("org.eclipse.e4.ui.css.context");
				if (appContext != null && appContext instanceof IEclipseContext) {
					IEclipseContext context = (IEclipseContext) appContext;
					IEclipseContext childContext = EclipseContextFactory.create(context, null);
					childContext.set(CTabFolder.class.getName(), control);
					IContributionFactory factory = (IContributionFactory) context.get(IContributionFactory.class.getName());
					Object rend = factory.create(rendURL, childContext);
					if (rend != null && rend instanceof CTabFolderRenderer){
						((CTabFolder) control).setRenderer((CTabFolderRenderer)rend);
					}
				}
			} else {
				((CTabFolder) control).setRenderer(null);
			}
		}
	}
	
	protected String retrieveCSSProperty(Control control, String property,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

}
