/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;



public interface IDocumentInformationMapping {
	
	IRegion getCoverage();
	
	
	int toOriginOffset(int imageOffset) throws BadLocationException;
	
	IRegion toOriginRegion(IRegion imageRegion) throws BadLocationException;
	
	IRegion toOriginLines(int imageLine) throws BadLocationException;
	
	int toOriginLine(int imageLine) throws BadLocationException;
	
	
	
	int toImageOffset(int originOffset) throws BadLocationException;
	
	IRegion toImageRegion(IRegion originRegion) throws BadLocationException;
	
	int toImageLine(int originLine) throws BadLocationException;
	
	int toClosestImageLine(int originLine) throws BadLocationException;
}