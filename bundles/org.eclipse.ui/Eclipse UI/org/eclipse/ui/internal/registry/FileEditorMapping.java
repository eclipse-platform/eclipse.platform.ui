package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.internal.*;
import org.eclipse.ui.*;
import org.eclipse.jface.resource.ImageDescriptor;
import java.util.*;


/* (non-Javadoc)
 * Implementation of IFileEditorMapping.
 */
public class FileEditorMapping extends Object 
	implements IFileEditorMapping, Cloneable 
{
	private String name = "*";//$NON-NLS-1$
	private String extension;
	
	// Collection of EditorDescriptor, where the first one
	// if considered the default one.
	private List editors = new ArrayList();
	private List deletedEditors = new ArrayList();
/**
 *  Create an instance of this class.
 *
 *  @param extension java.lang.String
 *  @param mimeType java.lang.String
 */
public FileEditorMapping(String extension) {
	this("*", extension);//$NON-NLS-1$
}
/**
 *  Create an instance of this class.
 *
 *  @param name java.lang.String
 *  @param extension java.lang.String
 */
public FileEditorMapping(String name, String extension) {
	super();
	if (name == null || name.length() < 1)
		setName("*");//$NON-NLS-1$
	else
		setName(name);
	if (extension == null)
		setExtension("");//$NON-NLS-1$
	else
		setExtension(extension);
}
/**
 * Add the given editor to the list of editors registered.
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
		FileEditorMapping clone = (FileEditorMapping)super.clone();
		clone.editors = (List)((ArrayList)editors).clone();
		return clone;
	} catch (CloneNotSupportedException e) {
		return null;
	}
}
/* (non-Javadoc)
 * Method declared on IFileEditorMapping.
 */
public IEditorDescriptor getDefaultEditor() {
	if (editors.size() == 0) 
		return null;
	else 
		return (IEditorDescriptor)editors.get(0);
}
/* (non-Javadoc)
 * Method declared on IFileEditorMapping.
 */
public IEditorDescriptor[] getEditors() {
	IEditorDescriptor[] array = new IEditorDescriptor[editors.size()];
	editors.toArray(array);
	return array;   
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
		return WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
	} else {
		return editor.getImageDescriptor();
	}
}
/* (non-Javadoc)
 * Method declared on IFileEditorMapping.
 */
public String getLabel() { 
	return name + (extension.length() == 0 ? "" : "." + extension);//$NON-NLS-1$ //$NON-NLS-2$
}
/* (non-Javadoc)
 * Method declared on IFileEditorMapping.
 */
public String getName() {
	return name;
}
/**
 * Remove the given editor from the set of editors registered.
 */
public void removeEditor(EditorDescriptor editor) {
	editors.remove(editor);
	deletedEditors.add(editor);
}
/**
 * Set the default editor registered for file type
 * described by this mapping.
 */
public void setDefaultEditor(EditorDescriptor editor) {
	editors.remove(editor);
	editors.add(0,editor);
}
/**
 * Set the collection of all editors (EditorDescriptor)
 * registered for the file type described by this mapping.
 * Typically an editor is registered either through a plugin or explicitly by
 * the user modifying the associations in the preference pages.
 * This modifies the internal list to share the passed list.
 * (hence the clear indication of list in the method name)
 */
public void setEditorsList(List newEditors) {
	editors = newEditors;
}
/**
 * Set the collection of all editors (EditorDescriptor)
 * formally registered for the file type described by this mapping 
 * which have been deleted by the user.
 * This modifies the internal list to share the passed list.
 * (hence the clear indication of list in the method name)
 */
public void setDeletedEditorsList(List newDeletedEditors) {
	deletedEditors = newDeletedEditors;
}
/**
 * Set the file's extension.
 */
public void setExtension(String extension) {
	this.extension = extension;
}
/**
 * Set the file's name.
 */
public void setName(String name) {
	this.name = name;
}
}
