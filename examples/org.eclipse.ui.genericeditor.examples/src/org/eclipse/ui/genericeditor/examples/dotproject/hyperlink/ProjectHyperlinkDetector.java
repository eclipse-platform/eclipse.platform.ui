/*******************************************************************************
 * Copyright (c) 2017 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Simon Scholz <simon.scholz@vogella.com> - initial implementation
 *******************************************************************************/
package org.eclipse.ui.genericeditor.examples.dotproject.hyperlink;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;
import org.eclipse.ui.texteditor.ITextEditor;

@SuppressWarnings("restriction")
public class ProjectHyperlinkDetector extends AbstractHyperlinkDetector {

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		ITextEditor textEditor = getAdapter(ITextEditor.class);

		IResource resource = textEditor.getEditorInput().getAdapter(IResource.class);

		String fileLocation = TextProcessor.process(IDEResourceInfoUtils.getLocationText(resource));

		ShowInSystemExplorerHyperlink showInSystemExplorerHyperlink = new ShowInSystemExplorerHyperlink(fileLocation,
				region);

		URLHyperlink fileUrlHyperlink = new URLHyperlink(region, fileLocation);

		URLHyperlink projectFileHelpLink = new URLHyperlink(region,
				"https://help.eclipse.org/oxygen/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fmisc%2Fproject_description_file.html");

		return new IHyperlink[] { showInSystemExplorerHyperlink, fileUrlHyperlink, projectFileHelpLink };
	}

}
