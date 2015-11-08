package com.fawkes.plugin.collectables;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;

public class Database {

	private Connection connection;

	public Database() throws SQLException {
		connection = DriverManager.getConnection("jdbc:mysql://167.114.67.196/MPG-MC4", "MPG-MC4", "bd879b7953");

	}

	public boolean setLevel(UUID uuid, short awardId, int level) throws SQLException {
		PreparedStatement ps = connection
				.prepareStatement("UPDATE playerawards SET level=? WHERE uuid=? AND awardid=?");

		ps.setInt(1, level);

		return ps.execute();

	}

	public boolean doesExist(UUID uuid, short awardId) {

		try {

			PreparedStatement ps = connection.prepareStatement("SELECT * FROM playerawards WHERE UUID=? awardid=?");

			ps.setObject(1, uuid.toString());
			ps.setShort(2, awardId);

			return ps.execute();

		} catch (SQLException e) {
			Bukkit.getLogger().severe("Error in checking database with doesExist");
			e.printStackTrace();
			return false;

		}

	}

	public boolean giveAward(UUID uuid, short awardId, long date, int level) throws SQLException {

		PreparedStatement ps = connection
				.prepareStatement("INSERT INTO playerawards (uuid, awardid, date, level) VALUES (?, ?, ?, ?)");

		ps.setObject(1, uuid.toString());
		ps.setShort(2, awardId);
		ps.setLong(3, date);
		ps.setInt(4, level);

		return ps.execute();

	}

	public ArrayList<Award> queryShowcase(UUID uuid) throws SQLException {

		// make the list we'll add the award variables to
		ArrayList<Award> awards = new ArrayList<Award>();

		// query the database
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM playerawards WHERE UUID=?");

		ps.setString(1, uuid.toString());

		ResultSet rs = ps.executeQuery();

		// loop through the results and turn them into awards
		while (rs.next()) {
			awards.add(new Award(rs.getShort("awardid"), rs.getLong("date"), rs.getInt("level")));

		}

		return awards;

	}

}
