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
import org.eclipse.team.core.history.IFileRevision;
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
	
	public EditionHistoryPage(IFile file, Object element) {
		Assert.isNotNull(file);
		Assert.isNotNull(element);
		this.file = file;
		this.element = element;
		this.localFileElement= new LocalResourceTypedElement(getFile());
		StructureCreatorDescriptor scd= CompareUIPlugin.getDefault().getStructureCreator(localFileElement.getType());
		if (scd != null) {
			structureCreator= scd.createStructureCreator();
		}
		localEdition = createLocalEdition();
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
		ITypedElement previousEdition = null;
		List result = new ArrayList();
		sortDescending(revisions);
		for (int i = 0; i < revisions.length; i++) {
			IFileRevision revision = revisions[i];
			ITypedElement edition = getEditionFor(new FileRevisionTypedElement(revision));
			if (edition != null && !contentsEqual(previousEdition, edition)) {
				editions.put(revision, edition);
				previousEdition = edition;
				result.add(revision);
			}
		}
		return (IFileRevision[]) result.toArray(new IFileRevision[result.size()]);
	}
	
	private void sortDescending(IFileRevision[] revisions) {
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

	private boolean contentsEqual(ITypedElement previousEdition,
			ITypedElement edition) {
		if (previousEdition == null)
			return false;
		String contents1 = structureCreator.getContents(previousEdition, false /* TODO: Ignore whitespace */);
		String contents2 = structureCreator.getContents(edition, false /* TODO: Ignore whitespace */);
		return (contents1 != null && contents2 != null && contents1.equals(contents2));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.history.LocalHistoryPage#getCompareInput(java.lang.Object)
	 */
	public ICompareInput getCompareInput(Object object) {
		ITypedElement edition = (ITypedElement)editions.get(object);
		if (edition != null && localEdition != null)
			return new DiffNode(localEdition, edition);
		return null;
	}
	
	private ITypedElement createLocalEdition() {
		IStructureCreator creator = structureCreator;
		if (creator == null)
			return null;
		ITypedElement result = null;
		if (creator instanceof IStructureCreator2) {
			IStructureCreator2 sc2 = (IStructureCreator2) creator;
			try {
				result = sc2.createElement(element, localFileElement, null);
			} catch (CoreException e) {
				TeamUIPlugin.log(e);
			}
		}
		if (result == null) {
			result = getEditionFor(localFileElement);
		}
		return result;
	}
	
	private ITypedElement getEditionFor(ITypedElement input) {
		IStructureCreator creator = structureCreator;
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
