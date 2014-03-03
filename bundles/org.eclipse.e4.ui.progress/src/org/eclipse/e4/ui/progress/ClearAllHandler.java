/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.progress;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.progress.internal.FinishedJobs;


/**
 * Removes finished jobs from the progress view.
 *
 * @noreference
 */
public class ClearAllHandler {

	@Execute
	public static void clearAll(FinishedJobs finishedJobs) {
		finishedJobs.clearAll();
	}


}
