/*******************************************************************************
 * Copyright (c) 2002, 2004 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany) and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 * 	   IBM Corporation - bug fixes
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.model;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.xml.utils.URI;
import org.apache.xml.utils.URI.MalformedURIException;
import org.eclipse.ant.internal.ui.editor.AntEditorException;
import org.eclipse.core.runtime.Path;

/**
 * General representation of an Ant buildfile element.
 * 
 */
public class AntElementNode {
    
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
	 * The length of the source to select for this node
	 */
	protected int selectionLength;
	
    /**
     * The parent node.
     */
    protected AntElementNode parent;

    /**
     * The child nodes.
     */
    protected List childNodes = new ArrayList();


    /**
     * The (tag-)name of the element.
     */
    protected String name;

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
    public AntElementNode(String aName) {
       name = aName;
    }
    
    /**
     * Creates an instance with the specified name.
     */
    public AntElementNode() {
    }
    
    
    /**
     * Returns the name.
     */
    public String getName() {
        return name;
    }
    
    
    /**
     * Returns the label that is used for display in outline view.
     * <P>
     * The default implementation returns just the same as the method <code>getName()</code>.
     * Override this method in your own subclass for special elements in order to provide a
     * custom label.
     */
    public String getLabel() {
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
    public AntElementNode getParentNode() {
        return parent;
    }    
    
    
    /**
     * Adds the specified element as child.
     * <P>
     * The specified element will have this assigned as its parent.
     * 
     * @throws AntEditorException if the specified child element allready
     * has a parent.
     */
    public void addChildNode(AntElementNode childElement) {
        if(childElement.getParentNode() != null) {
            throw new AntEditorException(MessageFormat.format(AntModelMessages.getString("XmlElement.XmlElement_cannot_be_added_as_a_child"), new String[]{childElement.toString(), childElement.getParentNode().toString()})); //$NON-NLS-1$
        }
        childElement.parent = this;
        childNodes.add(childElement);
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
		return "Ant Element Node: Offset: " + getOffset() + " Length: " + getLength();  //$NON-NLS-1$//$NON-NLS-2$
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

	private String getElementPath() {
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
			buffer.append(escape(new StringBuffer(getLabel() != null ? getLabel() : ""), '\\', "$/[]\\").toString()); //$NON-NLS-1$ //$NON-NLS-2$
			
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

	private int getElementIndexOf(AntElementNode child) {
		if (getChildNodes() == null) {
			return -1;
		}
		
		int result= -1;
		
		Iterator iter= getChildNodes().iterator();
		AntElementNode current= null;
		while (current != child && iter.hasNext()) {
			current= (AntElementNode) iter.next();
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
		if (!(o1 instanceof AntElementNode || o2 instanceof AntElementNode)) {
			return o2.equals(o1);
		}
		if (!(o1 instanceof AntElementNode && o2 instanceof AntElementNode)) {
			return false;
		}
		
		AntElementNode e1= (AntElementNode) o1;
		AntElementNode e2= (AntElementNode) o2;
	
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
		if (!(o1 instanceof AntElementNode)) {
			return o1.hashCode();
		}

		AntElementNode e1= (AntElementNode) o1;
		
		return e1.getElementPath().hashCode();
	}

	/**
	 * Returns the length of source to select for this node.
	 * @return the length of source to select
	 */
	public int getSelectionLength() {
		return selectionLength;
	}
	
	public void setSelectionLength(int selectionLength) {
		this.selectionLength= selectionLength;
	}
	
	public boolean contains(int childOffset) {
		return childOffset < (offset + length); 
	}
}