/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchImages;

/**
 * Implementation of IFileEditorMapping.
 * 
 */
public class FileEditorMapping extends Object implements IFileEditorMapping, 
    Cloneable {
	
	private static final String STAR = "*"; //$NON-NLS-1$ 
	private static final String DOT = ".";	//$NON-NLS-1$ 
	
    private String name = STAR;

    private String extension;

    // Collection of EditorDescriptor, where the first one
    // if considered the default one.
    private List editors = new ArrayList(1);

    private List deletedEditors = new ArrayList(1);

	private List declaredDefaultEditors = new ArrayList(1);

    /**
     *  Create an instance of this class.
     *
     *  @param extension java.lang.String
     */
    public FileEditorMapping(String extension) {
        this(STAR, extension); 
    }

    /**
     *  Create an instance of this class.
     *
     *  @param name java.lang.String
     *  @param extension java.lang.String
     */
    public FileEditorMapping(String name, String extension) {
        super();
        if (name == null || name.length() < 1) {
            setName(STAR);
        } else {
			setName(name);
		}
        if (extension == null) {
			setExtension("");//$NON-NLS-1$
		} else {
			setExtension(extension);
		}
    }

    /**
     * Add the given editor to the list of editors registered.
     * 
     * @param editor the editor to add
     */
    public void addEditor(EditorDescriptor editor) {
        editors.add(editor);
        deletedEditors.remove(editor);
    }

    /**
     * Clone the receiver.
     */
    public Object clone() {
        try {
            FileEditorMapping clone = (FileEditorMapping) super.clone();
            clone.editors = (List) ((ArrayList) editors).clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
	
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj) {
			return true;
		}
        if (!(obj instanceof FileEditorMapping)) {
			return false;
		}
        FileEditorMapping mapping = (FileEditorMapping) obj;
        if (!this.name.equals(mapping.name)) {
			return false;
		}
        if (!this.extension.equals(mapping.extension)) {
			return false;
		}

        if (!compareList(this.editors, mapping.editors)) {
			return false;
		}
        return compareList(this.deletedEditors, mapping.deletedEditors);
    }

    /**
     * Compare the editor ids from both lists and return true if they
     * are equals.
     */
    private boolean compareList(List l1, List l2) {
        if (l1.size() != l2.size()) {
			return false;
		}

        Iterator i1 = l1.iterator();
        Iterator i2 = l2.iterator();
        while (i1.hasNext() && i2.hasNext()) {
            Object o1 = i1.next();
            Object o2 = i2.next();
            if (!(o1 == null ? o2 == null : o1.equals(o2))) {
				return false;
			}
        }
        return true;
    }

    /* (non-Javadoc)
     * Method declared on IFileEditorMapping.
     */
    public IEditorDescriptor getDefaultEditor() {

        if (editors.size() == 0 || WorkbenchActivityHelper.restrictUseOf(editors.get(0))) {
			return null;
		}
        
        return (IEditorDescriptor) editors.get(0);
    }

    /* (non-Javadoc)
     * Method declared on IFileEditorMapping.
     */
    public IEditorDescriptor[] getEditors() {
    	Collection descs = WorkbenchActivityHelper.restrictCollection(editors, new ArrayList());
		return (IEditorDescriptor[]) descs.toArray(new IEditorDescriptor[descs.size()]);
    }

    /* (non-Javadoc)
     * Method declared on IFileEditorMapping.
     */
    public IEditorDescriptor[] getDeletedEditors() {
        IEditorDescriptor[] array = new IEditorDescriptor[deletedEditors.size()];
        deletedEditors.toArray(array);
        return array;
    }

    /* (non-Javadoc)
     * Method declared on IFileEditorMapping.
     */
    public String getExtension() {
        return extension;
    }

    /* (non-Javadoc)
     * Method declared on IFileEditorMapping.
     */
    public ImageDescriptor getImageDescriptor() {
        IEditorDescriptor editor = getDefaultEditor();
        if (editor == null) {
            return WorkbenchImages
                    .getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
        } 
        return editor.getImageDescriptor();   
    }

    /* (non-Javadoc)
     * Method declared on IFileEditorMapping.
     */
    public String getLabel() {
        return TextProcessor.process(name + (extension.length() == 0 ? "" : DOT + extension), STAR + DOT); 	//$NON-NLS-1$  
    }

    /* (non-Javadoc)
     * Method declared on IFileEditorMapping.
     */
    public String getName() {
        return name;
    }

    /**
     * Remove the given editor from the set of editors registered.
     * 
     * @param editor the editor to remove
     */
    public void removeEditor(EditorDescriptor editor) {
        editors.remove(editor);
        deletedEditors.add(editor);
        declaredDefaultEditors.remove(editor);
    }

    /**
     * Set the default editor registered for file type
     * described by this mapping.
     * 
     * @param editor the editor to be set as default
     */
    public void setDefaultEditor(EditorDescriptor editor) {
        editors.remove(editor);
        editors.add(0, editor);
        declaredDefaultEditors.remove(editor);
        declaredDefaultEditors.add(0, editor);
    }

    /**
     * Set the collection of all editors (EditorDescriptor)
     * registered for the file type described by this mapping.
     * Typically an editor is registered either through a plugin or explicitly by
     * the user modifying the associations in the preference pages.
     * This modifies the internal list to share the passed list.
     * (hence the clear indication of list in the method name)
     * 
     * @param newEditors the new list of associated editors
     */
    public void setEditorsList(List newEditors) {
        editors = newEditors;       
        declaredDefaultEditors.retainAll(newEditors);
    }

    /**
     * Set the collection of all editors (EditorDescriptor)
     * formally registered for the file type described by this mapping 
     * which have been deleted by the user.
     * This modifies the internal list to share the passed list.
     * (hence the clear indication of list in the method name)
     * 
     * @param newDeletedEditors the new list of associated (but deleted) editors
     */
    public void setDeletedEditorsList(List newDeletedEditors) {
        deletedEditors = newDeletedEditors;
    }

    /**
     * Set the file's extension.
     * 
     * @param extension the file extension for this mapping
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * Set the file's name.
     * 
     * @param name the file name for this mapping
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
	 * Get the editors that have been declared as default. This may be via plugin
	 * declarations or the preference page.
	 * 
	 * @return the editors the default editors
	 * @since 3.1
	 */
	public IEditorDescriptor [] getDeclaredDefaultEditors() {
		Collection descs = WorkbenchActivityHelper.restrictCollection(declaredDefaultEditors, new ArrayList());
		return (IEditorDescriptor []) descs.toArray(new IEditorDescriptor[descs.size()]);
	}
	
	/**
	 * Return whether the editor is declared default.
	 * If this is EditorDescriptor fails the ExpressionsCheck it will always
	 * return <code>false</code>, even if it's the original default editor.
	 * 
	 * @param editor the editor to test
	 * @return whether the editor is declared default
	 * @since 3.1
	 */
	public boolean isDeclaredDefaultEditor (IEditorDescriptor editor) {
		return declaredDefaultEditors.contains(editor)
				&& !WorkbenchActivityHelper.restrictUseOf(editor);
	}

	/**
	 * Set the default editors for this mapping.
	 * 
	 * @param defaultEditors the editors
	 * @since 3.1
	 */
	public void setDefaultEditors(List defaultEditors) {
		declaredDefaultEditors = defaultEditors;		
	}
}
