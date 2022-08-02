drop table if exists doodle cascade;
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

drop table if exists doodleuser cascade;
create table if not exists doodleuser (
    id varchar(255),
    firstname varchar(255),
    lastname varchar(255),
    role varchar(255) default 'user',
    primary key(id)
);

drop table if exists userindoodle cascade;
create table if not exists userindoodle (
    id varchar(255),
    slotdate varchar(255),
    slotbin varchar(255),
    slotwhere varchar(255),
    checked varchar(255),
    primary key(id, slotdate, slotbin, slotwhere),
    foreign key (slotdate, slotbin, slotwhere) references doodle(slotdate, slotbin, slotwhere),
    foreign key (id) references doodleuser(id)
);

select b.*, a.id, a.checked from userindoodle a join doodle b on (a.slotdate = b.slotdate and a.slotbin = b.slotbin and a.slotwhere = b.slotwhere);
select * from doodle;
select * from doodleuser;
UPDATE userindoodle SET checked='true' WHERE id='foo.bar@gmail.com' AND slotbin='PM' AND slotwhere='PED' AND slotdate='2022-08-01';