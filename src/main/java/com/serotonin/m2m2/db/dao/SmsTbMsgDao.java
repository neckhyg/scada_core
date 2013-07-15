package com.serotonin.m2m2.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.m2m2.vo.sms.SmsTbMsg;


public class SmsTbMsgDao extends BaseDao {
	final ExtendedJdbcTemplate ejt2 = ejt;
	public int UpdateSms(String strDateTime,String strRecord)
	{
		String sql="Update sms_tb_msg Set msg_status='Y',finish_datetime=? where sms_msg_no=?";
		int res=ejt2.update(sql,new Object[]{strDateTime,strRecord});
		return res;
	}
	
	public List<SmsTbMsg> getSmsList(String datestring,String datestr)
	{
		String mSqlStr="select sms_msg_no,to_mobile,content,msg_status from sms_tb_msg where msg_status='N' and  enter_datetime<? and DATE_FORMAT(substr(enter_datetime,1,10),'%Y-%m-%d')=DATE_FORMAT(?,'%Y-%m-%d')";
		List<SmsTbMsg> smslist=ejt2.query(mSqlStr, new Object[]{datestring,datestr},new SmsTbMsgRowWapper());
		return smslist;
	}

    public int insertSmsRecord(String smsNo, String toMobile, String content, String msgStatus){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(date);
        String sql = "INSERT INTO sms_msg_no (sms_msg_no,to_mobile,content,msg_status,enter_datetime) VALUES (?,?,?,?,?)";
        int result = doInsert(sql,new Object[]{smsNo,toMobile,content,msgStatus,currentTime});
        return result;
    }

	class SmsTbMsgRowWapper implements RowMapper<SmsTbMsg>
	{
		public SmsTbMsg mapRow(ResultSet resultSet, int i) throws SQLException {
			SmsTbMsg stm=new SmsTbMsg();
			stm.setSms_msg_no(resultSet.getString("sms_msg_no"));
			stm.setTo_mobile(resultSet.getString("to_mobile"));
			stm.setContent(resultSet.getString("content"));
			stm.setMsg_status(resultSet.getString("msg_status"));
			return stm;
		}
	}
}
