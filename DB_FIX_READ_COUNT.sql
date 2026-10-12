-- Safe, repeatable MySQL migration for the reserved `reads` column issue.
-- Run while connected to the chooseyourpath database. Hibernate also adds
-- read_count automatically because ddl-auto is set to update.
SET @has_read_count = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stories' AND COLUMN_NAME = 'read_count'
);
SET @has_old_reads = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stories' AND COLUMN_NAME = 'reads'
);
SET @add_column_sql = IF(
  @has_read_count = 0,
  'ALTER TABLE stories ADD COLUMN read_count BIGINT NOT NULL DEFAULT 0',
  'SELECT ''read_count already exists'' AS migration_status'
);
PREPARE add_column_stmt FROM @add_column_sql;
EXECUTE add_column_stmt;
DEALLOCATE PREPARE add_column_stmt;

-- Preserve historical values if an older schema did manage to create `reads`.
SET @copy_old_values_sql = IF(
  @has_old_reads = 1 AND @has_read_count = 0,
  'UPDATE stories SET read_count = COALESCE(`reads`, 0)',
  'SELECT ''no old reads column to copy'' AS migration_status'
);
PREPARE copy_old_values_stmt FROM @copy_old_values_sql;
EXECUTE copy_old_values_stmt;
DEALLOCATE PREPARE copy_old_values_stmt;
