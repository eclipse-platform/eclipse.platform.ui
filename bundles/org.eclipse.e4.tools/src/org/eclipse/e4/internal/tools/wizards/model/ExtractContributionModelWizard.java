/*******************************************************************************
 * Copyright (c) 2010-2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Sopot Cela <sopotcela@gmail.com> - initial API and implementation
 * Marco Descher <marco@descher.at> - Bug 434371
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

public class ExtractContributionModelWizard extends BaseApplicationModelWizard {

	private final List<MApplicationElement> oes = new ArrayList<MApplicationElement>();

	public ExtractContributionModelWizard(MApplicationElement oe) {
		super();
		oes.add(oe);
	}

	public ExtractContributionModelWizard(List<MApplicationElement> oes) {
		this.oes.addAll(oes);
	}

	public ExtractContributionModelWizard() {
		super();
	}

	@Override
	public String getDefaultFileName() {
		return "fragment.e4xmi"; //$NON-NLS-1$
	}

	HashMap<String, MCommand> importCommands = new HashMap<String, MCommand>();

	public void resolveCommandImports(MApplicationElement moe, MModelFragments mModelFragments) {
		if (moe instanceof MHandler) {

			final MHandler mhandler = (MHandler) moe;
			final MCommand command = ((MHandler) moe).getCommand();
			final MCommand existImportCommand = importCommands.get(command.getElementId());
			if (existImportCommand == null) {
				final MApplicationElement copy = (MApplicationElement) EcoreUtil.copy((EObject) command);
				mhandler.setCommand((MCommand) copy);
				importCommands.put(copy.getElementId(), (MCommand) copy);
				mModelFragments.getImports().add(copy);
			} else {
				mhandler.setCommand(existImportCommand);
			}
		} else if (moe instanceof MHandledItem) {
			final MHandledItem mh = (MHandledItem) moe;
			final MCommand command = mh.getCommand();
			final MCommand existImportCommand = importCommands.get(command.getElementId());
			if (existImportCommand == null) {
				final MApplicationElement copy = (MApplicationElement) EcoreUtil.copy((EObject) command);
				mh.setCommand((MCommand) copy);
				importCommands.put(copy.getElementId(), command);
				mModelFragments.getImports().add(copy);
			} else {
				mh.setCommand(existImportCommand);
			}
		}
	}

	@Override
	protected EObject createInitialModel() {
		final MModelFragments createModelFragments = MFragmentFactory.INSTANCE.createModelFragments();
		for (final MApplicationElement moe : oes) {
			final EObject eObject = (EObject) moe;
			final TreeIterator<EObject> eAllContents = eObject.eAllContents();
			boolean hasNext = eAllContents.hasNext();
			if (!hasNext) {
				resolveCommandImports(moe, createModelFragments);
			}
			while (hasNext) {
				final MApplicationElement next = (MApplicationElement) eAllContents.next();
				resolveCommandImports(next, createModelFragments);
				hasNext = eAllContents.hasNext();
			}
			final MStringModelFragment createStringModelFragment = MFragmentFactory.INSTANCE
				.createStringModelFragment();
			final MApplicationElement e = (MApplicationElement) EcoreUtil.copy((EObject) moe);
			final String featurename = ((EObject) moe).eContainmentFeature().getName();
			createStringModelFragment.setParentElementId(((MApplicationElement) ((EObject) moe).eContainer())
				.getElementId());
			createStringModelFragment.getElements().add(e);
			createStringModelFragment.setFeaturename(featurename);

			createModelFragments.getFragments().add(createStringModelFragment);
		}
		return (EObject) createModelFragments;
	}

	public void setup(IProject project) {
		init(PlatformUI.getWorkbench(), new StructuredSelection(project));
	}

	@Override
	protected NewModelFilePage createWizardPage(ISelection selection) {
		return new NewModelFilePage(selection, getDefaultFileName());
	}

	@Override
	protected void adjustDependencies(IFile file) {
		super.adjustFragmentDependencies(file);
	}

}