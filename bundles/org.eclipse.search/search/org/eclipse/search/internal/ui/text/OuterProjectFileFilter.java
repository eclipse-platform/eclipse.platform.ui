/*******************************************************************************
 * Copyright (c) 2023 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.resources.IFile;

import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchFilter;

public class OuterProjectFileFilter extends MatchFilter {

	@Override
	public boolean filters(Match match) {
		if (match instanceof FileMatch) {
			IFile file= ((FileMatch)match).getFile();
			URI locationUri = file.getLocationURI();
			IFile innermostFile = locationUri == null ? file : //
					Arrays.stream(file.getWorkspace().getRoot().findFilesForLocationURI(locationUri)) //
							.min(Comparator.comparingInt(aFile -> aFile.getFullPath().segments().length))
							// shortest workspace (project relative) full path
							// means most nested project
							.orElse(file);
			return !file.equals(innermostFile);
		}
		return false;
	}

	@Override
	public String getName() {
		return SearchMessages.TextSearchInnermostProjectFilter_name;
	}

	@Override
	public String getDescription() {
		return SearchMessages.TextSearchInnermostProjectFilter_description;
	}

	@Override
	public String getActionLabel() {
		return SearchMessages.TextSearchInnermostProjectFilter_action_label;
	}

	@Override
	public String getID() {
		return "filter_innermost_project"; //$NON-NLS-1$
	}

}
