package org.eclipse.ant.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Vector;

/**
 * Object which stores the index and the length of the output for a given
 * target or task (represented by a string)
 */

public class OutputStructureElement {

	private OutputStructureElement parent = null;
	private Vector children;
	private String name;
	private int startIndex = 0;
	private int length = 0;


/**
 * This constructor is intended to be used only by the first element of the structure (the root element)
 */
public OutputStructureElement(String name) {
	// there's at least one target as it is the root which may be instantiated via this constructor
	children = new Vector(1);
	this.name = name;
}

/**
 * This constructor is used for any element but the first element of the structure (the root element)
 */	
public OutputStructureElement(String name, OutputStructureElement parent, int startIndex) {
	children = new Vector(0);
	this.name = name;
	this.startIndex = startIndex;
	parent.addChild(this);
}

public void addChild(OutputStructureElement child) {
	children.add(child);
	child.setParent(this);
}

public boolean hasChildren() {
	return !children.isEmpty();
}

public String getName() {
	return name;
}

public void setName(String name) {
	this.name = name;
}

public void setParent(OutputStructureElement parent) {
	this.parent = parent;
}

public OutputStructureElement getParent() {
	return parent;
}

public OutputStructureElement[] getChildren() {
	return (OutputStructureElement[]) children.toArray(new OutputStructureElement[children.size()]);
}

public void setStartIndex(int index) {
	startIndex = index;
}

public void setEndIndex(int index) {
	length = index - startIndex;
}

public int getStartIndex() {
	return startIndex;
}

public int getEndIndex() {
	return startIndex + length;
}

public int getLength() {
	return length;
}

public String toString() {
	return name;
}

}

