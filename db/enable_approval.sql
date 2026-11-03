UPDATE sys_config SET config_value='true' WHERE config_key='approval.enabled';
SELECT config_key,config_value FROM sys_config WHERE config_key LIKE 'approval%';