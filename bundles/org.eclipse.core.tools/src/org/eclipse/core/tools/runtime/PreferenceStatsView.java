/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tools.runtime;

import java.util.*;
import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.*;
import org.eclipse.core.tools.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.text.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Text view that dumps sizeof info and stats about the
 * Eclipse preferences tree.
 * 
 * @since 3.0
 */
public class PreferenceStatsView extends SpyView {

	// The JFace widget used for showing the Element Tree info.  
	protected TextViewer viewer;

	private IAction updateAction;

	class UpdateAction extends Action {

		// number of nodes in the tree
		int nodeCount;
		// number of key/value pairs in the tree
		int kvCount;
		// number of nodes without key/value pairs
		int emptyNodes;
		// size of the tree
		int treeSize;
		// list of node with key/value pairs
		Set nonEmptyNodes;

		// root node
		IEclipsePreferences rootNode = Platform.getPreferencesService().getRootNode();

		UpdateAction() {
			super("Update view"); //$NON-NLS-1$
			this.setToolTipText("Update"); //$NON-NLS-1$
			this.setImageDescriptor(CoreToolsPlugin.createImageDescriptor("refresh.gif")); //$NON-NLS-1$
		}

		public void run() {
			super.run();
			reset();
			try {
				visitTree();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
			updateTextView();
			reset();
		}

		void reset() {
			nodeCount = 0;
			kvCount = 0;
			emptyNodes = 0;
			treeSize = 0;
			nonEmptyNodes = new TreeSet();
		}

		int basicSizeof(IEclipsePreferences node) {
			if (node instanceof EclipsePreferences)
				return basicSizeof((EclipsePreferences) node);

			// name
			int count = sizeof(node.name());

			// key/value pairs
			try {
				String[] keys = node.keys();
				for (int i = 0; i < keys.length; i++) {
					count += sizeof(keys[i]);
					String value = node.get(keys[i], null);
					count += sizeof(value);
				}
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
			return count;
		}

		int calculateOldSize() {
			int count = 0;
			IPluginRegistry registry = Platform.getPluginRegistry();
			if (registry == null)
				return count;
			IPluginDescriptor[] descriptors = registry.getPluginDescriptors();
			if (descriptors == null)
				return count;
			for (int i = 0; i < descriptors.length; i++) {
				IPluginDescriptor desc = descriptors[i];
				//				if (desc.isPluginActivated())
				count += calculateOldSizeFor(desc.getUniqueIdentifier());
			}
			return count;
		}

		int calculateOldSizeFor(String pluginID) {
			int count = 0;
			// 12 for the object header + 4 for each field
			count += 12;

			// dirty boolean
			count += 4;

			// listener list
			// TODO
			count += 4;

			// Properties properties
			count += 4;
			IEclipsePreferences node = new InstanceScope().getNode(pluginID);
			if (node != null) {
				// add the key/value pairs
				// TODO rough estimate
				try {
					String[] keys = node.keys();
					for (int i = 0; i < keys.length; i++) {
						count += sizeof(keys[i]);
						String value = node.get(keys[i], null);
						count += sizeof(value);
					}
				} catch (BackingStoreException e) {
					e.printStackTrace();
				}
			}

			// Properties defaults
			count += 4;
			node = new DefaultScope().getNode(pluginID);
			if (node != null) {
				// add the key/value pairs
				// TODO rough estimate
				try {
					String[] keys = node.keys();
					for (int i = 0; i < keys.length; i++) {
						count += sizeof(keys[i]);
						String value = node.get(keys[i], null);
						count += sizeof(value);
					}
				} catch (BackingStoreException e) {
					e.printStackTrace();
				}
			}

			return count;
		}

		/*
		 * 12 for the object header
		 * 4 for each slot
		 */
		int basicSizeof(EclipsePreferences node) {
			int count = 12;

			// name
			count += 4;
			count += sizeof(node.name());

			// dirty boolean
			count += 4;

			// removed boolean
			count += 4;

			// loading boolean
			count += 4;

			// slot for the parent pointer
			count += 4;

			// child map
			// TODO this isn't quite right but is ok for now
			count += 4;
			try {
				String[] childrenNames = node.childrenNames();
				for (int i = 0; i < childrenNames.length; i++)
					count += sizeof(childrenNames[i]);
			} catch (BackingStoreException e) {
				//this is truly exceptional!
			}

			// node change listener list
			// TODO
			count += 4;

			// preference change listener list
			// TODO
			count += 4;

			// cached path
			count += 4;
			count += sizeof(node.absolutePath());

			// key/value pairs
			// TODO this isn't quite right but is ok for now
			count += 4;
			String[] keys = node.keys();
			for (int i = 0; i < keys.length; i++) {
				count += sizeof(keys[i]);
				String value = node.get(keys[i], null);
				count += sizeof(value);
			}

			return count;
		}

		int basicSizeof(Map map) {
			if (map == null)
				return 0;

			//formula taken from BundleStats
			int count = (int) Math.round(44 + (16 + (map.size() * 1.25 * 4)) + (24 * map.size()));

			for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				count += sizeof(entry.getKey());
				count += sizeof(entry.getValue());
			}
			return count;
		}

		/**
		 * All sizeof tests should go through this central method to weed out
		 * duplicates.
		 */
		int sizeof(Object object) {
			if (object == null)//|| DeepSize.ignore(object))
				return 0;
			if (object instanceof String)
				return 44 + 2 * ((String) object).length();
			if (object instanceof byte[])
				return 16 + ((byte[]) object).length;
			if (object instanceof Integer)
				return 16;
			if (object instanceof Map)
				return basicSizeof((Map) object);
			if (object instanceof IEclipsePreferences)
				return basicSizeof((IEclipsePreferences) object);
			if (object instanceof QualifiedName) {
				QualifiedName name = (QualifiedName) object;
				return 20 + sizeof(name.getQualifier()) + sizeof(name.getLocalName());
			}
			// unknown -- use deep size
			return 0;
		}

		void visitTree() throws BackingStoreException {
			// count the number of nodes in the preferences tree
			reset();
			IPreferenceNodeVisitor visitor = new IPreferenceNodeVisitor() {
				public boolean visit(IEclipsePreferences node) {
					try {
						treeSize += sizeof(node);
						nodeCount++;
						int keys = node.keys().length;
						kvCount += keys;
						if (keys == 0)
							emptyNodes++;
						else
							nonEmptyNodes.add(node.absolutePath() + " (" + keys + ")"); //$NON-NLS-1$//$NON-NLS-2$
					} catch (BackingStoreException e) {
						e.printStackTrace();
					}
					return true;
				}
			};
			rootNode.accept(visitor);
		}

		void updateTextView() {
			final StringBuffer buffer = new StringBuffer();
			buffer.append("Size of Eclipse 2.1 preference objects: " + prettyPrint(calculateOldSize()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			buffer.append("Total node count: " + prettyPrint(nodeCount) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			buffer.append("Nodes without keys: " + prettyPrint(emptyNodes) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			buffer.append("Key/value pairs: " + prettyPrint(kvCount) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			buffer.append("Total size of tree: " + prettyPrint(treeSize) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			buffer.append("Nodes with key/value pairs:\n"); //$NON-NLS-1$
			for (Iterator i = nonEmptyNodes.iterator(); i.hasNext();)
				buffer.append("\t" + i.next() + "\n"); //$NON-NLS-1$//$NON-NLS-2$

			//post changes to UI thread
			viewer.getControl().getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (!viewer.getControl().isDisposed()) {
						IDocument doc = viewer.getDocument();
						doc.set(buffer.toString());
						viewer.setDocument(doc);
					}
				}
			});
		}

		private String prettyPrint(int i) {
			StringBuffer buf = new StringBuffer();
			for (;;) {
				if (i < 1000) {
					String val = Integer.toString(i);
					//pad with zeros if necessary
					if (buf.length() > 0) {
						if (val.length() < 2)
							buf.append('0');
						if (val.length() < 3)
							buf.append('0');
					}
					buf.append(val);
					return buf.toString();
				}
				if (i < 1000000) {
					String val = Integer.toString(i / 1000);
					//pad with zeros if necessary
					if (buf.length() > 0) {
						if (val.length() < 2)
							buf.append('0');
						if (val.length() < 3)
							buf.append('0');
					}
					buf.append(val);
					buf.append(',');
					i = i % 1000;
					continue;
				}
				buf.append(Integer.toString(i / 1000000));
				buf.append(',');
				i = i % 1000000;
			}
		}
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {

		viewer = new TextViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.WRAP | SWT.READ_ONLY);
		viewer.setDocument(new Document());

		IActionBars bars = getViewSite().getActionBars();

		final GlobalAction clearOutputAction = new ClearTextAction(viewer.getDocument());
		clearOutputAction.registerAsGlobalAction(bars);

		final GlobalAction selectAllAction = new SelectAllAction(viewer);
		selectAllAction.registerAsGlobalAction(bars);

		IMenuManager barMenuManager = getViewSite().getActionBars().getMenuManager();
		updateAction = new UpdateAction();
		barMenuManager.add(updateAction);

		// Delete action shortcuts are not captured by the workbench
		// so we need our key binding service to handle Delete keystrokes for us

		this.viewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.DEL)
					clearOutputAction.run();
			}
		});

		GlobalAction copyAction = new CopyTextSelectionAction(viewer);
		copyAction.registerAsGlobalAction(bars);

		bars.getToolBarManager().add(updateAction);
		bars.getToolBarManager().add(clearOutputAction);

		bars.updateActionBars();

		// creates a context menu with actions and adds it to the viewer control
		MenuManager menuMgr = new MenuManager();
		menuMgr.add(copyAction);
		menuMgr.add(clearOutputAction);
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		// populate the view with the initial data
		if (updateAction != null)
			updateAction.run();
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		updateAction = null;
	}

}
