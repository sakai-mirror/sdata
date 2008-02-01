package org.sakaiproject.sdata.services.mra;

public class MyRecentChangesSqlresult
{

	private String version;

	private String context;

	private String name;

	private String tool;

	public void setVersion(String version)
	{
		this.version = version;
	}

	public String getVersion()
	{
		return version;
	}

	public void setContext(String context)
	{
		this.context = context;
	}

	public String getContext()
	{
		return context;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setTool(String tool)
	{
		this.tool = tool;
	}

	public String getTool()
	{
		return tool;
	}

}
