package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.core.ContentReference;
import org.eclipse.update.core.IContentConsumer;

/**
 * ContentConsumer for a non plugin entry of a feature
 */

public class NonPluginEntryContentConsumer extends ContentConsumer {

	private boolean closed = false;

	private IContentConsumer contentConsumer;
		
	/*
	 * Constructor
	 */
	public NonPluginEntryContentConsumer(IContentConsumer contentConsumer){
		this.contentConsumer = contentConsumer;
	}

	/*
	 * @see ContentConsumer#store(ContentReference, IProgressMonitor)
	 */
	public void store(ContentReference contentReference, IProgressMonitor monitor) throws CoreException {
		if (!closed){
			contentConsumer.store( contentReference,monitor);
		} else {
			UpdateManagerPlugin.warn("Attempt to store in a closed NonPluginEntryContentConsumer",new Exception());
		}
	}

	/*
	 * @see ContentConsumer#close()
	 */
	public void close() throws CoreException  {
		if (!closed){
			closed = true;
			contentConsumer.close();
		} else {
			UpdateManagerPlugin.warn("Attempt to close a closed NonPluginEntryContentConsumer",new Exception());
		}
	}

}