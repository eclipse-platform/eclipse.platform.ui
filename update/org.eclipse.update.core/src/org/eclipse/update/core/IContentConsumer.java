package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
 
 /**
  * A content consumer manages the storage of content.
  */
 
public interface IContentConsumer {

	/**
	 * Stores a content reference
	 * @param ContentReference the content reference to store
	 * @param IProgressMonitor the progress monitor
	 * @throws CoreException if an error occurs storing the content reference
	 * @since 2.0 
	 */

	void store(ContentReference contentReference, IProgressMonitor monitor) throws CoreException;

	/**
	 * closes the opened ContentConsumer
	 * @since 2.0 
	 */

	void close() throws CoreException ;
	
	}


