package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IFile;

/**
 * This interface is used as API for the Readme parser extension 
 * point. The default implementation simply looks for lines
 * in the file that start with a number and assumes that they
 * represent sections. Tools are allowed to replace this 
 * algorithm by defining an extension and supplying an 
 * alternative that implements this interface.
 */
public interface IReadmeFileParser {
/**
 * Parses the contents of the provided file
 * and generates a collection of sections.
 */
public MarkElement[] parse(IFile readmeFile);
}
