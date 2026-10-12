# MySQL schema fix: story read counter

The Java field `Story.reads` is now mapped to the non-reserved MySQL column `read_count`.
This avoids MySQL syntax errors because `READS` is a reserved keyword.

The application uses `spring.jpa.hibernate.ddl-auto: update`, so stop the backend and
restart it after extracting this project. Hibernate should add `read_count` automatically.
Do not drop the `stories` table; that would delete existing stories.

If startup reports that `read_count` is still missing, inspect the table first:

```sql
USE chooseyourpath;
SHOW COLUMNS FROM stories;
```

Only if `read_count` is absent, run:

```sql
ALTER TABLE stories ADD COLUMN read_count BIGINT NOT NULL DEFAULT 0;
```
