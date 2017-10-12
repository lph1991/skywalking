package org.skywalking.apm.collector.client.h2;

import java.sql.*;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.util.IOUtils;
import org.skywalking.apm.collector.core.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pengys5
 */
public class H2Client implements Client {

    private final Logger logger = LoggerFactory.getLogger(H2Client.class);

    private JdbcConnectionPool cp;
    private Connection conn;
    private String url;
    private String userName;
    private String password;

    public H2Client() {
        this.url = "jdbc:h2:mem:collector";
        this.userName = "";
        this.password = "";
    }

    public H2Client(String url, String userName, String password) {
        this.url = url;
        this.userName = userName;
        this.password = password;
    }

    @Override public void initialize() throws H2ClientException {
        try {
            cp = JdbcConnectionPool.
                    create(this.url, this.userName, this.password);
            conn = cp.getConnection();
        } catch (Exception e) {
            throw new H2ClientException(e.getMessage(), e);
        }
    }

    @Override public void shutdown() {
        if (cp != null) {
            cp.dispose();
        }
        IOUtils.closeSilently(conn);
    }

    public Connection getConnection() throws H2ClientException {
        return conn;
    }

    public void execute(String sql) throws H2ClientException {
        try (Statement statement = getConnection().createStatement()) {
            statement.execute(sql);
            statement.closeOnCompletion();
        } catch (SQLException e) {
            throw new H2ClientException(e.getMessage(), e);
        }
    }

    public ResultSet executeQuery(String sql, Object[] params) throws H2ClientException {
        logger.info("execute query with result: {}", sql);
        ResultSet rs;
        PreparedStatement statement;
        try {
            statement = getConnection().prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
            }
            rs = statement.executeQuery();
            statement.closeOnCompletion();
        } catch (SQLException e) {
            throw new H2ClientException(e.getMessage(), e);
        }
        return rs;
    }

    public boolean execute(String sql, Object[] params) throws H2ClientException {
        logger.info("execute insert/update/delete: {}", sql);
        boolean flag;
        Connection conn = getConnection();
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
            }
            flag = statement.execute();
            conn.commit();
        } catch (SQLException e) {
            throw new H2ClientException(e.getMessage(), e);
        }
        return flag;
    }
}
