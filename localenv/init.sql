revoke all on schema public from public;

create user exampleuser password 'exampleuserpassword';

grant usage on schema public to exampleuser;
alter default privileges in schema public grant select, insert, update, delete on tables to exampleuser;
alter default privileges in schema public grant usage, select on sequences to exampleuser;

create table "user" (
  id serial primary key,
  name varchar(50),
  date_of_birth date
);
