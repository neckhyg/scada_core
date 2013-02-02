package com.serotonin.eazytec.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RejectedExecutionException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.RowMapper;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.DatabaseProxy.DatabaseType;
import com.serotonin.m2m2.db.dao.BaseDao;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.rt.maint.work.WorkItem;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.monitor.IntegerMonitor;
import com.serotonin.util.queue.ObjectQueue;

public class DayPowerPointValueDao extends BaseDao{
	private static final String DAY_ENERGY_POINT_VALUE_INSERT_START = "insert into dayPowerPointValue (dataPointId,  dayValue, ts,xid) values ";
	private static final String DAY_ENERGY_POINT_VALUE_INSERT_VALUES = "(?,?,?,?)";
	private static final int DAY_ENERGY_POINT_VALUE_INSERT_VALUES_COUNT = 4;
	
	public DayPowerPointValueDao() {
		super();
	}

	public DayPowerPointValueDao(DataSource dataSource) {
		//super(dataSource);
	}
	
	public long savePointValue(final int pointId, final int dataType, double dvalue,
			final long time, final String svalue, final SetPointSource source,
			boolean async) {
		// Apply database specific bounds on double values.
	//	dvalue = DatabaseAccess.getDatabaseAccess().applyBounds(dvalue);
		 dvalue = Common.databaseProxy.applyBounds(dvalue);
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date dt = new Date(time ); 
        java.util.Date currentDay = new Date();
 //       int hours  = currentDay.getHours()-1;
        String sDateTime = sdf.format(dt);  //得到精确到秒的表示：08/31/2006 21:08:00
        String currentDayStr = sdf.format(currentDay);  //得到精确到秒的表示：08/31/2006 21:08:00
        currentDayStr = currentDayStr.substring(0,10);
     //   currentDayStr += hours;
        currentDayStr += " 23:59:00";
        
//        System.out.println(" sDateTime = "+ sDateTime+ "currentDayStr = "+currentDayStr);
       	SimpleDateFormat df2=new SimpleDateFormat("yyyy-MM-dd HH:mm");
       	Date dateTime = null;
       	Date writeDate = null;
       	try{
       		dateTime = (Date)df2.parse(sDateTime);
       		writeDate = (Date)df2.parse(currentDayStr);
           // System.out.println(" dateTime = "+ dateTime+ "writeDate = "+writeDate);       		
       	}catch(Exception e){
       		
       		e.printStackTrace();
       	}
    	
       	if (  writeDate.getTime() > dateTime.getTime() + 15*60*1000 ){
       		
       	//	System.out.println(" dateTime = "+ dateTime.getTime() + "writeDate = "+ writeDate.getTime());
       		
       		return 0;
       	}
       		
		 DataPointDao dataPointDao = new DataPointDao();
		 
		 DataPointVO dataPointVo = dataPointDao.getDataPoint(pointId);
			
		 System.out.println("First Xid ="+dataPointVo.getXid() + "First DataSourceXid = " + dataPointVo.getDataSourceXid());		

          String xid = dataPointVo.getXid();
       
		if (async) {
//			BatchWriteBehind.add(new BatchWriteBehindEntry(pointId, dataType,
//					dvalue, time), ejt);
			
			BatchWriteBehind.add(new BatchWriteBehindEntry(pointId, 
					dvalue, dateTime,xid), ejt);
			
			return -1;
		}

		int retries = 5;
		while (true) {
			try {
				return savePointValueImpl(pointId, dataType, dvalue, time,
						svalue, source);
			} catch (ConcurrencyFailureException e) {
				if (retries <= 0)
					throw e;
				retries--;
			} catch (RuntimeException e) {
				throw new RuntimeException(
						"Error saving point value: dataType=" + dataType
								+ ", dvalue=" + dvalue, e);
			}
		}
	}

	private long savePointValueImpl(int pointId, int dataType, double dvalue,
			long time, String svalue, SetPointSource source) {
/*		
		long id = doInsertLong(DAY_ENERGY_POINT_VALUE_INSERT_START, new Object[] { pointId,
				dataType, dvalue, time });

		if (svalue == null && dataType == DataTypes.IMAGE)
			svalue = Long.toString(id);

		// Check if we need to create an annotation.
		if (svalue != null || source != null) {
			Integer sourceType = null, sourceId = null;
			if (source != null) {
				sourceType = source.getSetPointSourceType();
				sourceId = source.getSetPointSourceId();
			}

			String shortString = null;
			String longString = null;
			if (svalue != null) {
				if (svalue.length() > 128)
					longString = svalue;
				else
					shortString = svalue;
			}

//			ejt.update(POINT_VALUE_ANNOTATION_INSERT, new Object[] { id,
//					shortString, longString, sourceType, sourceId }, new int[] {
//					Types.INTEGER, Types.VARCHAR, Types.CLOB, Types.SMALLINT,
//					Types.INTEGER });
		}

		return id;
		*/
        long id = doInsertLong(DAY_ENERGY_POINT_VALUE_INSERT_START, new Object[]{pointId, dataType, dvalue, time});

        if (svalue == null && dataType == DataTypes.IMAGE)
            svalue = Long.toString(id);

        // Check if we need to create an annotation.
        TranslatableMessage sourceMessage = null;
        if (source != null)
            sourceMessage = source.getSetPointSourceMessage();

        if (svalue != null || sourceMessage != null) {
            String shortString = null;
            String longString = null;
            if (svalue != null) {
                if (svalue.length() > 128)
                    longString = svalue;
                else
                    shortString = svalue;
            }

//            ejt.update(POINT_VALUE_ANNOTATION_INSERT, //
//                    new Object[]{id, shortString, longString, writeTranslatableMessage(sourceMessage)}, //
//                    new int[]{Types.INTEGER, Types.VARCHAR, Types.CLOB, Types.CLOB});
        }

        return id;			
	}

	DataValue createBaseValue(ResultSet rs)
	throws SQLException {
		
		DataValue value;

		value = new NumericValue(rs.getDouble(1));
	
		return value;
	}	
	
	class dayPowerPointValueRowMapper implements RowMapper<PointValueTime> {
	public PointValueTime mapRow(ResultSet rs, int rowNum)
			throws SQLException {
			DataValue value = new NumericValue(rs.getDouble(1));
         //             rs.get
			//Date date = rs.getTime(2);
			Date date = rs.getTimestamp(2);
			return new PointValueTime(value,date.getTime());
			
	}
}
	
	private List<PointValueTime> pointDayEnergyValuesQuery(String sql, Object[] params,
	int limit) {
				List<PointValueTime> result = query(sql, params,
				new dayPowerPointValueRowMapper(), limit);
				//updateAnnotations(result);
				return result;
	}
	
	public List<PointValueTime> getdayPowerPointValuesBetween(int dataPointId,long from, long to) {
			return pointDayEnergyValuesQuery(
			DAY_ENERGY_POINT_VALUE_SELECT
			+ " where pv.dataPointId=? and pv.ts >= ? and pv.ts<? order by ts",
			new Object[] { dataPointId, from, to }, 0);
	}
	public List<PointValueTime> getdayPowerPointValuesBetween(String xid,long from, long to) {
		
//		String sql = DAY_ENERGY_POINT_SUM_VALUE_SELECT  +  "where xid like  '"+xid+ "%' and pv.ts >= " + from + " and pv.ts< " + 
//		              to +  " group by left(ts,10)"; 
		String sql = DAY_ENERGY_POINT_SUM_VALUE_SELECT  +  "where xid like  '"+xid+ "%' and pv.ts >= " + from + " and pv.ts< " + 
        to +  " group by ts "; 		
		return pointDayEnergyValuesQuery( sql,new Object[] {}, 0);
}
	public List<PointValueTime> getdayPowerPointValuesBetween(String xid,Date from, Date to) {
		
//		String sql = DAY_ENERGY_POINT_SUM_VALUE_SELECT  +  "where xid like  '"+xid+ "%' and pv.ts >= '" + from + "' and pv.ts< '" + 
//		              to +  "' group by left(ts,10)"; 
		String sql = DAY_ENERGY_POINT_SUM_VALUE_SELECT  +  "where xid like  '"+xid+ "%' and pv.ts >= '" + from + "' and pv.ts< '" + 
	              to +  "' group by ts "; 		
		return pointDayEnergyValuesQuery( sql,new Object[] {}, 0);
}	
	public List<PointValueTime> getdayPowerPointValuesBetween(String xid,String from, String to) {
		
		String sql = DAY_ENERGY_POINT_SUM_VALUE_SELECT  +  "where xid like  '"+xid+ "%' and pv.ts >= '" + from + "' and pv.ts< '" + 
		              to +  "' group by left(ts,10)"; 
//		String sql = DAY_ENERGY_POINT_SUM_VALUE_SELECT  +  "where xid like  '"+xid+ "%' and pv.ts >= '" + from + "' and pv.ts< '" + 
//	              to +  "' group by ts "; 		
		return pointDayEnergyValuesQuery( sql,new Object[] {}, 0);
}	
	//////////////////////////////////////////////////////
	//select sum(dayValue) as dd,  ts as day from dayPowerPointValue where xid like 'DP_2001%' group by left(ts,13);
//	private static final String DAY_ENERGY_POINT_SUM_VALUE_SELECT = "select  sum(pv.dayValue) , pv.ts from dayPowerPointValue pv ";
	private static final String DAY_ENERGY_POINT_SUM_VALUE_SELECT = "select  sum(pv.dayValue) , min(pv.ts) from dayPowerPointValue pv ";	
	private static final String DAY_ENERGY_POINT_VALUE_SELECT = "select  pv.dayValue , pv.ts from dayPowerPointValue pv ";


	
	public List<PointValueTime> getdayPowerPointValueList(String xid) {
	//	String sql = DAY_ENERGY_POINT_SUM_VALUE_SELECT + "where xid like  '"+xid+ "%' group by left(ts,16) ";
		String sql = DAY_ENERGY_POINT_SUM_VALUE_SELECT + "where xid like  '"+xid+ "%' group by ts ";		
		List<PointValueTime> result = query(sql, new Object[] {},
		new dayPowerPointValueRowMapper(), 0);
		//updateAnnotations(result);
		return result;		
	//	return null;
	}
		////////////////////////////////////////////
		class BatchWriteBehindEntry {
			private final int pointId;
			//private final int dataType;
			private final double dvalue;
			private final Date date;
			private final String xid;
			
			public BatchWriteBehindEntry(int pointId,  double dvalue,
			Date date, String xid) {
			this.pointId = pointId;			
			this.dvalue = dvalue;
			this.date = date;
			this.xid = xid;
		}
		
		public void writeInto(Object[] params, int index) {
				index *= DAY_ENERGY_POINT_VALUE_INSERT_VALUES_COUNT;
				params[index++] = pointId;			
				params[index++] = dvalue;
				params[index++] = date;
				params[index++] = xid;													
			}
		}
/*		
		static class BatchWriteBehind implements WorkItem {
			private static final ObjectQueue<BatchWriteBehindEntry> ENTRIES = new ObjectQueue<DayPowerPointValueDao.BatchWriteBehindEntry>();
			private static final CopyOnWriteArrayList<BatchWriteBehind> instances = new CopyOnWriteArrayList<BatchWriteBehind>();
			private static Log LOG = LogFactory.getLog(BatchWriteBehind.class);
			private static final int SPAWN_THRESHOLD = 10000;
			private static final int MAX_INSTANCES = 5;
			private static int MAX_ROWS = 1000;
			private static final IntegerMonitor ENTRIES_MONITOR = new IntegerMonitor(
			"BatchWriteBehind.ENTRIES_MONITOR", null);
			private static final IntegerMonitor INSTANCES_MONITOR = new IntegerMonitor(
			"BatchWriteBehind.INSTANCES_MONITOR", null);
			
			static {
				if (Common.ctx.getDatabaseAccess().getType() == DatabaseAccess.DatabaseType.DERBY)
				// This has not bee tested to be optimal
				MAX_ROWS = 1000;
				else if (Common.ctx.getDatabaseAccess().getType() == DatabaseAccess.DatabaseType.MSSQL)
				// MSSQL has max rows of 1000, and max parameters of 2100. In
				// this case that works out to...
				MAX_ROWS = 524;
				else if (Common.ctx.getDatabaseAccess().getType() == DatabaseAccess.DatabaseType.MYSQL)
				// This appears to be an optimal value
				MAX_ROWS = 2000;
				else
				throw new ShouldNeverHappenException("Unknown database type: "
				+ Common.ctx.getDatabaseAccess().getType());
				
				Common.MONITORED_VALUES.addIfMissingStatMonitor(ENTRIES_MONITOR);
				Common.MONITORED_VALUES.addIfMissingStatMonitor(INSTANCES_MONITOR);
			}
			
		static void add(BatchWriteBehindEntry e, ExtendedJdbcTemplate ejt) {
			synchronized (ENTRIES) {
				ENTRIES.push(e);
				ENTRIES_MONITOR.setValue(ENTRIES.size());
				if (ENTRIES.size() > instances.size() * SPAWN_THRESHOLD) {
					if (instances.size() < MAX_INSTANCES) {
					BatchWriteBehind bwb = new BatchWriteBehind(ejt);
					instances.add(bwb);
					INSTANCES_MONITOR.setValue(instances.size());
						try {
						Common.ctx.getBackgroundProcessing().addWorkItem(
						bwb);
						} catch (RejectedExecutionException ree) {
						instances.remove(bwb);
						INSTANCES_MONITOR.setValue(instances.size());
						throw ree;
						}
					}
				}						
			}
		}
		
		private final ExtendedJdbcTemplate ejt;
		
		public BatchWriteBehind(ExtendedJdbcTemplate ejt) {
			this.ejt = ejt;
		}
		
		public void execute() {
					
			try {
			BatchWriteBehindEntry[] inserts;
				while (true) {
					synchronized (ENTRIES) {
					if (ENTRIES.size() == 0)
					break;
					
					inserts = new BatchWriteBehindEntry[ENTRIES.size() < MAX_ROWS ? ENTRIES
					.size() : MAX_ROWS];
					ENTRIES.pop(inserts);
					ENTRIES_MONITOR.setValue(ENTRIES.size());
					}
				
				// Create the sql and parameters
				Object[] params = new Object[inserts.length
				* DAY_ENERGY_POINT_VALUE_INSERT_VALUES_COUNT];
				StringBuilder sb = new StringBuilder();
				sb.append(DAY_ENERGY_POINT_VALUE_INSERT_START);
					for (int i = 0; i < inserts.length; i++) {
					if (i > 0)
					sb.append(',');
					sb.append(DAY_ENERGY_POINT_VALUE_INSERT_VALUES);
					inserts[i].writeInto(params, i);
					}
				
				// Insert the data
				int retries = 10;
					while (true) {
					try {
					ejt.update(sb.toString(), params);
					break;
					} catch (ConcurrencyFailureException e) {
						if (retries <= 0) {
						LOG.error("Concurrency failure saving "
							+ inserts.length
							+ " batch inserts after 10 tries. Data lost.");
						break;
						}
					
					int wait = (10 - retries) * 100;
						try {
							if (wait > 0) {
								synchronized (this) {
									wait(wait);
								}
							}
						} catch (InterruptedException ie) {
						// no op
						}
					
						retries--;
						} catch (RuntimeException e) {
						LOG.error("Error saving " + inserts.length
						+ " batch inserts. Data lost.", e);
						break;
						}
					}
				}
			} finally {
			instances.remove(this);
			INSTANCES_MONITOR.setValue(instances.size());
			}
		}
		
		public int getPriority() {
			return WorkItem.PRIORITY_HIGH;
			}
		}	
		
		*/
	    public static final String ENTRIES_MONITOR_ID = BatchWriteBehind.class.getName() + ".ENTRIES_MONITOR";
	    public static final String INSTANCES_MONITOR_ID = BatchWriteBehind.class.getName() + ".INSTANCES_MONITOR";

	    static class BatchWriteBehind implements WorkItem {
	        private static final ObjectQueue<BatchWriteBehindEntry> ENTRIES = new ObjectQueue<DayPowerPointValueDao.BatchWriteBehindEntry>();
	        private static final CopyOnWriteArrayList<BatchWriteBehind> instances = new CopyOnWriteArrayList<BatchWriteBehind>();
	        private static Log LOG = LogFactory.getLog(BatchWriteBehind.class);
	        private static final int SPAWN_THRESHOLD = 10000;
	        private static final int MAX_INSTANCES = 5;
	        private static int MAX_ROWS = 1000;
	        private static final IntegerMonitor ENTRIES_MONITOR = new IntegerMonitor(ENTRIES_MONITOR_ID,
	                "internal.monitor.BATCH_ENTRIES");
	        private static final IntegerMonitor INSTANCES_MONITOR = new IntegerMonitor(INSTANCES_MONITOR_ID,
	                "internal.monitor.BATCH_INSTANCES");

	        private static List<Class<? extends RuntimeException>> retriedExceptions = new ArrayList<Class<? extends RuntimeException>>();

	        static {
	            if (Common.databaseProxy.getType() == DatabaseType.DERBY)
	                // This has not been tested to be optimal
	                MAX_ROWS = 1000;
	            else if (Common.databaseProxy.getType() == DatabaseType.MSSQL)
	                // MSSQL has max rows of 1000, and max parameters of 2100. In this case that works out to...
	                MAX_ROWS = 524;
	            else if (Common.databaseProxy.getType() == DatabaseType.MYSQL)
	                // This appears to be an optimal value
	                MAX_ROWS = 2000;
	            else
	                throw new ShouldNeverHappenException("Unknown database type: " + Common.databaseProxy.getType());

	            Common.MONITORED_VALUES.addIfMissingStatMonitor(ENTRIES_MONITOR);
	            Common.MONITORED_VALUES.addIfMissingStatMonitor(INSTANCES_MONITOR);

	            retriedExceptions.add(RecoverableDataAccessException.class);
	            retriedExceptions.add(TransientDataAccessException.class);
	            retriedExceptions.add(TransientDataAccessResourceException.class);
	            retriedExceptions.add(CannotGetJdbcConnectionException.class);
	        }

	        static void add(BatchWriteBehindEntry e, ExtendedJdbcTemplate ejt) {
	            synchronized (ENTRIES) {
	                ENTRIES.push(e);
	                ENTRIES_MONITOR.setValue(ENTRIES.size());
	                if (ENTRIES.size() > instances.size() * SPAWN_THRESHOLD) {
	                    if (instances.size() < MAX_INSTANCES) {
	                        BatchWriteBehind bwb = new BatchWriteBehind(ejt);
	                        instances.add(bwb);
	                        INSTANCES_MONITOR.setValue(instances.size());
	                        try {
	                            Common.backgroundProcessing.addWorkItem(bwb);
	                        } catch (RejectedExecutionException ree) {
	                            instances.remove(bwb);
	                            INSTANCES_MONITOR.setValue(instances.size());
	                            throw ree;
	                        }
	                    }
	                }
	            }
	        }

	        private final ExtendedJdbcTemplate ejt;

	        public BatchWriteBehind(ExtendedJdbcTemplate ejt) {
	            this.ejt = ejt;
	        }

	        public void execute() {
	            try {
	                BatchWriteBehindEntry[] inserts;
	                while (true) {
	                    synchronized (ENTRIES) {
	                        if (ENTRIES.size() == 0)
	                            break;

	                        inserts = new BatchWriteBehindEntry[ENTRIES.size() < MAX_ROWS ? ENTRIES.size() : MAX_ROWS];
	                        ENTRIES.pop(inserts);
	                        ENTRIES_MONITOR.setValue(ENTRIES.size());
	                    }

	                    // Create the sql and parameters
	                    Object[] params = new Object[inserts.length * DAY_ENERGY_POINT_VALUE_INSERT_VALUES_COUNT];
	                    StringBuilder sb = new StringBuilder();
	                    sb.append(DAY_ENERGY_POINT_VALUE_INSERT_START);
	                    for (int i = 0; i < inserts.length; i++) {
	                        if (i > 0)
	                            sb.append(',');
	                        sb.append(DAY_ENERGY_POINT_VALUE_INSERT_VALUES);
	                        inserts[i].writeInto(params, i);
	                    }

	                    // Insert the data
	                    int retries = 10;
	                    while (true) {
	                        try {
	                            ejt.update(sb.toString(), params);
	                            break;
	                        } catch (RuntimeException e) {
	                            if (retriedExceptions.contains(e.getClass())) {
	                                if (retries <= 0) {
	                                    LOG.error("Concurrency failure saving " + inserts.length
	                                            + " batch inserts after 10 tries. Data lost.");
	                                    break;
	                                }

	                                int wait = (10 - retries) * 100;
	                                try {
	                                    if (wait > 0) {
	                                        synchronized (this) {
	                                            wait(wait);
	                                        }
	                                    }
	                                } catch (InterruptedException ie) {
	                                    // no op
	                                }

	                                retries--;
	                            } else {
	                                LOG.error("Error saving " + inserts.length + " batch inserts. Data lost.", e);
	                                break;
	                            }
	                        }
	                    }
	                }
	            } finally {
	                instances.remove(this);
	                INSTANCES_MONITOR.setValue(instances.size());
	            }
	        }

	        public int getPriority() {
	            return WorkItem.PRIORITY_HIGH;
	        }
	    }		
}
