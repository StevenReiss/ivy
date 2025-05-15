/********************************************************************************/
/*                                                                              */
/*              BowerDatabasePool.java                                          */
/*                                                                              */
/*      Simple query interface with a database pool and prepared statements     */
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



package edu.brown.cs.ivy.bower;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.JSONObject;

import edu.brown.cs.ivy.file.IvyDatabase;
import edu.brown.cs.ivy.file.IvyLog;

public class BowerDatabasePool implements BowerConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String database_name;
private List<Connection> connection_pool;
private List<Connection> used_connections;

private static int INITIAL_POOL_SIZE = 10;
private static int MAX_POOL_SIZE = 15;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public BowerDatabasePool(File props,String name) throws SQLException
{
   IvyDatabase.setProperties(props);
   initialize(name);
}


public BowerDatabasePool(Properties props,String name) throws SQLException
{
   IvyDatabase.setProperties(props);
   initialize(name);
}



public BowerDatabasePool(InputStream props,String name) throws SQLException
{
   IvyDatabase.setProperties(props);
   initialize(name);
}


private void initialize(String name) throws SQLException
{
   database_name = name;
   if (name == null) throw new SQLException("No database name");
   
   connection_pool = new ArrayList<>();
   used_connections = new ArrayList<>();
   
   for (int i = 0; i < INITIAL_POOL_SIZE; ++i) {
      connection_pool.add(IvyDatabase.openDatabase(database_name));
    }
}


/********************************************************************************/
/*                                                                              */
/*      SQL access methods                                                      */
/*                                                                              */
/********************************************************************************/

public int sqlUpdate(String query,Object... data)
{
   IvyLog.logD("BOWER","SQL: " + query + " " + getDataString(data));
   
   try {
      return executeUpdateStatement(query,data);
    }
   catch (SQLException e) {
      IvyLog.logE("BOWER","SQL problem",e);
    }
   
   return -1;
}


public JSONObject sqlQuery1(String query,Object... data)
{
   IvyLog.logD("BOWER","SQL: " + query + " " + getDataString(data));
   
   JSONObject rslt = null;
   
   try {
      ResultSet rs = executeQueryStatement(query,data);
      if (rs.next()) {
	 rslt = getJsonFromResultSet(rs);
       }
    }
   catch (SQLException e) {
      IvyLog.logE("BOWER","SQL problem",e);
    }
   
   return rslt;
}



public JSONObject sqlQueryOnly1(String query,Object... data)
{
   IvyLog.logD("BOWER","SQL: " + query + " " + getDataString(data));
   
   JSONObject rslt = null;
   
   try {
      ResultSet rs = executeQueryStatement(query,data);
      if (rs.next()) {
	 rslt = getJsonFromResultSet(rs);
       }
      if (rs.next()) rslt = null;
    }
   catch (SQLException e) {
      IvyLog.logE("BOWER","SQL problem",e);
    }
   
   return rslt;
}





public List<JSONObject> sqlQueryN(String query,Object... data)
{
   IvyLog.logD("BOWER","SQL: " + query + " " + getDataString(data));
   
   List<JSONObject> rslt = new ArrayList<>();;
   
   try {
      ResultSet rs = executeQueryStatement(query,data);
      while (rs.next()) {
	 JSONObject json = getJsonFromResultSet(rs);
	 rslt.add(json);
       }
    }
   catch (SQLException e) {
      IvyLog.logE("BURL","SQL problem",e);
    }
   
   return rslt;
}



public ResultSet executeQueryStatement(String q,Object... data) throws SQLException
{
   for ( ; ; ) {
      Connection c = getConnection();
      try {
         PreparedStatement pst = setupStatement(c,q,data);
	 ResultSet rslt = pst.executeQuery();
	 return rslt;
       }
      catch (SQLException e) {
         c = checkDatabaseError(c,e);
	 if (c != null) throw e;
       }
      finally {
         releaseConnection(c);
       }
    }
}


private int executeUpdateStatement(String q,Object... data) throws SQLException
{
   for ( ; ; ) {
      Connection c = getConnection();
      try {
         PreparedStatement pst = setupStatement(c,q,data);
	 int rslt = pst.executeUpdate();
	 return rslt;
       }
      catch (SQLException e) {
	 c = checkDatabaseError(c,e);
         if (c != null) throw e;
       }
      finally {
         releaseConnection(c);
       }
    }
}



/********************************************************************************/
/*                                                                              */
/*      Utility methods                                                         */
/*                                                                              */
/********************************************************************************/


private PreparedStatement setupStatement(Connection c,String query,
      Object... data) throws SQLException
{
   query = query.replaceAll("\\$[0-9]+","?");
   PreparedStatement pst = c.prepareStatement(query);
   for (int i = 0; i < data.length; ++i) {
      Object v = data[i];
      if (v instanceof String) {
	 pst.setString(i+1,(String) v);
       }
      else if (v instanceof Integer) {
	 pst.setInt(i+1,(Integer) v);
       }
      else if (v instanceof Long) {
	 pst.setLong(i+1,(Long) v);
       }
      else if (v instanceof Date) {
	 pst.setDate(i+1,(Date) v);
       }
      else if (v instanceof Timestamp) {
	 pst.setTimestamp(i+1,(Timestamp) v);
       }
      else if (v instanceof Boolean) {
	 pst.setBoolean(i+1,(Boolean) v);
       }
      else if (v instanceof Enum) {
         pst.setInt(i+1,((Enum<?>) v).ordinal());
       }
      else {
	 pst.setObject(i+1,v);
       }
    }
   return pst;
}


private String getDataString(Object... data)
{
   if (data.length == 0) return "";
   
   StringBuffer buf = new StringBuffer();
   for (int i = 0; i < data.length; ++i) {
      if (i == 0) buf.append("[");
      else buf.append(",");
      buf.append(String.valueOf(data[i]));
    }
   buf.append("]");
   
   return buf.toString();
}


public JSONObject getJsonFromResultSet(ResultSet rs)
{
   JSONObject rslt = new JSONObject();
   try {
      ResultSetMetaData meta = rs.getMetaData();
      for (int i = 1; i <= meta.getColumnCount(); ++i) {
	 String nm = meta.getColumnName(i);
	 Object v = rs.getObject(i);
	 if (v instanceof Date) {
	    Date d = (Date) v;
	    v = d.getTime();
	  }
	 else if (v instanceof Timestamp) {
	    Timestamp ts = (Timestamp) v;
	    v = ts.getTime();
	  }
	 if (v != null) rslt.put(nm,v);
       }
    }
   catch (SQLException e) {
      IvyLog.logE("BOWER","Database problem decoding result set ",e);
    }
   
   return rslt;
}



/********************************************************************************/
/*                                                                              */
/*      Pool access methods                                                     */
/*                                                                              */
/********************************************************************************/

private Connection getConnection() 
{
   synchronized (this) {
      for (int i = 0; i < 2 && connection_pool.isEmpty(); ++i) {
         try {
            wait(10000);
          }
         catch (InterruptedException e) { }
       }
    }
   while (connection_pool.isEmpty()) {
      if (used_connections.size() >= MAX_POOL_SIZE) {
         IvyLog.logE("BOWER","Too many connections in use " +
               used_connections.size());
       }
      try {
         connection_pool.add(IvyDatabase.openDatabase(database_name));
         break;
       }
      catch (SQLException e) {
         IvyLog.logE("BOWER","Unable to open connection");
       }
      try {
         Thread.sleep(1000);
       }
      catch (InterruptedException e) { }
    }
  
   Connection c = connection_pool.remove(connection_pool.size() - 1);
   used_connections.add(c);
   
   return c;
}


private void releaseConnection(Connection c)
{
   if (c == null) return;
   synchronized (this) {
      connection_pool.add(c);
      notifyAll();
    }
   used_connections.remove(c);
}


private Connection checkDatabaseError(Connection c,SQLException e)
{
   boolean rem = false;
   String msg = e.getMessage();
   if (msg.contains("FATAL")) rem = true;
   Throwable ex = e.getCause();
   if (ex instanceof IOException) rem = true;
   if (rem) {
      IvyLog.logE("BOWER","Database lost connection",e);
      used_connections.remove(c);
      c = null;
    }
   return c;
}




}       // end of class BowerDatabasePool




/* end of BowerDatabasePool.java */

