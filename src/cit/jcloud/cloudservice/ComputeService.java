package cit.jcloud.cloudservice;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.contains;
import static com.google.common.collect.Iterables.getOnlyElement;
import static org.jclouds.aws.ec2.reference.AWSEC2Constants.PROPERTY_EC2_AMI_QUERY;
import static org.jclouds.aws.ec2.reference.AWSEC2Constants.PROPERTY_EC2_CC_AMI_QUERY;
import static org.jclouds.compute.config.ComputeServiceProperties.TIMEOUT_SCRIPT_COMPLETE;
import static org.jclouds.compute.options.TemplateOptions.Builder.overrideLoginUser;
import static org.jclouds.compute.options.TemplateOptions.Builder.runScript;
import static org.jclouds.compute.predicates.NodePredicates.TERMINATED;
import static org.jclouds.compute.predicates.NodePredicates.inGroup;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.apis.Apis;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.Providers;
import org.jclouds.scriptbuilder.domain.Statement;
import org.jclouds.scriptbuilder.statements.login.AdminAccess;
import org.jclouds.sshj.config.SshjSshClientModule;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.inject.Module;

public class ComputeService extends CloudService{
	
	   public static enum Action {
		      ADD, RUN, EXEC, DESTROY;
		   }
	
	private static final String AmazonS3 = "aws-ec2";
	
	public static final Map<String, ApiMetadata> allApis = Maps.uniqueIndex(Apis.viewableAs(ComputeServiceContext.class),
	        Apis.idFunction());
	   
	 public static final Map<String, ProviderMetadata> appProviders = Maps.uniqueIndex(Providers.viewableAs(ComputeServiceContext.class),
	        Providers.idFunction());
	   
	 public static final Set<String> allKeys = ImmutableSet.copyOf(Iterables.concat(appProviders.keySet(), allApis.keySet()));
	   
	   
	private String MachineType;
	private double CPUCount;
	private String CPUUnit;
	private double RamCount;
	private String RamUnit;
	private double DiskCount;
	private String DiskUnit;
	private String OS;
	private String PlatForm;
	private String ServiceType;
	private List<ReservePackPrice> prices;
	
	public ComputeService(int id, String Provider, String Location, String MachineType, double CPUCount,
			String CPUUnit, double RamCount, String RamUnit, double DiskCount, String DiskUnit, 
			String OS, String PlatForm, String ServiceType, List<ReservePackPrice> prices)
	{
		super(id, Provider, Location);
		this.MachineType = MachineType;
		this.CPUCount = CPUCount;
		this.CPUUnit = CPUUnit;
		this.RamCount = RamCount;
		this.RamUnit = RamUnit;
		this.DiskCount = DiskCount;
		this.DiskUnit = DiskUnit;
		this.OS = OS;
		this.PlatForm = PlatForm;
		this.ServiceType = ServiceType;
		this.prices = prices;
	}
	
	public ComputeService(String provider){
		super(0,provider,null);
	}
	
	public void createVirtualMachine(String groupName, String identity, String credential)
	{
		String operation = "add";	      
	    Action action = Action.valueOf(operation.toUpperCase());
		String minRam = System.getProperty("minRam");
	    String loginUser = System.getProperty("loginUser", "toor");
	      
	    checkArgument(contains(allKeys, AmazonS3), "provider %s not in supported list: %s", AmazonS3, allKeys);
	    LoginCredentials login = (action != Action.DESTROY) ? getLoginForCommandExecution(action) : null;
	
	    org.jclouds.compute.ComputeService compute = initComputeService(AmazonS3, identity, credential);
	    
        // Default template chooses the smallest size on an operating system
        // that tested to work with java, which tends to be Ubuntu or CentOS
        TemplateBuilder templateBuilder = compute.templateBuilder();
        
        // If you want to up the ram and leave everything default, you can 
        // just tweak minRam
        if (minRam != null)
           templateBuilder.minRam(Integer.parseInt(minRam));
        
        
        // note this will create a user with the same name as you on the
        // node. ex. you can connect via ssh publicip
        Statement bootInstructions = AdminAccess.standard();

        // to run commands as root, we use the runScript option in the template.
        if(AmazonS3.equalsIgnoreCase("virtualbox"))
           templateBuilder.options(overrideLoginUser(loginUser).runScript(bootInstructions));
        else
           templateBuilder.options(runScript(bootInstructions));
        
        try {
			NodeMetadata node = getOnlyElement(compute.createNodesInGroup(groupName, 1, templateBuilder.build()));
		} catch (RunNodesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
	         compute.getContext().close();
	      }
	}
	
	public void removeVirtualMachine(String groupName, String identity, String credential)
	{
		String operation = "destroy";	      
	    Action action = Action.valueOf(operation.toUpperCase());
		String minRam = System.getProperty("minRam");
	    String loginUser = System.getProperty("loginUser", "toor");
	      
	    checkArgument(contains(allKeys, AmazonS3), "provider %s not in supported list: %s", AmazonS3, allKeys);
	    LoginCredentials login = (action != Action.DESTROY) ? getLoginForCommandExecution(action) : null;
	
	    org.jclouds.compute.ComputeService compute = initComputeService(AmazonS3, identity, credential);
        // you can use predicates to select which nodes you wish to destroy.
        Set<? extends NodeMetadata> destroyed = compute.destroyNodesMatching(//
              Predicates.<NodeMetadata> and(not(TERMINATED), inGroup(groupName)));
	}
	

	public String getMachineType()
	{
		return this.MachineType;
	}
	
	public double getCPUCount()
	{
		return this.CPUCount;
	}
	
	public String getCPUUnit()
	{
		return this.CPUUnit;
	}
	
	public double getRamCount()
	{
		return this.RamCount;
	}
	
	public String getRamUnit()
	{
		return this.RamUnit;
	}
	
	public double getDiskCount()
	{
		return this.DiskCount;
	}
	
	public String getDiskUnit()
	{
		return this.DiskUnit;
	}
	
	public String getOS()
	{
		return this.OS;
	}
	
	public String getPlatForm()
	{
		return this.PlatForm;
	}
	
	public String getServiceType()
	{
		return this.ServiceType;
	}

	   private static org.jclouds.compute.ComputeService initComputeService(String provider, String identity, String credential) {

		      // example of specific properties, in this case optimizing image list to
		      // only amazon supplied
		      Properties properties = new Properties();
		      properties.setProperty(PROPERTY_EC2_AMI_QUERY, "owner-id=137112412989;state=available;image-type=machine");
		      properties.setProperty(PROPERTY_EC2_CC_AMI_QUERY, "");
		      long scriptTimeout = TimeUnit.MILLISECONDS.convert(20, TimeUnit.MINUTES);
		      properties.setProperty(TIMEOUT_SCRIPT_COMPLETE, scriptTimeout + "");

		      // example of injecting a ssh implementation
		      Iterable<Module> modules = ImmutableSet.<Module> of(
		            new SshjSshClientModule(),
		            new SLF4JLoggingModule(),
		            new EnterpriseConfigurationModule());

		      ContextBuilder builder = ContextBuilder.newBuilder(provider)
		                                             .credentials(identity, credential)
		                                             .modules(modules)
		                                             .overrides(properties);
		                                             
		      //servercomment System.out.printf(">> initializing %s%n", builder.getApiMetadata());

		      return builder.buildView(ComputeServiceContext.class).getComputeService();
		   }
	
	   private static LoginCredentials getLoginForCommandExecution(Action action) {
		      try {
		        String user = System.getProperty("user.name");
		        String privateKey = Files.toString(
		            new File(System.getProperty("user.home") + "/.ssh/id_rsa"), UTF_8);
		        return LoginCredentials.builder().
		            user(user).privateKey(privateKey).build();
		      } catch (Exception e) {
		         System.err.println("error reading ssh key " + e.getMessage());
		         System.exit(1);
		         return null;
		      }
		   }


	   public List<ReservePackPrice> getPrices()
		{
			return this.prices;
		}
}
