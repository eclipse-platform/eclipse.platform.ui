Menu Contributions/Widget in a toolbar
======================================

Widget in the main toolbar
==========================

You can use the extension point to contribute a control to the toolbar. You use the <control/> element instead of the <command/> element.

```java
        <extension point="org.eclipse.ui.menus">
            <menuContribution locationURI="toolbar:org.eclipse.search.toolbar">
                <control id="org.eclipse.ui.examples.menus.searchBar"
        class="org.eclipse.ui.examples.menus.internal.SearchBar">
                    <visibleWhen>
                        <with variable="activeContexts">
                            <iterator operator="or">
                                <equals value="org.eclipse.jdt.ui.SearchActionSet" />
                            </iterator>
                        </with>
                    </visibleWhen>
                </control>
            </menuContribution>
        </extension>
```

The control class must implement WorkbenchWindowControlContribution as of **3.3M5**.


I'm not sure how far to go with IWorkbenchWidget. We already use this interface for adding controls to the trim, and there are open bug requests about adding arbitrary controls to the toolbars. It looks like we'll deprecate it in favour of WorkbenchWindowControlContribution.

Also, there are risks associated with this like eager plugin activation. Maybe we allow widget activation but restrict it to programmatic API only (after the plugin has been instantiated) or still allow declarative contributions but only with certain types of `<visibleWhen/>` clauses.

 There are two separate reasons to use an extension point to contribute to the toolbar. One reason is to defer plug-in activation. But the other, is to allow a plug-in to contribute to another plug-in even though it depends on that plug-in, or it ships as additional, add-on function. In either case, the plug-in owning the toolbar can not depend on the contributing plug-in. While in general, one wants plug-in activation to occur as late as possible, there are cases where you just don't care. It's great to see that I can now contribute anything I want to another plug-in's toolbar.

I think this flexibility needs to be supported. Isn't it possible for a bundle to specify exceptions that would prevent the bundle from being started even though the contribution's classes are loaded to create the Control?

While we're still looking at this in **3.3M6** the preliminary implementation will probably be our standard proxy pattern. That means that contributing a control to the main toolbar will just start the contributing plugin (so we'll just ask people to be careful :-). But as an aside, I'm pretty sure that an exception that prevents a bundle from being started will also prevent us from getting the plugin control contribution from IConfigurationElement#createExecutableExtension(*)


