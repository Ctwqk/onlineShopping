-- Flyway (MySQL) reads performance_schema.user_variables_by_thread before migrating. The application user is not
-- allowed to select from it by default, which makes startup fail with "Unable to determine value for
-- 'foreign_key_checks'". Grant just that one table.
GRANT SELECT ON performance_schema.user_variables_by_thread TO 'reggie'@'%';
