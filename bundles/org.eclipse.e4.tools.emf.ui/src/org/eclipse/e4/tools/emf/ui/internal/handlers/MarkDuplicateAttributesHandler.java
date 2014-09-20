/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 432372, Ongoing Maintenance
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.handlers;

import java.util.Collection;
import java.util.HashSet;
import javax.inject.Named;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.IViewEObjects;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.TitleAreaFilterDialogWithEmptyOptions;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class MarkDuplicateAttributesHandler extends MarkDuplicateItemsBase {

	@Execute
	public void execute(@Named(VIEWER_KEY) IViewEObjects viewer, IEclipseContext context, final Messages Messages) {
		final HashSet<String> set = new HashSet<String>();
		Collection<EObject> allEObjects = viewer.getAllEObjects();
		for (EObject obj : allEObjects) {
			for (EAttribute attribute : obj.eClass().getEAllAttributes()) {
				set.add(attribute.getName());
			}
		}
		ILabelProvider renderer = new LabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(element);
			}
		};

		TitleAreaFilterDialogWithEmptyOptions dlg = new TitleAreaFilterDialogWithEmptyOptions(context.get(Shell.class), renderer) {
			@Override
			protected Control createContents(Composite parent) {
				Control ret = super.createContents(parent);
				setMessage(Messages.MarkDuplicateAttributesHandler_SelectAnAttributeToMarkeDuplicate);
				setTitle(Messages.MarkDuplicateAttributesHandler_MarkDuplicates);
				setElements(set.toArray(new String[0]));
				return ret;
			}
		};
		if (dlg.open() == Window.OK) {
			Collection<EObject> marked = getDuplicateList(dlg.getFirstElement().toString(), allEObjects);
			applyEmptyOption(marked, dlg.getFirstElement().toString(), dlg.getEmptyFilterOption());
			viewer.highlightEObjects(marked);
		}
	}
}