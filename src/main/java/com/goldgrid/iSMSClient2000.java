package com.goldgrid;
import java.lang.*;
import java.net.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class iSMSClient2000 {
  private Socket ClientSocket;
  private DataInputStream is;
  private DataOutputStream os;

  public String Error;
  public String Option;
  public String Record;
  public String Mobile;
  public String Content;
  public String DateTime;

  public iSMSClient2000() {
    Error = "";
    Option = "";
    Record = "";
    Mobile = "";
    Content = "";
    DateTime = "";
  }

  public boolean OpenSMS(String mServer,int mPort){
    String mBuffer="";
    boolean mResult=false;
    try{
      if (mServer==""){
        mServer="localhost";
      }
      if (mPort==0){
        mPort=8090;
      }
      ClientSocket = new Socket(mServer,mPort);
      is = new DataInputStream(new BufferedInputStream(ClientSocket.getInputStream()));
      os= new DataOutputStream(new BufferedOutputStream(ClientSocket.getOutputStream()));

      mBuffer=is.readLine();
      if (mBuffer.equalsIgnoreCase("OK")){
        os.write(("SMS\r\n").getBytes());
        mResult = true;
      }
    }
    catch(Exception e)
    {
      mResult = false;
    }
    return(mResult);
  }


  public boolean SendSMS(String mRecord,String mMobile,String mContent){
    boolean mResult=false;
    byte[] bRecord;
    byte[] bMobile;
    byte[] bContent;
    byte[] bFlag;
    try{
      bRecord = (mRecord + "\r\n" ).getBytes() ;
      bMobile = (mMobile + "\r\n" ).getBytes() ;
      bContent =(mContent + "\r\n").getBytes();
      bFlag = new byte[3];
      bFlag[0]=0x1A;
      bFlag[1]='\r';
      bFlag[2]='\n';

      os.write(bFlag);
      os.write(bRecord);
      os.write(bMobile);
      os.write(bContent);
	  //os.writeUTF(mContent);
      os.write(bFlag);
      os.flush();

      Error=is.readLine();
      if (Error.equalsIgnoreCase("OK")){
        mResult = true;
      }else{
        mResult = false;
      }
    }
    catch(Exception e)
    {
      mResult = false;
    }
    return(mResult);
  }

  public String ReadSMS(){
    String mResult="";
    byte[] bFlag;
    try{
      bFlag = new byte[3];
      bFlag[0]=0x1B;
      bFlag[1]='\r';
      bFlag[2]='\n';

      os.write(bFlag);
      os.flush();
      mResult=is.readLine();
      Option=is.readLine();
      Record=is.readLine();
      Mobile=is.readLine();
      Content=is.readLine();
	  //Content=is.readUTF();
      DateTime=is.readLine();
      Error=is.readLine();
      if (Error.equalsIgnoreCase("OK")){
        mResult = mResult;
      }else{
        mResult = "0";
      }
    }
    catch(Exception e)
    {
      mResult = "0";
    }
    return(mResult);
  }

  public boolean CloseSMS(){
    boolean mResult=false;
    try
    {
      os.write(("END\r\n").getBytes());
      ClientSocket.close();
      mResult = true;
    }
    catch(Exception e)
    {
      mResult = false;
    }
    return(mResult);
  }

}
