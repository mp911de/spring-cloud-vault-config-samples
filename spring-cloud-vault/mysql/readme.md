Spring Cloud Vault Config MySQL Integration
===========================================

This example uses Spring Cloud Vault Config
usage with the MySQL integration.

Vault will obtain Username/Password for you so you'll be able to
use a generated login.

## Running the Example

The MySQL example requires a running MySQL database
on `localhost:3306` with an user that is authorized
to create new users.

The example setup script `src/test/bash/setup_examples.sh`
will use the user `spring` with the password `vault`
as administrative MySQL user.

You can run the following lines to setup your local MySQL server
for Vault

```bash
$ mysql -e "CREATE USER 'spring' IDENTIFIED by 'vault';"
$ mysql -uroot -e "GRANT ALL PRIVILEGES ON *.* TO 'spring'@'%' WITH GRANT OPTION;";
```

