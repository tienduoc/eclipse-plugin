package com.aixcoder.extension;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.aixcoder.core.API;
import com.aixcoder.core.PredictCache;
import com.aixcoder.lib.Preference;

public class AiXPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private IPropertyChangeListener onPropertyChange;

	public AiXPreferencePage() {
		super(GRID);
	}

	public void createFieldEditors() {
		new AiXPreInitializer().initializeDefaultPreferences();
		Composite parent = getFieldEditorParent();
		addField(new BooleanFieldEditor(Preference.ACTIVE, "&Enable aiXcoder", parent));
		addField(new StringFieldEditor(Preference.ENDPOINT, "&Server URL", parent));
		addField(new StringFieldEditor(Preference.SEARCH_ENDPOINT, "Searc&h URL", parent));
		addField(new StringFieldEditor(Preference.SOCKET_ENDPOINT, "S&ocket Endpoint", parent));
		addField(new BooleanFieldEditor(Preference.SEARCH_ON_STARTUP, "Open Search View on Startup", parent));
		addField(new BooleanFieldEditor(Preference.SEACH_ON_SELECT, "Search on Select", parent));

		String[][] entryNamesAndValues = getModels();
		addField(new ComboFieldEditor(Preference.MODEL, "&Model", entryNamesAndValues, parent));
		addField(new StringFieldEditor(Preference.PARAMS, "Additional &Parameters", parent));
	}

	private String[][] getModels() {
		String[][] entryNamesAndValues;
		try {
			String[] models = API.getModels();
			entryNamesAndValues = new String[models.length][2];
			for (int i = 0; i < models.length; i++) {
				entryNamesAndValues[i] = new String[] { models[i], models[i] };
			}
		} catch (Exception e) {
			entryNamesAndValues = new String[0][2];
		}
		return entryNamesAndValues;
	}

	@Override
	public void init(IWorkbench workbench) {
		// second parameter is typically the plug-in id
		setPreferenceStore(Preference.preferenceManager);
		onPropertyChange = new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				PredictCache.getInstance().cache.clear();
			}
		};
		getPreferenceStore().addPropertyChangeListener(onPropertyChange);
		setDescription(
				"AiXcoder is an AI-powered code completion service. Visit https://aixcoder.com for more information.");
	}

	public void dispose() {
		getPreferenceStore().removePropertyChangeListener(onPropertyChange);
		super.dispose();
	}
}
