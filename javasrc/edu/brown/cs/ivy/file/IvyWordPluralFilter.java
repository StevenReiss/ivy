/********************************************************************************/
/*										*/
/*		IvyWordPluralFilter.java					*/
/*										*/
/*	Convert plurals to singular form					*/
/*										*/
/********************************************************************************/
/*	Copyright 2011 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 * This program and the accompanying materials are made available under the	 *
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, *
 * and is available at								 *
 *	http://www.eclipse.org/legal/epl-v10.html				 *
 *										 *
 ********************************************************************************/

/* SVN: $Id: IvyWordPluralFilter.java,v 1.1 2020/05/03 01:18:51 spr Exp $ */



package edu.brown.cs.ivy.file;


import java.util.HashMap;


public class IvyWordPluralFilter
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static final HashMap<String,String> IRREGULAR_WORDS;

private static final int SHORT_LENGTH = 3;

static {
   IRREGULAR_WORDS = new HashMap<String,String>();

   // ves -> f
   IRREGULAR_WORDS.put("calves","calf");
   IRREGULAR_WORDS.put("elves","elf");
   IRREGULAR_WORDS.put("halves","half");
   IRREGULAR_WORDS.put("hooves","hoof");
   IRREGULAR_WORDS.put("knives","knife");
   IRREGULAR_WORDS.put("leaves","leaf");
   IRREGULAR_WORDS.put("lives","life");
   IRREGULAR_WORDS.put("loaves","loaf");
   IRREGULAR_WORDS.put("scarves","scarf");
   IRREGULAR_WORDS.put("selves","self");
   IRREGULAR_WORDS.put("sheaves","sheaf");
   IRREGULAR_WORDS.put("shelves","shelf");
   IRREGULAR_WORDS.put("thieves","thief");
   IRREGULAR_WORDS.put("wives","wife");
   IRREGULAR_WORDS.put("wolves","wolf");
   // pure irregular
   IRREGULAR_WORDS.put("firemen","fireman");
   IRREGULAR_WORDS.put("feet","foot");
   IRREGULAR_WORDS.put("geese","goose");
   IRREGULAR_WORDS.put("lice","louse");
   IRREGULAR_WORDS.put("men","man");
   IRREGULAR_WORDS.put("mice","mouse");
   IRREGULAR_WORDS.put("teeth","tooth");
   IRREGULAR_WORDS.put("women","woman");
   // old english
   IRREGULAR_WORDS.put("children","child");
   IRREGULAR_WORDS.put("oxen","ox");
   // oes -> o
   IRREGULAR_WORDS.put("echoes","echo");
   IRREGULAR_WORDS.put("embargoes","embargo");
   IRREGULAR_WORDS.put("heroes","hero");
   IRREGULAR_WORDS.put("potatoes","potato");
   IRREGULAR_WORDS.put("tomatoes","tomato");
   IRREGULAR_WORDS.put("torpedoes","torpedo");
   IRREGULAR_WORDS.put("vetoes","veto");
   // ces -> x
   IRREGULAR_WORDS.put("apices","apex");
   IRREGULAR_WORDS.put("appendices","appendix");
   IRREGULAR_WORDS.put("cervices","cervix");
   IRREGULAR_WORDS.put("indices","index");
   IRREGULAR_WORDS.put("matrices","matrix");
   IRREGULAR_WORDS.put("vortices","vortex");
   // es -> is
   IRREGULAR_WORDS.put("analyses","analysis");
   IRREGULAR_WORDS.put("axes","axis");
   IRREGULAR_WORDS.put("bases","basis");
   IRREGULAR_WORDS.put("crises","crisis");
   IRREGULAR_WORDS.put("diagnoses","diagnosis");
   IRREGULAR_WORDS.put("emphases","emphasis");
   IRREGULAR_WORDS.put("hypotheses","hypothesis");
   IRREGULAR_WORDS.put("neuroses","neurosis");
   IRREGULAR_WORDS.put("oases","oasis");
   IRREGULAR_WORDS.put("parentheses","parenthesis");
   IRREGULAR_WORDS.put("synopses","synopsis");
   IRREGULAR_WORDS.put("theses","thesis");
   // ies -> ie
   IRREGULAR_WORDS.put("Libyan","Libya");
   IRREGULAR_WORDS.put("Syrian","Syria");
   IRREGULAR_WORDS.put("libyan","libya");
   IRREGULAR_WORDS.put("syrian","syria");
   // singular words ending in s
   IRREGULAR_WORDS.put("news","news");
   IRREGULAR_WORDS.put("atlas","atlas");
   IRREGULAR_WORDS.put("cosmos","cosmos");
   IRREGULAR_WORDS.put("bias","bias");
   IRREGULAR_WORDS.put("andes","andes");
   IRREGULAR_WORDS.put("aries","aries");
}



/********************************************************************************/
/*										*/
/*	Compute singular							*/
/*										*/
/********************************************************************************/

public static final String findSingular(String w)
{	
   // quick check
   if (w.length() <= SHORT_LENGTH || w.charAt(w.length()-1) != 's') {
      return w;
    }

   // don't strip posssesive form
   if (w.endsWith("'s")) {
      //if (w.length() > 2)
      //      return w.substring(0,w.length()-2);
      return w;
    }

   // irregular forms
   String irregular = IRREGULAR_WORDS.get(w);
   if (irregular != null)
      return irregular;

   // similar to step 1a of porter2 stemmer
   if (w.endsWith("sses")) {
      return w.substring(0,w.length()-2);
    }
   else if (w.endsWith("ies")){
      if (w.length() == SHORT_LENGTH+1) // ties -> tie
	 return w.substring(0,SHORT_LENGTH);
      else // flies -> fly
	 return w.substring(0,w.length()-SHORT_LENGTH)+"y";
    }
   else if (w.endsWith("ss") || w.endsWith("us")) {
      return w;
    }
   else if (w.endsWith("xes")) {
      return w.substring(0,w.length()-2);
    }
   else if (w.endsWith("s")) {
      return w.substring(0,w.length()-1);
    }

   return w;
}


}	// end of class IvyWordPluralFilter




/* end of IvyWordPluralFilter.java */


















































