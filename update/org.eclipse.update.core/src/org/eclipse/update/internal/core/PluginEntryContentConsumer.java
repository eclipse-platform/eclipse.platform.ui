package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.*;

/**
 * Default implementation of an Executable DefaultFeature
 */

public class PluginEntryContentConsumer extends ContentConsumer {

	private boolean closed = false;

	private IContentConsumer contentConsumer;
		
	/**
	 * Constructor
	 */
	public PluginEntryContentConsumer(IContentConsumer contentConsumer){
		this.contentConsumer = contentConsumer;
	}

	/*
	 * @see IFeatureContentConsumer#store(ContentReference, IProgressMonitor)
	 */
	public void store(ContentReference contentReference, IProgressMonitor monitor) throws CoreException {
		contentConsumer.store( contentReference,monitor);
	}

	/*
	 * @see IFeatureContentConsumer#close()
	 */
	public void close() throws CoreException {
		closed = true;
		contentConsumer.close();
	}

}