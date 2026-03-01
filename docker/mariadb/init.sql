-- Force root to use mysql_native_password
ALTER USER 'root'@'%' IDENTIFIED VIA mysql_native_password USING PASSWORD('root');
ALTER USER 'root'@'localhost' IDENTIFIED VIA mysql_native_password USING PASSWORD('root');
FLUSH PRIVILEGES;
