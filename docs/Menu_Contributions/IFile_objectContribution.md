Menu Contributions/IFile objectContribution
===========================================


IFile object contribution
=========================

We also have to provide object contributions (which in the past were scoped by objectClass).

Here's an example from one of our plugin.xml:

```xml
      <objectContribution adaptable="true"
      objectClass="org.eclipse.core.resources.IFile"
      nameFilter="*.xml"
      id="org.eclipse.jdt.internal.ui.javadocexport.JavadocWizard">
      <visibility>
            <objectState name="contentTypeId"
      value="org.eclipse.ant.core.antBuildFile" />
      </visibility>
      <action label="Create Javadoc"
      class="org.eclipse.jdt.internal.ui.CreateJavadocActionDelegate"
      enablesFor="1" id="LaunchJavadocWizard"/>
      </objectContribution>
```

**enablesFor** is now a property of the active handler, not the visible GUI element.

Menus
-----

There will be a reserved popup ID, "org.eclipse.ui.popup.any" that will allow contributions to any popup menu.

```xml
      <extension point="org.eclipse.core.expressions.definitions">
         <definition id="org.eclipse.ui.example.antFile">
            <iterate ifEmpty="false">
               <adapt type="org.eclipse.core.resources.IFile">
                  <test property="org.eclipse.core.resources.name"  value="*.xml"/>
                  <test property="org.eclipse.core.resources.contentTypeId" value="org.eclipse.ant.core.antBuildFile"/>
               </adapt>
            </iterate>
         </definition>
      </extension>
      <extension point="org.eclipse.ui.menus">
         <menuContribution locationURI="popup:org.eclipse.ui.popup.any">
            <command commandId="org.eclipse.jdt.ui.launchJavadocWizard" id="LaunchJavadocWizard" label="Create Javadoc" style="push">
               <visibleWhen checkEnabled="false">
                  <or>
                     <with variable="activeMenuSelection">
                        <reference definitionId="org.eclipse.ui.example.antFile"/>
                     </with>
                     <with variable="activeMenuEditorInput">
                        <reference definitionId="org.eclipse.ui.example.antFile"/>
                     </with>
                  </or>
               </visibleWhen>
            </command>
         </menuContribution>
      </extension>
```

The default variable for visibleWhen/activeWhen/enabledWhen expressions is **selection**. But it's better to be specific and use `<with variable="selection".../>` if that's what you need.

Menus API
---------

Here is a similar example programmatically.

```java
      public static void addFileContribution() {
         final IMenuService menuService = (IMenuService) PlatformUI
                  .getWorkbench().getService(IMenuService.class);
         // an expression that walks the selection looking for objectclasses
         final ObjectClassExpression ifileExpression = new ObjectClassExpression(
                  "org.eclipse.core.resources.IFile");
       
         final ImageDescriptor postIcon = AbstractUIPlugin
                  .imageDescriptorFromPlugin("org.eclipse.ui.tests",
                           "icons/full/elcl16/post_wiki.gif");
         final ImageDescriptor loadIcon = AbstractUIPlugin
                  .imageDescriptorFromPlugin("org.eclipse.ui.tests",
                           "icons/full/elcl16/load_wiki.gif");
         AbstractContributionFactory factory = new AbstractContributionFactory(
                  "popup:org.eclipse.ui.popup.any?after=additions") {
               public void createContributionItems(IMenuService menuService,
                     List additions) {
                  CommandContributionItem item = new CommandContributionItem(
                           "org.eclipse.ui.examples.wiki.post",
                           "org.eclipse.ui.examples.wiki.post", null, postIcon,
                           null, null, null, "P", null,
                           CommandContributionItem.STYLE_PUSH);
                  menuService.registerVisibleWhen(item, ifileExpression);
                  additions.add(item);
       
                  item = new CommandContributionItem(
                           "org.eclipse.ui.examples.wiki.load",
                           "org.eclipse.ui.examples.wiki.load", null, loadIcon,
                           null, null, null, "L", null,
                           CommandContributionItem.STYLE_PUSH);
                  menuService.registerVisibleWhen(item, ifileExpression);
                  additions.add(item);
               }
       
               public void releaseContributionItems(IMenuService menuService,
                     List items) {
               }
         };
         menuService.addContributionFactory(factory);
      }
```

The location of org.eclipse.ui.popup.any specifies any context menu, and the expression ties it to a specific objectClass. Using the new expression syntax you can make your conditions more complex.

You can set your visibleWhen expression on each item as you create it.

In **3.3M6** registerVisibleWhen(*) method might be changing.

