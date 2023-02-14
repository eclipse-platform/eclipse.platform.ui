package org.eclipse.e4.tools.services;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public interface IResourcePool {
	public Image getImage(String imageKey) throws CoreException;
	public Image getImageUnchecked(String imageKey);

	public Color getColor(String imageKey) throws CoreException;
	public Color getColorUnchecked(String imageKey);

	public Font getFont(String imageKey) throws CoreException;
	public Font getFontUnchecked(String imageKey);
}