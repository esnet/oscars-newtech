alter table held drop column expiration;
alter table held add column expiration timestamp;

alter table router_command_history drop column date;
alter table router_command_history add column date timestamp;

alter table version drop column updated;
alter table version add column updated timestamp;


alter table schedule drop column beginning;
alter table schedule drop column ending;

alter table schedule add column beginning timestamp;
alter table schedule add column ending timestamp;

alter table event_log drop column created;
alter table event_log drop column archived;

alter table event_log add column created timestamp;
alter table event_log add column archived timestamp;

alter table event drop column at;
alter table event add column at timestamp;

