package org.sakaiproject.sdata.services.mra;

public class MyRecentChangesResult extends MyRecentChangesSqlresult
{

	private String sitename;
	private String reference;
	public MyRecentChangesResult(String context,String name,String tool,String version){
		
		super.setContext(context);
		super.setName(name);
		super.setTool(tool);
		super.setVersion(version);
		
	}
	public MyRecentChangesResult(){
		
	}
	public void setSitename(String sitename)
	{
		this.sitename = sitename;
	}
	public String getSitename()
	{
		return sitename;
	}
	public void setReference(String reference)
	{
		this.reference = reference;
	}
	public String getReference()
	{
		return reference;
	}
	
	
	
}
