/********************************************************************************/
/*                                                                              */
/*              SwingKey.java                                                   */
/*                                                                              */
/*      Handle keyboard mappings                                                */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.ivy.swing;

import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;

import edu.brown.cs.ivy.file.IvyLog;

public class SwingKey
{


/********************************************************************************/
/*                                                                              */
/*      Private Static Storage                                                  */
/*                                                                              */
/********************************************************************************/

public static final String	MENU_KEY = "menu";
public static final String	XALT_KEY = "xalt";
public static final String      YALT_KEY = "yalt";

private static final String	menu_keyname;
private static final String	xalt_keyname;
private static final String     yalt_keyname; 

private static Map<String,Map<String,Set<String>>> command_keys = new HashMap<>();
private static Map<String,Map<String,String>> defined_keys = new TreeMap<>();

private static File             save_file = null;
private static boolean          keys_changed = false;

static {
   int mask = SwingText.getMenuShortcutKeyMaskEx();
   if (mask == InputEvent.META_DOWN_MASK) {
      menu_keyname = "meta";
      xalt_keyname = "ctrl";
      yalt_keyname = "alt";
    }
   else if (mask == InputEvent.CTRL_DOWN_MASK) {
      menu_keyname = "ctrl";
      xalt_keyname = "alt";
      yalt_keyname = "meta";
    }
   else {
      menu_keyname = "ctrl";
      xalt_keyname = "alt";
      yalt_keyname = "meta";
    }
}


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private List<KeyStroke> key_strokes;
private Action          key_action;



/********************************************************************************/
/*                                                                              */
/*      Setup methods                                                           */
/*                                                                              */
/********************************************************************************/

public static void loadKeyDefinitions(File f) throws IOException
{
   if (f == null) return;
   
   if (f.exists() && f.canRead()) {
      loadKeyDefinitions(new FileInputStream(f),true);
    }
   save_file = f;
}



public static void loadKeyDefinitions(InputStream ins,boolean overwrite) throws IOException
{
   if (ins == null) return;
   
   try (BufferedReader br = new BufferedReader(new InputStreamReader(ins))) {
      for ( ; ; ) {
         String ln = br.readLine();
         if (ln == null) break;
         ln = ln.trim();
         if (ln.length() == 0) continue;
         if (ln.startsWith("#") || ln.startsWith("/")) continue;
         StringTokenizer tok = new StringTokenizer(ln,":=");
         int tct = tok.countTokens();
         
         String when = null;
         String act = null;
         List<String> defs = new ArrayList<>();
         if (tct < 2) continue;
         else if (tct > 2) {
            when = tok.nextToken().trim();
            act = tok.nextToken().trim();
          }
         else {
            when = "*";
            act = tok.nextToken().trim();
          }
         
         String d1 = tok.nextToken(",;:").trim();
         if (d1.length() > 0) defs.add(d1);
         while (tok.hasMoreTokens()) {
            d1 = tok.nextToken().trim();
            if (d1.length() > 0) defs.add(d1);
          }
         if (defs.isEmpty()) continue;
         Map<String,String> dkeys = defined_keys.get(when);
         if (dkeys == null) {
            dkeys = new HashMap<>();
            defined_keys.put(when,dkeys);
          }
         
         for (String def : defs) {
            def = normalizeKey(def);
            String ocmd = dkeys.get(def);
            if (ocmd != null) {
               if (!overwrite) continue;
               removeKeyForCommand(when,ocmd,def);
             }
            addKeyForCommand(when,act,def);
          }
       }
    }
   
   keys_changed = false;
}


public static void setSaveFile(File f)
{
   save_file = f;
}



public static void clearKeyDefinitions()
{
   command_keys.clear();
   defined_keys.clear();
}



public static void saveKeyDefinitions()
{
   if (!keys_changed || save_file == null) return;
   
   try (PrintWriter pw = new PrintWriter(save_file)) {
      for (String when : defined_keys.keySet()) {
         // build the set of actions for this when
         Map<String,Set<String>> acts = new TreeMap<>();
         for (Map.Entry<String,String> ent : defined_keys.get(when).entrySet()) {
            String key = ent.getKey();
            String act = ent.getValue();
            if (act == null) continue;
            Set<String> actkeys = acts.get(act);
            if (actkeys == null) {
               actkeys = new TreeSet<>();
               acts.put(act,actkeys);
             }
            actkeys.add(key);
          }
         for (Map.Entry<String,Set<String>> ent : acts.entrySet()) {
            pw.print(when + ": " + ent.getKey() + ": ");
            int ct = 0;
            for (String k : ent.getValue()) {
               if (ct++ > 0) pw.print(", ");
               pw.print(k);
             }
            pw.println();
          }
       }
    }
   catch (IOException e) {
      IvyLog.logE("Problem writing out key file",e);
    }
}



public static Map<String,Map<String,Set<String>>> getKeyMappings()
{
   return command_keys;
}


/********************************************************************************/
/*                                                                              */
/*      Keep Track of when-key-action mappings                                  */
/*                                                                              */
/********************************************************************************/

private static String normalizeKey(String keydef)
{
   StringBuffer buf = new StringBuffer();
   String [] pfxset = new String [] { "menu", "xalt", "yalt", "alt", "meta", "ctrl", "shift" };
   int ct = 0;
   for (String s : pfxset) {
      int idx = keydef.indexOf(s);
      if (idx >= 0) {
         String s1 = keydef.substring(idx+s.length()).trim();
         if (idx == 0) {
            keydef = s1;
          }
         else keydef = keydef.substring(0,idx) + " " + s1;
         if (ct++ > 0) buf.append(" ");
         buf.append(s);
       }
    }
   if (ct > 0) buf.append(" ");
   buf.append(keydef);

   return buf.toString();
}


private static Set<String> getKeysForCommand(String when,String cmd)
{
   if (when == null) when = "*";
   
   Map<String,Set<String>> cmdkeys = command_keys.get(when);
   if (cmdkeys != null) {
      Set<String> keys = cmdkeys.get(cmd);
      if (keys != null) return keys;
    }
   if (!when.equals("*")) return getKeysForCommand("*",cmd);
   return null;
}
   

private static void removeKeyForCommand(String when,String cmd,String key)
{
   if (when == null) when = "*";
   
   Map<String,Set<String>> cmdkeys = command_keys.get(when);
   if (cmdkeys != null) {
      Set<String> keys = cmdkeys.get(cmd);
      if (keys != null) {
         keys.remove(key);
         return;
       }
    }
}


private static void addKeyForCommand(String when,String cmd,String key)
{
   if (when == null) when = "*";
   
   Map<String,Set<String>> cmdkeys = command_keys.get(when);
   if (cmdkeys == null) {
      cmdkeys = new HashMap<>();
      command_keys.put(when,cmdkeys);
    }
   Set<String> keys = cmdkeys.get(cmd);
   if (keys == null) {
      keys = new HashSet<>();
      cmdkeys.put(cmd,keys);
    }
   keys.add(key);
   
   Map<String,String> keycmds = defined_keys.get(when);
   if (keycmds == null) {
      keycmds = new HashMap<>();
      defined_keys.put(when,keycmds);
    }
   keycmds.put(key,cmd);
}



private static String getCommandName(Action a)
{
   if (a == null) {
      IvyLog.logE("No action given for key");
      return null;
    }   
   
   String nm = (String) a.getValue(Action.NAME);
   if (nm == null) {
      nm = a.getClass().getName();
      int idx = nm.lastIndexOf(".");
      if (idx > 0) nm = nm.substring(idx+1);
    }
   
   return nm;
}



/********************************************************************************/
/*                                                                              */
/*      Direct Calls                                                            */
/*                                                                              */
/********************************************************************************/

public static void registerKeyAction(JComponent jc,Action a,String ... key)
{
   registerKeyAction(jc,getCommandName(a),a,key);
}


public static void registerKeyAction(String when,JComponent jc,Action a,String... key)
{
   registerKeyAction(when,jc,getCommandName(a),a,key);
}


public static void registerKeyAction(JComponent jc,String nm,Action a,String ... key) 
{
   SwingKey sk = new SwingKey(nm,a,key);
   sk.registerKeyAction(jc);
}


public static void registerKeyAction(String when,JComponent jc,String nm,
      Action a,String ... key) 
{
   SwingKey sk = new SwingKey(when,nm,a,key);
   sk.registerKeyAction(jc);
}


public static void registerKeyAction(String when,String nm,String ... key) 
{
    new SwingKey(when,nm,null,key);
} 



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public SwingKey(Action a,String... key)
{
   this(null,a,key);
}



public SwingKey(String when,Action a,String... dfltkey)
{
   this(when,null,a,dfltkey);
}


public SwingKey(String when,String name,String... dfltkey)
{
   this(when,name,null,dfltkey);
}


public SwingKey(String when,String name,Action a,String... dfltkey)
{
   if (name == null) name = getCommandName(a);
   if (when == null) when = "*";
   
   Set<String> defs = getKeysForCommand(when,name);
   if (defs == null || defs.isEmpty()) {
      defs = new HashSet<>();
      for (String s : dfltkey) {
         s = normalizeKey(s);
         addKeyForCommand(when,name,s);
         defs.add(s);
         keys_changed = true;
       }
    }
   key_strokes = new ArrayList<>();
   for (String s : defs) {
      String s1 = fixKey(s);
      KeyStroke ks = KeyStroke.getKeyStroke(s1);
      if (ks != null) key_strokes.add(ks);
      else {
         IvyLog.logE("SWING","Bad Key Definition for " + name + ": " + s);
       }
    }
   key_action = a;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public void addToKeyMap(Keymap kmp)
{
   if (key_action == null) return;
   for (KeyStroke ks : key_strokes) {
      kmp.addActionForKeyStroke(ks,key_action);
    }
}


public void registerKeyAction(JComponent jc)
{
   if (key_action == null || jc == null) return;
   
   String cmd = getCommandName(key_action);
   // this needs to use the actual action name, not the user name
   
   InputMap im = jc.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
   for (KeyStroke ks : key_strokes) {
      im.put(ks,cmd);
      jc.getActionMap().put(cmd,key_action);
    }
}



/********************************************************************************/
/*                                                                              */
/*      Mapping methods                                                         */
/*                                                                              */
/********************************************************************************/

private String fixKey(String key)
{
   key = key.replace(MENU_KEY,menu_keyname);
   key = key.replace(XALT_KEY,xalt_keyname);
   key = key.replace(YALT_KEY,yalt_keyname);
   return key;
}



}       // end of class SwingKey




/* end of SwingKey.java */

