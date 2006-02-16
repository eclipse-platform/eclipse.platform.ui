package org.eclipse.ui.internal.intro.impl.model;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.intro.impl.model.util.BundleUtil;
import org.osgi.framework.Bundle;
import org.w3c.dom.Element;


public class IntroTheme extends AbstractIntroIdElement {
	private static final String ATT_PATH = "path"; //$NON-NLS-1$
	private String name;
	private String path;
	
	public IntroTheme(IConfigurationElement element) {
		super(element);
		name = element.getAttribute(name);
		path = element.getAttribute(ATT_PATH);
		path = BundleUtil.getResolvedResourceLocation(path, getBundle());
	}

	public IntroTheme(Element element, Bundle bundle) {
		super(element, bundle);
	}

	public IntroTheme(Element element, Bundle bundle, String base) {
		super(element, bundle, base);
	}
	
	public String getName() {
		return name;
	}
	
	public String getPath() {
		return path;
	}

	public int getType() {
		return THEME;
	}
}
