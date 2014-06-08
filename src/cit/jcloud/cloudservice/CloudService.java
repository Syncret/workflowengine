package cit.jcloud.cloudservice;

public class CloudService {
	
	private int id;
	private String Provider;
	private String Location;
	
	public CloudService(int id, String Provider, String Location)
	{
		this.id = id;
		this.Provider = Provider;
		this.Location = Location;
	}
	
	public String getProvider()
	{
		return this.Provider;
	}
	
	public String getLocation()
	{
		return this.Location;
	}
	
	public int getID()
	{
		return this.id;
	}
	
	public void setProvider(String Provider)
	{
		this.Provider = Provider;
	}
	
}
