/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.deferred;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.internal.CommonNavigatorMessages;
import org.eclipse.ui.navigator.internal.InstanceSchedulingRule;
import org.eclipse.ui.progress.UIJob;


/**
 * Elements of the following class were adapted from (@see
 * org.eclipse.ui.navigator.DeferredTreeContentManager)
 * 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class TreeViewerElementCollector implements IPendingElementCollector {

	private Map instanceRuleCache = null;

	private AbstractTreeViewer treeViewer = null;

	public TreeViewerElementCollector(AbstractTreeViewer treeViewer) {
		this.treeViewer = treeViewer;
	}

	public synchronized void collectChildren(Object parent, Object[] children) {
		getDefaultUIUpdateJob(parent, children).schedule();
	}

	public void done(PendingUpdateAdapter placeHolder) {

		//Only schedule if there is a workbench
		if (!placeHolder.isRemoved() && PlatformUI.isWorkbenchRunning()) {
			getDefaultUIComplete(placeHolder).schedule();
		}
	}

	protected UIJob getDefaultUIUpdateJob(Object parent, Object[] children) {
		return new TreeViewerUIUpdateJob(parent, children);
	}

	/**
	 * @param placeHolder
	 *            the placeHolder which needs to be removed from the TreeViewer
	 * @param monitor
	 * @return the default instance of the UI completion job for immediate scheduling
	 */
	protected UIJob getDefaultUIComplete(PendingUpdateAdapter placeHolder) {
		return new ClearPlaceHolderJob(placeHolder);
	}

	/**
	 * @return Returns the treeViewer.
	 */
	protected AbstractTreeViewer getTreeViewer() {
		return treeViewer;
	}

	protected Map getWrappedRuleCache() {
		if (instanceRuleCache == null)
			instanceRuleCache = new WeakHashMap();
		return instanceRuleCache;
	}

	protected ISchedulingRule getCachedRule(Object ruleObject) {
		ISchedulingRule rule = (ISchedulingRule) getWrappedRuleCache().get(ruleObject);
		if (rule == null) {
			rule = new InstanceSchedulingRule(ruleObject);
			getWrappedRuleCache().put(ruleObject, rule);
		}
		return rule;
	}

	public class TreeViewerUIUpdateJob extends UIJob {

		protected Object parent = null;

		protected Object[] children = null;

		public TreeViewerUIUpdateJob(Object parent, Object[] children) {
			super(CommonNavigatorMessages.TreeViewerElementCollector_0); 

			this.parent = parent;
			this.children = children;

			//this.setRule(TreeViewerElementCollector.this.getCachedRule(this.parent));
			this.setRule(new InstanceSchedulingRule(treeViewer));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.navigator.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus runInUIThread(IProgressMonitor updateMonitor) {
			if (children == null || children.length == 0)
				return Status.OK_STATUS;

			synchronized (treeViewer) {
				//Cancel the job if the tree viewer got closed
				if (treeViewer.getControl().isDisposed())
					return Status.CANCEL_STATUS;

				//Prevent extra redraws on deletion and addition
				treeViewer.getControl().setRedraw(false);
				treeViewer.add(parent, children);
				treeViewer.getControl().setRedraw(true);
			}

			return Status.OK_STATUS;
		}
	}

	public class ClearPlaceHolderJob extends UIJob {

		private PendingUpdateAdapter placeHolder = null;

		public ClearPlaceHolderJob(PendingUpdateAdapter placeHolder) {
			super(CommonNavigatorMessages.TreeViewerElementCollector_1); 
			setSystem(true);
			this.placeHolder = placeHolder;

			//this.setRule(TreeViewerElementCollector.this.getCachedRule(this.parent));
			this.setRule(new InstanceSchedulingRule(treeViewer));

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.navigator.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus runInUIThread(IProgressMonitor monitor) {
			synchronized (treeViewer) {
				if (!placeHolder.isRemoved()) {
					Control control = treeViewer.getControl();

					if (control.isDisposed())
						return Status.CANCEL_STATUS;

					getTreeViewer().remove(placeHolder);
					placeHolder.setRemoved(true);
				}
				return Status.OK_STATUS;
			}
		}
	}
}
