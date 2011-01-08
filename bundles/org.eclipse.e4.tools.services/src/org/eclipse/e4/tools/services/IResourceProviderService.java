package org.eclipse.e4.tools.services;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public interface IResourceProviderService {
	public Image getImage(Display display, String key);
	public Image getFont(Display display, String key);
	public Image getColor(Display display, String key);
}