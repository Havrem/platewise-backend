create table users (
    id bigserial primary key,
    email varchar(255) unique not null,
    password_hash varchar(255) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz
);

create table categories (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    name varchar(100) not null,
    icon varchar(100) not null,
    type varchar(100) not null check (type in ('GROCERY', 'RECIPES', 'GENERAL')),
    created_at timestamptz not null default now(),
    updated_at timestamptz
);

create table item_lists (
    id bigserial primary key,
    title varchar(500) not null,
    category_id bigint not null references categories(id) on delete restrict,
    bookmarked boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz
);

create table items (
    id bigserial primary key,
    item_list_id bigint not null references item_lists(id) on delete cascade,
    type varchar(100) not null check (type in ('BULLET', 'CHECKED', 'NUMBERED', 'NONE')),
    text varchar(500),
    completed boolean not null default false
);

create index idx_item_lists_category_id on item_lists(category_id);

create index idx_items_list_id on items(item_list_id);

create index idx_categories_user_id on categories(user_id);