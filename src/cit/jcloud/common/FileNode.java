package cit.jcloud.common;

import org.jclouds.blobstore.domain.StorageType;

public class FileNode {

	
	private String value;
	private StorageType type;
	private String path;
	private String bucket;
	private FileNode next;
	private FileNode son;

	/**
	 * Constructor with value as parameter
	 * 
	 * @param value
	 */
	public FileNode(String value, StorageType type, String path, String bucket)
	{
		this.value = value;
		this.type = type;
		this.path = path;
		this.bucket = bucket;
		this.next = null;
		this.son = null;
	}
	
	/**
	 * Constructor with full parameters
	 * 
	 * @param value
	 * @param next
	 * @param son
	 */
	public FileNode(String value, StorageType type, String path, String bucket, FileNode next, FileNode son)
	{
		this.value = value;
		this.type = type;
		this.path = path;
		this.bucket = bucket;
		this.next = next;
		this.son = son;
	}
	/**
	 * Get value
	 * 
	 * @return
	 */
	public String getValue()
	{
		return this.value;
	}
	
	/**
	 * Get type
	 * 
	 * @return
	 */
	public StorageType getType()
	{
		return this.type;
	}
	
	/**
	 * Get path
	 * 
	 * @return
	 */
	public String getPath()
	{
		return this.path;
	}
	
	/**
	 * Get bucket
	 * 
	 * @return
	 */
	public String getBucket()
	{
		return this.bucket;
	}
	
	/**
	 * Get file node next to this
	 * 
	 * @return
	 */
	public FileNode getNext()
	{
		return this.next;
	}
	
	/**
	 * Get file node which is the first son of this
	 * 
	 * @return
	 */
	public FileNode getSon()
	{
		return this.son;
	}
	
	/**
	 * Set value
	 * 
	 * @param value
	 */
	public void setValue(String value)
	{
		this.value = value;
	}
	
	
	/**
	 * Set type
	 * 
	 * @param type
	 */
	public void setType(StorageType type)
	{
		this.type = type;
	}
	
	/**
	 * Set path
	 * 
	 * @param value
	 */
	public void setPath(String path)
	{
		this.path = path;
	}
	
	/**
	 * Set bucket
	 * 
	 * @param bucket
	 */
	public void setBucket(String bucket)
	{
		this.bucket = bucket;
	}
	
	/**
	 * Set the next file node
	 * 
	 * @param next
	 */
	public void setNext(FileNode next)
	{
		this.next = next;
	}
	
	/**
	 * Set the son file node
	 * 
	 * @param son
	 */
	public void setSon(FileNode son)
	{
		this.son = son;
	}
	
	public String buildPath(String name)
	{
		if(this.path == null)
			return name;
		return this.path + "/" + name;
	}
	

}
