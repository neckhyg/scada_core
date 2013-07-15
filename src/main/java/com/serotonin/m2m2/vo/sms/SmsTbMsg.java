package com.serotonin.m2m2.vo.sms;

import java.util.Date;

public class SmsTbMsg {
	
 public String getSms_msg_no() {
		return sms_msg_no;
	}
	public void setSms_msg_no(String sms_msg_no) {
		this.sms_msg_no = sms_msg_no;
	}
	public String getFrom_mobile() {
		return from_mobile;
	}
	public void setFrom_mobile(String from_mobile) {
		this.from_mobile = from_mobile;
	}
	public String getTo_mobile() {
		return to_mobile;
	}
	public void setTo_mobile(String to_mobile) {
		this.to_mobile = to_mobile;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getMsg_status() {
		return msg_status;
	}
	public void setMsg_status(String msg_status) {
		this.msg_status = msg_status;
	}
	public Date getFinish_datetime() {
		return finish_datetime;
	}
	public void setFinish_datetime(Date finish_datetime) {
		this.finish_datetime = finish_datetime;
	}
	public Date getEnter_datetime() {
		return enter_datetime;
	}
	public void setEnter_datetime(Date enter_datetime) {
		this.enter_datetime = enter_datetime;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getRegid() {
		return regid;
	}
	public void setRegid(int regid) {
		this.regid = regid;
	}

 String sms_msg_no;
 String from_mobile;
 String to_mobile;
 String content;
 String msg_status;
 Date finish_datetime;
 Date enter_datetime;
 String ip;
 int regid;
}
