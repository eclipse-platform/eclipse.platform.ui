Menu Contributions/TextEditor viewerContribution
================================================

Contents
--------

*   [1 Text editor popup action](#Text-editor-popup-action)
    *   [1.1 Commands](#Commands)
    *   [1.2 Menus](#Menus)
    *   [1.3 Menus API](#Menus-API)

Text editor popup action
========================

Popups can be targetted at any registered context menu, or at all of them. This is the Scramble Text command to be added the the standard text editor context menu.

Commands
--------

First define the command and its handler.

        <extension point="org.eclipse.ui.commands">
            <command id="org.eclipse.ui.examples.menus.scramble.text"
        defaultHandler="org.eclipse.ui.examples.menus.internal.ScrambleTextHandler"
        name="%ScrambleText.name"
        description="%ScrambleText.description" />
        </extension>

Menus
-----

Placing the action (which is specifically a menu or button linked to a command) can be accomplished with the org.eclipse.ui.menus extension point.

        <extension point="org.eclipse.ui.menus">
            <menuContribution locationURI="popup:#TextEditorContext?after=additions">
                <command commandId="org.eclipse.ui.examples.menus.scramble.text"
        mnemonic="%ScrambleText.mnemonic"
        icon="$nl$/icons/full/eobj16/scramble.gif" />
            </menuContribution>
        </extension>

Menus API
---------

Programmatically do this, you would have to go through the IMenuService.

        public static void addTextMenuContribition() {
            final IMenuService menuService = (IMenuService) PlatformUI
                    .getWorkbench().getService(IMenuService.class);
         
            final ImageDescriptor scrambleIcon = AbstractUIPlugin
                    .imageDescriptorFromPlugin("org.eclise.ui.tests",
                            "icons/full/eobj16/scramble.gif");
            AbstractContributionFactory factory = new AbstractContributionFactory(
                    "popup:#TextEditorContext?after=additions") {
                        public void createContributionItems(IMenuService menuService,
                                List additions) {
                            CommandContributionItem item = new CommandContributionItem(
                                    "org.eclipse.ui.examples.menus.scramble.text",
                                    "org.eclipse.ui.examples.menus.scramble.text",
                                    null, scrambleIcon, null, null, null, "c", null,
                                    CommandContributionItem.STYLE_PUSH);
                            additions.add(item);
                        }
         
                        public void releaseContributionItems(IMenuService menuService,
                                List items) {
                        }
            };
            menuService.addContributionFactory(factory);
        }

