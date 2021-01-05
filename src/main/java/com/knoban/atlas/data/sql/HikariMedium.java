package com.knoban.atlas.data.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Alden Bansemer (kNoAPP)
 */
public class HikariMedium {

	private HikariDataSource ds;

	/**
	 * Configure a {@link Connection} pool. Requires authentication with a database.
	 * @param host The ip address of the database
	 * @param port The port of the database
	 * @param database The database name to use
	 * @param username The username to authenticate with
	 * @param password The password to authenticate with
	 */
	public HikariMedium(@NotNull String host, int port, @NotNull String database, @NotNull String username,
						@NotNull String password) {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false");
		config.setUsername(username);
		config.setPassword(password);
		/*
		 * Pterodactyl MariaDB default is 600 seconds or 10 minutes.
		 * Always use a connection lifetime a few seconds shorter than the DB's default.
		 * Thus, we use a maxLifetime of 240000ms or 240 seconds or 4 minutes.
		 */
		config.addDataSourceProperty("maxLifetime", "240000");
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		ds = new HikariDataSource(config);
	}

	/**
	 * Get a {@link Connection} from the Hikari connection pool
	 * @return A new {@link Connection}
	 * @throws SQLException If a {@link Connection} cannot be made
	 */
	public Connection getConnection() throws SQLException {
		return ds.getConnection();
	}

	/**
	 * Shutdown the DataSource and its associated pool.
	 */
	public void close() {
		ds.close();
	}
}
