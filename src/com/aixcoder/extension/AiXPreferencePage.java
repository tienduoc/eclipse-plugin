package com.aixcoder.extension;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
		addField(new BooleanFieldEditor("ACTIVE", "&Enable aiXcoder", getFieldEditorParent()));
		addField(new StringFieldEditor("ENDPOINT", "&Server URL", getFieldEditorParent()));

		String[][] entryNamesAndValues = getModels();
		addField(new ComboFieldEditor("MODEL", "&Model", entryNamesAndValues, getFieldEditorParent()));
		addField(new StringFieldEditor("PARAMS", "Additional &Parameters", getFieldEditorParent()));
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