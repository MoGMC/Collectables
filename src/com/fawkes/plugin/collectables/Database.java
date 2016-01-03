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
		PreparedStatement ps = connection.prepareStatement("UPDATE awards SET level=? WHERE uuid=? AND awardid=?");

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

	public boolean storeOfflineAward(UUID uuid, QueryAward a) throws SQLException {
		PreparedStatement ps = connection
				.prepareStatement("INSERT INTO offlineplayers (uuid, awardid, wildcard) VALUES (?, ?, ?)");

		ps.setString(1, uuid.toString());
		ps.setString(2, a.getId());

		if (a.hasMeta()) {
			ps.setString(3, a.getMeta());

		} else {
			ps.setString(3, "{wildcard}");

		}

		return ps.execute();

	}

	public boolean doesExist(UUID uuid, String awardId) {

		try {

			// prepares the SQL statement
			/*
			 * selects all of the rows that have both "uuid" equals
			 * "uuid.toString()" and "awardid" equals "awardId"
			 */
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM awards WHERE uuid=? AND awardid=?");

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

		boolean hasmeta = a.hasMeta();

		// prepare the SQL statement
		// basically just adds a new row into the table

		PreparedStatement ps = connection
				.prepareStatement("INSERT INTO awards (uuid, awardid, date, level, hasmeta) VALUES (?, ?, ?, ?, ?)");

		// insert the variables in place of the ?s
		ps.setObject(1, uuid.toString());
		ps.setString(2, a.getId());
		ps.setLong(3, a.getDate());
		ps.setInt(4, a.getLevel());
		ps.setBoolean(5, hasmeta);

		// if there is meta, go set it in the other table.
		if (hasmeta) {
			PreparedStatement metaps = connection
					.prepareStatement("INSERT INTO metadata (uuid, awardid, meta) VALUES (?, ?, ?)");

			metaps.setObject(1, uuid.toString());
			metaps.setString(2, a.getId());
			metaps.setString(3, a.getMeta());

			metaps.execute();

		}

		// execute the SQL statement and return if it was successful.
		return ps.execute();

	}

	// TODO: return if it was successfull or not
	public boolean removeAward(UUID uuid, String awardid) throws SQLException {

		// query database and see if it had any awardid;
		PreparedStatement metacheckps = connection
				.prepareStatement("SELECT * FROM metadata WHERE uuid=? AND awardid=?");

		metacheckps.setString(1, uuid.toString());
		metacheckps.setString(2, awardid);

		ResultSet metacheckrs = metacheckps.executeQuery();

		if (metacheckrs.next()) {
			// wow there is meta! go delete it.
			PreparedStatement metaps = connection.prepareStatement("DELETE FROM metadata WHERE uuid=? AND awardid=?");

			metaps.setObject(1, uuid.toString());
			metaps.setString(2, awardid);

			metaps.execute();

		}

		PreparedStatement ps;
		// prepare the SQL statement
		// removes all rows with uuid = uuid and awardid = awardId
		ps = connection.prepareStatement("DELETE FROM awards WHERE uuid=? AND awardid=?");

		// insert the variables in place of the ?s
		ps.setObject(1, uuid.toString());
		ps.setString(2, awardid);

		// execute the SQL statement and return if it was successful.
		return ps.execute();

	}

	public int getAwardCount(UUID uuid) throws SQLException {

		int count = 0;

		// query the database
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM awards WHERE uuid=?");

		// insert variables in place of ?s
		ps.setString(1, uuid.toString());

		// get the results of the query
		ResultSet rs = ps.executeQuery();

		while (rs.next()) {
			count++;

		}

		return count;

	}

	public List<QueryAward> queryShowcase(UUID uuid) throws SQLException {
		// make the list we'll add the award variables to
		ArrayList<QueryAward> awards = new ArrayList<QueryAward>();

		// query the database
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM awards WHERE uuid=?");

		// insert variables in place of ?s
		ps.setString(1, uuid.toString());

		// get the results of the query
		ResultSet rs = ps.executeQuery();

		// loop through the results and turn them into awards
		while (rs.next()) {

			// prepare the award to add to the list
			QueryAward a = new QueryAward(rs.getString("awardid"), rs.getLong("date"), rs.getInt("level"));

			// if the award has meta, go get it from the meta table!
			if (rs.getBoolean("hasmeta")) {
				// same drill
				PreparedStatement metaps = connection
						.prepareStatement("SELECT * FROM metadata WHERE uuid=? AND awardid=?");

				metaps.setObject(1, uuid.toString());
				metaps.setString(2, a.getId());

				ResultSet metars = metaps.executeQuery();

				// goes to the row, but also checks if it isn't there.
				if (!metars.next()) {
					// screwup
					Bukkit.getLogger().severe("Could not get meta for supposed meta award: " + rs.getString("awardid")
							+ " for UUID " + uuid.toString());

				}

				// add meta to queryaward
				a.addMeta(metars.getString("meta"));

			}

			awards.add(a);

		}

		return awards;

	}

}
