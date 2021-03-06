// CHECK TABLE : launched and current run for keywords ranking
// to get all the idCheck you want
select * from SERPOSCOPE.check 

// RANK table 
select * from SERPOSCOPE.rank
// to get the results from a specified check 
select * from SERPOSCOPE.rank  where idCheck = 24

// RUN table informations about the run (the dates and the logs)
// you get the idRun from the idCheck in the check group
select * from SERPOSCOPE.run 
// to get the current run
select * from SERPOSCOPE.run where isnull(dateStop)


// Mapping idTarget and idGroup
select * from SERPOSCOPE.target
// get idTargets dedicated to a group and the name (ici www.cdiscount.com )
SELECT idTarget,name FROM SERPOSCOPE.target WHERE idGroup =

// insert a check row
INSERT INTO SERPOSCOPE.check (idGroup,idRun,date) VALUES(?,?,?)

// EVENT TABLE
select * from SERPOSCOPE.event 

// GROUP TABLE (group definition and properties)
select * from SERPOSCOPE.group
// select elements from a defined group (idGroup, name, options)
select * from SERPOSCOPE.group where name in ('CDISCOUNT_TOP_5000') ORDER BY rand()


// Keyword table (name and group affectation)
select * from SERPOSCOPE.keyword

// Option table (algorithm parameters : timeout, sleep, nb of pages fetched)
select * from SERPOSCOPE.option

// PROXY table : list all proxies
select * from SERPOSCOPE.proxy

// killing a running run
update SERPOSCOPE.run set dateStop=now() where idRun = 26

 selectCurrentRun = "select * from SERPOSCOPE.run where isnull(dateStop)";
 runInsertString ="insert into SERPOSCOPE.run (dateStart, dateStop, logs, pid, haveError) VALUES (?,?,?,?,?)";
 check_insert = "INSERT INTO SERPOSCOPE.check (idGroup,idRun,date) VALUES(?,?,?)";
 keyword_insert = "INSERT INTO SERPOSCOPE.rank (idCheck,idTarget,idKeyword,position,url) VALUES (?,?,?,?,?)";
 group_request = "select * from SERPOSCOPE.group";
 keyword_query = "SELECT idKeyword,name FROM SERPOSCOPE.keyword WHERE idGroup=";
 target_query = "SELECT idTarget,name FROM SERPOSCOPE.target WHERE idGroup =";
 close_run="UPDATE SERPOSCOPE.run SET dateStop=? WHERE isnull(dateStop)";
 reopen_run="UPDATE SERPOSCOPE.run SET dateStop=null WHERE idRun=?";
 get_checkId_matching_run = "select idCheck from SERPOSCOPE.check where idRun=?";
 missing_keywords = "select * from SERPOSCOPE.keyword where idKeyword not in (select idKeyword from SERPOSCOPE.rank where idCheck=?)";

