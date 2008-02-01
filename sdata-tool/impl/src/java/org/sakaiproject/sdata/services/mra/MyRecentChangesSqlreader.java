package org.sakaiproject.sdata.services.mra;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.db.api.SqlReader;

public class MyRecentChangesSqlreader implements SqlReader
{
	public MyRecentChangesSqlresult readSqlResultRecord(ResultSet result)
	{

		MyRecentChangesSqlresult res = new MyRecentChangesSqlresult();

		try
		{

			res.setVersion(result.getString("version"));
			res.setContext(result.getString("context"));
			res.setName(result.getString("name"));
			res.setTool(result.getString("tool"));

		}
		catch (SQLException e)
		{

			e.printStackTrace();

		}

		return res;
	}
}
