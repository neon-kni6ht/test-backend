alter table budget add column author_id int;
alter table budget add constraint fk_budget_authors foreign key (author_id) references author (id);