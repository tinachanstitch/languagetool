/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Jaume Ortolà  i Font
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.rules.ca;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Category;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This rule checks if an adjective doesn't agree with the previous noun and 
 * at the same time it doesn't agree with any of the previous words.
 * Takes care of some exceptions. 
 *   
 * @author Jaume Ortolà i Font
 */
public class ComplexAdjectiveConcordanceRule extends CatalanRule {

	/**
	 * Patterns
	 */

	private static final Pattern NOM = Pattern.compile("N.*");
	private static final Pattern NOM_MS = Pattern.compile("N.[MC][SN].*");
	private static final Pattern NOM_FS = Pattern.compile("N.[FC][SN].*");
	private static final Pattern NOM_MP = Pattern.compile("N.[MC][PN].*");
	private static final Pattern NOM_FP = Pattern.compile("N.[FC][PN].*");
	private static final Pattern DET_CS = Pattern.compile("D[NDA0I]0CS0");
	private static final Pattern DET_MS = Pattern.compile("D[NDA0I]0MS0"); 
	private static final Pattern DET_FS = Pattern.compile("D[NDA0I]0FS0");
	private static final Pattern DET_MP = Pattern.compile("D[NDA0I]0MP0");
	private static final Pattern DET_FP = Pattern.compile("D[NDA0I]0FP0");
	private static final Pattern GN_MS = Pattern.compile("N.[MC][SN].*|D[NDA0I]0MS0");
	private static final Pattern GN_FS = Pattern.compile("N.[FC][SN].*|D[NDA0I]0FS0");
	private static final Pattern GN_MP = Pattern.compile("N.[MC][PN].*|D[NDA0I]0MP0");
	private static final Pattern GN_FP = Pattern.compile("N.[FC][PN].*|D[NDA0I]0FP0");
	private static final Pattern GN_CP = Pattern.compile("N.[FMC][PN].*|D[NDA0I]0[FM]P0");
	private static final Pattern GN_CS = Pattern.compile("N.[FMC][SN].*|D[NDA0I]0[FM]S0");

	private static final Pattern ADJECTIU = Pattern.compile("AQ.*|V.P.*|PX.*");
	private static final Pattern ADJECTIU_MS = Pattern.compile("A..[MC][SN].*|V.P..SM|PX.MS.*");
	private static final Pattern ADJECTIU_FS = Pattern.compile("A..[FC][SN].*|V.P..SF|PX.FS.*");
	private static final Pattern ADJECTIU_MP = Pattern.compile("A..[MC][PN].*|V.P..PM|PX.MP.*");
	private static final Pattern ADJECTIU_FP = Pattern.compile("A..[FC][PN].*|V.P..PF|PX.FP.*");
	private static final Pattern ADJECTIU_CP = Pattern.compile("A..C[PN].*");
	private static final Pattern ADJECTIU_CS = Pattern.compile("A..C[SN].*");
	private static final Pattern ADJECTIU_M = Pattern.compile("A..[MC].*|V.P...M|PX.M.*");
	private static final Pattern ADJECTIU_F = Pattern.compile("A..[FC].*|V.P...F|PX.F.*");
	private static final Pattern ADJECTIU_S = Pattern.compile("A...[SN].*|V.P..S.|PX..S.*");
	private static final Pattern ADJECTIU_P = Pattern.compile("A...[PN].*|V.P..P.|PX..P.*");
	private static final Pattern CONCORDA = Pattern.compile("_GN_.*");
	private static final Pattern UPPERCASE = Pattern.compile("\\p{Lu}[\\p{Ll}\u00B7]*");
	private static final Pattern COORDINACIO = Pattern.compile(",|i|o");
	private static final Pattern COORDINACIO_IONI = Pattern.compile("i|o|ni");
	private static final Pattern KEEP_COUNT = Pattern.compile("A.*|N.*|D[NAID].*|SPS.*|R.*|V.P.*");
	private static final Pattern KEEP_COUNT2 = Pattern.compile(",|i|o|ni");
	private static final Pattern PREPOSICIONS = Pattern.compile("SPS.*");
	private static final Pattern VERB_AUXILIAR = Pattern.compile("V[AS].*");
	private static final Pattern EXCEPCIONS_PARTICIPI = Pattern.compile("atès|atés|atesa|atesos|ateses|donat|donats|donada|donades");
	private static final Pattern EXCEPCIONS_PREVIA = Pattern.compile("volt(a|es)|vegad(a|es)|cops?|termes?|paraul(a|es)|mots?|vocables?");


	public ComplexAdjectiveConcordanceRule(ResourceBundle messages) throws IOException {
		if (messages != null) {
			super.setCategory(new Category("Concordances en grups nominals"));
		}
	}

	@Override
	public String getId() {
		return "CONCORDANCES_ADJECTIU_POSPOSAT";
	}

	@Override
	public String getDescription() {
		return "Comprova si un adjectiu concorda amb noms del voltant.";
	}

	@Override
	public RuleMatch[] match(final AnalyzedSentence text) {
		final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
		final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
		for (int i = 1; i < tokens.length; i++) {   //ignoring token 0, i.e., SENT_START
			if ( matchPostagRegexp(tokens[i],ADJECTIU) && !matchPostagRegexp(tokens[i],CONCORDA) )
			{
				final String token=tokens[i].getToken();
				final String prevToken=tokens[i-1].getToken();
//				String prevPrevToken="";
//				if (i > 2) {
//					prevPrevToken = tokens[i-2].getToken();
//				}
				String nextToken="";
				if (i < tokens.length-1) {
					nextToken = tokens[i+1].getToken();
				}
				int j;
				boolean adjectiveAgrees = false;
				boolean theRuleMaches = false; 
				boolean isException = false;
				boolean isPlural = true;
				boolean isPrevNoun = false;
				Pattern substPattern = null;
				Pattern adjPattern = null;
				Matcher isUpperCase = UPPERCASE.matcher(token);

				//Counts nouns and determiners before the adjectives. 
				//Takes care of acceptable combinations. 
				int maxLevels=4;
				int[] cNt = new int[maxLevels];
				int[] cNMS = new int[maxLevels]; int[] cNFS = new int[maxLevels];
				int[] cNMP = new int[maxLevels]; int[] cNFP = new int[maxLevels];
				int[] cDMS = new int[maxLevels]; int[] cDFS = new int[maxLevels];
				int[] cDMP = new int[maxLevels]; int[] cDFP = new int[maxLevels];
				int[] cN = new int[maxLevels]; int[] cD = new int[maxLevels]; 
				for (j=0; j<maxLevels; j++) { 
					cNt[j] = 0; 
					cNMS[j] = 0; cNFS[j] = 0; cNMP[j] = 0; cNFP[j] = 0; 
					cDMS[j] = 0; cDFS[j] = 0; cDMP[j] = 0; cDFP[j] = 0; 
					cN[j] = 0; cD[j] = 0;
				}

				int level=0;
				j=1; 
				boolean keepCounting=true;
				while (keepCounting && i-j>0 && level<maxLevels)
				{
					if (!isPrevNoun) {
						if (matchPostagRegexp(tokens[i-j],NOM_MS)) {cNMS[level]++;}
						if (matchPostagRegexp(tokens[i-j],NOM_FS)) {cNFS[level]++;}
						if (matchPostagRegexp(tokens[i-j],NOM_MP)) {cNMP[level]++;}
						if (matchPostagRegexp(tokens[i-j],NOM_FP)) {cNFP[level]++;}
					}
					if (matchPostagRegexp(tokens[i-j],NOM)) {cNt[level]++; isPrevNoun=true;} else {isPrevNoun=false;}; //avoid two consecutive nouns
					if (matchPostagRegexp(tokens[i-j],DET_CS)) {
						if (matchPostagRegexp(tokens[i-j+1],NOM_MS)) {cDMS[level]++;}
						if (matchPostagRegexp(tokens[i-j+1],NOM_FS)) {cDFS[level]++;}
					}
					if (matchPostagRegexp(tokens[i-j],DET_MS)) {cDMS[level]++;}
					if (matchPostagRegexp(tokens[i-j],DET_FS)) {cDFS[level]++;}
					if (matchPostagRegexp(tokens[i-j],DET_MP)) {cDMP[level]++;}
					if (matchPostagRegexp(tokens[i-j],DET_FP)) {cDFP[level]++;}	    		  
					if (i-j>0) {
						if (matchPostagRegexp(tokens[i-j],PREPOSICIONS) && !matchRegexp(tokens[i-j-1].getToken(),COORDINACIO_IONI )) {
							level++;
						}
					}
					if (level>0 && matchRegexp(tokens[i-j].getToken(),COORDINACIO_IONI) ) {
						int k=1;
						while (k<4 && i-j-k>0 
								&& (matchPostagRegexp(tokens[i-j-k],KEEP_COUNT) 
										|| matchRegexp(tokens[i-j-k].getToken(),KEEP_COUNT2) ) ) {
							if (matchPostagRegexp(tokens[i-j-k],PREPOSICIONS)) {
								j=j+k;
								break;
							}
							k++;
						}
					}
					j++;
					keepCounting=matchPostagRegexp(tokens[i-j],KEEP_COUNT) || matchRegexp(tokens[i-j].getToken(),KEEP_COUNT2);
				}
				level++;
				if (level>maxLevels) {
					level=maxLevels;
				}
				j=0;
				while (j<level) { 
					cN[j]=cNMS[j]+cNFS[j]+cNMP[j]+cNFP[j];
					cD[j]=cDMS[j]+cDFS[j]+cDMP[j]+cDFP[j];

					//exceptions: adjective is plural and there are several nouns before
					if (matchPostagRegexp(tokens[i],ADJECTIU_MP) 
							&& (cN[j]>1 || cD[j]>1) 
							&& (cNMS[j]+cNMP[j]+cDMS[j]+cDMP[j])>0 
							&& (cNFS[j]+cNFP[j]<=cNt[j]) ) {	
						isException=true;
						break;
					}
					if (!isException && matchPostagRegexp(tokens[i],ADJECTIU_FP) 
							&& (cN[j]>1 || cD[j]>1) 
							&& ((cNMS[j]+cNMP[j]+cDMS[j]+cDMP[j])==0 || (cNFS[j]+cNFP[j]>=cNt[j]))) {
						isException=true;
						break;
					}
					//Adjective can't be singular
					if (cN[j]+cD[j]>0) { //&& level>1
						isPlural=isPlural && cD[j]>1; //cN[j]>1
					}
//					else {
//						isPlural=false;
//					}
					j++;
				}

				//exception: noun (or adj) plural + two or more adjectives	
				if (!isException && i < tokens.length-2){
					Matcher pCoordina = COORDINACIO.matcher(nextToken);
					if (pCoordina.matches()) {
						if (   ((matchPostagRegexp(tokens[i-1],NOM_MP) || matchPostagRegexp(tokens[i-1],ADJECTIU_MP)) && matchPostagRegexp(tokens[i],ADJECTIU_M) && matchPostagRegexp(tokens[i+2],ADJECTIU_M))
								|| ((matchPostagRegexp(tokens[i-1],NOM_FP) || matchPostagRegexp(tokens[i-1],ADJECTIU_FP)) && matchPostagRegexp(tokens[i],ADJECTIU_F) && matchPostagRegexp(tokens[i+2],ADJECTIU_F)) )
						{
							isException=true;} 
					}	
				}

				//exception: una vegada + participi, a vegades, etc.
				if (!isException && matchRegexp(prevToken,EXCEPCIONS_PREVIA)) {
					isException=true;}

				//exceptions: llevat de, tret de, majúsucula inicial
				if ( !isException && ( ((token.equals("tret") || token.equals("llevat") ) && nextToken.equals("de")) 
						|| token.equals("primer") || token.equals("junts")  
						|| isUpperCase.matches() ) ) {
					isException=true;}
				
				//exceptions: atès, atesos..., donat, donats... 
				if ( !isException && matchRegexp(token,EXCEPCIONS_PARTICIPI) ) {
					isException=true;}

				//exceptions: segur que
				if (!isException && i < tokens.length-1 
						&& (token.equals("segur")||token.equals("major")||token.equals("menor")) && nextToken.equals("que") ){
					isException=true;      	
				}        	

				//look into 15 previous words
				if (!isException)
				{ 
					if (matchPostagRegexp(tokens[i],ADJECTIU_CS)) { substPattern=GN_CS; adjPattern=ADJECTIU_S; }
					else if (matchPostagRegexp(tokens[i],ADJECTIU_CP)) { substPattern=GN_CP; adjPattern=ADJECTIU_P; }
					else if (matchPostagRegexp(tokens[i],ADJECTIU_MS)) { substPattern=GN_MS; adjPattern=ADJECTIU_MS;}
					else if (matchPostagRegexp(tokens[i],ADJECTIU_FS)) { substPattern=GN_FS; adjPattern=ADJECTIU_FS;}
					else if (matchPostagRegexp(tokens[i],ADJECTIU_MP)) { substPattern=GN_MP; adjPattern=ADJECTIU_MP;}
					else if (matchPostagRegexp(tokens[i],ADJECTIU_FP)) { substPattern=GN_FP; adjPattern=ADJECTIU_FP;}

					if (substPattern!=null) {
						//previous token is a non-agreeing noun or adjective
						if ( (matchPostagRegexp(tokens[i-1],NOM) && !matchPostagRegexp(tokens[i-1],substPattern)) ||
								(i>3 /*&& !matchPostagRegexp(tokens[i],NOM)*/ && matchPostagRegexp(tokens[i-1],ADJECTIU) && !matchPostagRegexp(tokens[i-1],adjPattern) 
										&& !matchPostagRegexp(tokens[i-2],VERB_AUXILIAR) && !matchPostagRegexp(tokens[i-3],VERB_AUXILIAR))) {  
							j=1;
							keepCounting=true;
							while (i-j>0 && !adjectiveAgrees && keepCounting)
							{
								if (matchPostagRegexp(tokens[i-j],substPattern)) {
									adjectiveAgrees=true; // there is a previous agreeing noun
								}
								j++;
								keepCounting=matchPostagRegexp(tokens[i-j],KEEP_COUNT) || matchRegexp(tokens[i-j].getToken(),KEEP_COUNT2);
							}
							theRuleMaches=!adjectiveAgrees;
							//Adjective can't be singular
							if (isPlural && matchPostagRegexp(tokens[i],ADJECTIU_S)) {
								theRuleMaches=true;
							} 		
						}
					}
				}
				if (theRuleMaches) {
					final String msg = "L'adjectiu \u00AB"+token+"\u00BB no concorda apropiadament.";
					final RuleMatch ruleMatch = new RuleMatch(this, tokens[i].getStartPos(), tokens[i].getStartPos()+token.length(), msg, "Falta de concordança.");
					ruleMatches.add(ruleMatch);
				}
			}
		}
		return toRuleMatchArray(ruleMatches);
	}

	/**
	 * Match POS tag with regular expression
	 */
	private boolean matchPostagRegexp(AnalyzedTokenReadings aToken, Pattern pattern) {
		boolean matches = false;
		final int readingsLen = aToken.getReadingsLength();
		for (int i = 0; i < readingsLen; i++) {
			final String posTag = aToken.getAnalyzedToken(i).getPOSTag();
			if (posTag != null) {
				final Matcher m = pattern.matcher(posTag);
				if (m.matches()) {
					matches = true;
					break;
				}
			}
		}
		return matches;
	}

	/**
	 * Match String with regular expression
	 */
	private boolean matchRegexp(String s, Pattern pattern) {
		final Matcher m = pattern.matcher(s);
		return m.matches();
	}


	@Override
	public void reset() {
		// nothing
	}

}