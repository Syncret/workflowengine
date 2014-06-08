package cit.jcloud.cloudservice;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.apis.Apis;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.io.Payload;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.Providers;

import static org.jclouds.blobstore.options.ListContainerOptions.Builder.*;
import cit.jcloud.common.FileNode;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class StorageService extends CloudService{
	
	private static final String AmazonS3 = "aws-s3";
	
	
	
	public static final Map<String, ApiMetadata> allApis = Maps.uniqueIndex(Apis.viewableAs(BlobStoreContext.class),
	        Apis.idFunction());
	   
	public static final Map<String, ProviderMetadata> appProviders = Maps.uniqueIndex(Providers.viewableAs(BlobStoreContext.class),
	        Providers.idFunction());
	   
	public static final Set<String> allKeys = ImmutableSet.copyOf(Iterables.concat(appProviders.keySet(), allApis.keySet()));
	
	
	private String ServiceType;
	private List<StagePrice> prices;
	
	
	private String Identity;
	private String Credential;
	private BlobStoreContext context;
	
	public StorageService(int id, String Provider, String Location, String ServiceType, List<StagePrice> prices)
	{
		super(id, Provider, Location);
		if(this.getProvider().toLowerCase().equals("amazon"))
		{
			this.setProvider(AmazonS3);
		}
		this.ServiceType = ServiceType;
		this.prices = prices;
	}
	
	public String getServiceType()
	{
		return this.ServiceType;
	}
	
	public List<StagePrice> getPrices()
	{
		return this.prices;
	}
	
	public boolean ServiceInit(String Identity, String Credential)
	{
		if(!allKeys.contains(this.getProvider()))
		{
			return false;
		}
		
		this.Identity = Identity;
		this.Credential = Credential;
		
		if(context == null){
			this.context = ContextBuilder.newBuilder(this.getProvider())
	                .credentials(this.Identity, this.Credential)
	                .buildView(BlobStoreContext.class);
		}
		
		return true;
	}

	public boolean UploadFile(String folderName, String fileName, File file)
	{
		if(this.context == null)
			return false;
		
		// Create Container
        BlobStore blobStore = context.getBlobStore();
        blobStore.createContainerInLocation(null, folderName);
        
        // Add Blob
        Blob blob = blobStore.blobBuilder(fileName).payload(file).build();
        blobStore.putBlob(folderName, blob);
        
        return true;
	}
	
	public boolean DownloadFile(String bucketName, String srcFileName, String dstFileName) throws Exception
	{
		if(this.context == null)
			return false;
		
		BlobStore blobStore = context.getBlobStore();
		InputStream in = null;
		Blob b = blobStore.getBlob(bucketName, srcFileName);
      	Payload pl = b.getPayload();
      	in = pl.getInput();
        
        if(in == null)
        	return false;
        
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
        byte[] data = new byte[4096];  
        int count = -1;  
        while((count = in.read(data,0,4096)) != -1)  
            outStream.write(data, 0, count);  
          
        data = null;  
        String resultStr = new String(outStream.toByteArray(),"ISO-8859-1");
       	 
       	 File result = new File(dstFileName);
       	BufferedWriter bw = null;
       	 if(!result.exists())
       	 {
       		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(result)));
       		bw.write(resultStr);
       	 }
       	 bw.close();
        
        return true;
	}

	public boolean CreateBucket(String bucketName)
	{
		if(this.context == null)
			return false;
		
		BlobStore blobStore = context.getBlobStore();
		return blobStore.createContainerInLocation(null, bucketName);
	}
	
	public boolean CreateDirectory(String bucketName, String containerName)
	{
		if(this.context == null)
			return false;
		
		BlobStore blobStore = context.getBlobStore();
		blobStore.createDirectory(bucketName, containerName);
		
		return true;
	}

	public boolean DeleteBlob(String bucketName, String blobName)
	{
		if(this.context == null)
			return false;
		
		BlobStore blobStore = context.getBlobStore();
		blobStore.removeBlob(bucketName, blobName);
		
		return true;
	}
	
	public boolean DeleteBucket(String bucketName)
	{
		if(this.context == null)
			return false;
		
		BlobStore blobStore = context.getBlobStore();
		blobStore.deleteContainer(bucketName);
		
		return true;
	}
	
	public boolean DeleteDirectory(String bucketName, String directoryName)
	{
		if(this.context == null)
			return false;
		
		BlobStore blobStore = context.getBlobStore();
		blobStore.deleteDirectory(bucketName, directoryName);
		
		return true;
	}
	
	public FileNode getFileTopology()
	{
		BlobStore blobStore = context.getBlobStore();
		FileNode root = null;
		
		
		PageSet<? extends StorageMetadata> smList = blobStore.list();
        Iterator<? extends StorageMetadata> it = smList.iterator();
        root = new FileNode(null, null, null, null);
        FileNode curNode = root;
        
        while(it.hasNext())
        {
        	StorageMetadata sm = it.next();
        	String name = sm.getName();
        	StorageType type = sm.getType();
        	
        	curNode.setNext(new FileNode(name, type, null, name));
        	curNode = curNode.getNext();
        	
        	PageSet<? extends StorageMetadata> subSmList = blobStore.list(name);
        	Iterator<? extends StorageMetadata> subIt = subSmList.iterator();
        	
        	FileNode rootSubNode = null;
        	FileNode curSubNode = rootSubNode;
        	StorageMetadata subSm = null;
        	if(subIt.hasNext() && (subSm = subIt.next()) != null)
        	{
        		String subName = subSm.getName();
        		StorageType subType = subSm.getType();
        		rootSubNode = new FileNode(subName, subType, curNode.buildPath(subName), curNode.getBucket());
        		curSubNode = rootSubNode;
        		curNode.setSon(rootSubNode);
        		if(subType == StorageType.RELATIVE_PATH)
            	{
            		PageSet<? extends StorageMetadata> postList = 
                   		 blobStore.list(name,inDirectory(curSubNode.getPath()).maxResults(1000));
            		buildFileSonTopology(curSubNode, blobStore, postList);
            	}
        	}
            while(subIt.hasNext())
            {
            	subSm = subIt.next();
            	String subName = subSm.getName();
            	StorageType subType = subSm.getType();
            	curSubNode.setNext(new FileNode(subName, subType, curNode.buildPath(subName), curNode.getBucket()));
        		curSubNode = curSubNode.getNext();
            	
            	if(subType == StorageType.RELATIVE_PATH)
            	{
            		PageSet<? extends StorageMetadata> postList = 
                   		 blobStore.list(name,inDirectory(curSubNode.getPath()).maxResults(1000));
            		buildFileSonTopology(curSubNode, blobStore, postList);
            	}
            }
        	
        }
        
        return root;
	}

	
	public void buildFileSonTopology(FileNode curNode, BlobStore blobStore, 
			PageSet<? extends StorageMetadata> smList)
	{
		if(curNode == null || blobStore == null)
			return ;
		
		Iterator<? extends StorageMetadata> subIt = smList.iterator();
		
		FileNode rootSubNode = null;
    	FileNode curSubNode = rootSubNode;
    	StorageMetadata subSm = null;
    	while(subIt.hasNext()){
	    	if(curSubNode == null && (subSm = subIt.next()) != null)
	    	{
	    		if(subSm.getName().equals(curNode.getPath()))
	    			continue;
	    		String subName = subSm.getName();
	    		StorageType subType = subSm.getType();
	    		subName = subName.substring(curNode.getPath().length() + 1);
	    		rootSubNode = new FileNode(subName, subType, curNode.buildPath(subName), curNode.getBucket());
	    		curSubNode = rootSubNode;
	    		curNode.setSon(rootSubNode);
	    		if(subType == StorageType.RELATIVE_PATH)
	        	{
	        		PageSet<? extends StorageMetadata> postList = 
	               		 blobStore.list(curNode.getBucket(),inDirectory(curSubNode.getPath()).maxResults(1000));
	        		buildFileSonTopology(curSubNode, blobStore, postList);
	        	}
	    	}
			while(subIt.hasNext())
			{
				if(rootSubNode == null)
					break;
				subSm = subIt.next();
	        	String subName = subSm.getName();
	        	if(subName.equals(curNode.getPath()))
	        		continue;
	        	subName = subName.substring(curNode.getPath().length() + 1);
	        	StorageType subType = subSm.getType();
	        	curSubNode.setNext(new FileNode(subName, subType, curNode.buildPath(subName), curNode.getBucket()));
	    		curSubNode = curSubNode.getNext();
	        	
	        	if(subType == StorageType.RELATIVE_PATH)
	        	{
	        		PageSet<? extends StorageMetadata> postList = 
	               		 blobStore.list(curNode.getBucket(),inDirectory(curSubNode.getPath()).maxResults(1000));
	        		buildFileSonTopology(curSubNode, blobStore, postList);
	        	}
			}
		}
	}
	

	public void ServiceClose()
	{
		this.context.close();
	}
	
	
}
