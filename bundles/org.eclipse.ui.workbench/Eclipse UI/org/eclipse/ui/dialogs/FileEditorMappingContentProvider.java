package org.eclipse.ui.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A content provider for displaying of <code>IFileEditorMapping</code>
 * objects in viewers.
 * <p>
 * This class has a singleton instance, 
 * <code>FileEditorMappingContentProvider.INSTANCE</code>,
 * which can be used any place this kind of content provider is needed.
 * </p>
 *
 * @see org.eclipse.ui.IResourceTypeEditorMapping
 * @see org.eclipse.jface.viewers.IContentProvider
 */ 
public class FileEditorMappingContentProvider implements IStructuredContentProvider {

	/**
	 * Singleton instance accessor.
	 */
	public final static FileEditorMappingContentProvider INSTANCE = new FileEditorMappingContentProvider();
/**
 * Creates an instance of this class.  The private visibility of this
 * constructor ensures that this class is only usable as a singleton.
 */
private FileEditorMappingContentProvider() {
	super();
}
/* (non-Javadoc)
 * Method declared on IContentProvider.
 */
public void dispose() {}
/* (non-Javadoc)
 * Method declared on IStructuredContentProvider.
 */
public Object[] getElements(Object element) {
	IFileEditorMapping[] array = (IFileEditorMapping[]) element;
	return array == null ? new Object[0] : array;
}
/* (non-Javadoc)
 * Method declared on IContentProvider.
 */
public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
}
