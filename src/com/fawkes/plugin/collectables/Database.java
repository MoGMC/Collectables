package com.fawkes.plugin.collectables;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

public class Database {

	private Connection connection;

	public Database() throws SQLException {
		// connect to the SQL database
		connection = DriverManager.getConnection("jdbc:mysql://167.114.67.196/MPG-MC4", "MPG-MC4", "bd879b7953");

	}

	public boolean setLevel(UUID uuid, String awardId, int level) throws SQLException {

		// prepares the SQL statement
		PreparedStatement ps = connection
				.prepareStatement("UPDATE playerawards SET level=? WHERE uuid=? AND awardid=?");

		// replaces the ?s in the prepared statement with variables
		ps.setInt(1, level);
		ps.setObject(2, uuid.toString());
		ps.setString(3, awardId);

		// execute the SQL statement and return if it was successful.
		return ps.execute();

	}

	public boolean clearOfflineAwards(UUID uuid) throws SQLException {
		// prepares the SQL statement
		PreparedStatement ps = connection.prepareStatement("DELETE FROM offlineplayers WHERE uuid=?");

		ps.setString(1, uuid.toString());

		return ps.execute();

	}

	public ResultSet getOfflineAwards(UUID uuid) throws SQLException {
		// prepares the SQL statement
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM offlineplayers WHERE uuid=?");

		ps.setString(1, uuid.toString());

		return ps.executeQuery();

	}

	public boolean storeOfflineAward(UUID uuid, String awardId) throws SQLException {
		PreparedStatement ps = connection.prepareStatement("INSERT INTO offlineplayers (uuid, awardid) VALUES (?, ?)");

		ps.setString(1, uuid.toString());
		ps.setString(2, awardId);

		return ps.execute();

	}

	public boolean doesExist(UUID uuid, String awardId) {

		try {

			// prepares the SQL statement
			/*
			 * selects all of the rows that have both "uuid" equals
			 * "uuid.toString()" and "awardid" equals "awardId"
			 */
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM playerawards WHERE uuid=? AND awardid=?");

			// replaces the ?s in the prepared statement with variables
			ps.setObject(1, uuid.toString());
			ps.setString(2, awardId);

			/*
			 * execute the SQL statement and return if it was successful (if
			 * there was a result)
			 */
			return ps.executeQuery().next();

		} catch (SQLException e) {
			Bukkit.getLogger().severe("Error in checking database with doesExist");
			e.printStackTrace();
			return false;

		}

	}

	public boolean giveAward(UUID uuid, QueryAward a) throws SQLException {

		boolean wildcard = a instanceof QueryWildcardAward;

		// prepare the SQL statement
		// basically just adds a new row into the table

		PreparedStatement ps;

		if (wildcard) {
			ps = connection.prepareStatement(
					"INSERT INTO wildcardawards (uuid, awardid, date, level, wildcard) VALUES (?, ?, ?, ?, ?)");
		} else {
			ps = connection
					.prepareStatement("INSERT INTO playerawards (uuid, awardid, date, level) VALUES (?, ?, ?, ?)");

		}

		// insert the variables in place of the ?s
		ps.setObject(1, uuid.toString());
		ps.setString(2, a.getId());
		ps.setLong(3, a.getDate());
		ps.setInt(4, a.getLevel());

		if (wildcard) {
			ps.setString(5, ((QueryWildcardAward) a).getWildcard());

		}

		// execute the SQL statement and return if it was successful.
		return ps.execute();

	}

	public boolean removeAward(UUID uuid, String id, boolean wildcard) throws SQLException {

		PreparedStatement ps;

		if (wildcard) {
			// this means it's a wildcard wowo

			// prepare the SQL statement
			// removes all rows with uuid = uuid and awardid = awardId
			ps = connection.prepareStatement("DELETE FROM wildcardawards WHERE uuid=? AND awardid=?");

			// insert the variables in place of the ?s
			ps.setObject(1, uuid.toString());
			ps.setString(2, id);

		} else {
			// prepare the SQL statement
			// removes all rows with uuid = uuid and awardid = awardId
			ps = connection.prepareStatement("DELETE FROM playerawards WHERE uuid=? AND awardid=?");

			// insert the variables in place of the ?s
			ps.setObject(1, uuid.toString());
			ps.setString(2, id);

		}

		// execute the SQL statement and return if it was successful.
		return ps.execute();

	}

	public List<QueryAward> queryShowcase(UUID uuid) throws SQLException {

		// make the list we'll add the award variables to
		ArrayList<QueryAward> awards = new ArrayList<QueryAward>();

		// query the database
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM playerawards WHERE uuid=?");

		// insert variables in place of ?s
		ps.setString(1, uuid.toString());

		// get the results of the query
		ResultSet rs = ps.executeQuery();

		// loop through the results and turn them into awards
		while (rs.next()) {
			awards.add(new QueryAward(rs.getString("awardid"), rs.getLong("date"), rs.getInt("level")));

		}

		/*
		 * wait a minute111!!1!!1 do they have wildcard awards?
		 */

		// query the database
		PreparedStatement ps1 = connection.prepareStatement("SELECT * FROM wildcardawards WHERE uuid=?");

		// insert variables in place of ?s
		ps1.setString(1, uuid.toString());

		// get the results of the query
		ResultSet rs1 = ps1.executeQuery();

		// loop through the results and turn them into awards
		while (rs1.next()) {
			awards.add(new QueryWildcardAward(rs1.getString("awardid"), rs1.getLong("date"), rs1.getInt("level"),
					rs1.getString("wildcard")));

		}

		return awards;

	}

}
