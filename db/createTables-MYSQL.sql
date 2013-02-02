--
--    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
--    @author Matthew Lohbihler
--

-- Make sure that everything get created with utf8 as the charset.
alter database default character set utf8;

--
-- System settings
create table systemSettings (
  settingName varchar(64) not null,
  settingValue longtext,
  primary key (settingName)
) engine=InnoDB;


--
-- Users
create table users (
  id int not null auto_increment,
  username varchar(40) not null,
  password varchar(30) not null,
  email varchar(255) not null,
  phone varchar(40),
  admin char(1) not null,
  disabled char(1) not null,
  lastLogin bigint,
  homeUrl varchar(255),
  receiveAlarmEmails int not null,
  receiveOwnAuditEvents char(1) not null,
  timezone varchar(50),
  primary key (id)
) engine=InnoDB;

create table userComments (
  userId int,
  commentType int not null,
  typeKey int not null,
  ts bigint not null,
  commentText varchar(1024) not null
) engine=InnoDB;
alter table userComments add constraint userCommentsFk1 foreign key (userId) references users(id);


--
-- Mailing lists
create table mailingLists (
  id int not null auto_increment,
  xid varchar(50) not null,
  name varchar(40) not null,
  primary key (id)
) engine=InnoDB;
alter table mailingLists add constraint mailingListsUn1 unique (xid);

create table mailingListInactive (
  mailingListId int not null,
  inactiveInterval int not null
) engine=InnoDB;
alter table mailingListInactive add constraint mailingListInactiveFk1 foreign key (mailingListId) 
  references mailingLists(id) on delete cascade;

create table mailingListMembers (
  mailingListId int not null,
  typeId int not null,
  userId int,
  address varchar(255)
) engine=InnoDB;
alter table mailingListMembers add constraint mailingListMembersFk1 foreign key (mailingListId) 
  references mailingLists(id) on delete cascade;




--
--
-- Data Sources
--
create table dataSources (
  id int not null auto_increment,
  xid varchar(50) not null,
  name varchar(40) not null,
  dataSourceType varchar(40) not null,
  data longblob not null,
  rtdata longblob,
  primary key (id)
) engine=InnoDB;
alter table dataSources add constraint dataSourcesUn1 unique (xid);


-- Data source permissions
create table dataSourceUsers (
  dataSourceId int not null,
  userId int not null
) engine=InnoDB;
alter table dataSourceUsers add constraint dataSourceUsersFk1 foreign key (dataSourceId) references dataSources(id);
alter table dataSourceUsers add constraint dataSourceUsersFk2 foreign key (userId) references users(id) on delete cascade;



--
--
-- Data Points
--
create table dataPoints (
  id int not null auto_increment,
  xid varchar(50) not null,
  dataSourceId int not null,
  name varchar(255),
  deviceName varchar(255),
  enabled char(1),
  pointFolderId int,
  loggingType int,
  intervalLoggingPeriodType int,
  intervalLoggingPeriod int,
  intervalLoggingType int,
  tolerance double,
  purgeOverride char(1),
  purgeType int,
  purgePeriod int,
  defaultCacheSize int,
  discardExtremeValues char(1),
  engineeringUnits int,
  data longblob not null,
  primary key (id)
) engine=InnoDB;
alter table dataPoints add constraint dataPointsUn1 unique (xid);
alter table dataPoints add constraint dataPointsFk1 foreign key (dataSourceId) references dataSources(id);


-- Data point hierarchy
CREATE TABLE dataPointHierarchy (
  id int NOT NULL auto_increment,
  parentId int,
  name varchar(100),
  PRIMARY KEY (id)
) engine=InnoDB;


-- Data point permissions
create table dataPointUsers (
  dataPointId int not null,
  userId int not null,
  permission int not null
) engine=InnoDB;
alter table dataPointUsers add constraint dataPointUsersFk1 foreign key (dataPointId) references dataPoints(id);
alter table dataPointUsers add constraint dataPointUsersFk2 foreign key (userId) references users(id) on delete cascade;


--
--
-- Point Values (historical data)
--
create table pointValues (
  id bigint not null auto_increment,
  dataPointId int not null,
  dataType int not null,
  pointValue double,
  ts bigint not null,
  primary key (id)
) engine=MyISAM;
create index pointValuesIdx1 on pointValues (dataPointId, ts);

create table pointValueAnnotations (
  pointValueId bigint not null,
  textPointValueShort varchar(128),
  textPointValueLong longtext,
  sourceMessage longtext,
  primary key (pointValueId)
) engine=MyISAM;


--
--
-- Point event detectors
--
create table pointEventDetectors (
  id int not null auto_increment,
  xid varchar(50) not null,
  alias varchar(255),
  dataPointId int not null,
  detectorType int not null,
  alarmLevel int not null,
  stateLimit double,
  duration int,
  durationType int,
  binaryState char(1),
  multistateState int,
  changeCount int,
  alphanumericState varchar(128),
  weight double,
  primary key (id)
) engine=InnoDB;
alter table pointEventDetectors add constraint pointEventDetectorsUn1 unique (xid, dataPointId);
alter table pointEventDetectors add constraint pointEventDetectorsFk1 foreign key (dataPointId) 
  references dataPoints(id);


--
--
-- Events
--
create table events (
  id int not null auto_increment,
  typeName varchar(32) not null,
  subtypeName varchar(32),
  typeRef1 int not null,
  typeRef2 int not null,
  activeTs bigint not null,
  rtnApplicable char(1) not null,
  rtnTs bigint,
  rtnCause int,
  alarmLevel int not null,
  message longtext,
  ackTs bigint,
  ackUserId int,
  alternateAckSource longtext,
  primary key (id)
) engine=InnoDB;
alter table events add constraint eventsFk1 foreign key (ackUserId) references users(id);

create table userEvents (
  eventId int not null,
  userId int not null,
  silenced char(1) not null,
  primary key (eventId, userId)
) engine=InnoDB;
alter table userEvents add constraint userEventsFk1 foreign key (eventId) references events(id) on delete cascade;
alter table userEvents add constraint userEventsFk2 foreign key (userId) references users(id);


--
--
-- Event handlers
--
create table eventHandlers (
  id int not null auto_increment,
  xid varchar(50) not null,
  alias varchar(255),
  
  -- Event type, see events
  eventTypeName varchar(32) not null,
  eventSubtypeName varchar(32),
  eventTypeRef1 int not null,
  eventTypeRef2 int not null,
  
  data longblob not null,
  primary key (id)
) engine=InnoDB;
alter table eventHandlers add constraint eventHandlersUn1 unique (xid);


--
--
-- Publishers
--
create table publishers (
  id int not null auto_increment,
  xid varchar(50) not null,
  publisherType varchar(40) not null,
  data longblob not null,
  rtdata longblob,
  primary key (id)
) engine=InnoDB;
alter table publishers add constraint publishersUn1 unique (xid);

CREATE TABLE `dayenergypointvalue` (
  `id` bigint(20) NOT NULL auto_increment,
  `dataPointId` int(11) NOT NULL,
  `dayValue` double(20,4) default NULL,
  `ts` bigint(20) default NULL,
  `xid` varchar(10) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB;
CREATE TABLE `meter_item` (
  `id` int(11) NOT NULL auto_increment,
  `code` varchar(10) default NULL,
  `name` varchar(32) default NULL,
  `parentId` int(11) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB;

-- ----------------------------
-- Records
-- ----------------------------
INSERT INTO `meter_item` VALUES ('1', '10', '江苏卓易', '0');
INSERT INTO `meter_item` VALUES ('2', '20', '江苏环保', '0');
INSERT INTO `meter_item` VALUES ('3', '30', '宜兴环科园', '0');
INSERT INTO `meter_item` VALUES ('4', '1001', '机柜1', '1');
INSERT INTO `meter_item` VALUES ('5', '1002', '机柜2', '1');
INSERT INTO `meter_item` VALUES ('6', '2001', '机柜3', '2');
INSERT INTO `meter_item` VALUES ('7', '100101', '仪表1', '4');
INSERT INTO `meter_item` VALUES ('8', '100102', '仪表2', '4');
INSERT INTO `meter_item` VALUES ('9', '200101', '仪表3', '6');

CREATE TABLE `hourpowerpointvalue` (
  `id` bigint(20) NOT NULL auto_increment,
  `dataPointId` int(11) NOT NULL,
  `dayValue` double(20,4) default NULL,
  `ts` datetime default NULL,
  `xid` varchar(10) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB;
CREATE TABLE `daypowerpointvalue` (
  `id` bigint(20) NOT NULL auto_increment,
  `dataPointId` int(11) NOT NULL,
  `dayValue` double(20,4) default NULL,
  `ts` date default NULL,
  `xid` varchar(10) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB;