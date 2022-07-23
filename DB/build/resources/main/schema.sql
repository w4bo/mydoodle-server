drop database if exists doodle;
create database if not exists doodle;
use doodle;

drop table if exists doodle;
create table if not exists doodle (
    slotdate varchar(255),
    slotmonth int,
    slotyear int,
    weekday int,
    weekyear int,
    slotbin varchar(255),
    slotwhere varchar(255),
    weekdayname varchar(255),
    createdon varchar(255),
    primary key(slotdate, slotbin, slotwhere)
);

select * from doodle;

create table if not exists doodleuser (
    id varchar(255),
    firstname varchar(255),
    lastname varchar(255),
    role varchar(255) default 'user',
    primary key(id)
);

create table if not exists userindoodle (
    id varchar(255),
    slotdate varchar(255),
    slotbin varchar(255),
    slotwhere varchar(255),
    primary key(id, slotdate, slotbin, slotwhere),
    foreign key (slotdate, slotbin, slotwhere) references doodle(slotdate, slotbin, slotwhere),
    foreign key (id) references doodleuser(id)
);