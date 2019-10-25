ALTER TABLE router_commands ADD COLUMN template_version character varying(255);
UPDATE router_commands SET template_version = '1.0.0';

ALTER TABLE router_command_history ADD COLUMN template_version character varying(255);
UPDATE router_command_history SET template_version = '1.0.0';
