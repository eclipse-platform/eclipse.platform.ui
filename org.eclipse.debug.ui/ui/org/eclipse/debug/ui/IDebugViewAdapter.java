package org.eclipse.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.viewers.StructuredViewer;

/**
 * An adapter that debug views containing a structured viewer implement.
 * Provides access to enclosed viewer and debug model presentation being
 * used by a viewer. This allows clients to do such things as add and
 * remove filters to a viewer, and configure a debug model presentation.
 * The following debug views support this adapter:
 * <ul>
 * <li>Debug view</li>
 * <li>Process view</li>
 * <li>Breakpoint view</li>
 * <li>Variable view</li>
 * <li>Inspector view</li>
 * </ul>
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * <p>
 * <b>NOTE:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see org.eclipse.core.runtime.IAdaptable
 * @see IDebugModelPresentation
 */

public interface IDebugViewAdapter {
	
	/**
	 * Returns the structured viewer contained in this debug view.
	 *
	 * @return viewer
	 */
	public StructuredViewer getViewer();
	
	/**
	 * Returns the debug model presentation for this view specified
	 * by the debug model identifier.
	 *
	 * @param id the debug model identifier that corresponds to the <code>id</code>
	 *     attribute of a debug model presentation extension
	 * @return the debug model presentation, or <code>null</code> if no
	 *     presentation is registered for the specified id
	 */
	public IDebugModelPresentation getPresentation(String id);
	
}