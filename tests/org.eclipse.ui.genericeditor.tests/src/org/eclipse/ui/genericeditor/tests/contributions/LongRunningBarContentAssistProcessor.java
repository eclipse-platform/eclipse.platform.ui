/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests.contributions;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class LongRunningBarContentAssistProcessor extends BarContentAssistProcessor {

	public static final String LONG_RUNNING_BAR_CONTENT_ASSIST_PROPOSAL = "bars are also good for soft drink cocktails.";
	public static final int TIMEOUT_MSEC = 10_000;
	private static boolean running = false;
	
	public LongRunningBarContentAssistProcessor() {
		super(LONG_RUNNING_BAR_CONTENT_ASSIST_PROPOSAL);
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		try {
			long startExecutionTime = System.currentTimeMillis();
			while (running && (System.currentTimeMillis() - startExecutionTime) < TIMEOUT_MSEC) {
				Thread.sleep(20);
			}
		} catch (InterruptedException e) {
			// Just finish on unexpected interrupt
		}
		return super.computeCompletionProposals(viewer, offset);
	}
	
	public static void enable() {
		running = true;
	}
	
	public static void finish() {
		running = false;
	}
	
}
