# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table team (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_team primary key (id))
;

create sequence team_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists team;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists team_seq;

