//
// XmlElement.java
//
// Copyright:
// GEBIT Gesellschaft fuer EDV-Beratung
// und Informatik-Technologien mbH, 
// Berlin, Duesseldorf, Frankfurt (Germany) 2002
// All rights reserved.
//
package org.eclipse.ui.externaltools.internal.ant.editor.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.externaltools.internal.ant.editor.PlantyException;

/**
 * General representation of an xml element.
 * <P>
 * Here an xml element is refered to as what is specified using like
 * '<elementName>' in an xml file.
 * 
 * @version 27.11.2002
 * @author Alf Schiefelbein
 */
public class XmlElement {
	
	/*
	 * (T)
	 * Eventually extract an interface XmlSourceRange in respect to 
	 * ISourceReference that contains the two methods getOffset and
	 * getLength
	 */
	
    
	/**
	 * The offset of the corresponding source.
	 * @see #getOffset()
	 */
	protected int offset;
	
	
	/**
	 * The length of the corresponding source.
	 * @see #getLength()
	 */
	protected int length;
	
	
    /**
     * The parent node.
     */
    protected XmlElement parent;
    

    /**
     * The attributes.
     */
    protected List attributes = new ArrayList();


    /**
     * The child nodes.
     */
    protected List childNodes = new ArrayList();


    /**
     * The (tag-)name of the element.
     */
    protected String name;
    
    
    /**
     * The startingRow where the element begins at.
     * <P>
     * The first startingRow has the index '1'.
     */
    protected int startingRow;


    /**
     * The startingColumn where the element begins at.
     * <P>
     * The first startingColumn has the index '1'.
     */
    protected int startingColumn;


    /**
     * The row where the element ends at.
     * <P>
     * The first ending row has the index '1'.
     */
    protected int endingRow;


    /**
     * The column where the element ends at.
     * <P>
     * The first ending column has the index '1'.
     * The ending column is actually the index on this line after '>'.
     */
    protected int endingColumn;

	/**
	 * Whether this element has been generated as part of an element hierarchy
	 * this is not complete as a result of an error.
	 */
	private boolean isErrorNode;

    /**
     * Creates an instance with the specified name.
     */
    public XmlElement(String aName) {
       name = aName;
    }
    
    
    /**
     * Returns the name.
     */
    public String getName() {
        return name;
    }
    
    
    /**
     * Returns the name that is used for display in outline view.
     * <P>
     * The default implementation returns just the same as the method <code>getName()</code>.
     * Override this method in your own subclass for special elements in order to provide a
     * custome display name.
     */
    public String getDisplayName() {
    	return getName();
    }
    
    
    /**
     * Returns the child nodes.
     */
    public List getChildNodes() {
        return childNodes;
    }
    
    
    /**
     * Returns the parent XmlElement.
     * 
     * @return the parent or <code>null</code> if this element has no parent.
     */
    public XmlElement getParentNode() {
        return parent;
    }    
    
    
    /**
     * Adds the specified element as child.
     * <P>
     * The specified element will have this assigned as its parent.
     * 
     * @throws PlantyException if the specified child element allready
     * has a parent.
     */
    public void addChildNode(XmlElement aChildElement) {
        if(aChildElement.getParentNode() != null) {
            throw new PlantyException("Cannot add XmlElement '" +aChildElement+ "' as child since it allready is a child of '" +aChildElement.getParentNode()+ "'");
        }
        aChildElement.parent = this;
        childNodes.add(aChildElement);
    }


    /**
     * Returns all attributes.
     */
    public List getAttributes() {
        return attributes;
    }   

    
    /**
     * Adds the specified attribute.
     */
    public void addAttribute(XmlAttribute anAttribute) {
        attributes.add(anAttribute);
    }


    /**
     * Returns the attribute with the specified name or <code>null</code>,
     * if non existing.
     */
    public XmlAttribute getAttributeNamed(String anAttributeName) {
        for (Iterator i = attributes.iterator(); i.hasNext();) {
            XmlAttribute anAttribute = (XmlAttribute) i.next();
            if(anAttributeName.equals(anAttribute.name)) {
                return anAttribute;
            }
        }
        return null;
    }


    /**
     * Returns the startingColumn.
     * <P>
     * The first column has the index '1'.
     */
    public int getStartingColumn() {
        return startingColumn;
    }


    /**
     * Returns the startingRow.
     * <P>
     * The first row has the index '1'.
     */
    public int getStartingRow() {
        return startingRow;
    }


    /**
     * Returns the endingRow.
     * <P>
     * The first row has the index '1'.
     */
    public int getEndingRow() {
        return endingRow;
    }


    /**
     * Sets the endingRow.
     * <P>
     * The first row has the index '1'.
     */
    public void setEndingRow(int endingRow) {
        this.endingRow = endingRow;
    }


    /**
     * Returns the ending column.
     * <P>
     * The first column has the index '1'.
     * The ending column is actually the index right after '>' on the ending 
     * line.
     */
    public int getEndingColumn() {
        return endingColumn;
    }


    /**
     * Sets the endingColumn.
     * <P>
     * The first column has the index '1'.
     * The ending column is actually the index right after '>' on the ending 
     * line.
     */
    public void setEndingColumn(int endingColumn) {
        this.endingColumn = endingColumn;
    }


    /**
     * Sets the startingColumn.
     * <P>
     * The first column has the index '1'.
     */
    public void setStartingColumn(int startingColumn) {
        this.startingColumn = startingColumn;
    }


    /**
     * Sets the startingRow.
     * <P>
     * The first row has the index '1'.
     */
    public void setStartingRow(int startingRow) {
        this.startingRow = startingRow;
    }


	/**
	 * Returns the 0-based index of the first character of the source code for this element,
	 * relative to the source buffer in which this element is contained.
	 * 
	 * @return the 0-based index of the first character of the source code for this element,
	 * relative to the source buffer in which this element is contained
	 */
	public int getOffset() {
		return offset;
	}

	
	/**
	 * Sets the offset.
	 * 
	 * @see #getOffset()
	 */
	public void setOffset(int anOffset) {
		offset = anOffset;
	}
	

	/**
	 * Returns the number of characters of the source code for this element,
	 * relative to the source buffer in which this element is contained.
	 * 
	 * @return the number of characters of the source code for this element,
	 * relative to the source buffer in which this element is contained
	 */
    public int getLength() {
        return length;
    }
    

	/**
	 * Sets the length.
	 * 
	 * @see #getLength()
	 */
	public void setLength(int aLength) {
		length = aLength;
	}


	/**
	 * Returns a string representation of this element.
	 */
	public String toString() {
		return "XmlElement[name="+name+"; starting="+startingRow+","+startingColumn+"; ending="+endingRow+","+endingColumn+"]";		
	}
	
	/**
	 * Returns whether this element has been generated as part of an element
	 * hierarchy this is not complete as a result of an error.
	 */
	public boolean isErrorNode() {
		return isErrorNode;
	}
	
	/**
	 * Sets whether this element has been generated as part of an element
	 * hierarchy this is not complete as a result of an error.
	 */
	public void setIsErrorNode(boolean isErrorNode) {
		this.isErrorNode= isErrorNode;
	}
}
