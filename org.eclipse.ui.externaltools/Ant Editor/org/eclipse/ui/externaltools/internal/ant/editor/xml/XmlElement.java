/**********************************************************************
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

// Copyright:
// GEBIT Gesellschaft fuer EDV-Beratung
// und Informatik-Technologien mbH, 
// Berlin, Duesseldorf, Frankfurt (Germany) 2002
// All rights reserved.
//
package org.eclipse.ui.externaltools.internal.ant.editor.xml;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.xerces.util.URI;
import org.apache.xerces.util.URI.MalformedURIException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.externaltools.internal.ant.editor.PlantyException;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;

/**
 * General representation of an xml element.
 * <P>
 * Here an xml element is refered to as what is specified using like
 * '<elementName>' in an xml file.
 * 
 * @author Alf Schiefelbein
 */
public class XmlElement implements IAdaptable {
	
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
	 * The absolute file system path of the file this element is
	 * defined within.
	 */
	private String filePath;
	
	/**
	 * Whether this element has been generated from an external entity definition
	 */
	private boolean isExternal = false;
	
	/**
	 * Whether this element is the root external element generated from an external entity definition
	 */
	private boolean isRootExternal = false;

	/**
	 * The unique (in the corresponding element tree) path of this element.
	 */
	private String fElementPath;
	
	/**
	 * The (not necessarily unique) identifier of this element.
	 */
	private String fElementIdentifier;


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
            throw new PlantyException(MessageFormat.format(AntEditorXMLMessages.getString("XmlElement.XmlElement_cannot_be_added_as_a_child"), new String[]{aChildElement.toString(), aChildElement.getParentNode().toString()})); //$NON-NLS-1$
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
	 * Sets the absolute file system path of the file this element is defined
	 * within.
	 */
	public void setFilePath(String path) {
		URI uri= null;
		try {
			uri= new URI(path);
		} catch (MalformedURIException e) {
			filePath= path;
			return;
		}
		filePath = new Path(new File(uri.getPath()).getAbsolutePath()).toString();
	}
	
	/**
	 * Returns the absolute file system path of the file this element is defined
     * within.
     */
	public String getFilePath() {
		return filePath;
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
		return MessageFormat.format(AntEditorXMLMessages.getString("XmlElement.XmlElement_toString"), new String[]{name, Integer.toString(startingRow), Integer.toString(startingColumn), Integer.toString(endingRow), Integer.toString(endingColumn)}); //$NON-NLS-1$
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
	/**
	 * Returns whether this xml element is defined in an external entity.
	 * 
	 * @return boolean
	 */
	public boolean isExternal() {
		return isExternal;
	}

	/**
	 * Sets whether this xml element is defined in an external entity.
	 */
	public void setExternal(boolean isExternal) {
		this.isExternal = isExternal;
	}
	
	/**
	 * Sets whether this xml element is the root external entity.
	 */
	public void setRootExternal(boolean isExternal) {
		this.isRootExternal = isExternal;
	}
	
	/**
	 * Returns whether this xml element is the root external entity.
	 */
	public boolean isRootExternal() {
		return isRootExternal;
	}

	public String getElementPath() {
		if (fElementPath == null) {
			StringBuffer buffer= new StringBuffer(getParentNode() != null ? getParentNode().getElementPath() : ""); //$NON-NLS-1$
			buffer.append('/');
			buffer.append(getElementIdentifier());
			buffer.append('[');
			buffer.append(getParentNode() != null ? getParentNode().getElementIndexOf(this) : 0);
			buffer.append(']');
			
			fElementPath= buffer.toString();
		}
		return fElementPath;
	}

	private String getElementIdentifier() {
		if (fElementIdentifier == null) {
			StringBuffer buffer= escape(new StringBuffer(getName() != null ? getName() : ""), '\\', "$/[]\\"); //$NON-NLS-1$ //$NON-NLS-2$
			buffer.append('$');
			buffer.append(escape(new StringBuffer(getDisplayName() != null ? getDisplayName() : ""), '\\', "$/[]\\").toString()); //$NON-NLS-1$ //$NON-NLS-2$
			
			fElementIdentifier= buffer.toString();
		}
		return fElementIdentifier;
	}

	private StringBuffer escape(StringBuffer sb, char esc, String special) {
		for (int i= 0; i < sb.length(); i++) {
			if (special.indexOf(sb.charAt(i)) >= 0) {
				sb.insert(i++, esc);
			}
		}

		return sb;
	}

	private int getElementIndexOf(XmlElement child) {
		if (getChildNodes() == null) {
			return -1;
		}
		
		int result= -1;
		
		Iterator iter= getChildNodes().iterator();
		XmlElement current= null;
		while (current != child && iter.hasNext()) {
			current= (XmlElement) iter.next();
			if (child.getElementIdentifier().equals(current.getElementIdentifier()))
				result++;
		}
		
		if (current != child) {
			return -1;
		}
		
		return result;
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o2) {
		// prepared to be used in an IElementComparer, depends on http://dev.eclipse.org/bugs/show_bug.cgi?id=32254
		Object o1= this;
		
		if (o1 == o2) {
			return true;
		}
		if (o1 == null || o2 == null) {
			return false;
		}
		if (!(o1 instanceof XmlElement || o2 instanceof XmlElement)) {
			return o2.equals(o1);
		}
		if (!(o1 instanceof XmlElement && o2 instanceof XmlElement)) {
			return false;
		}
		
		XmlElement e1= (XmlElement) o1;
		XmlElement e2= (XmlElement) o2;
	
		if (e1.getElementPath().equals(e2.getElementPath())) {
			return true;
		}

		return false;
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		// prepared to be used in an IElementComparer, depends on http://dev.eclipse.org/bugs/show_bug.cgi?id=32254
		Object o1= this;

		if (o1 == null) {
			return 0;
		}
		if (!(o1 instanceof XmlElement)) {
			return o1.hashCode();
		}

		XmlElement e1= (XmlElement) o1;
		
		return e1.getElementPath().hashCode();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IResource.class) {
			if (getFilePath() != null) {
				return AntUtil.getFile(getFilePath());
			}
		}
		return null;
	}

}