package cit.jcloud.cloudservice;

public class StagePrice extends BasePrice{
	
	private double StartNum;
	private String StartUnit;
	private double EndNum;
	private String EndUnit;
	
	public StagePrice(double Count, String Currency, String Unit, String PriceType, 
			double StartNum, String StartUnit, double EndNum, String EndUnit)
	{
		super(Count, Currency, Unit, PriceType);
		this.StartNum = StartNum;
		this.StartUnit = StartUnit;
		this.EndNum = EndNum;
		this.EndUnit = EndUnit;
	}
	
	public double getStartNum()
	{
		return this.StartNum;
	}
	
	public String getStartUnit()
	{
		return this.StartUnit;
	}
	
	public double getEndNum()
	{
		return this.EndNum;
	}
	
	public String getEndUnit()
	{
		return this.EndUnit;
	}

}
