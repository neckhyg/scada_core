package com.goldgrid;

import java.io.PrintStream;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class iDBManager2000
{
    public String ClassString;
    public String ConnectionString;
    public String UserName;
    public String PassWord;
    public Connection Conn;
    public Statement Stmt;

    public iDBManager2000()
    {
    //For ODBC
    //ClassString="sun.jdbc.odbc.JdbcOdbcDriver";
    //ConnectionString=("jdbc:odbc:DBDemo");
    //UserName="dbdemo";
    //PassWord="dbdemo";


    //For Access Driver
    //ClassString="sun.jdbc.odbc.JdbcOdbcDriver";
    //ConnectionString=("jdbc:odbc:Driver={MicroSoft Access Driver (*.mdb)};DBQ=C:\\DBstep.mdb;ImplicitCommitSync=Yes;MaxBufferSize=512;MaxScanRows=128;PageTimeout=5;SafeTransactions=0;Threads=3;UserCommitSync=Yes;").replace('\\','/');

    //For SQLServer Driver
/*    ClassString="com.microsoft.sqlserver.jdbc.SQLServerDriver";
    ConnectionString="jdbc:microsoft:sqlserver://127.0.0.1:3306;DatabaseName=scada_db;User=root;Password=";
    UserName="iSMSDemo";
    PassWord="iSMSDemo";*/

    //For Oracle Driver
    //ClassString="oracle.jdbc.driver.OracleDriver";
    //ConnectionString="jdbc:oracle:thin:@hstation:1521:dbstep";
    //UserName="system";
    //PassWord="manager";

    //For MySQL Driver
    //ClassString="org.gjt.mm.mysql.Driver";
    //ConnectionString="jdbc:mysql://localhost/softforum?user=...&password=...&useUnicode=true&characterEncoding=8859_1";
    ClassString="com.mysql.jdbc.Driver";
    ConnectionString="jdbc:mysql://localhost/scada_db?user=root&password=&useUnicode=true&characterEncoding=utf-8";

    //For Sybase Driver
    //ClassString="com.sybase.jdbc.SybDriver";
    //ConnectionString="jdbc:sybase:Tds:localhost:5007/tsdata"; //tsdataΪ�����ݿ���
    //Properties sysProps = System.getProperties();
    //SysProps.put("user","userid");
    //SysProps.put("password","user_password");
    //If using Sybase then DriverManager.getConnection(ConnectionString,sysProps);

    }

    public boolean OpenConnection()
    {
        boolean mResult = true;
        try
        {
            Class.forName(ClassString);
            if(UserName == null && PassWord == null)
                Conn = DriverManager.getConnection(ConnectionString);
            else
                Conn = DriverManager.getConnection(ConnectionString, UserName, PassWord);
            Stmt = Conn.createStatement();
            mResult = true;
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
            mResult = false;
        }
        return mResult;
    }

    public void CloseConnection()
    {
        try
        {
            Stmt.close();
            Conn.close();
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }
    }

    public String GetDateTime()
    {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String mDateTime = formatter.format(cal.getTime());
        return mDateTime;
    }

    public Date GetDate()
    {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String mDateTime = formatter.format(cal.getTime());
        return Date.valueOf(mDateTime);
    }

    public String MarkText(String str)
    {
        String oldStr = "'";
        String newStr = "''";
        int n = 0;
        if(str.indexOf(oldStr) > -1)
            while(str.indexOf(oldStr, n) > -1) 
            {
                int i = str.length();
                if(str.indexOf(oldStr, n) == 0)
                {
                    str = String.valueOf(newStr) + String.valueOf(str.substring(1, i));
                    n = 2;
                } else
                {
                    int t = str.indexOf(oldStr, n);
                    str = String.valueOf(String.valueOf((new StringBuffer(String.valueOf(String.valueOf(str.substring(0, t))))).append(newStr).append(str.substring(t + 1, i))));
                    n = t + 2;
                }
            }
        return str;
    }

    public int GetMaxID(String vTableName, String vFieldName)
    {
        int mResult = 0;
        boolean mConn = true;
        String mSql = new String();
        mSql = String.valueOf(String.valueOf((new StringBuffer("select max(")).append(vFieldName).append(")+1 as MaxID from ").append(vTableName)));
        try
        {
            if(Conn != null)
                mConn = Conn.isClosed();
            if(mConn)
                OpenConnection();
            ResultSet result = ExecuteQuery(mSql);
            if(result.next())
                mResult = result.getInt("MaxID");
            result.close();
            if(mConn)
                CloseConnection();
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }
        return mResult;
    }

    public ResultSet ExecuteQuery(String SqlString)
    {
        ResultSet result = null;
        try
        {
            result = Stmt.executeQuery(SqlString);
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }
        return result;
    }

    public int ExecuteUpdate(String SqlString)
    {
        int result = 0;
        try
        {
            result = Stmt.executeUpdate(SqlString);
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }
        return result;
    }
}