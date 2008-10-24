/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.services.IEvaluationService;

/**
 * A source provider for the active debug context variable.
 * 
 * @since 3.5
 */
public class DebugContextSourceProvider extends AbstractSourceProvider implements IDebugContextListener {

    /**
     * The names of the sources supported by this source provider.
     */
    private static final String[] PROVIDED_SOURCE_NAMES = new String[] { IDebugUIConstants.DEBUG_CONTEXT_SOURCE_NAME };
    
    private final IDebugContextService fDebugContextService;
    
	private final IEvaluationService fEvaluationService;
	
	/**
	 * Creates the source provider.  It registers it as a listener to the 
	 * given debug context service, and as a provider with the given 
	 * evaluation service.
	 */
	public DebugContextSourceProvider(IDebugContextService debugContextService, IEvaluationService evaluationService) {
		fDebugContextService = debugContextService;
		fDebugContextService.addDebugContextListener(this);
		fEvaluationService = evaluationService;
		fEvaluationService.addSourceProvider(this);
	}

	public void debugContextChanged(DebugContextEvent event) {
        Map values = new HashMap(1);
        values.put(IDebugUIConstants.DEBUG_CONTEXT_SOURCE_NAME, event.getContext());
        fireSourceChanged(ISources.ACTIVE_CURRENT_SELECTION, values);
	}
	
	public void dispose() {
		fDebugContextService.removeDebugContextListener(this);
		fEvaluationService.removeSourceProvider(this);
	}

	public String[] getProvidedSourceNames() {
		return PROVIDED_SOURCE_NAMES;
	}

	public Map getCurrentState() {
	    Map currentState = new HashMap(1);
	    currentState.put(IDebugUIConstants.DEBUG_CONTEXT_SOURCE_NAME, fDebugContextService.getActiveContext());
	    return currentState;
	}

}
