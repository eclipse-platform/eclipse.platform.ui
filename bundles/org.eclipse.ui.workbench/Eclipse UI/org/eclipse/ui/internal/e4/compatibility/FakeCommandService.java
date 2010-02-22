package org.eclipse.ui.internal.e4.compatibility;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.ParameterType;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.SerializationException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementReference;
import org.eclipse.ui.menus.UIElement;

/**
 * @since 3.5
 *
 */
public final class FakeCommandService implements ICommandService {
	CommandManager manager = new CommandManager();

	public void addExecutionListener(IExecutionListener listener) {
		E4Util.unsupported("addExecutionListener"); //$NON-NLS-1$
	}

	public void defineUncategorizedCategory(String name, String description) {
		E4Util.unsupported("defineUncategorizedCategory"); //$NON-NLS-1$
	}

	public ParameterizedCommand deserialize(String serializedParameterizedCommand)
			throws NotDefinedException, SerializationException {
		E4Util.unsupported("deserialize"); //$NON-NLS-1$
		return null;
	}

	public Category getCategory(String categoryId) {
		E4Util.unsupported("getCategory"); //$NON-NLS-1$
		return null;
	}

	public Command getCommand(String commandId) {
		E4Util.unsupported("getCommand"); //$NON-NLS-1$
		return manager.getCommand(commandId);
	}

	public Category[] getDefinedCategories() {
		E4Util.unsupported("getDefinedCategories"); //$NON-NLS-1$
		return null;
	}

	public Collection getDefinedCategoryIds() {
		E4Util.unsupported("getDefinedCategoryIds"); //$NON-NLS-1$
		return null;
	}

	public Collection getDefinedCommandIds() {
		E4Util.unsupported("getDefinedCommandIds"); //$NON-NLS-1$
		return Collections.EMPTY_LIST;
	}

	public Command[] getDefinedCommands() {
		E4Util.unsupported("getDefinedCommands"); //$NON-NLS-1$
		return null;
	}

	public Collection getDefinedParameterTypeIds() {
		E4Util.unsupported("getDefinedParameterTypeIds"); //$NON-NLS-1$
		return null;
	}

	public ParameterType[] getDefinedParameterTypes() {
		E4Util.unsupported("getDefinedParameterTypes"); //$NON-NLS-1$
		return null;
	}

	public String getHelpContextId(Command command) throws NotDefinedException {
		E4Util.unsupported("getHelpContextId"); //$NON-NLS-1$
		return null;
	}

	public String getHelpContextId(String commandId) throws NotDefinedException {
		E4Util.unsupported("getHelpContextId"); //$NON-NLS-1$
		return null;
	}

	public ParameterType getParameterType(String parameterTypeId) {
		E4Util.unsupported("getParameterType"); //$NON-NLS-1$
		return null;
	}

	public void readRegistry() {
		E4Util.unsupported("readRegistry"); //$NON-NLS-1$
	}

	public void removeExecutionListener(IExecutionListener listener) {
		E4Util.unsupported("removeExecutionListener"); //$NON-NLS-1$
	}

	public void setHelpContextId(IHandler handler, String helpContextId) {
		E4Util.unsupported("setHelpContextId"); //$NON-NLS-1$
	}

	public IElementReference registerElementForCommand(ParameterizedCommand command,
			UIElement element) throws NotDefinedException {
		E4Util.unsupported("registerElementForCommand"); //$NON-NLS-1$
		return null;
	}

	public void registerElement(IElementReference elementReference) {
		E4Util.unsupported("registerElement"); //$NON-NLS-1$
	}

	public void unregisterElement(IElementReference elementReference) {
		E4Util.unsupported("unregisterElement"); //$NON-NLS-1$
	}

	public void refreshElements(String commandId, Map filter) {
		E4Util.unsupported("refreshElements"); //$NON-NLS-1$
	}

	public void dispose() {
		E4Util.unsupported("dispose"); //$NON-NLS-1$
	}
}