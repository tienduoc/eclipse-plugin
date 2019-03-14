package com.aixcoder.extension;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.aixcoder.lib.HttpRequest;
import com.aixcoder.lib.JSON;
import com.aixcoder.lib.Preference;

public class AiXPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private ComboFieldEditor modelsEditor;
	private IPropertyChangeListener onPropertyChange;

	public AiXPreferencePage() {
		super(GRID);
	}

	public void createFieldEditors() {
		new AiXPreInitializer().initializeDefaultPreferences();
		addField(new BooleanFieldEditor("ACTIVE", "&Enable aiXcoder", getFieldEditorParent()));
		addField(new StringFieldEditor("ENDPOINT", "&Server URL", getFieldEditorParent()));

		String[][] entryNamesAndValues = getModels();
		modelsEditor = new ComboFieldEditor("MODEL", "&Model", entryNamesAndValues, getFieldEditorParent());
		modelsEditor.load();
		addField(modelsEditor);
	}

	private String[][] getModels() {
		String[][] entryNamesAndValues;
		try {
			String body = HttpRequest.get(getPreferenceStore().getString("ENDPOINT") + "getmodels").body();
			String[] models = JSON.getStringList(JSON.decode(body).getList());
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
		setDescription("AiXcoder preferences");
	}

	public void dispose() {
		getPreferenceStore().removePropertyChangeListener(onPropertyChange);
		super.dispose();
	}
}
