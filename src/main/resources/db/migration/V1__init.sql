create table users (
    id bigserial primary key,
    email varchar(255) unique not null,
    password_hash varchar(255) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz
);

create table refresh_tokens (
    id bigserial primary key,
    token_hash varchar(255) unique not null,
    user_id bigint not null references users(id) on delete cascade,
    expires_at timestamptz not null,
    created_at timestamptz not null default now()
);

create index idx_refresh_tokens_token_hash on refresh_tokens(token_hash);
create index idx_refresh_tokens_user_id on refresh_tokens(user_id);

create table categories (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    name varchar(100) not null,
    icon varchar(100) not null,
    kind varchar(20) not null default 'USER' check (kind in ('USER', 'SHARED')),
    created_at timestamptz not null default now(),
    updated_at timestamptz
);

create table item_lists (
    id bigserial primary key,
    title varchar(500) not null,
    category_id bigint not null references categories(id) on delete restrict,
    type varchar(100) not null check (type in ('GROCERY', 'RECIPES', 'GENERAL')),
    bookmarked boolean not null default false,
    rank varchar(64) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz
);

create table list_members (
    list_id bigint not null references item_lists(id) on delete cascade,
    user_id bigint not null references users(id) on delete cascade,
    is_owner boolean not null default false,
    joined_at timestamptz not null default now(),
    primary key (list_id, user_id)
);

create table list_invites (
    id bigserial primary key,
    list_id bigint not null references item_lists(id) on delete cascade,
    inviter_id bigint not null references users(id) on delete cascade,
    invitee_id bigint not null references users(id) on delete cascade,
    created_at timestamptz not null default now(),
    unique (list_id, invitee_id)
);

create table list_sections (
    id bigserial primary key,
    item_list_id bigint not null references item_lists(id) on delete cascade,
    text varchar(500) not null,
    rank varchar(64) not null
);

create table items (
    id bigserial primary key,
    item_list_id bigint not null references item_lists(id) on delete cascade,
    section_id bigint references list_sections(id) on delete set null,
    user_id bigint not null references users(id) on delete cascade,
    type varchar(100) not null check (type in ('BULLET', 'CHECKED', 'NUMBERED', 'NONE')),
    text varchar(500),
    completed boolean not null default false,
    rank varchar(64) not null
);

create index idx_item_lists_category_id on item_lists(category_id);

create index idx_list_members_user_id on list_members(user_id);

create index idx_list_invites_invitee_id on list_invites(invitee_id);

create index idx_list_sections_list_rank on list_sections(item_list_id, rank);

create index idx_items_list_id on items(item_list_id);

create index idx_items_section_id on items(section_id);

create index idx_items_user_list_rank on items(user_id, item_list_id, rank);

create index idx_categories_user_id on categories(user_id);

create index idx_items_user_id on items(user_id);
