/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IFile;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.internal.core.refactoring.ParticipantDescriptor;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * An abstract base implementation for refactorings that are split into
 * one refactoring processor and 0..n participants.
 * <p>
 * This class should be subclassed by clients wishing to provide a special
 * refactoring which uses a processor/participant architecture.
 * </p>
 * @since 3.0 
 */
public abstract class ProcessorBasedRefactoring extends Refactoring {
	
	private RefactoringParticipant[] fParticipants;
	private SharableParticipants fSharedParticipants= new SharableParticipants();
	
	private Map/*<Object, TextChange>*/ fTextChangeMap;
	
	private static final RefactoringParticipant[] EMPTY_PARTICIPANTS= new RefactoringParticipant[0];

	private static class ProcessorChange extends CompositeChange {
		private Map fParticipantMap;
		public ProcessorChange(String name) {
			super(name);
		}
		public void setParticipantMap(Map map) {
			fParticipantMap= map;
		}
		protected void internalHandleException(Change change, Throwable e) {
			RefactoringParticipant participant= (RefactoringParticipant)fParticipantMap.get(change);
			if (participant != null) {
				ParticipantDescriptor descriptor= participant.getDescriptor();
				descriptor.disable();
				RefactoringCorePlugin.logRemovedParticipant(descriptor, e);
			}
		}
	}
	
	/**
	 * Creates a new processor based refactoring.
	 * 
	 * @deprecated use {@link #ProcessorBasedRefactoring(RefactoringProcessor)} instead
	 */
	protected ProcessorBasedRefactoring() {
	}
	
	/**
	 * Creates a new processor based refactoring.
	 * 
	 * @param processor the refactoring's main processor
	 *
	 * @since 3.1
	 */
	protected ProcessorBasedRefactoring(RefactoringProcessor processor) {
		processor.setRefactoring(this);
	}
	
	/**
	 * Return the processor associated with this refactoring. The
	 * method must not return <code>null</code>.
	 * 
	 * @return the processor associated with this refactoring
	 */
	public abstract RefactoringProcessor getProcessor();
	
	
	/**
	 * Checks whether the refactoring is applicable to the elements to be
	 * refactored or not.
	 * <p>
	 * This default implementation forwards the call to the refactoring
	 * processor.
	 * </p>
	 * @return <code>true</code> if the refactoring is applicable to the
	 *         elements; otherwise <code>false</code> is returned.
	 * @throws CoreException if the test fails
	 */
	public final boolean isApplicable() throws CoreException {
		return getProcessor().isApplicable();
	}
		
	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return getProcessor().getProcessorName();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
		if (pm == null)
			pm= new NullProgressMonitor();
		RefactoringStatus result= new RefactoringStatus();
		pm.beginTask("", 10); //$NON-NLS-1$
		pm.setTaskName(RefactoringCoreMessages.getString("ProcessorBasedRefactoring.initial_conditions")); //$NON-NLS-1$
		
		result.merge(getProcessor().checkInitialConditions(new SubProgressMonitor(pm, 8)));
		if (result.hasFatalError()) {
			pm.done();
			return result;
		}
		pm.done();
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException {
		if (pm == null)
			pm= new NullProgressMonitor();
		RefactoringStatus result= new RefactoringStatus();
		CheckConditionsContext context= createCheckConditionsContext();
		
		pm.beginTask("", 9); //$NON-NLS-1$
		pm.setTaskName(RefactoringCoreMessages.getString("ProcessorBasedRefactoring.final_conditions")); //$NON-NLS-1$
		
		result.merge(getProcessor().checkFinalConditions(new SubProgressMonitor(pm, 5), context));
		if (result.hasFatalError()) {
			pm.done();
			return result;
		}
		if (pm.isCanceled())
			throw new OperationCanceledException();
		
		fParticipants= getProcessor().loadParticipants(result, fSharedParticipants);
		if (fParticipants == null) 
			fParticipants= EMPTY_PARTICIPANTS;
		if (result.hasFatalError()) {
			pm.done();
			return result;
		}
		IProgressMonitor sm= new SubProgressMonitor(pm, 2);
		sm.beginTask("", fParticipants.length); //$NON-NLS-1$
		for (int i= 0; i < fParticipants.length && !result.hasFatalError(); i++) {
			result.merge(fParticipants[i].checkConditions(new SubProgressMonitor(sm, 1), context));
			if (sm.isCanceled())
				throw new OperationCanceledException();
		}
		sm.done();
		if (result.hasFatalError()) {
			pm.done();
			return result;
		}
		result.merge(context.check(new SubProgressMonitor(pm, 1)));
		pm.done();
		return result;		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Change createChange(IProgressMonitor pm) throws CoreException {
		if (pm == null)
			pm= new NullProgressMonitor();
		pm.beginTask("", fParticipants.length + 2); //$NON-NLS-1$
		pm.setTaskName(RefactoringCoreMessages.getString("ProcessorBasedRefactoring.create_change")); //$NON-NLS-1$
		Change processorChange= getProcessor().createChange(new SubProgressMonitor(pm, 1));
		if (pm.isCanceled())
			throw new OperationCanceledException();
		
		fTextChangeMap= new HashMap();
		addToTextChangeMap(processorChange);
		
		List changes= new ArrayList();
		Map participantMap= new HashMap();
		for (int i= 0; i < fParticipants.length; i++) {
			final RefactoringParticipant participant= fParticipants[i];
			try {
				Change change= participant.createChange(new SubProgressMonitor(pm, 1));
				if (change != null) {
					changes.add(change);
					participantMap.put(change, participant);
					addToTextChangeMap(change);
				}
			} catch (CoreException e) {
				disableParticipant(participant, e);
				throw e;
			} catch (RuntimeException e) {
				disableParticipant(participant, e);
				throw e;
			}
			if (pm.isCanceled())
				throw new OperationCanceledException();
		}
		
		fTextChangeMap= null;
		
		Change postChange= getProcessor().postCreateChange(
			(Change[])changes.toArray(new Change[changes.size()]), 
			new SubProgressMonitor(pm, 1));
		
		ProcessorChange result= new ProcessorChange(getName());
		result.add(processorChange);
		result.addAll((Change[]) changes.toArray(new Change[changes.size()]));
		result.setParticipantMap(participantMap);
		if (postChange != null)
			result.add(postChange);
		return result;
	}
	
	/**
	 * Returns the text change for the given element or <code>null</code>
	 * if a text change doesn't exist. This method only returns a valid
	 * result during change creation. Outside of change creation always
	 * <code>null</code> is returned.
	 * 
	 * @param element the element to be modified for which a text change
	 *  is requested
	 *  
	 * @return the text change or <code>null</code> if no text change exists
	 *  for the element
	 *
	 * @since 3.1
	 */
	public TextChange getTextChange(Object element) {
		if (fTextChangeMap == null)
			return null;
		return (TextChange)fTextChangeMap.get(element);
	}
	
	/**
	 * Adapts the refactoring to the given type. The adapter is resolved
	 * as follows:
	 * <ol>
	 *   <li>the refactoring itself is checked whether it is an instance
	 *       of the requested type.</li>
	 *   <li>its processor is checked whether it is an instance of the
	 *       requested type.</li>
	 *   <li>the request is delegated to the super class.</li>
	 * </ol>
	 * 
	 * @param clazz the adapter class to look up
	 * 
	 * @return the requested adapter or <code>null</code>if no adapter
	 *  exists. 
	 */
	public Object getAdapter(Class clazz) {
		if (clazz.isInstance(this))
			return this;
		if (clazz.isInstance(getProcessor()))
			return getProcessor();
		return super.getAdapter(clazz);
	}
	
	/* non java-doc
	 * for debugging only
	 */
	public String toString() {
		return getName();
	}
	
	//---- Helper methods ---------------------------------------------------------------------
	
	private CheckConditionsContext createCheckConditionsContext() throws CoreException {
		CheckConditionsContext result= new CheckConditionsContext();
		IConditionChecker checker= new ValidateEditChecker(getValidationContext());
		result.add(checker);
		return result;
	}

	private void disableParticipant(final RefactoringParticipant participant, Throwable e) {
		ParticipantDescriptor descriptor= participant.getDescriptor();
		descriptor.disable();
		RefactoringCorePlugin.logRemovedParticipant(descriptor, e);
	}
	
	private void addToTextChangeMap(Change change) {
		if (change instanceof TextChange) {
			Object element= ((TextChange)change).getModifiedElement();
			if (element != null) {
				fTextChangeMap.put(element, change);
			}
			// check if we have a subclass of TextFileChange. If so also put the change
			// under the file resource into the hash table if possible.
			if (change instanceof TextFileChange && !change.getClass().equals(TextFileChange.class)) {
				IFile file= ((TextFileChange)change).getFile();
				fTextChangeMap.put(file, change);
			}
		} else if (change instanceof CompositeChange) {
			Change[] children= ((CompositeChange)change).getChildren();
			for (int i= 0; i < children.length; i++) {
				addToTextChangeMap(children[i]);
			}
		}
	}
}