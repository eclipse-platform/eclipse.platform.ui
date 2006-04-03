/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.cheatsheets.composite.model;

import java.util.ArrayList;
import org.eclipse.ui.internal.cheatsheets.composite.parser.ITaskParseStrategy;
import org.eclipse.ui.internal.cheatsheets.composite.parser.TaskGroupParseStrategy;
import org.eclipse.ui.internal.provisional.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.internal.provisional.cheatsheets.ITaskGroup;

public class TaskGroup extends AbstractTask implements ITaskGroup {
	
	public interface CompletionStrategy {
		public int computeState(TaskGroup taskGroup);
	}
	
	private ITaskParseStrategy parserStrategy;

	private ArrayList subtasks;
	
	private CompletionStrategy completionStrategy;

	public TaskGroup(CompositeCheatSheetModel model, String id, String name, String kind) {
		super(model, id, name, kind);
		if (kind == null) {
			this.kind = ITaskGroup.SET;
		}
		parserStrategy = new TaskGroupParseStrategy();
		completionStrategy = determineCompletionStrategy(kind);
	}

	private CompletionStrategy determineCompletionStrategy(String kind) {
		if (ITaskGroup.CHOICE.equals(kind)) {
			return new TaskChoiceCompletionStrategy();
		}
		return new TaskSetCompletionStrategy();
	}

	public ITaskParseStrategy getParserStrategy() {
		return parserStrategy;
	}
	
	public ICompositeCheatSheetTask[] getSubtasks() {
		if (subtasks==null) return EMPTY;
		return (ICompositeCheatSheetTask[])subtasks.toArray(new ICompositeCheatSheetTask[subtasks.size()]);
	}
	
	public void addSubtask(ICompositeCheatSheetTask task) {
		if (subtasks==null) {
			subtasks = new ArrayList();
		}
		subtasks.add(task);
		((AbstractTask)task).setParent(this);
	}
	
	/**
	 * Called when the state of a child has changed or when the model
	 * has been restored.
	 */
	public void checkState() {
		int newState = computeState();
		if (newState != state) {
			setStateNoNotify(newState);
		}
	}

	/**
	 * Determine the state based on the state of the children, which 
     * will use a different computation depending on whether this is a set,
     * sequence or choice.
	 */
	public int computeState() {
		return completionStrategy.computeState(this);
	}

}
