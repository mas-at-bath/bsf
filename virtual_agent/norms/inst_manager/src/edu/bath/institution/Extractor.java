package edu.bath.institution;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Extractor {
	
	// End offset is used if match has been truncated
	static public String patternExtractor(String pattern, String match,int endOffset)
	{
		//System.out.println("Pattern:"+pattern);
		//System.out.println("Match:"+match);
		int start = pattern.indexOf(".*");
		int end = pattern.length()- (start + 3) - endOffset;
		String extract = match.substring(start, match.length() - end);
		return extract;
	}
	
	static public String powerExtractor(String match)
	{
		String regex = "pow(.*,.*)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(match);
	    if (m.find()){
				int i = match.indexOf(",");
				String extract = "pow(" + match.substring(i+1); 
				return extract;
		}
	    return match;
	}
}
