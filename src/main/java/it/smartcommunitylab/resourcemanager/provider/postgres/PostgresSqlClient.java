package it.smartcommunitylab.resourcemanager.provider.postgres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class PostgresSqlClient {

    private final String HOST;
    private final int PORT;
    private final String USERNAME;
    private final String PASSWORD;
    private final boolean SSL;

    // postgres system schema
    // required for JDBC connection
    private final static String DB = "postgres";

    public PostgresSqlClient(String host, int port, boolean ssl, String username, String password) {
        super();
        HOST = host;
        PORT = port;
        USERNAME = username;
        PASSWORD = password;
        SSL = ssl;
    }

    private Connection connect(String database, boolean readonly) throws SQLException {
        // required DB NAME to successfully connect via JDBC
        // use internal "postgres" if needed
        String url = "jdbc:postgresql://" + HOST + ":" + String.valueOf(PORT) + "/" + database;
        Properties props = new Properties();
        props.setProperty("user", USERNAME);
        props.setProperty("password", PASSWORD);
        props.setProperty("ssl", String.valueOf(SSL));
        // set read-only if required by operation
        props.setProperty("readOnly", String.valueOf(readonly));

        Connection conn = DriverManager.getConnection(url, props);

        return conn;
    }

    public boolean ping() {
        try {
            // use internal "postgres" db
            Connection conn = connect(DB, true);
            // 1-second timeout for check
            boolean ret = conn.isValid(1);
            conn.close();
            return ret;
        } catch (Exception ex) {
            // log error?
            return false;
        }
    }

    public boolean hasDatabase(String name) throws SQLException {
        // use internal "postgres" db
        Connection conn = connect(DB, true);

        PreparedStatement stmt = conn.prepareStatement("SELECT 1 AS EXISTS from pg_database WHERE datname = ?");
        // no validation
        stmt.setObject(1, name);
        ResultSet result = stmt.executeQuery();
        // if query has one row, db exists
        boolean ret = result.next();

        result.close();
        stmt.close();
        conn.close();

        return ret;
    }

    public void createDatabase(String name) throws SQLException {
        // use internal "postgres" db
        Connection conn = connect(DB, false);

        // can not use prepared statements here
        Statement stmt = conn.createStatement();

        // no validation
        stmt.execute("CREATE DATABASE " + name);

        stmt.close();
        conn.close();

        // revoke CREATE to public schema
        // otherwise any valid user will be able to create tables in newdb.public
        // need a new connection to the db
        conn = connect(name, false);
        Statement revokeCreate = conn.createStatement();
        revokeCreate.execute("REVOKE CREATE ON SCHEMA public FROM public");
        revokeCreate.close();

        // also revoke all on database object
        // users will need explicit CONNECT, USAGE privileges
        Statement revokeAll = conn.createStatement();
        revokeAll.execute("REVOKE ALL ON DATABASE " + name + " FROM public");
        revokeAll.close();

        conn.close();

    }

    public void deleteDatabase(String name) throws SQLException {
        // use internal "postgres" db
        Connection conn = connect(DB, false);

        // can not use prepared statements here
        Statement stmt = conn.createStatement();

        // no validation - wil give error if connection active
        stmt.execute("DROP DATABASE IF EXISTS " + name);

        stmt.close();
        conn.close();
    }

    public void createRole(String database, String name, String policy) throws SQLException {
        // use provided db - must exist
        Connection conn = connect(database, false);

        // no validation
        // create role with NO login
        // can not use prepared statements here
        Statement create = conn.createStatement();
        create.execute("CREATE ROLE " + name);
        create.close();

        // grant base privileges
        // grant on database
        Statement grantDB = conn.createStatement();
        grantDB.execute("GRANT CONNECT ON DATABASE " + database + " TO " + name);
        grantDB.close();

        if (POLICY_RW.equals(policy)) {
            // grant ALL on public schema
            Statement grantSchema = conn.createStatement();
            grantSchema.execute("GRANT ALL ON SCHEMA public TO " + name);
            grantSchema.close();

            // grant on public schema tables
            Statement grantTables = conn.createStatement();
            grantTables.execute("GRANT ALL ON ALL TABLES IN SCHEMA public TO " + name);
            grantTables.close();

        } else if (POLICY_RO.equals(policy)) {
            // grant USAGE on public schema
            Statement grantSchema = conn.createStatement();
            grantSchema.execute("GRANT USAGE ON SCHEMA public TO " + name);
            grantSchema.close();

            // grant on public schema tables
            Statement grantTables = conn.createStatement();
            grantTables.execute("GRANT SELECT ON ALL TABLES IN SCHEMA public TO " + name);
            grantTables.close();

        }

        conn.close();
    }

    public void deleteRole(String database, String name) throws SQLException {
        // use provided db - must exist
        Connection conn = connect(database, false);

        // can not use prepared statements here
        Statement owned = conn.createStatement();

        // no validation - drop ALL objects with cascade
        owned.execute("DROP OWNED BY " + name + " CASCADE");
        owned.close();

        // can not use prepared statements here
        Statement drop = conn.createStatement();

        // no validation
        drop.execute("DROP ROLE IF EXISTS " + name);
        drop.close();

        conn.close();

    }

    public void alterDefault(String database, String name, String policy, String role) throws SQLException {
        // use provided db - must exist
        Connection conn = connect(database, false);

        if (POLICY_RW.equals(policy)) {
            // default privileges
            Statement grantDefault = conn.createStatement();
            grantDefault.execute(
                    "ALTER DEFAULT PRIVILEGES FOR " + name + " IN SCHEMA public GRANT ALL ON TABLES TO " + role);
            grantDefault.close();

        } else if (POLICY_RO.equals(policy)) {
            // default privileges
            Statement grantDefault = conn.createStatement();
            grantDefault.execute(
                    "ALTER DEFAULT PRIVILEGES FOR " + name + " IN SCHEMA public GRANT SELECT ON TABLES TO " + role);
            grantDefault.close();

        }

        conn.close();
    }

    public void createUser(String database, String username, String password, String policy) throws SQLException {
        // use provided db - must exist
        Connection conn = connect(database, false);

        // no validation
        // create user (role)
        // can not use prepared statements here
        Statement create = conn.createStatement();
        create.execute("CREATE ROLE " + username + " WITH LOGIN");
        create.close();

        // set password
        // can not use prepared statements here
        Statement alter = conn.createStatement();
        alter.execute("ALTER ROLE " + username + " WITH PASSWORD '" + password + "'");
        alter.close();

        // grant privileges
        // can not use prepared statements here
        // grant on database
        Statement grantDB = conn.createStatement();
        grantDB.execute("GRANT CONNECT ON DATABASE " + database + " TO " + username);
        grantDB.close();

        if (POLICY_RW.equals(policy)) {
            // grant ALL on public schema
            Statement grantSchema = conn.createStatement();
            grantSchema.execute("GRANT ALL ON SCHEMA public TO " + username);
            grantSchema.close();

            // grant on public schema tables
            Statement grantTables = conn.createStatement();
            grantTables.execute("GRANT ALL ON ALL TABLES IN SCHEMA public TO " + username);
            grantTables.close();

        } else if (POLICY_RO.equals(policy)) {
            // grant USAGE on public schema
            Statement grantSchema = conn.createStatement();
            grantSchema.execute("GRANT USAGE ON SCHEMA public TO " + username);
            grantSchema.close();

            // grant on public schema tables
            Statement grantTables = conn.createStatement();
            grantTables.execute("GRANT SELECT ON ALL TABLES IN SCHEMA public TO " + username);
            grantTables.close();

        }

//        // grant on public schema
//        Statement grantSchema = conn.createStatement();
//        grantSchema.execute("GRANT ALL ON SCHEMA public TO " + username);
//        grantSchema.close();
//
//        // grant on public schema tables
//        Statement grantTables = conn.createStatement();
//        grantTables.execute("GRANT ALL ON ALL TABLES IN SCHEMA public TO " + username);
//        grantTables.close();

        conn.close();
    }

    public void createUser(String database, String username, String password, String policy, String role)
            throws SQLException {
        // use provided db - must exist
        Connection conn = connect(database, false);

        // no validation
        // create user (role)
        // can not use prepared statements here
        Statement create = conn.createStatement();
        create.execute("CREATE ROLE " + username + " WITH LOGIN");
        create.close();

        // set password
        // can not use prepared statements here
        Statement alter = conn.createStatement();
        alter.execute("ALTER ROLE " + username + " WITH PASSWORD '" + password + "'");
        alter.close();

        // grant privileges
        // can not use prepared statements here
        // grant on database
        Statement grantDB = conn.createStatement();
        grantDB.execute("GRANT CONNECT ON DATABASE " + database + " TO " + username);
        grantDB.close();

        // inherit role
        Statement grantSchema = conn.createStatement();
        grantSchema.execute("GRANT " + role + " TO " + username);
        grantSchema.close();

        // DEPRECATED
//        // grant on public schema
//        Statement grantSchema = conn.createStatement();
//        grantSchema.execute("GRANT ALL ON SCHEMA public TO " + username);
//        grantSchema.close();
//
//        // grant on public schema tables
//        Statement grantTables = conn.createStatement();
//        grantTables.execute("GRANT ALL ON ALL TABLES IN SCHEMA public TO " + username);
//        grantTables.close();

//        DISABLED works only for objects create by current user (ie postgres)
//        // default privileges
//        Statement grantDefault = conn.createStatement();
//        grantDefault.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO " + username);
//        grantDefault.close();

        conn.close();
    }

    public void deleteUser(String database, String username, String role) throws SQLException {
        // use provided db - must exist
        Connection conn = connect(database, false);

        // can not use prepared statements here
        Statement owned = conn.createStatement();

        if (role.isEmpty()) {
            // no validation - drop ALL objects with cascade
            owned.execute("DROP OWNED BY " + username + " CASCADE");
            owned.close();
        } else {
            // reassign to role
            owned.execute("REASSIGN OWNED BY " + username + " TO " + role);
            owned.close();
        }

        // can not use prepared statements here
        Statement drop = conn.createStatement();

        // no validation
        drop.execute("DROP USER IF EXISTS " + username);
        drop.close();

        conn.close();

    }

    public static final String POLICY_RW = "readwrite";
    public static final String POLICY_RO = "readonly";

}
