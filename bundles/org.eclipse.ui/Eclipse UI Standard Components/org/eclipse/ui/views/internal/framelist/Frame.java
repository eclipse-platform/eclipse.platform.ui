package org.eclipse.ui.views.internal.framelist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * @deprecated This has been promoted to API and will be removed for 2.0.  
 *   Use the corresponding class in package org.eclipse.ui.views.framelist instead.
 */
public class Frame {
	private int index = -1;
	private FrameList parent;
	private String name = "";//$NON-NLS-1$
	private String toolTipText;
public Frame() {
	super();
}
public int getIndex() {
	return index;
}
public String getName() {
	return name;
}
public FrameList getParent() {
	return parent;
}
public String getToolTipText() {
	return toolTipText;
}
public void setIndex(int index) {
	this.index = index;
}
public void setName(String name) {
	this.name = name;
}
public void setParent(FrameList parent) {
	this.parent = parent;
}
public void setToolTipText(String toolTipText) {
	this.toolTipText = toolTipText;
}
}
