/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search.federated;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.help.internal.search.*;

/**
 * A search engine that is a participant in the help federated search.
 */
public interface ISearchEngine {
	void run(String query, ISearchScope scope, ISearchEngineResultCollector collector, IProgressMonitor monitor) throws CoreException;
	void cancel();
}
