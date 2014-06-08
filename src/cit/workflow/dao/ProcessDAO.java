/*
 * Created on 2005-4-11
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import cit.workflow.Constants;
import cit.workflow.elements.FlowObject;
import cit.workflow.elements.variables.DocumentVariable;
import cit.workflow.elements.variables.InherentVariable;
import cit.workflow.elements.variables.ObjectVariable;
import cit.workflow.elements.variables.ProcessObject;
import cit.workflow.elements.variables.XMLVariable;
import cit.workflow.engine.droolsruleengine.ExtendedDroolsRule;
import cit.workflow.exception.WorkflowTransactionException;
import cit.workflow.utils.DBUtility;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ProcessDAO  extends DBUtility {
	
	public ProcessDAO(Connection conn) {
		super(conn);
	}
	
	

	//sxh add 2007
	/**
	 * 功能：查询数据库获得当前运行的流程
	 * @return 流程列表
	 * @throws WorkflowTransactionException
	 */
	public List getProcess() throws WorkflowTransactionException {
		sql = "SELECT * FROM ProcessInformation where state=?";
		params = new Object[] {new String(Constants.PROCESS_STATE_RUNNING)};
		types = new int[] {Types.VARCHAR};
		List resultList = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException("ProcessDAO.getProcess throw exception.");
		}
		return resultList;
	}
	
	/**
	 * 功能：根据角色ID选择该用户能够进行相应操作的流程ID集合
	 * @param roleID
	 * @param functionType
	 * @return 流程ID集合
	 * @throws WorkflowTransactionException
	 */
	public String[] getProcessIDSet(int roleID, int functionType) throws WorkflowTransactionException {
		sql = "SELECT proi.processID FROM ProcessInformation proi LEFT JOIN ProcessResponsibleRoles prr ON prr.ProcessID=proi.ProcessID WHERE prr.roleID = ? AND prr.FunctionType = ?";
		params = new Object[] {new Integer(roleID), new Integer(functionType)};
		types = new int[] {Types.INTEGER, Types.INTEGER};
		List resultList = null;
		int size;
		String[] processIDSet = null;
		Map processMap;
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException("ProcessDAO.getProcessIDSet throw exception.");
		}
		size = resultList.size();
		processIDSet = new String[size];
		for (int i = 0; i < size; ++i) {
			processMap = (Map)resultList.get(i);
			processIDSet[i] = (String)processMap.get("processID");
		}
		return processIDSet;
	}
	
	//sxh add 2007 end
	/**
	 * 得到流程信息
	 * @param processID 流程ID
	 * @return 流程信息，以Map形式返回
	 * @throws WorkflowTransactionException
	 */
	public Map getProcessInformation(String processID) throws WorkflowTransactionException {
		sql = "SELECT * FROM ProcessInformation WHERE ProcessID=?";
		params = new Object[] {processID};
		types = new int[] {Types.VARCHAR};
		List resultList = null;
		Map resultMap = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException("ProcessDAO.getProcessInformation throw exception.");
		}
		if (resultList != null) {
			resultMap = (Map)resultList.get(0);
		}
		return resultMap;
	}
	
	/**
	 * 得到流程信息
	 * @param processID 流程ID
	 * @return 流程信息，以Map形式返回
	 * @throws WorkflowTransactionException
	 */
	public Map getProcessMonitorInformation(String processID) throws WorkflowTransactionException {
		sql = "SELECT pi.processID, pi.processName, pti.definedStartDate, pti.actualStartDate, pti.definedEndDate, pti.actualEndDate, pi.state FROM ProcessInformation pi LEFT JOIN ProcessResponsibleRoles prr ON pi.processID = prr.processID LEFT JOIN ProcessTWCInformation pti ON pi.twcID = pti.twcID WHERE pi.processID = ?";
		params = new Object[] {processID};
		types = new int[] {Types.VARCHAR};
		List resultList = null;
		Map resultMap = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException("ProcessDAO.getProcessInformation throw exception.");
		}
		if (resultList != null) {
			resultMap = (Map)resultList.get(0);
		}
		return resultMap;
	}
	/**
	 * 得到Monitor流程信息列表
	 * @param roleID 角色ID
	 * @param functionType 功能类型
	 * @return 流程信息列表
	 * @throws WorkflowTransactionException
	 */
	public List getProcessMonitorInformationList(int roleID, int functionType) throws WorkflowTransactionException {
		sql = "SELECT pi.processID, pi.processName, pti.definedStartDate, pti.actualStartDate, pti.definedEndDate, pti.actualEndDate, pi.state FROM ProcessInformation pi LEFT JOIN ProcessResponsibleRoles prr ON pi.processID = prr.processID LEFT JOIN ProcessTWCInformation pti ON pi.processID = pti.processID AND pi.twcID = pti.twcID WHERE prr.roleID = ? AND prr.functionType = ?";
		params = new Object[] {new Integer(roleID), new Integer(functionType)};
		types = new int[] {Types.INTEGER, Types.INTEGER};
		List resultList = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException("ProcessDAO.getProcessIDSet throw exception.");
		}
		return resultList;
	}
	
	
	
	/**
	 * 功能：根据角色ID选择该用户能够进行相应操作的流程集合
	 * @param roleID				角色的ID
	 * @param functionType			见表：ProcessResponsibleRoles中的FunctionType
	 * @return						工作流模型集合
	 */
	public List getProcess(int roleID, int functionType) throws WorkflowTransactionException {
		sql = "SELECT * FROM ProcessInformation proi LEFT JOIN ProcessResponsibleRoles prr ON prr.ProcessID=proi.ProcessID WHERE prr.roleID = ? AND prr.FunctionType = ?";
		params = new Object[] {new Integer(roleID), new Integer(functionType)};
		types = new int[] {Types.INTEGER, Types.INTEGER};
		List resultList = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException("ProcessDAO.getProcess throw exception.");
		}
		return resultList;
	}
	
	/**
	 * 根据条件参数得到活动列表
	 * @param paramMap 条件参数，将多组条件以Map形式封装，每一组条件对应于一组key和value，key为数据库中字段名称，value为字段的值
	 * @return 符合条件的活动列表
	 * @throws WorkflowTransactionException
	 */
	public List getActivity(Map paramMap) throws WorkflowTransactionException {
		StringBuffer sqlBuffer = new StringBuffer("SELECT * FROM ProcessActivityInformation pai WHERE 1=1 ");
		List paramList = new ArrayList();
		Set keySet= paramMap.entrySet();
		Iterator iterator = keySet.iterator();
		while (iterator.hasNext()) {
		    Map.Entry entry = (Map.Entry) iterator.next();
			sqlBuffer.append("AND ").append((String) entry.getKey()).append("=? ");
			paramList.add(entry.getValue());
		}
		
		sql = sqlBuffer.toString();
		params = paramList.toArray();
		types = null;
		
		List resultList = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException("ProcessDAO.getActivity throw exception.");
		}
		return resultList;
	}
	//sxh modified 2007.9
	public Map getActivityInformation(String processID, int activityID) throws WorkflowTransactionException {
		sql = "SELECT * FROM ProcessActivityInformation WHERE ProcessID=? AND ActivityID=?";
		params = new Object[] {processID, new Integer(activityID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		List resultList = null;
		Map resultMap = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException("ProcessDAO.getActivityInformation throw exception.");
		}
		if (resultList != null) {
			resultMap = (Map)resultList.get(0);
		}
		return resultMap;
	}
	
	public List getProcessActivityList(String processID) throws WorkflowTransactionException {
		sql = "SELECT * FROM ProcessActivityInformation WHERE ProcessID=?";
		params = new Object[] {processID};
		types = new int[] {Types.VARCHAR};
		List resultList = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException("ProcessDAO.getActivityInformation throw exception.");
		}
		return resultList;
	}
	
	public String getActivityURL(String processID, int activityID) throws WorkflowTransactionException {
		sql = "SELECT activityURL FROM ProcessActivityInformation WHERE ProcessID=? AND ActivityID=?";
		params = new Object[] {processID, new Integer(activityID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		List resultList = null;
		Map resultMap = null;
		String url = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException("ProcessDAO.getActivityInformation throw exception.");
		}
		if (resultList != null) {
			resultMap = (Map)resultList.get(0);
			url = (String)resultMap.get("activityURL");
		}
		return url;
	}
	//sxh modified 2007.9 end
	
	public boolean setProcessInformation(String processID,String processName, String processDescription, boolean persistent) throws WorkflowTransactionException {
		boolean suceess = false;
		sql = "UPDATE ProcessInformation SET ProcessName=?,Description=?,Persistent=?  WHERE ProcessID=?";		 
		params = new Object[] {processName, processDescription,new Boolean(persistent),processID};
		types = new int[] {Types.VARCHAR, Types.VARCHAR,Types.BIT,Types.VARCHAR};
		try {
			executeUpdate();
			suceess = true;
		}catch(SQLException e) {
			e.printStackTrace();
			return false;			
		}		
		return suceess;
	}
	
	//sxh add 2007.11
	/**
	 * 设置流程名称
	 * @param processID 该流程的ID
	 * @param processName 将要设置的流程名称
	 * @return 是否设置成功
	 * @throws WorkflowTransactionException
	 */
	public boolean setProcessName(String processID, String processName) throws WorkflowTransactionException {
		boolean suceess = false;
		sql = "UPDATE ProcessInformation SET ProcessName=? WHERE ProcessID=?";		 
		params = new Object[] {processName, processID};
		types = new int[] {Types.VARCHAR, Types.VARCHAR};
		try {
			executeUpdate();
			suceess = true;
		}catch(SQLException e) {
			e.printStackTrace();
			return false;			
		}		
		return suceess;
	}
	//sxh add 2007.11 end
	
	
	//sxh add 2007.10
	public boolean addProcessPersonBinding(String processID, int personID, int roleID) {
		boolean success = false;
		
		sql = "INSERT ProcessPerson SET ProcessID=?, PersonID=?, RoleID=?";
		params = new Object[] {processID, new Integer(personID), new Integer(roleID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER, Types.INTEGER};
		try {
			executeUpdate();
			success = true;
		}catch(SQLException e) {
			e.printStackTrace();
			return false;			
		}
		return success;
	}
	
	public boolean addProcessActivityTeamMember(String processID, int activityID, int personID, int roleID) {
		boolean success = false;
		sql = "INSERT ProcessActivityTeamMember SET ProcessID=?, ActivityID=?, PersonID=?, RoleID=?";
		params = new Object[] {processID, new Integer(activityID), new Integer(personID), new Integer(roleID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.INTEGER};
		try {
			executeUpdate();
			success = true;
		}catch(SQLException e) {
			e.printStackTrace();
			return false;			
		}
		return success;
	}
	//sxh add 2007.10 end
	
//	 sxh add 2007

	/**
	 * 获得某活动的输出对象列表
	 * 
	 * @param processID:该活动所属的流程ID
	 * @param activityID:该活动ID
	 * @return 输出对象列表
	 * @throws SQLException
	 */
	public List getInputObjectList(String processID, int activityID)
			throws SQLException {
		List objectFlowList = new ArrayList();
		sql = "select objectID from processFlowObjects as a , ProcessFlowObjectControl as b where a.FromActivityID <> 0 and a.ToActivityID = ? and  a.ProcessID =? and b.ProcessID =? and b.State = ? and a.FlowID = b.FlowID ";
		params = new Object[] { new Integer(activityID), processID, processID,
				Constants.DATAFLOW_STATE_ACTIVE };
		types = new int[] { Types.INTEGER, Types.VARCHAR, Types.VARCHAR,
				Types.VARCHAR };

		List objectList = new ArrayList();
		try {
			objectList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException(
					"WorkflowObjectDAO.getActivityObjectFlow throw exception.");
		}
		Iterator objectIterator = objectList.iterator();
		while (objectIterator.hasNext()) {
			Map objectMap = (Map) objectIterator.next();
			int objectID = ((Integer) objectMap.get("ObjectID")).intValue();
			ProcessObject processObject = getProcessObjectDetail(processID,
					objectID);

			objectFlowList.add(processObject);
		}
		return objectFlowList;
	}

	/**
	 * 查询数据库获得某活动的输出对象列表
	 * 
	 * @param processID:该活动所属的流程ID
	 * @param activityID:该活动ID
	 * @return 输出对象列表
	 * @throws SQLException
	 */
	public List getOutputObjectList(String processID, int activityID)
			throws SQLException {
		List objectFlowList = new ArrayList();
		sql = "select distinct objectID from processFlowObjects as a , ProcessFlowObjectControl as b where a.FromActivityID = ? and  a.ProcessID =? and b.ProcessID =? and a.FlowID = b.FlowID ";
		params = new Object[] { new Integer(activityID), processID, processID };
		types = new int[] { Types.INTEGER, Types.VARCHAR, Types.VARCHAR };
		List objectList = new ArrayList();
		try {
			objectList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException(
					"WorkflowObjectDAO.getActivityObjectFlow throw exception.");
		}
		Iterator objectIterator = objectList.iterator();
		while (objectIterator.hasNext()) {
			Map objectMap = (Map) objectIterator.next();
			int objectID = ((Integer) objectMap.get("ObjectID")).intValue();
			ProcessObject processObject = getProcessObjectDetail(processID,
					objectID);

			objectFlowList.add(processObject);
		}
		return objectFlowList;
	}

	/**
	 * 获得流程的输入对象列表
	 * 
	 * @param processID:流程ID
	 * @return 输入对象列表
	 * @throws SQLException
	 */
	public List getProcessInputObjectList(String processID) throws SQLException {
		List objectFlowList = new ArrayList();
		int activityID = 0;
		sql = "select distinct objectID from processFlowObjects as a , ProcessFlowObjectControl as b where a.FromActivityID = ? and  a.ProcessID =? and b.ProcessID =? and a.FlowID = b.FlowID ";
		params = new Object[] { new Integer(activityID), processID, processID, };
		types = new int[] { Types.INTEGER, Types.VARCHAR, Types.VARCHAR };
		List objectList = new ArrayList();
		try {
			objectList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException(
					"WorkflowObjectDAO.getActivityObjectFlow throw exception.");
		}
		Iterator objectIterator = objectList.iterator();
		while (objectIterator.hasNext()) {
			Map objectMap = (Map) objectIterator.next();
			int objectID = ((Integer) objectMap.get("ObjectID")).intValue();
			ProcessObject processObject = getProcessObjectDetail(processID,
					objectID);

			objectFlowList.add(processObject);
		}
		return objectFlowList;
	}

	/**
	 * 获得流程的输出对象列表
	 * 
	 * @param processID:流程ID
	 * @return 输出对象列表
	 * @throws SQLException
	 */
	public List getProcessOutputObjectList(String processID)
			throws SQLException {
		List objectFlowList = getOutputObjectList(processID,
				Constants.ACTIVITY_END);
		return objectFlowList;
	}

	/**
	 * 重构流程对象
	 * @param processID 流程ID
	 * @param objectID 对象ID
	 * @return 重构好的对象
	 */
	private ProcessObject getProcessObjectDetail(String processID, int objectID) {
		ProcessObject processObject;
		sql = "select PO.*, PFOC.privilege from ProcessObject PO left join ProcessFlowObjectControl PFOC on PO.processID = PFOC.processID and PO.objectID = PFOC.objectID where PO.processID = ? and  PO.ObjectID = ?";
		params = new Object[] { processID, new Integer(objectID) };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		Map processObjectMap = null;
		processObject = null;

		try {
			processObjectMap = (Map) executeQuery(new MapHandler());
		} catch (SQLException e) {
			e.printStackTrace();
			// return;
		}
		if (processObjectMap != null) {
			int objectType, scope;
			objectType = ((Integer) processObjectMap.get("ObjectType"))
					.intValue();
			scope = ((Integer) processObjectMap.get("Scope")).intValue();
			// 内部变量
			if (objectType == 1) {
				processObject = new InherentVariable(conn, processID, objectID);
			} else if (objectType == 2) {
				processObject = new ObjectVariable(conn, processID, objectID);
			} else if (objectType == 3) {
				processObject = new XMLVariable(conn, processID, objectID);
			} else if (objectType == 4) {
				processObject = new DocumentVariable(conn, processID, objectID);
			}
			processObject.setPrivilege((Integer) processObjectMap.get("Privilege"));
		}
		return processObject;
	}

	// sxh add 2007 end

	// sxh add 2007.9
	public List getInternalObjectList(String processID, int activityID)
			throws SQLException {
		List objectFlowList = new ArrayList();
		sql = "select objectID from processFlowObjects as a , ProcessFlowObjectControl as b where a.FromActivityID = 0 and a.ToActivityID = ? and  a.ProcessID =? and b.ProcessID =? and b.State = ? and a.FlowID = b.FlowID";
		params = new Object[] { new Integer(activityID), processID, processID,
				Constants.DATAFLOW_STATE_ACTIVE };
		types = new int[] { Types.INTEGER, Types.VARCHAR, Types.VARCHAR,
				Types.VARCHAR };

		List objectList = new ArrayList();
		try {
			objectList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException(
					"WorkflowObjectDAO.getActivityObjectFlow throw exception.");
		}
		Iterator objectIterator = objectList.iterator();
		while (objectIterator.hasNext()) {
			Map objectMap = (Map) objectIterator.next();
			int objectID = ((Integer) objectMap.get("ObjectID")).intValue();
			ProcessObject processObject = getProcessObjectDetail(processID,
					objectID);
			objectFlowList.add(processObject);
		}
		return objectFlowList;
	}

	// sxh add 2007.9 end
	
	// sxh add 2007.9
	/*
	 * public List getDocumentInformatoin(String processID, int objectID) { sql =
	 * "select WI.packageID, WI.workflowID from workflowInformation as WI,
	 * processInformation as PI, processObject as PO where WI.WorkflowName =
	 * PI.ProcessName and PI.processID = PO.ProcessID and PO.ProcessID = ? and
	 * PO.ObjectID = ?"; params = new Object[] { processID, new
	 * Integer(objectID) }; types = new int[] { Types.VARCHAR, Types.INTEGER };
	 * List resultList = null; try { resultList = (List) executeQuery(new
	 * MapListHandler());
	 *  } catch (SQLException e) { e.printStackTrace(); } return resultList; }
	 */
	// sxh add 2007.9 end
	
	/**
	 * the function is to get the object flow sets of an activity
	 * 
	 * @param processID :
	 *            the running process
	 * @param activityID:
	 *            the activity of getting object flow
	 * @return list of object flows
	 * @throws SQLException
	 *             added by dy 2006.3.20
	 */
	public List getActivityObjectFlow(String processID, int activityID)
			throws SQLException {
		List objectFlowList = new ArrayList();
		sql = "select objectID from processFlowObjects as a , ProcessFlowObjectControl as b where a.ToActivityID = ? and  a.ProcessID =? and b.ProcessID =? and b.State = ? and a.FlowID = b.FlowID ";
		params = new Object[] { new Integer(activityID), processID, processID,
				Constants.DATAFLOW_STATE_ACTIVE };
		types = new int[] { Types.INTEGER, Types.VARCHAR, Types.VARCHAR,
				Types.VARCHAR };

		List objectList = new ArrayList();
		try {
			objectList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException(
					"WorkflowObjectDAO.getActivityObjectFlow throw exception.");
		}
		Iterator objectIterator = objectList.iterator();
		while (objectIterator.hasNext()) {
			Map objectMap = (Map) objectIterator.next();
			FlowObject flowObject = new FlowObject();
			// objectFlowList.add((Integer)objectMap.get("ObjectID"));
			flowObject.setObjectID(((Integer) objectMap.get("ObjectID"))
					.intValue());
			// flowObject.setObjectID();
			/**
			 * 根据ObjectID从相应的数据表中取出其详细信息
			 */
			getFlowObjectDetail(processID, flowObject);
			objectFlowList.add(flowObject);
		}
		return objectFlowList;
	}

	public void setXMLDocument(String processID, int objectID, String objectXML) {
		sql = "UPDATE ProcessXMLDocument SET XML=? WHERE ProcessID=? AND ObjectID=?";
		params = new Object[] { processID, new Integer(objectID) };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		try {
			executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * this method is used to get the object details,such as name,URL and so on
	 * 算法：1 根据objectID查询表ProcessObject得到object信息：objecttype,scope 2 根据objecttype
	 * 如果是（4：文档对象 ） 则根据scope的值： 0 从表ProcessDocument中读取信息 1 从package相关表中读取信息
	 * 
	 * 3 根据objecttype 如果是（3：XML对象 ） 则根据scope的值： 0 从表ProcessXMLDocument中读取信息 1
	 * 从package相关表中读取信息
	 * 
	 * @param processID
	 * @param flowObject
	 *            add by dy 2006.4.18
	 */
	private void getFlowObjectDetail(String processID, FlowObject flowObject) {
		sql = "select * from ProcessObject where processID = ? and  ObjectID =?";
		params = new Object[] { processID,
				new Integer(flowObject.getObjectID()) };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		Map processObjectMap = null;
		try {
			processObjectMap = (Map) executeQuery(new MapHandler());
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		if (processObjectMap != null) {
			int objectType, scope;
			objectType = ((Integer) processObjectMap.get("ObjectType"))
					.intValue();
			scope = ((Integer) processObjectMap.get("Scope")).intValue();
			flowObject.setObjectType(objectType);
			// 文档对象
			if (objectType == 4) {
				// 文档来自自身
				if (scope == 0) {
					sql = "select * from ProcessDocument where processID = ? and  ObjectID =? ";
					params = new Object[] { processID,
							new Integer(flowObject.getObjectID()) };
					types = new int[] { Types.VARCHAR, Types.INTEGER };
					Map resultMap = null;
					try {
						resultMap = (Map) executeQuery(new MapHandler());
						if (resultMap != null) {
							String objectName = resultMap.get("ObjectName")
									.toString();
							String objectPath = resultMap.get("Path")
									.toString();
							flowObject.setObjectName(objectName);
							flowObject.setObjectLocation(objectPath);
							flowObject.setDescription(resultMap.get(
									"Description").toString());
						}
					} catch (SQLException e) {
						e.printStackTrace();
						return;
					}
				}
				// 文档来自package
				else if (scope == 1) {

				}
			}
			// XML对象
			if (objectType == 3) {
				sql = "select * from ProcessXMLDocument where processID = ? and  ObjectID =? ";
				params = new Object[] { processID,
						new Integer(flowObject.getObjectID()) };
				types = new int[] { Types.VARCHAR, Types.INTEGER };
				Map resultMap = null;
				try {
					resultMap = (Map) executeQuery(new MapHandler());
					if (resultMap != null) {
						String objectName = resultMap.get("ObjectName")
								.toString();
						flowObject.setObjectName(objectName);
						flowObject
								.setObjectXML(resultMap.get("XML").toString());
						flowObject.setDescription(resultMap.get("Description")
								.toString());
					}
				} catch (SQLException e) {
					e.printStackTrace();
					return;
				}
			}
		}

	}
	//sxh add 2008.1
	public void getApplicationInput(String processID, int activityID) {
		
	}
	//sxh add 2008.1 end
	
//	lrj add begin  07-11-7
	/**
	 * @param processID
	 * @param eventID
	 * @return 规则文件
	 * @throws SQLException
	 */
	public ArrayList getProcessExtendedRule(String processID,int eventID) throws SQLException
	{
		ExtendedDroolsRule rule = new ExtendedDroolsRule();
		ArrayList ruleList = new ArrayList();
		PreparedStatement pstmt = null;
		sql = "SELECT * from ProcessExtendedRules " +
				"WHERE ProcessID = ? " +
				"AND EventID = ?";
		try
		{
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,processID);
			pstmt.setInt(2,eventID);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next())
			{
				rule.setProcessID(rs.getString("ProcessID"));
				rule.setEventID(rs.getInt("EventID"));
				rule.setRuleFileID(rs.getInt("RuleFileID"));
				rule.setRuleContent(rs.getString("RuleContent"));
				ruleList.add(rule);
			}
			pstmt.close();
		}
		catch (SQLException e)
		{
			//servercomment System.out.println(e);
			throw new WorkflowTransactionException("WorkflowAgentRules.getWorkflowAgentRuleContent throw exception.");
		}
		return ruleList;
	}

	//lrj add end  07-12-7
}