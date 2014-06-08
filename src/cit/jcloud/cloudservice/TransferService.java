package cit.jcloud.cloudservice;

import java.util.List;

public class TransferService extends CloudService{
	
	private String TransferType;
	private String ServiceType;
	private List<StagePrice> prices;
	
	public static final String BandWidth = "bandwidth";
	public static final String Interval = "interval";
	
	public TransferService(int id, String Provider, String Location, String ServiceType,
			String TransferType, List<StagePrice> prices)
	{
		super(id, Provider, Location);
		this.ServiceType = ServiceType;
		this.TransferType = TransferType;
		this.prices = prices;
	}
	
	public String getTransferType()
	{
		return this.TransferType;
	}
	
	public String getServiceType()
	{
		return this.ServiceType;
	}
	
	public List<StagePrice> getPrices()
	{
		return this.prices;
	}
}
