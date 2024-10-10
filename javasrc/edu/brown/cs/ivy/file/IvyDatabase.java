/********************************************************************************/
/*										*/
/*		IvyDatabase.java						*/
/*										*/
/*	Basic code for connecting to a SQL database				*/
/*										*/
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Redistribution and use in source and binary forms, with or without		 *
 *  modification, are permitted provided that the following conditions are met:  *
 *										 *
 *  + Redistributions of source code must retain the above copyright notice,	 *
 *	this list of conditions and the following disclaimer.			 *
 *  + Redistributions in binary form must reproduce the above copyright notice,  *
 *	this list of conditions and the following disclaimer in the		 *
 *	documentation and/or other materials provided with the distribution.	 *
 *  + Neither the name of the Brown University nor the names of its		 *
 *	contributors may be used to endorse or promote products derived from	 *
 *	this software without specific prior written permission.		 *
 *										 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  *
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE	 *
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE	 *
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE	 *
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 	 *
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF	 *
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS	 *
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN	 *
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)	 *
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE	 *
 *  POSSIBILITY OF SUCH DAMAGE. 						 *
 *										 *
 ********************************************************************************/


package edu.brown.cs.ivy.file;


import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.StringTokenizer;


public class IvyDatabase {



/********************************************************************************/
/*										*/
/*	Constant Definitions							*/
/*										*/
/********************************************************************************/

private enum DbmsType {
   UNKNOWN,
   POSTGRESQL,
   MYSQL,
   DERBY,
   DERBY_EMBED
}


private static final String POSTGRES_DRIVERS = "org.postgresql.Driver:postgresql.Driver";
private static final String POSTGRES_PREFIX = "jdbc:postgresql:";

private static final String MYSQL_DRIVERS = "com.mysql.jdbc.Driver";
private static final String MYSQL_PREFIX = "jdbc:mysql:";

private static final String DERBY_DRIVERS = "org.apache.derby.jdbc.ClientDriver";
private static final String DERBY_EMBED_DRIVERS = "org.apache.derby.jdbc.EmbeddedDriver";
private static final String DERBY_PREFIX = "jdbc:derby:";

private static final String HOME_FILE = "$(HOME)/.ivy/Database.props";
private static final String SYSTEM_FILE = "$(IVY)/lib/Database.props";

private static final String TYPE_PROP = "edu.brown.cs.ivy.file.dbmstype";
private static final String HOST_PROP = "edu.brown.cs.ivy.file.dbmshost";
private static final String USER_PROP = "edu.brown.cs.ivy.file.dbmsuser";
private static final String PWD_PROP = "edu.brown.cs.ivy.file.dbmspassword";
private static final String FILE_PROP = "edu.brown.cs.ivy.file.dbmsfiles";

private static boolean postgres_usestdin = true;

public static final OutputStream NULL_STREAM = new NullOutputStream();

static {
   System.setProperty("derby.stream.error.file","/dev/null");
   System.setProperty("derby.stream.error.field","edu.brown.cs.ivy.file.IvyDatabase.NULL_STREAM");
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static DbmsType 	dbms_type = DbmsType.UNKNOWN;
private static String		dbms_prefix = null;
private static String		dbms_host = null;
private static String		dbms_user = null;
private static String		dbms_password = null;
private static String		dbms_default = null;
private static boolean		dbms_files = false;




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public static Connection openDatabase(String name) throws SQLException
{
   if (dbms_type == DbmsType.UNKNOWN) setup(null);

   String nm = dbms_prefix;
   if (dbms_host != null) nm += "//" + dbms_host + "/";
   nm += name;

   IvyLog.logD("Connect to database " + nm);

   Connection conn = DriverManager.getConnection(nm,dbms_user,dbms_password);

   Runtime.getRuntime().addShutdownHook(new DatabaseCloser(conn));

   switch (dbms_type) {
      case DERBY :
      case DERBY_EMBED :
	 File f3 = new File(System.getProperty("user.dir"));
	 File f4 = new File(f3,"derby.log");
	 f4.deleteOnExit();
	 break;
      default :
	 break;
    }

   return conn;
}


public static Connection openDefaultDatabase() throws SQLException
{
   if (dbms_type == DbmsType.UNKNOWN) setup(null);

   return openDatabase(dbms_default);
}


public static void setProperties(String dflt) throws SQLException
{
   if (dflt == null) setup(null);
   else setup(new File(dflt));
}


public static void setProperties(File dflt) throws SQLException
{
   setup(dflt);
}


public static void setProperties(InputStream ins) throws SQLException
{
   Properties p = new Properties();
   try {
      p.loadFromXML(ins);
      ins.close();
    }
   catch (IOException e) { }
   setupProperties(p);
}



/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

private static void setup(File propfile) throws SQLException
{
   Properties p0 = null;

   if (propfile != null) p0 = getProperties(propfile);

   setupProperties(p0);
}


private static void setupProperties(Properties p0) throws SQLException
{
   if (dbms_type != DbmsType.UNKNOWN) return;

   File db1 = new File(IvyFile.expandName(HOME_FILE));
   File db2 = new File(IvyFile.expandName(SYSTEM_FILE));

   Properties p1 = getProperties(db1);
   Properties p2 = getProperties(db2);
   for (Object opn : p1.keySet()) {		// local overrides system
      String pn = (String) opn;
      String pv = p1.getProperty(pn);
      p2.setProperty(pn,pv);
    }
   if (p0 != null) {
      for (Object opn : p0.keySet()) {
	 String pn = (String) opn;
	 String pv = p0.getProperty(pn);
	 p2.setProperty(pn,pv);
       }
    }

   String typ = getProperty(p2,TYPE_PROP,null);
   if (typ != null && typ.equalsIgnoreCase("POSTGRESQL")) {
      checkLoad(POSTGRES_DRIVERS,DbmsType.POSTGRESQL);
    }
   else if (typ != null && typ.equalsIgnoreCase("MYSQL")) {
      checkLoad(MYSQL_DRIVERS,DbmsType.MYSQL);
    }
   else if (typ != null && (typ.equalsIgnoreCase("JAVADB") || 
         typ.equalsIgnoreCase("DERBY"))) {
      checkLoad(DERBY_DRIVERS,DbmsType.DERBY);
    }
   else if (typ != null && (typ.equalsIgnoreCase("JAVADBEMBED") || 
         typ.equalsIgnoreCase("DERBYEMBED"))) {
      checkLoad(DERBY_EMBED_DRIVERS,DbmsType.DERBY_EMBED);
    }
   else {
      checkLoad(POSTGRES_DRIVERS,DbmsType.POSTGRESQL);
      if (dbms_type == DbmsType.UNKNOWN) checkLoad(MYSQL_DRIVERS,DbmsType.MYSQL);
    }

   if (dbms_type == DbmsType.UNKNOWN) throw new SQLException("Unable to load database drivers");

   dbms_host = getProperty(p2,HOST_PROP,null);
   dbms_user = getProperty(p2,USER_PROP,System.getProperty("user.name"));
   dbms_password = getProperty(p2,PWD_PROP,"?");

   if (dbms_user.equals("*")) dbms_user = System.getProperty("user.name");

   if (dbms_password.equals("?")) {
      Console cons = System.console();
      if (cons == null) throw new SQLException("No console to read password from");
      char [] pwd = cons.readPassword("Enter Database Password: ");
      if (pwd == null) throw new SQLException("Error reading password from console");
      dbms_password = new String(pwd);
    }

   switch (dbms_type) {
      case POSTGRESQL :
	 dbms_prefix = POSTGRES_PREFIX;
	 dbms_default = "postgres";
	 dbms_files = false;
	 try {
	    Class.forName("org.postgresql.copy.CopyManager");
	    dbms_files = true;
	  }
	 catch (ClassNotFoundException e) { }
	 break;
      case MYSQL :
	 dbms_prefix = MYSQL_PREFIX;
	 dbms_default = "information_schema";
	 dbms_files = true;		// MYSQL supports external files
	 break;
      case DERBY :
	 File f1 = new File(System.getProperty("user.home"));
	 File f2 = new File(f1,"derby.log");
	 System.setProperty("derby.stream.error.field",
               "edu.brown.cs.ivy.file.IvyDatabase.NULL_STREAM");
	 System.setProperty("derby.stream.error.file",f2.getPath());
	 System.setProperty("derby.system.home",f1.getPath());
	 dbms_prefix = DERBY_PREFIX;
	 dbms_default = null;
	 dbms_files = false;
	 if (dbms_host == null) dbms_host = "localhost:1527";
	 break;
      case DERBY_EMBED :
	 File f3 = new File(System.getProperty("user.home"));
	 File f4 = new File(f3,"derby.log");
	 System.setProperty("derby.stream.error.field",
               "edu.brown.cs.ivy.file.IvyDatabase.NULL_STREAM");
	 System.setProperty("derby.stream.error.file",f4.getPath());
	 System.setProperty("derby.system.home",f4.getPath());
	 dbms_prefix = DERBY_PREFIX;
	 dbms_default = null;
	 dbms_files = false;
	 dbms_host = null;
	 break;
      default :
	 throw new SQLException("Unknown database type");
    }

   dbms_files = Boolean.parseBoolean(getProperty(p2,FILE_PROP,Boolean.toString(dbms_files)));
}



private static Properties getProperties(File dbf)
{
   Properties p = new Properties();

   if (dbf.exists()) {
      try {
	 FileInputStream fis = new FileInputStream(dbf);
	 p.loadFromXML(fis);
	 fis.close();
       }
      catch (IOException e) {
	 System.err.println("IVY: Problem reading property file " + dbf + ": " + e);
       }
    }

   return p;
}




private static String getProperty(Properties p,String name,String dflt)
{
   String v = System.getProperty(name); 	// allow -D options on command line

   if (v == null) v = p.getProperty(name);	// get from data file
   if (v == null) v = dflt;

   return v;
}




private static void checkLoad(String drvs,DbmsType typ)
{
   StringTokenizer tok = new StringTokenizer(drvs,":");

   while (tok.hasMoreTokens()) {
      String cls = tok.nextToken();
      try {
	 Class.forName(cls);
	 dbms_type = typ;
	 return;
       }
      catch (ClassNotFoundException _e) { }
    }
}



/********************************************************************************/
/*										*/
/*	Database independent methods to load relation from a file		*/
/*										*/
/*	This assumes the file is Tab-separated, unquoted values 		*/
/*	Alternatively the Csv form assumes comma-separated, possibly quoted	*/
/*	values (using double quote).						*/
/*										*/
/********************************************************************************/

public static boolean getSupportsFiles()		{ return dbms_files; }



public static void loadTableFromFile(Connection conn,String tbl,String file) throws SQLException
{
   String cmd = null;

   if (dbms_type == DbmsType.MYSQL) {
      cmd = "LOAD DATA LOCAL INFILE '" + file + "' INTO TABLE " + tbl +
	 " FIELDS TERMINATED BY '\t'";
      evalCommand(conn,cmd);
    }
   else if (dbms_type == DbmsType.POSTGRESQL && postgres_usestdin) {
      cmd = "COPY " + tbl + " FROM STDIN";
      evalCopy(conn,cmd,file);
    }
   else if (dbms_type == DbmsType.POSTGRESQL) {
      cmd = "COPY " + tbl + " FROM '" + file + "'";
      evalCommand(conn,cmd);
    }
   else throw new SQLException("File loading not supported");
}



public static void loadTableFromCsvFile(Connection conn,String tbl,String file) throws SQLException
{
   String cmd = null;

   if (dbms_type == DbmsType.MYSQL) {
      cmd = "LOAD DATA LOCAL INFILE '" + file + "' INTO TABLE " + tbl;
      evalCommand(conn,cmd);
    }
   else if (dbms_type == DbmsType.POSTGRESQL && postgres_usestdin) {
      cmd = "COPY " + tbl + " FROM STDIN";
      evalCopy(conn,cmd,file);
    }
   else if (dbms_type == DbmsType.POSTGRESQL) {
      cmd = "COPY " + tbl + " FROM '" + file + "' WITH CSV QUOTE '\"'";
      evalCommand(conn,cmd);
    }
   else throw new SQLException("File loading not supported");
}


private static void evalCommand(Connection conn,String cmd) throws SQLException
{
   Statement st = conn.createStatement();
   st.execute(cmd);
   st.close();
}


private static void evalCopy(Connection conn,String cmd,String file) throws SQLException
{
   try {
      Class<?> c = conn.getClass();
      Method m = c.getMethod("getCopyAPI");
      Object v = m.invoke(conn);
      Class<?> c1 = v.getClass();
      Method m1 = c1.getMethod("copyIntoDB",String.class,InputStream.class);
      FileInputStream fis = new FileInputStream(file);
      m1.invoke(v,cmd,fis);
    }
   catch (Throwable t) {
      if (t instanceof SQLException) throw (SQLException) t;
      System.err.println("IVY: Problem with copy interface: " + t);
      t.printStackTrace();
      throw new SQLException("Copy problem",t);
    }
}



/********************************************************************************/
/*										*/
/*	Database-dependent operations						*/
/*										*/
/********************************************************************************/

public static String getBooleanValue(boolean v)
{
   if (dbms_type == DbmsType.MYSQL) {
      return (v ? "1" : "0");
    }

   return Boolean.toString(v);
}


public static String getIdDefType()
{
   switch (dbms_type) {
      case UNKNOWN :
      case MYSQL :
	 return "int AUTO_INCREMENT";
      case DERBY :
      case DERBY_EMBED :
	 return "INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)";
      case POSTGRESQL :
	 return "serial";
    }

   return "int AUTO_INCREMENT";
}



/********************************************************************************/
/*										*/
/*	Dummy log file for DERBY						*/
/*										*/
/********************************************************************************/


public static final class NullOutputStream extends OutputStream {

   public void write(int b) { }
   public void write(byte[] b) { }
   public void write(byte[] b,int off,int len) { }

}	// end of inner class NullOutputStream




/********************************************************************************/
/*										*/
/*	Database closer 							*/
/*										*/
/********************************************************************************/

private static class DatabaseCloser extends Thread {

   private Connection for_database;

   DatabaseCloser(Connection c) {
      for_database = c;
    }

   @Override public void run() {
      try {
	 if (for_database.isClosed()) return;
	 if (!for_database.getAutoCommit()) {
	    for_database.commit();
	  }
       }
      catch (SQLException e) { }
      try {
	 if (for_database.isClosed()) return;
	 for_database.close();
       }
      catch (SQLException e) { }
    }

}	// end of inner class DataabseCloser


}	// end of class IvyDatabase




/* end of IvyDatabase.java */
