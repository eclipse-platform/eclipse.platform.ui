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
 *     John-Mason P. Shackelford (john-mason.shackelford@pearson.com) - bug 49445
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.model;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ant.internal.ui.editor.outline.AntModel;
import org.eclipse.ant.internal.ui.editor.outline.IProblem;
import org.eclipse.ant.internal.ui.editor.outline.LocationProvider;
import org.eclipse.ant.internal.ui.editor.outline.XMLProblem;
import org.eclipse.ant.internal.ui.model.AntImageDescriptor;
import org.eclipse.ant.internal.ui.model.AntUIImages;
import org.eclipse.ant.internal.ui.model.AntUtil;
import org.eclipse.ant.internal.ui.model.IAntUIConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * General representation of an Ant buildfile element.
 * 
 */
public class AntElementNode implements IAdaptable {
    
	/**
	 * The offset of the corresponding source.
	 * @see #getOffset()
	 */
	protected int offset= -1;
	
	/**
	 * The length of the corresponding source.
	 * @see #getLength()
	 */
	protected int length= -1;
	
	/**
	 * The length of the source to select for this node
	 */
	protected int selectionLength;
	
    /**
     * The parent node.
     */
    protected AntElementNode parent;
    
    /**
     * The import node that "imported" this element
     */
    private AntElementNode importNode;

    /**
     * The child nodes.
     */
    protected List childNodes= null;
    
    /**
     * The child nodes not including comments
     */
    protected List outlineNodes= null;

    /**
     * The (tag-)name of the element.
     */
    protected String name;

	/**
	 * Whether this element has been generated as part of an element hierarchy
	 * this has problems. This is the severity of the problem.
	 * @see XMLProblem#NO_PROBLEM
	 * @see XMLProblem#SEVERITY_ERROR
	 * @see XMLProblem#SEVERITY_WARNING
	 * @see XMLProblem#SEVERITY_FATAL_ERROR
	 */
	private int problemSeverity= XMLProblem.NO_PROBLEM;
	
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
	 * The unique (in the corresponding element tree) path of this element.
	 */
	private String fElementPath;
	
	/**
	 * The (not necessarily unique) identifier of this element.
	 */
	private String fElementIdentifier;

	/**
	 * The problem associated with this node. May be <code>null</code>.
     */
	private IProblem fProblem;
	
	/**
	 * Only used when opening an import element to indicate the location in the imported file
	 */
	private int fLine;
	private int fColumn;

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
     * Returns the child nodes that compose the outline tree under this node.
     * Does not include comments
     */
    public List getOutlineNodes() {
        return outlineNodes;
    }
    
    /**
     * Returns all the descendents of this node
     */
    public List getDescendents() {
    	if (childNodes == null) {
    		return null;
    	}
    	List descendents= new ArrayList();
        determineDescendents(descendents, childNodes);
        
        return descendents;
    }
    
    
    private void determineDescendents(List descendents, List childrenNodes) {
		Iterator itr= childrenNodes.iterator();
        while (itr.hasNext()) {
			AntElementNode element = (AntElementNode) itr.next();
			if (element.hasChildren()) {
				determineDescendents(descendents, element.getChildNodes());
			}
			descendents.add(element);
		}
	}

	/**
     * Returns the parent <code>AntElementNode</code>.
     * 
     * @return the parent or <code>null</code> if this element has no parent.
     */
    public AntElementNode getParentNode() {
        return parent;
    } 
    
    public AntProjectNode getProjectNode() {
    	AntElementNode projectParent= getParentNode();
    	while (projectParent != null && !(projectParent instanceof AntProjectNode)) {
    		projectParent= projectParent.getParentNode();
    	}
    	return (AntProjectNode)projectParent;
    }
    
    
    /**
     * Adds the specified element as child.
     * <P>
     * The specified element will have this assigned as its parent.
     * 
     */
    public void addChildNode(AntElementNode childElement) {
    	childElement.setParent(this);
        if (childNodes == null) {
        	childNodes= new ArrayList();
        }
        childNodes.add(childElement);
        if (!childElement.isCommentNode()) {
        	if (outlineNodes == null) {	
        		outlineNodes= new ArrayList();
        	}
        	outlineNodes.add(childElement);
        }
    }
    
	protected void setParent(AntElementNode node) {
		parent= node;
	}

	/**
	 * Sets the absolute file system path of the file this element is defined
	 * within.
	 */
	public void setFilePath(String path) {
		if (path == null) {
			return;
		}
		URL url= null;
		try {		
			url= new URL(path);
		} catch (MalformedURLException e) {		
			filePath= path;
			return;
		}
		filePath = new Path(new File(url.getPath()).getAbsolutePath()).toString();
	}
	
	/**
	 * Returns the absolute file system path of the file this element is defined
     * within. Only relevant for nodes that are external
     * @see #isExternal()
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
		if (fProblem != null && fProblem instanceof XMLProblem) {
			((XMLProblem)fProblem).setLength(aLength);
			fProblem= null;
		}
	}

	/**
	 * Returns a string representation of this element.
	 */
	public String toString() {
		return "Ant Element Node: " + getLabel() + " Offset: " + getOffset() + " Length: " + getLength();  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}
	
	/**
	 * Returns whether this element has been generated as part of an element
	 * hierarchy this is not complete as a result of an error.
	 */
	public boolean isErrorNode() {
		return problemSeverity == XMLProblem.SEVERITY_ERROR || problemSeverity == XMLProblem.SEVERITY_FATAL_ERROR;
	}
	
	/**
	 * Returns whether this element has been generated as part of an element
	 * hierarchy that has warning(s) associated with it
	 */
	public boolean isWarningNode() {
		return problemSeverity == XMLProblem.SEVERITY_WARNING;
	}
	
	/**
	 * Sets whether this element has been generated as part of an element
	 * hierarchy that has problems. The severity of the problem is provided.
	 */
	public void setProblemSeverity(int severity) {
		this.problemSeverity= severity;
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
	
		return e1.getElementPath().equals(e2.getElementPath());
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		// prepared to be used in an IElementComparer, depends on http://dev.eclipse.org/bugs/show_bug.cgi?id=32254
		
		return getElementPath().hashCode();
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
	
	/**
	 * Returns the node with the narrowest source range that contains the offset.
	 * It may be this node or one of its children or <code>null</code> if the offset is not in the source range of this node.
	 * @param sourceOffset The source offset
	 * @return the node that includes the offset in its source range or <code>null</code>
	 */
	public AntElementNode getNode(int sourceOffset) {
		if (childNodes != null) {
			for (Iterator iter = childNodes.iterator(); iter.hasNext(); ) {
				AntElementNode node = (AntElementNode) iter.next();
				AntElementNode containingNode= node.getNode(sourceOffset);
				if (containingNode != null) {
					return containingNode;
				}
			}
		}
		if (length == -1 && offset <= sourceOffset && !isExternal()) { //this is still an open element
			return this;
		}
		if (offset <= sourceOffset && sourceOffset <= (offset + length - 2)) {
			return this;
		}
		
		return null;
	}
	
	public Image getImage() {
		int flags = 0;
		
		if (isErrorNode()) {
			flags = flags | AntImageDescriptor.HAS_ERRORS;
		} else if (isWarningNode()) {
			flags = flags | AntImageDescriptor.HAS_WARNINGS;
		}
		if(importNode != null || isExternal()){
			flags = flags | AntImageDescriptor.IMPORTED;
		}
		ImageDescriptor base= getBaseImageDescriptor();
		return AntUIImages.getImage(new AntImageDescriptor(base, flags));			
	}

	protected ImageDescriptor getBaseImageDescriptor() {
		return AntUIImages.getImageDescriptor(IAntUIConstants.IMG_TASK_PROPOSAL);
	}
	
	protected AntModel getAntModel() {
		AntElementNode parentNode= getParentNode();
		while (!(parentNode instanceof AntProjectNode)) {
			parentNode= parentNode.getParentNode();
		}
		return parentNode.getAntModel();
	}

	/**
	 * Sets the problem associated with this element
	 * @param problem The problem associated with this element.
	 */
	public void associatedProblem(IProblem problem) {
		fProblem= problem;
	}

	protected void appendEntityName(StringBuffer displayName) {
		String path= getFilePath();
		
		if (getImportNode() != null) {
			displayName.append(MessageFormat.format(AntModelMessages.getString("AntElementNode.9"), new String[]{getImportNode().getLabel()})); //$NON-NLS-1$
		} else {
			String entityName= getAntModel().getEntityName(path);
			displayName.append(MessageFormat.format(AntModelMessages.getString("AntElementNode.9"), new String[]{entityName})); //$NON-NLS-1$
		}
	}
	
	public AntElementNode getImportNode() {
		return importNode;
	}
	
	public void setImportNode(AntElementNode importNode) {
		this.importNode = importNode;
	}
	
	public boolean hasChildren() {
		if (childNodes == null) {
			return false;
		}
		return !childNodes.isEmpty();
	}
	
	public boolean hasOutlineChildren() {
		if (outlineNodes == null) {
			return false;
		}
		return !outlineNodes.isEmpty();
	}

	public void reset() {
		childNodes= null;
	}

	public void setExternalInfo(int line, int column) {
		fLine= line;
		fColumn= column;
	}
	
	public int[] getExternalInfo() {
		return new int[] {fLine, fColumn};
	}
	
	/**
     * Return the resource that contains the definition of this
     * Ant node.
     * @return The resource that contains the definition of this ant node or <code>null</code>
     * if that resource could not be determined (a buildfile that is external to the workspace).
     */
	public IFile getIFile() {
		if (isExternal()) {
			return AntUtil.getFileForLocation(filePath, null);
		} 
		return getBuildFileResource();
	}
	
	/**
     * Return the resource that is the main build file for this
     * Ant node.
     * @return The resource that is the main buildfile for this ant node or <code>null</code>
     * if that resource could not be determined (a buildfile that is external to the workspace).
     */
	public IFile getBuildFileResource() {
		LocationProvider locationProvider= getAntModel().getLocationProvider();
		return locationProvider.getFile();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}
	
	public boolean isCommentNode() {
		return false;
	}
}