# Image URL modifier example

A minimal Eclipse 4 RCP application that swaps icons through an `IImageURLModifier` OSGi service.
`IconPackModifier` is a declarative services component, and the E4 workbench installs the service in JFace, so no startup code is needed.
The modifier looks up the file name of every requested icon in `iconpacks/<pack>/` and returns `null` to keep icons the pack does not replace.
The pack replaces the part icon and the toolbar icons, which the application model loads from SVG files; the extra tool items use a handler that does nothing.

The button in the part stores the selected pack in the instance preferences and restarts the workbench, since images that already exist are not reloaded.
Start it from `org.eclipse.ui.examples.urlmodifier.product` or with `Icon Pack Demo.launch`.
The launch configuration takes `org.eclipse.e4.ui.workbench.swt` from the workspace, which needs a version that contains the image URL modifier tracker.
