/*
 * Created on May 21, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.update.internal.ui.wizards;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.update.configuration.IActivity;
import org.eclipse.update.configuration.IInstallConfiguration;
import org.eclipse.update.configuration.ILocalSite;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.core.Utilities;
import org.eclipse.update.internal.ui.UpdateLabelProvider;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.UpdateUIImages;
import org.eclipse.update.internal.ui.parts.DefaultContentProvider;

public class RevertConfigurationWizardPage extends WizardPage {
	
	private static final String KEY_CONFIGURE = "InstallConfigurationPage.ActivitySection.action.configure";
	private static final String KEY_FEATURE_INSTALL = "InstallConfigurationPage.ActivitySection.action.featureInstall";
	private static final String KEY_FEATURE_REMOVE = "InstallConfigurationPage.ActivitySection.action.featureRemove";
	private static final String KEY_SITE_INSTALL = "InstallConfigurationPage.ActivitySection.action.siteInstall";
	private static final String KEY_SITE_REMOVE = "InstallConfigurationPage.ActivitySection.action.siteRemove";	
	private static final String KEY_UNCONFIGURE = "InstallConfigurationPage.ActivitySection.action.unconfigure";
	private static final String KEY_UNKNOWN = "InstallConfigurationPage.ActivitySection.action.unknown";
	private static final String KEY_REVERT = "InstallConfigurationPage.ActivitySection.action.revert";
	private static final String KEY_RECONCILIATION = "InstallConfigurationPage.ActivitySection.action.reconcile";	
	private static final String KEY_ADD_PRESERVED = "InstallConfigurationPage.ActivitySection.action.addpreserved";	
	
	private TableViewer activitiesViewer;
	private TableViewer configViewer;
	
	class ActivitiesContentProvider
		extends DefaultContentProvider
		implements IStructuredContentProvider {
		public Object[] getElements(Object element) {
			return ((IInstallConfiguration) element).getActivities();
		}
	}

	class ActivitiesLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				UpdateLabelProvider provider = UpdateUI.getDefault().getLabelProvider();
				switch (((IActivity)element).getStatus()) {
					case IActivity.STATUS_OK:
						return provider.get(UpdateUIImages.DESC_OK_ST_OBJ, 0);
					case IActivity.STATUS_NOK:
						return provider.get(UpdateUIImages.DESC_ERR_ST_OBJ, 0);
				}				
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			IActivity activity = (IActivity) element;
			switch(columnIndex) {
				case 1:
					return Utilities.format(activity.getDate());
				case 2:
					return activity.getLabel();
				case 3:
					return getActionLabel(activity);
			}
			return "";
		}
		
		private String getActionLabel(IActivity activity) {
			int action = activity.getAction();
			switch (action) {
				case IActivity.ACTION_CONFIGURE:
					return UpdateUI.getString(KEY_CONFIGURE);
				case IActivity.ACTION_FEATURE_INSTALL:
					return UpdateUI.getString(KEY_FEATURE_INSTALL);
				case IActivity.ACTION_FEATURE_REMOVE:
					return UpdateUI.getString(KEY_FEATURE_REMOVE);
				case IActivity.ACTION_SITE_INSTALL:
					return UpdateUI.getString(KEY_SITE_INSTALL);
				case IActivity.ACTION_SITE_REMOVE:
					return UpdateUI.getString(KEY_SITE_REMOVE);
				case IActivity.ACTION_UNCONFIGURE:
					return UpdateUI.getString(KEY_UNCONFIGURE);
				case IActivity.ACTION_REVERT:
					return UpdateUI.getString(KEY_REVERT);
				case IActivity.ACTION_RECONCILIATION:
					return UpdateUI.getString(KEY_RECONCILIATION);				
				case IActivity.ACTION_ADD_PRESERVED:
					return UpdateUI.getString(KEY_ADD_PRESERVED);					
				default:
					return UpdateUI.getString(KEY_UNKNOWN);		
			}
		}
	}

	
	public RevertConfigurationWizardPage() {
		super("RevertConfiguration");
		setTitle("Revert to a Previous Configuration");
		setDescription("Choose a stable configuration to which you would like to revert");
	}

	public void createControl(Composite parent) {
		SashForm composite = new SashForm(parent, SWT.VERTICAL);
		composite.setLayout(new GridLayout());
				
		createConfigurationsSection(composite);
		createActivitiesSection(composite);
		setControl(composite);
		setPageComplete(false);
	}
	
	private void createConfigurationsSection(Composite parent) {
		Composite tableContainer = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		tableContainer.setLayout(layout);
		
		Table table = new Table(tableContainer, SWT.BORDER|SWT.V_SCROLL);		
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		configViewer = new TableViewer(table);
		configViewer.setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				UpdateLabelProvider provider = UpdateUI.getDefault().getLabelProvider();
				return provider.get(UpdateUIImages.DESC_CONFIG_OBJ, 0);
			}
			public String getText(Object element) {
				return ((IInstallConfiguration)element).getLabel();
			}

		});
		configViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object element) {
				ArrayList result = new ArrayList();
				ILocalSite localSite = (ILocalSite) element;
				IInstallConfiguration current = localSite.getCurrentConfiguration();
				long currTimeline = current.getTimeline();
				IInstallConfiguration[] configurations =
					localSite.getConfigurationHistory();
				for (int i = configurations.length - 1; i >= 0; i--) {
					if (configurations[i].getTimeline() == currTimeline
						&& !current.equals(configurations[i])) {
						result.add(configurations[i]);
					}
				}
				return result.toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

		});

		configViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				IStructuredSelection ssel = (IStructuredSelection)e.getSelection();
				if (ssel.size() == 1) {
					activitiesViewer.setInput(((IInstallConfiguration)ssel.getFirstElement()));
				}
				setPageComplete(ssel.size() == 1);
			}
		});

		try {
			configViewer.setInput(SiteManager.getLocalSite());
		} catch (CoreException e1) {
		}
	}
	
	private void createActivitiesSection(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridlayout = new GridLayout();
		gridlayout.marginHeight = gridlayout.marginWidth = 0;
		composite.setLayout(gridlayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gd);
		
		Label line = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL );
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.widthHint = 1;
		line.setLayoutData(gd);
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("Activities that caused the creation of this configuration:");
		
		Table table = new Table(composite, SWT.BORDER);
		table.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		table.setHeaderVisible(true);
		
		TableColumn column = new TableColumn(table, SWT.NONE);
		
		column = new TableColumn(table, SWT.NONE);
		column.setText("Date");
		
		column = new TableColumn(table, SWT.NONE);
		column.setText("Target");
		
		column = new TableColumn(table, SWT.NONE);
		column.setText("Action");
		
		
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(20,20,false));
		layout.addColumnData(new ColumnWeightData(20,160,false));
		layout.addColumnData(new ColumnWeightData(20,183,false));
		layout.addColumnData(new ColumnWeightData(20,100,false));

		table.setLayout(layout);
		
		activitiesViewer = new TableViewer(table);
		activitiesViewer.setLabelProvider(new ActivitiesLabelProvider());
		activitiesViewer.setContentProvider(new ActivitiesContentProvider());
	}
	
	public boolean performFinish() {
		/*IStructuredSelection ssel = (IStructuredSelection) configViewer.getSelection();
		if (ssel.size() == 1) {
			IInstallConfiguration config = (IInstallConfiguration)ssel.getFirstElement();
		}*/
		MessageDialog.openInformation(
			getContainer().getShell(),
			"Configuration Manager",
			"It's 2:00 a.m.  I'm going to bed now.");
		return true;
	}
	
}
