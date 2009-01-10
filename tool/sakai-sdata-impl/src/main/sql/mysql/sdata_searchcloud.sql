create table sdata_searchcloud (
   searchquery varchar(255) not null,
   number int not null,
   primary key  (searchquery)
)
ENGINE = InnoDB
CHARACTER SET utf8
COLLATE utf8_unicode_ci;
