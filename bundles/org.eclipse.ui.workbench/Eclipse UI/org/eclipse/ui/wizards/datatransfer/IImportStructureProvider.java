package org.eclipse.ui.wizards.datatransfer;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.*;

/**
 * Interface which can provide structure and content information 
 * for an element (for example, a file system element).
 * Used by the import wizards to abstract the commonalities
 * between importing from the file system and importing from an archive.
 */
public interface IImportStructureProvider {
/**
 * Returns a collection with the children of the specified structured element.
 */
List getChildren(Object element);
/**
 * Returns the contents of the specified structured element, or
 * <code>null</code> if there is a problem determining the element's
 * contents.
 *
 * @param element a structured element
 * @return the contents of the structured element, or <code>null</code>
 */
InputStream getContents(Object element);
/**
 * Returns the full path of the specified structured element.
 *
 * @param element a structured element
 * @return the display label of the structured element
 */
String getFullPath(Object element);
/**
 * Returns the display label of the specified structured element.
 *
 * @param element a structured element
 * @return the display label of the structured element
 */
String getLabel(Object element);
/**
 * Returns a boolean indicating whether the passed structured element represents
 * a container element (as opposed to a leaf element).
 *
 * @return boolean
 * @param element java.lang.Object
 */
boolean isFolder(Object element);
}
