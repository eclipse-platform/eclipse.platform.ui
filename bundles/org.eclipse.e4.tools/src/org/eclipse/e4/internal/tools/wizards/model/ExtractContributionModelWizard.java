/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sopot Cela <sopotcela@gmail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.fragment.MFragmentFactory;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;


public class ExtractContributionModelWizard extends BaseApplicationModelWizard {

	private List<MApplicationElement> oes= new ArrayList<MApplicationElement>();

	public ExtractContributionModelWizard(MApplicationElement oe){
		super();
		oes.add(oe);
	}
	
	public ExtractContributionModelWizard(List<MApplicationElement> oes){
		this.oes.addAll(oes);
	}
	
	public ExtractContributionModelWizard(){
		super();
	}
	
	@Override
	public String getDefaultFileName() {
		return "fragment.e4xmi";
	}
	
	@Override
	protected EObject createInitialModel() {
		MModelFragments createModelFragments = MFragmentFactory.INSTANCE.createModelFragments();
		for (MApplicationElement moe : oes) {
	    MStringModelFragment createStringModelFragment = MFragmentFactory.INSTANCE.createStringModelFragment();	
		MApplicationElement e = (MApplicationElement) EcoreUtil.copy((EObject) moe);
		String featurename = ((EObject) moe).eContainmentFeature().getName();
		createStringModelFragment.setParentElementId(((MApplicationElement) ((EObject) moe).eContainer()).getElementId());
		createStringModelFragment.getElements().add(e);
		createStringModelFragment.setFeaturename(featurename);
		
		createModelFragments.getFragments().add(createStringModelFragment);
		}
		return (EObject)createModelFragments;
	}
	
	public void setup(IProject project){
		this.init(PlatformUI.getWorkbench(),new StructuredSelection(project));
	}
	@Override
	protected NewModelFilePage createWizardPage(ISelection selection) {
		return new NewModelFilePage(selection,getDefaultFileName());
	}
	
}