package cit.workflow.elements;

import java.io.Serializable;
import java.util.Map;

/**
 * This class handles the Role objects in the System
 * The Role handles the Task in the process
 * @author SilentWings
 */
public class Role implements Serializable {
	private int roleID;
	private String roleName;
	private String description;
	private String rights;
	private float standardCost;
	private float extraCost;
	
	public Role() {
		roleID = -1;
		roleName = null;
		description = null;
		rights = null;
		standardCost = 0;
		extraCost = 0;
	}
	
	public Role(Map roleMap) {
		this();
		if (null != roleMap) {
			if (null != roleMap.get("roleID")) {
				roleID = (Integer)roleMap.get("roleID");
			}
			if (null != roleMap.get("roleName")) {
				roleName = (String)roleMap.get("roleName");
			}
			if (null != roleMap.get("description")) {
				description = (String)roleMap.get("description");
			}
			if (null != roleMap.get("rights")) {
				rights = (String)roleMap.get("rights");
			}
			if (null != roleMap.get("standardCost")) {
				standardCost = (Float)roleMap.get("standardCost");
			}
			if (null != roleMap.get("extraCost")) {
				extraCost = (Float)roleMap.get("extraCost");
			}
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public float getExtraCost() {
		return extraCost;
	}

	public void setExtraCost(float extraCost) {
		this.extraCost = extraCost;
	}

	public String getRights() {
		return rights;
	}

	public void setRights(String rights) {
		this.rights = rights;
	}

	public int getRoleID() {
		return roleID;
	}

	public void setRoleID(int roleID) {
		this.roleID = roleID;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public float getStandardCost() {
		return standardCost;
	}

	public void setStandardCost(float standardCost) {
		this.standardCost = standardCost;
	}
	
	
}
