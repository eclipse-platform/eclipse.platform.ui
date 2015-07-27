/*******************************************************************************
 * Copyright (c) 2011-2015 EclipseSource Muenchen GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Jonas Helming - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.fragment.MFragmentFactory;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * @author Jonas
 *
 */
public class FragmentExtractHelper {

	/**
	 * Imports referenced commands
	 *
	 * @param moe
	 *            elements to resolved referenced commands for
	 * @param importCommands
	 *            already imported Commands
	 */
	public static void resolveCommandImports(MApplicationElement moe, Map<MCommand, MCommand> importCommands) {
		if (moe instanceof MHandler) {

			final MHandler mhandler = (MHandler) moe;
			final MCommand command = ((MHandler) moe).getCommand();
			if (command == null) {
				return;
			}
			final MCommand existImportCommand = importCommands.get(command);
			if (existImportCommand == null) {
				final MApplicationElement copy = (MApplicationElement) EcoreUtil.copy((EObject) command);
				mhandler.setCommand((MCommand) copy);
				importCommands.put(command, (MCommand) copy);

			} else {
				mhandler.setCommand(existImportCommand);
			}
		} else if (moe instanceof MHandledItem) {
			final MHandledItem mh = (MHandledItem) moe;
			final MCommand command = mh.getCommand();
			if (command == null) {
				return;
			}
			final MCommand existImportCommand = importCommands.get(command);
			if (existImportCommand == null) {
				final MApplicationElement copy = (MApplicationElement) EcoreUtil.copy((EObject) command);
				mh.setCommand((MCommand) copy);
				importCommands.put(command, (MCommand) copy);
			} else {
				mh.setCommand(existImportCommand);
			}
		}
	}

	/**
	 * @param extractedElements
	 *            the {@link MApplicationElement}s to be extracted
	 */
	public static MModelFragments createInitialModel(List<MApplicationElement> extractedElements) {
		final MModelFragments mModelFragments = MFragmentFactory.INSTANCE.createModelFragments();
		final HashMap<MCommand, MCommand> importCommands = new HashMap<MCommand, MCommand>();
		for (final MApplicationElement moe : extractedElements) {
			final EObject eObject = (EObject) moe;
			final TreeIterator<EObject> eAllContents = eObject.eAllContents();
			boolean hasNext = eAllContents.hasNext();
			if (!hasNext) {
				FragmentExtractHelper.resolveCommandImports(moe, importCommands);
			}
			while (hasNext) {
				final MApplicationElement next = (MApplicationElement) eAllContents.next();
				FragmentExtractHelper.resolveCommandImports(next, importCommands);
				hasNext = eAllContents.hasNext();
			}
			final MStringModelFragment createStringModelFragment = MFragmentFactory.INSTANCE
					.createStringModelFragment();
			final MApplicationElement e = (MApplicationElement) EcoreUtil.copy((EObject) moe);
			final String featurename = ((EObject) moe).eContainmentFeature().getName();
			createStringModelFragment
			.setParentElementId(((MApplicationElement) ((EObject) moe).eContainer()).getElementId());
			createStringModelFragment.getElements().add(e);
			createStringModelFragment.setFeaturename(featurename);

			mModelFragments.getFragments().add(createStringModelFragment);
		}

		final Set<MCommand> keySet = importCommands.keySet();
		for (final MCommand key : keySet) {
			mModelFragments.getImports().add(importCommands.get(key));
		}

		return mModelFragments;
	}

}
