/**********************************************************************
 * Copyright (c) 2002, 2004 Geoff Longman and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Geoff Longman - Initial API and implementation
 * IBM - Tightening integration with existing Platform
 **********************************************************************/
package org.eclipse.core.tools.resources;

import java.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.tools.SpyView;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;

/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class ResourceChangeView extends SpyView implements IResourceChangeListener {
	protected TreeViewer viewer;
	protected Set eventActions = new HashSet();
	private Action POST_BUILD;
	private Action PRE_BUILD;
	private Action POST_CHANGE;
	private Action PHANTOMS;
	protected Object rootObject = ResourcesPlugin.getWorkspace();
	protected int typeFilter = IResourceChangeEvent.POST_CHANGE;
	protected boolean showPhantoms = false;

	/**
	 * The constructor.
	 */
	public ResourceChangeView() {
		super();
	}

	private void setRoot(IResourceChangeEvent event) {
		if (viewer == null)
			return;
		Display display = viewer.getControl().getDisplay();
		if (display == null)
			return;
		rootObject = event;
		display.asyncExec(new Runnable() {
			public void run() {
				viewer.getControl().setRedraw(false);
				viewer.setInput(ResourceChangeView.this.rootObject);
				viewer.expandAll();
				viewer.getControl().setRedraw(true);
			}
		});
	}

	/**
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() != typeFilter)
			return;
		setRoot(event);
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(rootObject);

		initializeActions();

		IActionBars bars = getViewSite().getActionBars();
		IMenuManager menuMgr = bars.getMenuManager();
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(final IMenuManager manager) {
				fillPullDownBar(manager);
			}
		});
		fillPullDownBar(menuMgr);

		// register for all types of events and then filter out the one that we
		// want based on the action settings.
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_BUILD | IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_BUILD);
	}

	private void initializeActions() {
		// create the actions for the drop-down menu
		POST_BUILD = new FilterAction("POST_BUILD", IResourceChangeEvent.POST_BUILD); //$NON-NLS-1$
		POST_CHANGE = new FilterAction("POST_CHANGE", IResourceChangeEvent.POST_CHANGE); //$NON-NLS-1$
		// default event type
		POST_CHANGE.setChecked(true);
		PRE_BUILD = new FilterAction("PRE_BUILD", IResourceChangeEvent.PRE_BUILD); //$NON-NLS-1$
		eventActions.add(POST_BUILD);
		eventActions.add(POST_CHANGE);
		eventActions.add(PRE_BUILD);
		PHANTOMS = new FilterPhantoms();
	}

	protected void fillPullDownBar(IMenuManager manager) {
		manager.removeAll();

		for (Iterator i = eventActions.iterator(); i.hasNext();)
			manager.add((IAction) i.next());
		manager.add(new Separator("phantoms")); //$NON-NLS-1$
		manager.add(PHANTOMS);
	}

	class DeltaNode implements IAdaptable {
		private DeltaNode parent = null;
		private int deltaKind = -1;
		private int deltaFlags = -1;
		private IResource resource;
		private IPath path;
		private ArrayList children;

		public DeltaNode() {
			children = new ArrayList();
		}

		public DeltaNode(IResourceDelta delta) {
			this();
			populateFromDelta(delta);
		}

		public void setParent(DeltaNode parent) {
			this.parent = parent;
		}

		public IResource getResource() {
			return resource;
		}

		public DeltaNode getParent() {
			return parent;
		}

		public void addChild(DeltaNode child) {
			children.add(child);
			child.setParent(this);
		}

		public void removeChild(DeltaNode child) {
			children.remove(child);
			child.setParent(null);
		}

		public DeltaNode[] getChildren() {
			return (DeltaNode[]) children.toArray(new DeltaNode[children.size()]);
		}

		public boolean hasChildren() {
			return children.size() > 0;
		}

		public String toString() {
			return resource.getName();
		}

		public Object getAdapter(Class key) {
			if (key != IResource.class)
				return null;
			return resource;
		}

		public int getDeltaKind() {
			return deltaKind;
		}

		public int getDeltaFlags() {
			return deltaFlags;
		}

		public IPath getPath() {
			return path;
		}

		public void populateFromDelta(IResourceDelta delta) {
			deltaFlags = delta.getFlags();
			deltaKind = delta.getKind();
			path = delta.getFullPath();
			resource = delta.getResource();
			IResourceDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; i++)
				addChild(new DeltaNode(children[i]));
		}
	}

	class ResourceEventNode extends DeltaNode {
		private int eventType;

		public ResourceEventNode(IResourceChangeEvent event) {
			super(event.getDelta());
			eventType = event.getType();
		}

		public int getEventType() {
			return eventType;
		}
	}

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content
	 * (like Task List, for example).
	 */

	class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		private DeltaNode invisibleRoot;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			invisibleRoot = null;
		}

		public void dispose() {
			// do nothing
		}

		public Object[] getElements(Object parent) {

			if (parent.equals(rootObject)) {
				if (invisibleRoot == null) {
					if (rootObject instanceof IResourceChangeEvent) {
						initialize(rootObject);
					} else {
						initialize();
					}
				}
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			return ((DeltaNode) child).getParent();
		}

		public Object[] getChildren(Object parent) {
			return ((DeltaNode) parent).getChildren();
		}

		public boolean hasChildren(Object parent) {
			return ((DeltaNode) parent).hasChildren();
		}

		/*
		 * We will set up a dummy model to initialize tree heararchy.
		 * In a real code, you will connect to a real model and
		 * expose its hierarchy.
		 */
		private void initialize() {
			invisibleRoot = new DeltaNode();
		}

		private void initialize(Object input) {
			if (!(input instanceof IResourceChangeEvent))
				return;
			IResourceChangeEvent evt = (IResourceChangeEvent) input;
			ResourceEventNode root = new ResourceEventNode(evt);
			invisibleRoot = new DeltaNode();
			invisibleRoot.addChild(root);
		}
	}

	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			StringBuffer buffer = new StringBuffer(obj.toString());
			if (obj instanceof ResourceEventNode) {
				buffer.append("Workspace Root - "); //$NON-NLS-1$
				buffer.append(getEventTypeAsString(((ResourceEventNode) obj).getEventType()));
			}
			buffer.append(getKindAsString(((DeltaNode) obj).getDeltaKind()));
			buffer.append(getFlagsAsString(((DeltaNode) obj).getDeltaFlags()));
			return buffer.toString();
		}

		public Image getImage(Object obj) {
			DeltaNode node = (DeltaNode) obj;
			String imageKey;
			switch (node.getResource().getType()) {
				case IResource.ROOT :
					imageKey = ISharedImages.IMG_OBJ_FOLDER;
					break;
				case IResource.PROJECT :
					imageKey = ISharedImages.IMG_OBJ_PROJECT;
					break;
				case IResource.FOLDER :
					imageKey = ISharedImages.IMG_OBJ_FOLDER;
					break;
				case IResource.FILE :
				default :
					imageKey = ISharedImages.IMG_OBJ_FILE;
					break;
			}
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}

		private String getKindAsString(int kind) {
			if (kind == -1)
				return ""; //$NON-NLS-1$
			return " kind(" + getFlagsAsString(kind) + ") "; //$NON-NLS-1$ //$NON-NLS-2$
		}

		private String getEventTypeAsString(int eventType) {
			switch (eventType) {
				case IResourceChangeEvent.POST_BUILD :
					return "POST_BUILD"; //$NON-NLS-1$
				case IResourceChangeEvent.POST_CHANGE :
					return "POST_CHANGE"; //$NON-NLS-1$
				case IResourceChangeEvent.PRE_BUILD :
					return "PRE_BUILD"; //$NON-NLS-1$
			}
			return null;
		}

		/**
		 * Return a string representation of the change flags found
		 * within a resource change event.
		 */
		private String getFlagsAsString(int flags) {
			StringBuffer buffer = new StringBuffer();
			if ((IResourceDelta.ADDED & flags) != 0) {
				buffer.append("-"); //$NON-NLS-1$
				buffer.append("ADDED"); //$NON-NLS-1$
			}
			if ((IResourceDelta.ADDED_PHANTOM & flags) != 0 && showPhantoms) {
				buffer.append("-"); //$NON-NLS-1$
				buffer.append("ADDED_PHANTOM"); //$NON-NLS-1$
			}
			if ((IResourceDelta.ALL_WITH_PHANTOMS & flags) != 0 && showPhantoms) {
				buffer.append("-"); //$NON-NLS-1$
				buffer.append("ALL_WITH_PHANTOMS"); //$NON-NLS-1$
			}
			if ((IResourceDelta.CHANGED & flags) != 0) {
				buffer.append("-"); //$NON-NLS-1$
				buffer.append("CHANGED"); //$NON-NLS-1$
			}
			if ((IResourceDelta.CONTENT & flags) != 0) {
				buffer.append("-"); //$NON-NLS-1$
				buffer.append("CONTENT"); //$NON-NLS-1$
			}
			if ((IResourceDelta.DESCRIPTION & flags) != 0) {
				buffer.append("-"); //$NON-NLS-1$
				buffer.append("DESCRIPTION"); //$NON-NLS-1$
			}
			if ((IResourceDelta.MARKERS & flags) != 0) {
				buffer.append("-"); //$NON-NLS-1$
				buffer.append("MARKERS"); //$NON-NLS-1$
			}
			if ((IResourceDelta.MOVED_FROM & flags) != 0) {
				buffer.append("-"); //$NON-NLS-1$
				buffer.append("MOVED_FROM"); //$NON-NLS-1$
			}
			if ((IResourceDelta.MOVED_TO & flags) != 0) {
				buffer.append("-"); //$NON-NLS-1$
				buffer.append("MOVED_TO"); //$NON-NLS-1$
			}
			if ((IResourceDelta.NO_CHANGE & flags) != 0) {
				buffer.append("-"); //$NON-NLS-1$
				buffer.append("NO_CHANGE"); //$NON-NLS-1$
			}
			if ((IResourceDelta.OPEN & flags) != 0) {
				buffer.append("-"); //$NON-NLS-1$
				buffer.append("OPEN"); //$NON-NLS-1$
			}
			if ((IResourceDelta.REMOVED & flags) != 0) {
				buffer.append("-");
				buffer.append("REMOVED");
			}
			if ((IResourceDelta.REMOVED_PHANTOM & flags) != 0 && showPhantoms) {
				buffer.append("-"); //$NON-NLS-1$
				buffer.append("REMOVED_PHANTOM"); //$NON-NLS-1$
			}
			if ((IResourceDelta.REPLACED & flags) != 0) {
				buffer.append("-"); //$NON-NLS-1$
				buffer.append("REPLACED"); //$NON-NLS-1$
			}
			if ((IResourceDelta.SYNC & flags) != 0) {
				buffer.append("-"); //$NON-NLS-1$
				buffer.append("SYNC"); //$NON-NLS-1$
			}
			if ((IResourceDelta.TYPE & flags) != 0) {
				buffer.append("-"); //$NON-NLS-1$
				buffer.append("TYPE"); //$NON-NLS-1$
			}
			return buffer.toString();
		}
	}

	class FilterAction extends Action {
		private int type;

		public FilterAction(String text, int eventType) {
			super(text);
			type = eventType;
		}

		/**
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {
			typeFilter = type;
			for (Iterator i = eventActions.iterator(); i.hasNext();) {
				IAction action = (IAction) i.next();
				action.setChecked(action.equals(this));
			}
		}
	}

	class FilterPhantoms extends Action {
		public FilterPhantoms() {
			super("Show Phantoms"); //$NON-NLS-1$
		}

		public void run() {
			showPhantoms = !showPhantoms;
			setChecked(showPhantoms);
		}
	}
}