package engine.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author jkiang
 * 
 * TODO
 * i18n effort needs more work...
 */
public class HanziLookupBundleKeys {
	
	static public final String OK_BUNDLE_KEY = "ok";
	
	static public final String SETTINGS_BUNDLE_KEY = "settings";
	static public final String SAVE_SETTINGS_BUNDLE_KEY = "save_settings";
	
	static public final String CHARACTER_TYPE_BUNDLE_KEY = "character_type_bundle_key";
	static public final String SIMPLIFIED_TYPE_BUNDLE_KEY = "simplified_character_type";
	static public final String TRADITIONAL_TYPE_BUNDLE_KEY = "traditional_character_type";
	static public final String BOTH_TYPES_BUNDLE_KEY = "both_character_types";
	
	static public final String LOOKUP_OPTIONS_BUNDLE_KEY = "lookup_options";
	static public final String CHOOSE_FONT_BUNDLE_KEY = "choose_font";
	
	static public final String OPTIONS_BUNDLE_KEY = "options";
	static public final String AUTO_LOOKUP_BUNDLE_KEY = "auto_lookup";
	static public final String LOOKUP_LOOSENESS_BUNDLE_KEY = "lookup_looseness";
	static public final String MATCH_COUNT_BUNDLE_KEY = "match_count";
	static public final String TYPE_MACRO_BUNDLE_KEY = "type_macro";
	static public final String LOOKUP_MACRO_BUNDLE_KEY = "lookup_macro";
	static public final String UNDO_MACRO_BUNDLE_KEY = "undo_macro";
	static public final String CLEAR_MACRO_BUNDLE_KEY = "clear_macro";
	
	static {
		Map contents = new HashMap();
		
		contents.put(OK_BUNDLE_KEY, "Ok");
		contents.put(SETTINGS_BUNDLE_KEY, "Settings");
		contents.put(SAVE_SETTINGS_BUNDLE_KEY, "Save Settings");
		contents.put(CHARACTER_TYPE_BUNDLE_KEY, "Character Mode");
		contents.put(SIMPLIFIED_TYPE_BUNDLE_KEY, "Simplified");
		contents.put(TRADITIONAL_TYPE_BUNDLE_KEY,"Traditional");
		contents.put(BOTH_TYPES_BUNDLE_KEY, "Both");
		contents.put(LOOKUP_OPTIONS_BUNDLE_KEY, "Lookup options");
		contents.put(CHOOSE_FONT_BUNDLE_KEY, "Choose Font");
		contents.put(OPTIONS_BUNDLE_KEY, "Options");
		contents.put(AUTO_LOOKUP_BUNDLE_KEY, "Auto Lookup");
		contents.put(LOOKUP_LOOSENESS_BUNDLE_KEY, "Lookup Looseness");
		contents.put(MATCH_COUNT_BUNDLE_KEY, "Match Count");
		contents.put(TYPE_MACRO_BUNDLE_KEY, "(type macro)");
		contents.put(LOOKUP_MACRO_BUNDLE_KEY, "Lookup macro: ");
		contents.put(UNDO_MACRO_BUNDLE_KEY, "Undo macro: ");
		contents.put(CLEAR_MACRO_BUNDLE_KEY, "Clear macro: ");

		DEFAULT_ENGLISH_CONTENTS = Collections.unmodifiableMap(contents);
	}
	
	static public ResourceBundle DEFAULT_ENGLISH_BUNDLE = new DefaultEnglishBundle();
	
	static public Map DEFAULT_ENGLISH_CONTENTS;

	static public class DefaultEnglishBundle extends ListResourceBundle {
		
		private DefaultEnglishBundle() {
		}
		
		public Object[][] getContents() {
			
			Object[][] contents = new Object[DEFAULT_ENGLISH_CONTENTS.size()][2];
			Iterator entryIter = DEFAULT_ENGLISH_CONTENTS.entrySet().iterator();
			for(int i = 0; i < contents.length; i++) {
				Map.Entry entry = (Map.Entry)entryIter.next();
				contents[i][0] = entry.getKey();
				contents[i][1] = entry.getValue();
			}
			return contents;
		}
	}
}
