/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.synchronize.actions;

import org.eclipse.team.core.subscribers.SyncInfo;

/**
 * A SyncInfoFilter can be used to control, at a fine grain, {@link SyncInfo} 
 * instances in a SyncInfoSet.
 * 
 * @see SyncInfoSet
 * @since 3.0
 */
public class SyncInfoFilter {

	public static SyncInfoFilter getDirectionAndChangeFilter(int direction, int change) {
		return new AndSyncInfoFilter(new SyncInfoFilter[]{new SyncInfoDirectionFilter(direction), new SyncInfoChangeTypeFilter(change)});
	}

	public static abstract class CompoundSyncInfoFilter extends SyncInfoFilter {
		protected SyncInfoFilter[] filters;
		public CompoundSyncInfoFilter(SyncInfoFilter[] filters) {
			this.filters = filters;
		}
	}
	
	/**
	 * Selects SyncInfo which match all child filters
	 */
	public static class AndSyncInfoFilter extends CompoundSyncInfoFilter {
		public AndSyncInfoFilter(SyncInfoFilter[] filters) {
			super(filters);
		}
		/* (non-Javadoc)
		 * @see org.eclipse.team.ccvs.syncviews.views.SyncSetFilter#select(org.eclipse.team.core.sync.SyncInfo)
		 */
		public boolean select(SyncInfo info) {
			for (int i = 0; i < filters.length; i++) {
				SyncInfoFilter filter = filters[i];
				if (!filter.select(info)) {
					return false;
				}
			}
			return true;
		}

	}

	public static class AutomergableFilter extends SyncInfoFilter {
		public boolean select(SyncInfo info) {
			return (info.getKind() & SyncInfo.AUTOMERGE_CONFLICT) != 0;
		}
	}
	
	public static class PseudoConflictFilter extends SyncInfoFilter {
		public boolean select(SyncInfo info) {
			return info.getKind() != 0 && (info.getKind() & SyncInfo.PSEUDO_CONFLICT) == 0;
		}
	}
	
	/**
	 * Selects SyncInfo that match any of the child filters.
	 */
	public static class OrSyncInfoFilter extends CompoundSyncInfoFilter {
		public OrSyncInfoFilter(SyncInfoFilter[] filters) {
			super(filters);
		}
		/* (non-Javadoc)
		 * @see org.eclipse.team.ccvs.syncviews.views.SyncSetFilter#select(org.eclipse.team.core.sync.SyncInfo)
		 */
		public boolean select(SyncInfo info) {
			for (int i = 0; i < filters.length; i++) {
				SyncInfoFilter filter = filters[i];
				if (filter.select(info)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public static class SyncInfoChangeTypeFilter extends SyncInfoFilter {

		private int[] changeFilters = new int[]{SyncInfo.ADDITION, SyncInfo.DELETION, SyncInfo.CHANGE};

		public SyncInfoChangeTypeFilter(int[] changeFilters) {
			this.changeFilters = changeFilters;
		}

		public SyncInfoChangeTypeFilter(int change) {
			this(new int[]{change});
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.team.ccvs.syncviews.views.SyncSetFilter#select(org.eclipse.team.core.sync.SyncInfo)
		 */
		public boolean select(SyncInfo info) {
			int syncKind = info.getKind();
			for (int i = 0; i < changeFilters.length; i++) {
				int filter = changeFilters[i];
				if ((syncKind & SyncInfo.CHANGE_MASK) == filter)
					return true;
			}
			return false;
		}

	}
	
	public static class SyncInfoDirectionFilter extends SyncInfoFilter {

		int[] directionFilters = new int[] {SyncInfo.OUTGOING, SyncInfo.INCOMING, SyncInfo.CONFLICTING};

		public SyncInfoDirectionFilter(int[] directionFilters) {
			this.directionFilters = directionFilters;
		}
	
		public SyncInfoDirectionFilter(int direction) {
			this(new int[] { direction });
		}

		/* (non-Javadoc)
		 * @see SyncSetFilter#select(org.eclipse.team.core.sync.SyncInfo)
		 */
		public boolean select(SyncInfo info) {
			int syncKind = info.getKind();
			for (int i = 0; i < directionFilters.length; i++) {
				int filter = directionFilters[i];
				if ((syncKind & SyncInfo.DIRECTION_MASK) == filter)
					return true;
			}
			return false;
		}
	}

	/**
	 * Return true if the provided SyncInfo matches the filter. The default
	 * behavior it to include resources whose syncKind is non-zero.
	 * 
	 * @param info
	 * @return
	 */
	public boolean select(SyncInfo info) {
		return info.getKind() != 0;
	}
}