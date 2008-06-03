/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import java.io.*;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.mapping.IStorageMerger;
import org.eclipse.team.internal.ui.TeamUIMessages;

public class TextStorageMerger implements IStorageMerger {

	public IStatus merge(OutputStream output, String outputEncoding,
			IStorage ancestor, IStorage target, IStorage other,
			IProgressMonitor monitor) throws CoreException {

		LineComparator a, t, o;

		try {
			a= LineComparator.create(ancestor, outputEncoding);
			t= LineComparator.create(target, outputEncoding);
			o= LineComparator.create(other,outputEncoding);
		} catch (UnsupportedEncodingException e) {
			throw new CoreException (new Status(IStatus.ERROR, CompareUI.PLUGIN_ID, UNSUPPORTED_ENCODING, TeamUIMessages.TextAutoMerge_inputEncodingError, e));
		} catch (IOException e) {
			throw new CoreException (new Status(IStatus.ERROR, CompareUI.PLUGIN_ID, INTERNAL_ERROR, e.getMessage(), e));
		}

		try {
			boolean firstLine = true;
			String lineSeparator= System.getProperty("line.separator"); //$NON-NLS-1$
			if (lineSeparator == null)
				lineSeparator= "\n"; //$NON-NLS-1$

			RangeDifference[] diffs= RangeDifferencer.findRanges(monitor, a, t, o);

			for (int i= 0; i < diffs.length; i++) {
				RangeDifference rd= diffs[i];
				switch (rd.kind()) {
				case RangeDifference.ANCESTOR: // pseudo conflict
				case RangeDifference.NOCHANGE:
				case RangeDifference.RIGHT:
					for (int j= rd.rightStart(); j < rd.rightEnd(); j++) {
						String s= o.getLine(j);
						if (!firstLine)
							output.write(lineSeparator.getBytes(outputEncoding));
						output.write(s.getBytes(outputEncoding));
						firstLine = false;
					}
					break;

				case RangeDifference.LEFT:
					for (int j= rd.leftStart(); j < rd.leftEnd(); j++) {
						String s= t.getLine(j);
						if (!firstLine)
							output.write(lineSeparator.getBytes(outputEncoding));
						output.write(s.getBytes(outputEncoding));
						firstLine = false;
					}
					break;

				case RangeDifference.CONFLICT:
					return new Status(IStatus.WARNING, CompareUI.PLUGIN_ID, CONFLICT, TeamUIMessages.TextAutoMerge_conflict, null);

				default:
					break;
				}
			}

		} catch (UnsupportedEncodingException e) {
			throw new CoreException (new Status(IStatus.ERROR, CompareUI.PLUGIN_ID, UNSUPPORTED_ENCODING, TeamUIMessages.TextAutoMerge_outputEncodingError, e));
		} catch (IOException e) {
			return new Status(IStatus.ERROR, CompareUI.PLUGIN_ID, INTERNAL_ERROR, TeamUIMessages.TextAutoMerge_outputIOError, e);
		}

		return Status.OK_STATUS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IStorageMerger#canMergeWithoutAncestor()
	 */
	public boolean canMergeWithoutAncestor() {
		return false;
	}

}
