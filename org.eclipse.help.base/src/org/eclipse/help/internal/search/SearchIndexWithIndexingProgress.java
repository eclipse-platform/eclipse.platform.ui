/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;

import org.eclipse.help.internal.toc.*;

public class SearchIndexWithIndexingProgress extends SearchIndex {
	private ProgressDistributor progressDistributor;
	/**
	 * @param locale
	 * @param analyzerDesc
	 * @param tocManager
	 */
	public SearchIndexWithIndexingProgress(String locale,
			AnalyzerDescriptor analyzerDesc, TocManager tocManager) {
		super(locale, analyzerDesc, tocManager);
		progressDistributor = new ProgressDistributor();
	}
	/**
	 * @return Returns the progressDistributor.
	 */
	public ProgressDistributor getProgressDistributor() {
		return progressDistributor;
	}
}
