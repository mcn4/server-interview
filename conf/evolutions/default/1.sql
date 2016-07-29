# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table team (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_team primary key (id))
;

create table user (
  username                  varchar(255) not null,
  email                     varchar(255),
  first_name                varchar(255),
  last_name                 varchar(255),
  age                       integer,
  password                  varchar(255),
  constraint pk_user primary key (username))
;


create table teamMembers (
  teamId                         bigint not null,
  memberId                       varchar(255) not null,
  constraint pk_teamMembers primary key (teamId, memberId))
;
create sequence team_seq;

create sequence user_seq;




alter table teamMembers add constraint fk_teamMembers_team_01 foreign key (teamId) references team (id) on delete restrict on update restrict;

alter table teamMembers add constraint fk_teamMembers_user_02 foreign key (memberId) references user (username) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists team;

drop table if exists teamMembers;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists team_seq;

drop sequence if exists user_seq;

