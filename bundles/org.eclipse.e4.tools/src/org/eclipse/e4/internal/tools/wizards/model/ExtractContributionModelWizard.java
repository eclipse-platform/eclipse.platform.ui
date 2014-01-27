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
import java.util.HashMap;
import java.util.List;

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
	HashMap<String, MCommand> importCommands=new HashMap<String, MCommand>();
    public void resolveCommandImports(MApplicationElement moe,MModelFragments mModelFragments){
    	if (moe instanceof MHandler) {

    		MHandler mhandler = (MHandler) moe;
    		MCommand command = ((MHandler) moe).getCommand();
    		MCommand existImportCommand = importCommands.get(command.getElementId());
    		if(existImportCommand==null){
    			MApplicationElement copy = (MApplicationElement) EcoreUtil.copy((EObject)command);
    			mhandler.setCommand((MCommand) copy);
    			importCommands.put(copy.getElementId(), (MCommand) copy);
				mModelFragments.getImports().add(copy);
    		}else{
    			mhandler.setCommand(existImportCommand);
    		}
    	}else if(moe instanceof MHandledItem){
    		MHandledItem mh=(MHandledItem) moe;
    		MCommand command = mh.getCommand();
    		MCommand existImportCommand = importCommands.get(command.getElementId());
    		if(existImportCommand==null){
    			MApplicationElement copy = (MApplicationElement) EcoreUtil.copy((EObject)command);
    			mh.setCommand((MCommand) copy);
    			importCommands.put(copy.getElementId(), command);
    			mModelFragments.getImports().add(copy);
    		}else{
    			mh.setCommand(existImportCommand);
    		}
    	}
    }

	@Override
	protected EObject createInitialModel() {
		MModelFragments createModelFragments = MFragmentFactory.INSTANCE.createModelFragments();
		for (MApplicationElement moe : oes) {
		EObject eObject=(EObject) moe;
		TreeIterator<EObject> eAllContents = eObject.eAllContents();
		boolean hasNext = eAllContents.hasNext();
		if(!hasNext){
			resolveCommandImports(moe, createModelFragments);
		}
		while(hasNext){
			MApplicationElement next=(MApplicationElement) eAllContents.next();
			resolveCommandImports(next, createModelFragments);
			hasNext=eAllContents.hasNext();
		}
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