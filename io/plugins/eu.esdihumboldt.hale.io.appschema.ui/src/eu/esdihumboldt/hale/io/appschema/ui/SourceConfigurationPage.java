package eu.esdihumboldt.hale.io.appschema.ui;

public class SourceConfigurationPage {
}

/*
 * import java.util.ArrayList; import java.util.List;
 * 
 * import org.eclipse.jface.layout.GridDataFactory; import
 * org.eclipse.jface.layout.GridLayoutFactory; import
 * org.eclipse.jface.viewers.ArrayContentProvider; import
 * org.eclipse.jface.viewers.CheckboxTableViewer; import
 * org.eclipse.jface.viewers.ComboViewer; import
 * org.eclipse.jface.viewers.ISelectionChangedListener; import
 * org.eclipse.jface.viewers.IStructuredSelection; import
 * org.eclipse.jface.viewers.SelectionChangedEvent; import org.eclipse.swt.SWT;
 * import org.eclipse.swt.layout.GridData; import
 * org.eclipse.swt.widgets.Button; import org.eclipse.swt.widgets.Composite;
 * import org.eclipse.swt.widgets.Label;
 * 
 * import de.fhg.igd.slf4jplus.ALogger; import
 * de.fhg.igd.slf4jplus.ALoggerFactory; import
 * eu.esdihumboldt.hale.common.core.io.ImportProvider; import
 * eu.esdihumboldt.hale.common.core.io.Value; import
 * eu.esdihumboldt.hale.io.appschema.writer.AbstractAppSchemaConfigurator;
 * import eu.esdihumboldt.hale.io.mongo.Client; import
 * eu.esdihumboldt.hale.io.mongo.ClientBuilder; import
 * eu.esdihumboldt.hale.ui.io.config.AbstractConfigurationPage;
 * 
 * public class SourceConfigurationPage extends
 * AbstractConfigurationPage<AbstractAppSchemaConfigurator,
 * AppSchemaAlignmentExportWizard> {
 * 
 * private static final ALogger LOGGER =
 * ALoggerFactory.getLogger(SourceConfigurationPage.class);
 * 
 * private CheckboxTableViewer schemaTable; private final List<String> schemas =
 * new ArrayList<String>();
 * 
 * // private Composite innerPage; private Composite page;
 * 
 * private boolean isEnable = false; private boolean multipleSelection = true;
 * // private SchemaSelector customSelector = null; // private
 * DriverConfiguration config = null; private Button selectAll = null;
 * 
 * private ComboViewer collectionsCombo;
 * 
 * public SourceConfigurationPage() { super("schemaRetrieval",
 * "Schemas Retrieval", null);
 * setDescription("Please select the collection from where to import the schema."
 * ); }
 * 
 * @Override public void enable() { // Do nothing
 * 
 * }
 * 
 * @Override public void disable() { // Do nothing
 * 
 * }
 * 
 * @Override public boolean updateConfiguration(AbstractAppSchemaConfigurator
 * provider) { // provider.setParameter(SCHEMAS, Value.of(selSchemas)); return
 * true; }
 * 
 * @Override protected void createContent(Composite page) { // setup the UI
 * page, one column for labels and another for
 * page.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());
 * GridDataFactory labels = GridDataFactory.swtDefaults().align(SWT.END,
 * SWT.CENTER); GridDataFactory components =
 * GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER) .grab(true, false);
 * // set the label Label collectionsLabel = new Label(page, SWT.NONE);
 * collectionsLabel.setText("Select collection: ");
 * labels.applyTo(collectionsLabel); // create collection selector combo
 * collectionsCombo = new ComboViewer(page, SWT.DROP_DOWN | SWT.READ_ONLY);
 * collectionsCombo.setContentProvider(ArrayContentProvider.getInstance());
 * collectionsCombo.addSelectionChangedListener(new ISelectionChangedListener()
 * {
 * 
 * @Override public void selectionChanged(SelectionChangedEvent event) {
 * IStructuredSelection selection = (IStructuredSelection) event.getSelection();
 * if (selection.isEmpty()) { return; } Object collectionName =
 * selection.getFirstElement(); if (collectionName == null) { return; }
 * ImportProvider provider = getWizard().getProvider();
 * provider.setParameter(Constants.COLLECTION_NAME,
 * Value.of(collectionName.toString())); setPageComplete(true); } });
 * components.applyTo(collectionsCombo.getControl()); // update page status
 * page.layout(true, true); setPageComplete(false); }
 * 
 * @Override protected void onShowPage(boolean firstShow) { ImportProvider
 * provider = getWizard().getProvider(); try (Client client = new
 * ClientBuilder().withProvider(provider).build()) { // get the available
 * connections List<String> collectionsNames = client.getCollectionNames();
 * collectionsCombo.getCombo().removeAll();
 * collectionsCombo.add(collectionsNames.toArray()); } catch (Exception
 * exception) { LOGGER.error("Error getting collections names from MongoDB.",
 * exception); Label label = new Label(page, SWT.WRAP);
 * label.setText("Error getting collections names from MongoDB: " +
 * exception.getLocalizedMessage()); label.setLayoutData(new GridData(SWT.FILL,
 * SWT.FILL, true, false, 2, 1)); page.layout(true, true);
 * setErrorMessage("Connection error !"); setPageComplete(true); } } }
 */
