package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;

/**
 * This class reads the registry for extensions that plug into
 * 'editorActions' extension point.
 */
public class EditorActionBuilder extends PluginActionBuilder {
	private static final String TAG_CONTRIBUTION_TYPE = "editorContribution";//$NON-NLS-1$
	public class ExternalContributor implements IEditorActionBarContributor {
		private List cache;
		public ExternalContributor(List cache) {
			this.cache = cache;
		}
		public void dispose() {
		};
		public ActionDescriptor[] getExtendedActions() {
			if(cache == null)
				return new ActionDescriptor[0];
			ArrayList result = new ArrayList(cache.size());
			for (Iterator iter = cache.iterator(); iter.hasNext();) {
				Object element = (Object) iter.next();
				if (element instanceof ActionDescriptor) {
					result.add(element);
				}
			}
			return (ActionDescriptor[])result.toArray(new ActionDescriptor[result.size()]);
		}
		public void init(IActionBars bars, IWorkbenchPage page) {
			contributeToMenu(bars.getMenuManager());
			contributeToToolBar(bars.getToolBarManager());
			contributeToStatusLine(bars.getStatusLineManager());
		}
		public void contributeToMenu(IMenuManager menu) {
			for (int i = 0; i < cache.size(); i++) {
				Object obj = cache.get(i);
				if (obj instanceof IConfigurationElement) {
					IConfigurationElement menuElement = (IConfigurationElement) obj;
					contributeMenu(menuElement, menu, false);
				} else
					if (obj instanceof ActionDescriptor) {
						ActionDescriptor ad = (ActionDescriptor) obj;
						contributeMenuAction(ad, menu, false);
					}
			}
		}
		public void contributeToToolBar(IToolBarManager manager) {
			for (int i = 0; i < cache.size(); i++) {
				Object obj = cache.get(i);
				if (obj instanceof ActionDescriptor) {
					ActionDescriptor ad = (ActionDescriptor) obj;
					contributeToolbarAction(ad, manager, true);
				}
			}
		}
		public void setActiveEditor(IEditorPart editor) {
			for (int i=0; i<cache.size(); i++) {
				Object obj = cache.get(i);
				if (obj instanceof ActionDescriptor) {
					ActionDescriptor ad = (ActionDescriptor) obj;
					EditorPluginAction action = (EditorPluginAction)ad.getAction();
					action.editorChanged(editor);
				}
			}
		}
		public void contributeToStatusLine(IStatusLineManager manager) {
		}
	}
/**
 * The constructor.
 */
public EditorActionBuilder() {
}
/**
 * This factory method returns a new ActionDescriptor for the
 * configuration element.  It should be implemented by subclasses.
 */
protected ActionDescriptor createActionDescriptor(IConfigurationElement element) {
	return new ActionDescriptor(element, ActionDescriptor.T_EDITOR);
}
/**
 * Reads editor contributor if specified directly in the 'editor' extension point,
 * and all external contributions for this editor's ID registered in
 * 'editorActions' extension point. 
 */
public IEditorActionBarContributor readActionExtensions(IEditorDescriptor desc, IActionBars bars)
{
	ExternalContributor ext = null;
	readContributions(desc.getId(), TAG_CONTRIBUTION_TYPE, 
		IWorkbenchConstants.PL_EDITOR_ACTIONS);
	if (cache != null) {
		ext = new ExternalContributor(cache);
		cache = null;
	}
	return ext;
}
}
