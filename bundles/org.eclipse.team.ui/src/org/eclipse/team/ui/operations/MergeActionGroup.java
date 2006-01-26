package org.eclipse.team.ui.operations;

import org.eclipse.jface.action.*;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.mapping.*;
import org.eclipse.team.ui.mapping.SynchronizationActionProvider;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;

/**
 * Action group that contributes the merge actions to the model
 * synchronize participant. The groups adds the following:
 * <ul>
 * <li>A toolbar action for attempting an auto-merge
 * <li>Contetx menu merge actions that delegate to the 
 * model's merge action handlers.
 * <li>TODO a merge all and overwrite all menu item?
 * </ul>
 * <p>
 * Subclasses can configure the label and icons used for the merge actions
 * by overridding {@link #configureMergeAction(String, Action)} and can
 * configure where in the context menu the actions appear by overridding
 * {@link #addToContextMenu(String, Action, IMenuManager)}.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 **/
public class MergeActionGroup extends SynchronizePageActionGroup {

	/**
	 * The id of the merge action group that determines where the merge
	 * actions (e.g. merge and overwrite) appear in the context menu or toolbar.
	 */
	public static final String MERGE_ACTION_GROUP = "merge"; //$NON-NLS-1$

	/**
	 * The id of the action group that determines where the other
	 * actions (e.g. mark-as-mered) appear in the context menu.
	 */
	public static final String OTHER_ACTION_GROUP = "other"; //$NON-NLS-1$
	
	/**
	 * The id used to identify the Merge All action.
	 */
	protected static final String MERGE_ALL_ACTION_ID = "org.eclipse.team.ui.mergeAll"; //$NON-NLS-1$
	
	/**
	 * Create a merge action group.
	 */
	public MergeActionGroup() {
	}

	private MergeIncomingChangesAction updateToolbarAction;
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SynchronizePageActionGroup#initialize(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	public void initialize(ISynchronizePageConfiguration configuration) {
		super.initialize(configuration);
		
		ResourceMappingSynchronizeParticipant participant = ((ResourceMappingSynchronizeParticipant)configuration.getParticipant());
		if (participant.isMergingEnabled()) {
			updateToolbarAction = new MergeIncomingChangesAction(configuration);
			configureMergeAction(MERGE_ALL_ACTION_ID, updateToolbarAction);
			appendToGroup(
					ISynchronizePageConfiguration.P_TOOLBAR_MENU,
					MERGE_ACTION_GROUP,
					updateToolbarAction);
			// TODO: Should add a merge all to the context menu as well?
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SynchronizePageActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		if (menu instanceof CommonMenuManager) {
			CommonMenuManager cmm = (CommonMenuManager) menu;
			addMergeActions(cmm);
		}
	}
	
	/*
	 * Method to add the merge actions to the contetx menu. This method
	 * is called by the internal synchronization framework and should not
	 * to be invoked by other clients. Subsclasses can configure the
	 * merge actions by overriding {@link #configureMergeAction(String, Action)}
	 * and can control where in the context menu the action appears by 
	 * overriding {@link #addToContextMenu(String, Action, IMenuManager)}.
	 * @param cmm the menu manager
	 */
	private void addMergeActions(CommonMenuManager cmm) {
		ResourceMappingSynchronizeParticipant participant = ((ResourceMappingSynchronizeParticipant)getConfiguration().getParticipant());
		if (participant.isMergingEnabled()) {
			if (!isTwoWayMerge()) {
				MergeAction merge = new MergeAction(SynchronizationActionProvider.MERGE_ACTION_ID, cmm, getConfiguration());
				configureMergeAction(SynchronizationActionProvider.MERGE_ACTION_ID, merge);
				addToContextMenu(SynchronizationActionProvider.MERGE_ACTION_ID, merge, cmm);
			}
			MergeAction overwrite = new MergeAction(SynchronizationActionProvider.OVERWRITE_ACTION_ID, cmm, getConfiguration());
			configureMergeAction(SynchronizationActionProvider.OVERWRITE_ACTION_ID, overwrite);
			addToContextMenu(SynchronizationActionProvider.OVERWRITE_ACTION_ID, overwrite, cmm);
			if (!isTwoWayMerge()) {
				MergeAction markAsMerged = new MergeAction(SynchronizationActionProvider.MARK_AS_MERGE_ACTION_ID, cmm, getConfiguration());
				configureMergeAction(SynchronizationActionProvider.MARK_AS_MERGE_ACTION_ID, markAsMerged);
				addToContextMenu(SynchronizationActionProvider.MARK_AS_MERGE_ACTION_ID, markAsMerged, cmm);
			}
		}
	}
	
	/**
	 * Configure the merge action to have appropriate label, image, etc.
	 * Subclasses may override but should invoke the overridden
	 * method for unrecognized ids in order to support future additions.
	 * @param mergeActionId the id of the merge action (one of 
	 * {@link SynchronizationActionProvider#MERGE_ACTION_ID},
	 * {@link SynchronizationActionProvider#OVERWRITE_ACTION_ID} or
	 * {@link SynchronizationActionProvider#MARK_AS_MERGE_ACTION_ID})
	 * @param action the action for the given id
	 */
	protected void configureMergeAction(String mergeActionId, Action action) {
		if (mergeActionId == SynchronizationActionProvider.MERGE_ACTION_ID) {
			Utils.initAction(action, "action.merge."); //$NON-NLS-1$
		} else if (mergeActionId == SynchronizationActionProvider.OVERWRITE_ACTION_ID) {
			if (isTwoWayMerge()) {
				Utils.initAction(action, "action.replace."); //$NON-NLS-1$
			} else {
				Utils.initAction(action, "action.overwrite."); //$NON-NLS-1$
			}
		} else if (mergeActionId == SynchronizationActionProvider.MARK_AS_MERGE_ACTION_ID) {
			Utils.initAction(action, "action.markAsMerged."); //$NON-NLS-1$
		} else if (mergeActionId == MERGE_ALL_ACTION_ID) {
			if (isTwoWayMerge()) {
				Utils.initAction(action, "action.replaceAll."); //$NON-NLS-1$
			} else {
				Utils.initAction(action, "action.mergeAll."); //$NON-NLS-1$
			}
		}
	}
	
	private boolean isTwoWayMerge() {
		ResourceMappingSynchronizeParticipant participant = ((ResourceMappingSynchronizeParticipant)getConfiguration().getParticipant());
		ISynchronizationContext context = participant.getContext();
		if (context instanceof IMergeContext) {
			IMergeContext mc = (IMergeContext) context;
			return (mc.getMergeType() == ISynchronizationContext.TWO_WAY);
		}
		return false;
	}
	
	/**
	 * Add the merge action to the context menu manager. 
	 * Subclasses may override but should invoke the overridden
	 * method for unrecognized ids in order to support future additions.
	 * @param mergeActionId the id of the merge action (one of 
	 * {@link SynchronizationActionProvider#MERGE_ACTION_ID},
	 * {@link SynchronizationActionProvider#OVERWRITE_ACTION_ID} or
	 * {@link SynchronizationActionProvider#MARK_AS_MERGE_ACTION_ID})
	 * @param action the action for the given id
	 * @param manager the context menu manager
	 */
	protected void addToContextMenu(String mergeActionId, Action action, IMenuManager manager) {
		IContributionItem group = null;;
		if (mergeActionId == SynchronizationActionProvider.MERGE_ACTION_ID) {
			group = manager.find(MERGE_ACTION_GROUP);
		} else if (mergeActionId == SynchronizationActionProvider.OVERWRITE_ACTION_ID) {
			group = manager.find(MERGE_ACTION_GROUP);
		} else if (mergeActionId == SynchronizationActionProvider.MARK_AS_MERGE_ACTION_ID) {
			group = manager.find(OTHER_ACTION_GROUP);
		}
		if (group != null) {
			manager.appendToGroup(group.getId(), action);
		} else {
			manager.add(action);
		}
	}
}