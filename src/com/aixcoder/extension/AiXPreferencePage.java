package com.aixcoder.extension;

import static com.aixcoder.i18n.Localization.R;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.aixcoder.core.PredictCache;
import com.aixcoder.i18n.EN;
import com.aixcoder.i18n.Localization;
import com.aixcoder.i18n.ZH;
import com.aixcoder.lib.Preference;

public class AiXPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private IPropertyChangeListener onPropertyChange;

	public AiXPreferencePage() {
		super(GRID);
	}

	public void createFieldEditors() {
		new AiXPreInitializer().initializeDefaultPreferences();
		Composite parent = getFieldEditorParent();
		addField(new BooleanFieldEditor(Preference.ACTIVE, R(Localization.enableAiXCoder), parent));
//		BooleanFieldEditor selfLearnCheck = new BooleanFieldEditor(Preference.SELF_LEARN, R(Localization.selfLearn), parent);
//		selfLearnCheck.setEnabled(Preference.isProfessional(), parent);
//		addField(selfLearnCheck);
//		addField(new BooleanFieldEditor(Preference.AUTO_IMPORT, R(Localization.autoImportClasses), parent));
//		addField(new BooleanFieldEditor(Preference.SORT_ONLY, R(Localization.sortOnly), parent));
//		IntegerFieldEditor longResultRank = new IntegerFieldEditor(Preference.LONG_RESULT_RANK,
//				R(Localization.longResultRank), parent);
//		longResultRank.setValidRange(1, 5);
//		addField(longResultRank);
//
//		addField(new ComboFieldEditor(Preference.LONG_RESULT_CUT, R(Localization.longResultCut),
//				new String[][] { { R(Localization.longResultCutAuto), Localization.longResultCutAuto },
//						{ R(Localization.longResultCut0), Localization.longResultCut0 },
//						{ R(Localization.longResultCut1), Localization.longResultCut1 },
//						{ R(Localization.longResultCut2), Localization.longResultCut2 },
//						{ R(Localization.longResultCut3), Localization.longResultCut3 },
//						{ R(Localization.longResultCut4), Localization.longResultCut4 },
//						{ R(Localization.longResultCut5), Localization.longResultCut5 }, },
//				parent));
//		addField(new ComboFieldEditor(Preference.LONG_RESULT_CUT_SORT, R(Localization.longResultCutSort),
//				new String[][] { { R(Localization.longResultCutL2S), Localization.longResultCutL2S },
//						{ R(Localization.longResultCutS2L), Localization.longResultCutS2L } },
//				parent));

		addField(new BooleanFieldEditor(Preference.ALLOW_TELEMETRY, R(Localization.allowTelemetry), parent));
		addField(new BooleanFieldEditor(Preference.ALLOW_LOCAL_INCOMPLETE, R(Localization.allowLocalIncomplete), parent));
		if (Preference.hasLoginFile) {
			addField(new BooleanFieldEditor(Preference.USE_LOCAL_SERVICE, R(Localization.useLocalService), parent));
		}
		addField(new ComboFieldEditor(Preference.LANGUAGE, R(Localization.language),
				new String[][] { { EN.display, EN.id }, { ZH.display, ZH.id } }, parent));
		addField(new StringFieldEditor(Preference.PARAMS, R(Localization.additionalParameters), parent));
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
		setDescription(R(Localization.description));
	}

	public void dispose() {
		getPreferenceStore().removePropertyChangeListener(onPropertyChange);
		super.dispose();
	}
}
