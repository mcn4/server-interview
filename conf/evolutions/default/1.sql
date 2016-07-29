
# --- !Ups

create table user (
  username                  varchar(255) not null,
  email                     varchar(255),
  first_name                varchar(255),
  last_name                 varchar(255),
  age                       integer,
  password                  varchar(255),
  constraint pk_user primary key (username))
;

create sequence user_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists user_seq;

