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