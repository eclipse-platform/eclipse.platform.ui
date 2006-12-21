/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.history;

import java.util.*;

import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.StructureCreatorDescriptor;
import org.eclipse.compare.structuremergeviewer.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.core.history.LocalFileHistory;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.LocalResourceTypedElement;

/**
 * A history page for a sub-element of a file. 
 */
public class EditionHistoryPage extends LocalHistoryPage {
	
	private final IFile file;
	private final Object element;
	private final LocalResourceTypedElement localFileElement;
	private IStructureCreator structureCreator;
	private Map editions = new HashMap();
	private ITypedElement localEdition;
	
	public static ITypedElement getPreviousState(IFile file, Object element) throws TeamException {
		LocalResourceTypedElement localFileElement= new LocalResourceTypedElement(file);
		IStructureCreator structureCreator = getStructureCreator(localFileElement);
		if (structureCreator == null)
			return null;
		LocalFileHistory history = new LocalFileHistory(file, false);
		history.refresh(new NullProgressMonitor());
		IFileRevision[] revisions = history.getFileRevisions();
		if (revisions.length == 0)
			return null;
		sortDescending(revisions);
		ITypedElement localEdition = null;
		try {
			localEdition = createLocalEdition(structureCreator, localFileElement, element);
			for (int i = 0; i < revisions.length; i++) {
				IFileRevision revision = revisions[i];
				ITypedElement edition = createEdition(structureCreator, element, new FileRevisionTypedElement(revision));
				if (edition != null && !contentsEqual(structureCreator, localEdition, edition)) {
					return edition;
				}
			}
		} finally {
			if (localEdition != null)
				destroyLocalEdition(structureCreator, localFileElement, localEdition);
		}
		return null;
	}

	private static IStructureCreator getStructureCreator(ITypedElement element) {
		StructureCreatorDescriptor scd= CompareUIPlugin.getDefault().getStructureCreator(element.getType());
		if (scd != null) {
			return scd.createStructureCreator();
		}
		return null;
	}
	
	public EditionHistoryPage(IFile file, Object element) {
		Assert.isNotNull(file);
		Assert.isNotNull(element);
		this.file = file;
		this.element = element;
		this.localFileElement= new LocalResourceTypedElement(getFile());
		structureCreator = getStructureCreator(localFileElement);
		localEdition = createLocalEdition(structureCreator, localFileElement, element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.history.LocalHistoryPage#getFile()
	 */
	protected IFile getFile() {
		return file;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.history.LocalHistoryPage#inputSet()
	 */
	public boolean inputSet() {
		if (super.inputSet()) {
			return localEdition != null;	
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.history.LocalHistoryPage#update(org.eclipse.team.core.history.IFileRevision[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void update(IFileRevision[] revisions, IProgressMonitor monitor) {
		monitor.beginTask(null, 100);
		IFileRevision[] filtered = filterRevisions(revisions, Policy.subMonitorFor(monitor, 75));
		super.update(filtered, Policy.subMonitorFor(monitor, 25));
		monitor.done();
	}

	private IFileRevision[] filterRevisions(IFileRevision[] revisions,
			IProgressMonitor monitor) {
		ITypedElement previousEdition = localEdition;
		List result = new ArrayList();
		sortDescending(revisions);
		for (int i = 0; i < revisions.length; i++) {
			IFileRevision revision = revisions[i];
			ITypedElement edition = createEdition(new FileRevisionTypedElement(revision));
			if (edition != null && !contentsEqual(structureCreator, previousEdition, edition)) {
				editions.put(revision, edition);
				previousEdition = edition;
				result.add(revision);
			}
		}
		return (IFileRevision[]) result.toArray(new IFileRevision[result.size()]);
	}
	
	private static void sortDescending(IFileRevision[] revisions) {
		Arrays.sort(revisions, new Comparator() {
			public int compare(Object o1, Object o2) {
				IFileRevision d1= (IFileRevision) o1;
				IFileRevision d2= (IFileRevision) o2;
				long d= d2.getTimestamp() - d1.getTimestamp();
				if (d < 0)
					return -1;
				if (d > 0)
					return 1;
				return 0;
			}
		});
	}

	private static boolean contentsEqual(IStructureCreator creator, ITypedElement previousEdition,
			ITypedElement edition) {
		if (previousEdition == null)
			return false;
		String contents1 = creator.getContents(previousEdition, false /* TODO: Ignore whitespace */);
		String contents2 = creator.getContents(edition, false /* TODO: Ignore whitespace */);
		return (contents1 != null && contents2 != null && contents1.equals(contents2));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.history.LocalHistoryPage#getCompareInput(java.lang.Object)
	 */
	public ICompareInput getCompareInput(Object object) {
		ITypedElement edition = getEditionFor(object);
		if (edition != null && localEdition != null)
			return new DiffNode(localEdition, edition);
		return null;
	}

	public ITypedElement getEditionFor(Object object) {
		return (ITypedElement)editions.get(object);
	}
	
	private static ITypedElement createLocalEdition(IStructureCreator creator, ITypedElement input, Object element) {
		if (creator == null)
			return null;
		ITypedElement result = null;
		if (creator instanceof IStructureCreator2) {
			IStructureCreator2 sc2 = (IStructureCreator2) creator;
			try {
				result = sc2.createElement(element, input, null);
			} catch (CoreException e) {
				TeamUIPlugin.log(e);
			}
		}
		if (result == null) {
			result = createEdition(creator, element, input);
		}
		return result;
	}
	
	private ITypedElement createEdition(ITypedElement input) {
		return createEdition(structureCreator, element, input);
	}
	
	private static ITypedElement createEdition(IStructureCreator creator, Object element, ITypedElement input) {
		if (creator == null)
			return null;
		IStructureComparator edition = creator.locate(element, input);
		if (edition instanceof ITypedElement)
			return (ITypedElement) edition;
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.history.LocalHistoryPage#isValidInput(java.lang.Object)
	 */
	public boolean isValidInput(Object object) {
		// This page doesn't support input changes
		return object.equals(element);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.history.LocalHistoryPage#dispose()
	 */
	public void dispose() {
		try {
			disconnect();
		} finally {
			super.dispose();
		}
	}
	
	private void disconnect() {
		if (localFileElement != null)
			localFileElement.discardBuffer();
		if (localEdition != null && structureCreator instanceof IStructureCreator2) {
			IStructureCreator2 sc2 = (IStructureCreator2) structureCreator;
			sc2.destroy(localEdition);
		}
		localEdition = null;
		structureCreator = null;
	}
	
	private static void destroyLocalEdition(
			IStructureCreator structureCreator, LocalResourceTypedElement localFileElement, ITypedElement localEdition) {
		if (localFileElement != null)
			localFileElement.discardBuffer();
		if (localEdition != null && structureCreator instanceof IStructureCreator2) {
			IStructureCreator2 sc2 = (IStructureCreator2) structureCreator;
			sc2.destroy(localEdition);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.history.LocalHistoryPage#getNoChangesMessage()
	 */
	protected String getNoChangesMessage() {
		if (localEdition != null)
			return NLS.bind(TeamUIMessages.EditionHistoryPage_0, localEdition.getName());
		return TeamUIMessages.EditionHistoryPage_1;
	}
	
	protected Image getImage(Object object) {
		if (object == localEdition)
			return localEdition.getImage();
		Object revision = getRevisionFor(object);
		if (revision != null)
			return super.getImage(revision);
		return super.getImage(object);
	}
	
	protected String getLabel(Object object) {
		Object revision = getRevisionFor(object);
		if (revision != null)
			return super.getLabel(revision);
		return super.getLabel(object);
	}

	private Object getRevisionFor(Object object) {
		if (object == localEdition)
			return localFileElement;
		for (Iterator iterator = editions.keySet().iterator(); iterator.hasNext();) {
			IFileRevision revision = (IFileRevision) iterator.next();
			if (editions.get(revision) == object) {
				return revision;
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.history.LocalHistoryPage#getName()
	 */
	public String getName() {
		if (localEdition != null)
			return localEdition.getName();
		return super.getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.history.LocalHistoryPage#getDescription()
	 */
	public String getDescription() {
		if (localEdition != null)
			return NLS.bind(TeamUIMessages.EditionHistoryPage_2, localEdition.getName());
		return super.getDescription();
	}

}
