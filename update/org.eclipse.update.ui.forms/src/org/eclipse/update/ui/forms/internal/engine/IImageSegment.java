/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.ui.forms.internal.engine;
import org.eclipse.swt.graphics.Image;
import java.util.Hashtable;

/**
 * @version 	1.0
 * @author
 */
public interface IImageSegment extends IParagraphSegment, IObjectReference {
	public static final int TOP = 1;
	public static final int MIDDLE = 2;
	public static final int BOTTOM = 3;
	
	public int getVerticalAlignment();

	Image getImage(Hashtable objectTable);
}