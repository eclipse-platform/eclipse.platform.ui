/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.ui.forms.internal.engine;

/**
 * @version 	1.0
 * @author
 */
public interface IParagraph {
	public int getIndent();
	public IParagraphSegment [] getSegments();
	public void addSegment(IParagraphSegment segment);
	public boolean getAddVerticalSpace();
}
