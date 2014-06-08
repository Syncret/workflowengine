package cit.jcloud.cloudservice;

public class BasePrice {
	
	private double Count;
	private String Currency;
	private String Unit;
	private String PriceType;
	
	public static final String StagePrice = "StagePrice";
	public static final String PackPrice = "PackPrice";
	public static final String US_Dollar = "US Dollar";
	public static final String Yuan = "Yuan";
	
	public BasePrice(double Count, String Currency, String Unit, String PriceType)
	{
		this.Count = Count;
		this.Currency = Currency;
		this.Unit = Unit;
		this.PriceType = PriceType;
	}
	
	public double getCount()
	{
		return this.Count;
	}
	
	public String getCurrency()
	{
		return this.Currency;
	}
	
	public String getUnit()
	{
		return this.Unit;
	}
	
	public String getPriceType()
	{
		return this.PriceType;
	}
	
	
}
