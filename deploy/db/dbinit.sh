dropdb --if-exists dev_mem
dropuser --if-exists dev
psql -U $POSTGRES_USER <<EOF
CREATE USER dev WITH PASSWORD 'dev';
CREATE DATABASE dev_mem WITH OWNER = 'dev';
GRANT ALL ON SCHEMA public TO dev;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO dev;
EOF

