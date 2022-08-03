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
    token varchar(255),
    primary key(token, slotdate, slotbin, slotwhere)
);

drop table if exists doodleuser cascade;
create table if not exists doodleuser (
    id varchar(255),
    firstname varchar(255),
    lastname varchar(255),
    token varchar(255),
    role varchar(255) default 'user',
    primary key(id, token)
);

drop table if exists userindoodle cascade;
create table if not exists userindoodle (
    id varchar(255),
    u_token varchar(255),
    d_token varchar(255),
    slotdate varchar(255),
    slotbin varchar(255),
    slotwhere varchar(255),
    checked varchar(255),
    primary key(id, u_token, slotdate, slotbin, slotwhere),
    unique(id, u_token, d_token, slotdate, slotbin, slotwhere),
    foreign key (d_token, slotdate, slotbin, slotwhere) references doodle(token, slotdate, slotbin, slotwhere) on delete cascade,
    foreign key (id, u_token) references doodleuser(id, token) on delete cascade
);