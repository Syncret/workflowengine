package cit.jcloud.cloudservice;

public class ReservePackPrice extends PackPrice{
	
	private double Upfront;
	private String UsageLevel;
	
	public ReservePackPrice(double Count, String Currency, String Unit, String PriceType, double Upfront, String UsageLevel)
	{
		super(Count, Currency, Unit, PriceType);
		this.Upfront = Upfront;
		this.UsageLevel = UsageLevel;
	}
	
	public double getUpfront()
	{
		return this.Upfront;
	}
	
	public String getUsageLevel()
	{
		return this.UsageLevel;
	}
	

}
