package org.eclipse.e4.tools.services;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public interface IResourceProviderService {
	public Image getImage(Display display, String key);
	public Font getFont(Display display, String key);
	public Color getColor(Display display, String key);
}