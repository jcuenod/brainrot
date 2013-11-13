package jcuenod.brainrot;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class LanguageUtils {
	private static String LOG_TAG = "BrainRot LangU";
	
	public static String normalize (String denormalised)
	{
		return Normalizer.normalize(denormalised, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}
	public static String transliterate (String unicodeStr)
	{
		String ret = unicodeStr;
	    for (Map.Entry<String, String> entry : getTransliterationHash().entrySet()) {
	    	ret = ret.replace(entry.getKey(), entry.getValue());
	    }
		return ret;
	}
	public static String domagic (String denormalised)
	{
		return LanguageUtils.transliterate(LanguageUtils.normalize(denormalised));
	}
	
	private static Map<String, String> getTransliterationHash()
	{
		Map<String, String> hash = new HashMap<String, String>();
		hash.put("α", "a");
		hash.put("β", "b");
		hash.put("γ", "g");
		hash.put("δ", "d");
		hash.put("ε", "e");
		hash.put("ζ", "z");
		hash.put("η", "e");
		hash.put("θ", "th");
		hash.put("ι", "i");
		hash.put("κ", "k");
		hash.put("λ", "l");
		hash.put("μ", "m");
		hash.put("ν", "n");
		hash.put("ξ", "x");
		hash.put("ο", "o");
		hash.put("π", "p");
		hash.put("ρ", "r");
		hash.put("σ", "s");
		hash.put("ς", "s");
		hash.put("τ", "t");
		hash.put("υ", "u");
		hash.put("φ", "ph");
		hash.put("χ", "ch");
		hash.put("ψ", "ps");
		hash.put("ω", "o");

		hash.put("Α", "a");
		hash.put("Β", "b");
		hash.put("Γ", "g");
		hash.put("Δ", "d");
		hash.put("Ε", "e");
		hash.put("Ζ", "z");
		hash.put("Η", "e");
		hash.put("Θ", "th");
		hash.put("Ι", "i");
		hash.put("Κ", "k");
		hash.put("Λ", "l");
		hash.put("Μ", "m");
		hash.put("Ν", "n");
		hash.put("Ξ", "x");
		hash.put("Ο", "o");
		hash.put("Π", "p");
		hash.put("Ρ", "r");
		hash.put("Σ", "s");
		hash.put("Σ", "s");
		hash.put("Τ", "t");
		hash.put("Υ", "u");
		hash.put("Φ", "ph");
		hash.put("Χ", "ch");
		hash.put("Ψ", "ps");
		hash.put("Ω", "o");
		return hash;
	}
}
