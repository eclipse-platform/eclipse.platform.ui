package org.eclipse.e4.core.commands;

import java.util.Map;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.ParameterizedCommand;

/**
 */
public interface ECommandService {
	public ParameterizedCommand createCommand(String id, Map parameters);

	public Category defineCategory(String id, String name, String description);

	public Command defineCommand(String id, String name, String description, Category category,
			IParameter[] parameters);

	public Category getCategory(String categoryId);

	public Command getCommand(String commandId);
}
