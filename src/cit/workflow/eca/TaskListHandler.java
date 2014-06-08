/*
 * Created on 2004-10-20
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.eca;

import java.sql.Connection;
import java.util.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ListIterator;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.log4j.Logger;

import cit.workflow.Constants;
import cit.workflow.elements.Task;
import cit.workflow.elements.activities.AbstractActivity;
import cit.workflow.utils.DBUtility;

/**
 * @author weiwei
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TaskListHandler extends DBUtility {

	private static Logger logger = Logger.getLogger(TaskListHandler.class);

	private ProcessManager processManager;

	public TaskListHandler(Connection conn, ProcessManager processManager) {
		super(conn);

		this.processManager = processManager;
	}

	// sxh add 2007.10
	public boolean acceptTask(String processID, int activityID) {
		int personID = this.processManager.getCurrentUser().getActorID();
		// 1、调用GetTaskList，判断用户是否可以接受该任务，这意味着该任务必须在任务项中，并且分配状态为“UnAccepted”
		List taskList = getTaskList();
		if (taskList != null) {
			try {
				Iterator taskIterator = taskList.iterator();
				while (taskIterator.hasNext()) {
					Task task = (Task) taskIterator.next();
					int roleID = task.getRoleID();
					if (task.getProcessID().equals(processID)
							&& task.getActivityID() == activityID
							&& task.getAllocateState().equals(
									Constants.TASK_STATE_UNACCEPTED)) {
						// 从ProcessActivityInformation表提取信息
						sql = "select * from processActivityInformation where processID = ? and activityID = ?";
						params = new Object[] { processID,
								new Integer(activityID) };
						types = new int[] { Types.VARCHAR, Types.INTEGER };
						Map processActivityInformationMap = (Map) executeQuery(new MapHandler());
						int repeatedTime = 0;
						if (processActivityInformationMap != null
								&& processActivityInformationMap
										.get("repeatedTime") != null) {
							repeatedTime = (Integer) processActivityInformationMap
									.get("repeatedTime");
						}

						// 从ProcessTWCInformation表提取信息
						sql = "select PTI.* from processTWCInformation PTI left join processActivityInformation PAI on PTI.twcid = PAI.twcid where PAI.processID = ? and PAI.activityID = ?";
						params = new Object[] { processID,
								new Integer(activityID) };
						types = new int[] { Types.VARCHAR, Types.INTEGER };
						Map processTWCInformationMap = (Map) executeQuery(new MapHandler());
						Timestamp definedStartDate = null;
						Timestamp actualStartDate = null;
						Timestamp definedEndDate = null;
						Timestamp actualEndDate = null;
						int definedWorkload = 0;
						int actualWorkload = 0;

						if (processTWCInformationMap != null
								&& processTWCInformationMap.get("definedStartDate") != null) {
							definedStartDate = (Timestamp) processTWCInformationMap.get("definedStartDate");
						}
						if (processTWCInformationMap != null
								&& processTWCInformationMap.get("actualStartDate") != null) {
							actualStartDate = (Timestamp) processTWCInformationMap.get("actualStartDate");
						}
						if (processTWCInformationMap != null
								&& processTWCInformationMap.get("definedEndDate") != null) {
							definedEndDate = (Timestamp) processTWCInformationMap.get("definedEndDate");
						}
						if (processTWCInformationMap != null
								&& processTWCInformationMap.get("actualEndDate") != null) {
							actualEndDate = (Timestamp) processTWCInformationMap.get("actualEndDate");
						}
						if (processTWCInformationMap != null
								&& processTWCInformationMap.get("definedWorkload") != null) {
							definedWorkload = (Integer) processTWCInformationMap
									.get("definedWorkload");
						}

						if (processTWCInformationMap != null
								&& processTWCInformationMap.get("actualWorkload") != null) {
							actualWorkload = (Integer) processTWCInformationMap
									.get("actualWorkload");
						}

						// 2、在ProcessActivityPerson表中加入记录,其中State为Accepted,其它字段的信息来自于ActivityInformation(时间信息，RepeatedTime),
						// DefinedWorkload来自活动对应的TWCInformation的DefinedWorkload*(ProcessActivityRole.WorkloadRatio/AllocatedNumber)
						sql = "select * from processActivityRole where processid = ? and activityid = ? and roleid = ?";
						params = new Object[] { processID,
								new Integer(activityID), new Integer(roleID) };
						types = new int[] { Types.VARCHAR, Types.INTEGER,
								Types.INTEGER };
						Map processActivityRoleMap = (Map) executeQuery(new MapHandler());

						int allocatedNumber = 1;
						if (processActivityRoleMap != null
								&& processActivityRoleMap
										.get("allocatedNumber") != null) {
							allocatedNumber = (Integer) processActivityRoleMap
									.get("allocatedNumber");

						}
						float workloadRatio = 0;
						if (processActivityRoleMap != null
								&& processActivityRoleMap.get("workloadRatio") != null) {
							workloadRatio = (Float) processActivityRoleMap
									.get("workloadRatio");
						}

						sql = "insert processActivityPerson set processID = ?, activityID = ?, roleID = ?, personID = ?, repeatedTime = ?, DefinedStartDate = ?, ActualStartDate = ?, DefinedEndDate = ?, ActualEndDate = ?, DefinedWorkload = ?, ActualWorkload = ?, State = ?";
						params = new Object[] {
								processID,
								new Integer(activityID),
								new Integer(roleID),
								new Integer(personID),
								new Integer(repeatedTime),
								definedStartDate,
								actualStartDate,
								definedEndDate,
								actualEndDate,
								new Integer(
										(int) (definedWorkload * (workloadRatio / allocatedNumber))),
								actualWorkload, Constants.TASK_STATE_ACCEPTED };
						types = new int[] { Types.VARCHAR, Types.INTEGER,
								Types.INTEGER, Types.INTEGER, Types.INTEGER,
								Types.DATE, Types.DATE, Types.DATE, Types.DATE,
								Types.INTEGER, Types.INTEGER, Types.VARCHAR };
						executeUpdate();
						// (注意,每当该角色多分配一个人,那么,原来该任务的所有作为该角色的多位承担者所定义的工作负荷都需要更新)；将ProcessActivityRole中的AllocatedNumber加1，返回1：成功
						sql = "update processActivityRole set allocatedNumber = (allocatedNumber + 1) where processid = ? and activityID = ? and roleID = ?";
						params = new Object[] { processID,
								new Integer(activityID), new Integer(roleID) };
						executeUpdate();
						
						//(注意,每当该角色多分配一个人,那么,原来该任务的所有作为该角色的多位承担者所定义的工作负荷都需要更新)；
						sql = "update processActivityPerson set definedWorkload = ? where processID = ? and activityID = ? and roleID = ?";
						params = new Object[] { new Integer((int)(definedWorkload * (workloadRatio / allocatedNumber + 1))), processID, new Integer(activityID), new Integer(roleID) };
						types = new int[] { Types.INTEGER, Types.VARCHAR, Types.INTEGER, Types.INTEGER };
						executeUpdate();
						return true;
					}
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		return false;
	}

	// sxh add 2007.10 end

	public List getTaskList() {
		try {
			List taskList = new ArrayList();

			int personID = processManager.getCurrentUser().getActorID();
			// 1.读ProcessActivityPerson表,以PersonID查找到State为"Accepted"的任务,这些任务已经为该人员所接受,自然是分配给他了
			sql = "SELECT PAP.*, PAI.ActivityName, PAI.State as activityState, PI.ProcessName FROM ProcessActivityPerson PAP LEFT JOIN ProcessActivityInformation PAI ON PAI.ProcessID=PAP.ProcessID AND PAI.ActivityID=PAP.ActivityID LEFT JOIN ProcessInformation PI ON PI.ProcessID=PAP.ProcessID WHERE PAP.PersonID=? AND PAP.State=?";
			params = new Object[] { new Integer(personID),
					Constants.TASK_STATE_ACCEPTED };
			types = new int[] { Types.INTEGER, Types.VARCHAR };
			List processActivityPersonList = (List) executeQuery(new MapListHandler());

			Iterator processActivityPersonIterator = processActivityPersonList
					.iterator();
			while (processActivityPersonIterator.hasNext()) {
				Map processActivityPersonMap = (Map) processActivityPersonIterator
						.next();
				Task task = new Task();
				if (processActivityPersonMap.get("ProcessID") != null) {
					task.setProcessID((String) processActivityPersonMap
							.get("ProcessID"));
				}
				if (processActivityPersonMap.get("ProcessName") != null) {
					task.setProcessName((String) processActivityPersonMap
							.get("ProcessName"));
				}
				if (processActivityPersonMap.get("ActivityID") != null) {
					task.setActivityID(((Integer) processActivityPersonMap
							.get("ActivityID")).intValue());
				}
				if (processActivityPersonMap.get("ActivityName") != null) {
					task.setActivityName((String) processActivityPersonMap
							.get("ActivityName"));
				}
				if (processActivityPersonMap.get("roleID") != null) {
					task.setRoleID((Integer) processActivityPersonMap
							.get("roleID"));
				}
				if (processActivityPersonMap.get("DefinedStartDate") != null) {
					task.setDefinedStartDate(new java.sql.Date(
							((Date) processActivityPersonMap
									.get("DefinedStartDate")).getTime()));
				}
				if (processActivityPersonMap.get("DefinedEndDate") != null) {
					task.setDefinedEndDate(new java.sql.Date(
							((Date) processActivityPersonMap
									.get("DefinedEndDate")).getTime()));
				}
				if (processActivityPersonMap.get("ActivityState") != null) {
					task.setState((String) processActivityPersonMap
							.get("ActivityState"));
				}
				if (processActivityPersonMap.get("State") != null) {
					task.setAllocateState((String) processActivityPersonMap
							.get("State"));
				}
				// task.setisBind(true);

				// 增加任务到任务列表
				if (!taskList.contains(task)) {
					taskList.add(task);
					/*
					 * if ((!task.getState().equals("Completed")) &&
					 * logger.isInfoEnabled()) logger.info("ADD PERSON TASK
					 * LIST----" + task);
					 */
				}
			}
			

			// 2.读出该人员的RoleID
			sql = "SELECT roleID FROM personrole where personid=?";
			params = new Object[] { new Integer(personID) };
			types = new int[] { Types.INTEGER };
			List roleList = (List) executeQuery(new MapListHandler());
			Iterator roleIterator = roleList.iterator();
			while (roleIterator.hasNext()) {
				// 2.1选择下一RoleID
				Map roleMap = (Map) roleIterator.next();
				int roleID = (Integer) roleMap.get("roleID");
				// 2.2以RoleID去查ProcessActivityRole，ActivityInformation表，读出那些原子活动，并且活动状态为“Ready”和“Running”的记录集；
				// （原子活动，并且活动状态为“Ready”和“Running”的才可以是任务，如何判断一个活动是原子活动呢？当没有一个活动的父活动是它时，就是一个原子活动）
				sql = "SELECT PAR.*, PAI.activityName, PAI.state as activityState, PTI.definedstartdate, PTI.definedenddate FROM ProcessActivityRole PAR left join ProcessActivityInformation PAI on PAR.processid = PAI.processid and PAR.activityid = PAI.activityid left join ProcessTWCInformation PTI on PAI.processid = PTI.processid and PAI.twcid = PTI.twcid where PAR.RoleID = ?  and PAI.state in (?, ?)";
				params = new Object[] { new Integer(roleID),
						Constants.ACTIVITY_STATE_READY,
						Constants.ACTIVITY_STATE_RUNNING };
				types = new int[] { Types.INTEGER, Types.VARCHAR, Types.VARCHAR };
				List processActivityRoleList = (List) executeQuery(new MapListHandler());
				Iterator processActivityRoleIterator = processActivityRoleList
						.iterator();

				while (processActivityRoleIterator.hasNext()) {
					// 2.2.1取下一记录，得到某一任务
					Map processActivityRoleMap = (Map) processActivityRoleIterator
							.next();
					String processID = (String) processActivityRoleMap
							.get("processID");
					int activityID = (Integer) processActivityRoleMap
							.get("activityID");

					// 判断是否为原子活动
					sql = "SELECT * from processActivityInformation where parentID = ? and processid = ?";
					params = new Object[] { new Integer(activityID), processID };
					types = new int[] { Types.INTEGER, Types.VARCHAR };
					List processActivityInformationList = (List) executeQuery(new MapListHandler());
					if (processActivityInformationList.size() > 0) {
						// 如果不是原子活动
						continue;
					}

					// 2.2.2判断是否该任务已经分配给他（以ActivityID和PersonID查ProcessActivityPerson表）,如果是,则返回2.2.1;

					sql = "SELECT PAP.State FROM ProcessActivityPerson PAP where processid=? and activityid=? and personid=?";
					params = new Object[] { processID, new Integer(activityID),
							new Integer(personID) };
					types = new int[] { Types.VARCHAR, Types.INTEGER,
							Types.INTEGER };

					Map allocateStateMap = (Map) executeQuery(new MapHandler());
					if (allocateStateMap != null) {
						String allocateState = (String) allocateStateMap
								.get("State");
						if (allocateState.equals(Constants.TASK_STATE_ACCEPTED)) {
							continue;
						}
					}
					// 2.2.3判断针对该Process是否有ProcessPerson的定义
					sql = "SELECT * from ProcessPerson where processid=?";
					params = new Object[] { processID };
					types = new int[] { Types.VARCHAR };
					List processPersonList = (List) executeQuery(new MapListHandler());

					// 如果有,判断是否定义了该人员可以以该角色参与该过程
					if (processPersonList.size() > 0) {
						sql = "SELECT * from ProcessPerson where processid=? and personid=? and roleid=?";
						params = new Object[] { processID,
								new Integer(personID), new Integer(roleID) };
						types = new int[] { Types.VARCHAR, Types.INTEGER,
								Types.INTEGER };
						Map processPersonMap = (Map) executeQuery(new MapHandler());
						// 如果没有,则返回2.2.1
						if (processPersonMap == null) {
							continue;
						}// 如果有,进入2.2.4
					}// 如果没有,进入2.2.4

					// 2.2.4判断是否针对其父活动有ProcessActivityTeamMember的定义(注意这是要逐步向上查的,直到查到顶层)
					sql = "select parentID from processActivityInformation where processID = ? and activityID = ?";
					params = new Object[] { processID, new Integer(activityID) };
					types = new int[] { Types.VARCHAR, Types.INTEGER };
					Map processActivityInformationMap = (Map) executeQuery(new MapHandler());
					int parentID = -1;

					if (processActivityInformationMap != null
							&& processActivityInformationMap.get("parentID") != null) {
						parentID = (Integer) processActivityInformationMap
								.get("parentID");
					}

					boolean isTeamFound = false;
					while (parentID != -1) {
						sql = "select * from processActivityTeamMember where processID = ? and activityID = ?";
						params = new Object[] { processID,
								new Integer(activityID) };
						types = new int[] { Types.VARCHAR, Types.INTEGER };
						List processActivityTeamMemberList = (List) executeQuery(new MapListHandler());

						if (processActivityTeamMemberList.size() > 0) {
							isTeamFound = true;
							break;

						} else {
							sql = "select parentID from processActivityInformation where processID = ? and activityID = ?";
							params = new Object[] { processID,
									new Integer(activityID) };
							types = new int[] { Types.VARCHAR, Types.INTEGER };
							processActivityInformationMap = (Map) executeQuery(new MapHandler());
							if (processActivityInformationMap.get("parentID") != null) {
								parentID = (Integer) processActivityInformationMap
										.get("parentID");
							} else {
								parentID = -1;
							}
							continue;
						}

					}

					if (isTeamFound == true) {
						// 如果有,判断是否定义了该人员可以以该角色参与活动:
						sql = "select * from processActivityTeamMember where processID = ? and activityID = ? and personID = ? and roleID = ?";
						params = new Object[] { processID,
								new Integer(activityID), new Integer(personID),
								new Integer(roleID) };
						types = new int[] { Types.VARCHAR, Types.INTEGER,
								Types.INTEGER, Types.INTEGER };
						List processActivityTeamMemberList = (List) executeQuery(new MapListHandler());

						if (processActivityTeamMemberList.size() == 0) {
							// 如果没有,返回2.2.1
							continue;
						} else {
							// 如果有,进入2.2.5
						}
					}

					// 2.2.5比较该任务针对该RoleID的MaximalNumber和AllocatedNumber:
					int allocatedNumber = (Integer) processActivityRoleMap
							.get("AllocatedNumber");
					int maximalNumber = (Integer) processActivityRoleMap
							.get("MaximalNumber");
					if (allocatedNumber < maximalNumber) {
						// 如果AllocatedNumber<MaximalNumber,
						// 那么用户可以获得该任务信息，该任务项的分配状态为“UnAccepted”
						String activityName = null;
						if (processActivityRoleMap.get("activityName") != null) {
							activityName = (String) processActivityRoleMap
									.get("activityName");
						}

						Task task = new Task();
						task.setProcessID(processID);

						/*
						 * task .setProcessName((String) processActivityRoleMap
						 * .get("ProcessName"));
						 */

						task.setActivityID(activityID);
						task.setActivityName(activityName);
						task.setRoleID(roleID);

						if (processActivityRoleMap.get("DefinedStartDate") != null) {
							task
									.setDefinedStartDate(new java.sql.Date(
											((Date) processActivityRoleMap
													.get("DefinedStartDate"))
													.getTime()));
						}

						if (processActivityRoleMap.get("DefinedEndDate") != null) {
							task.setDefinedEndDate(new java.sql.Date(
									((Date) processActivityRoleMap
											.get("DefinedEndDate")).getTime()));
						}
						if (processActivityRoleMap.get("ActivityState") != null) {
							task.setState((String) processActivityRoleMap
									.get("ActivityState"));
						}
						task.setAllocateState(Constants.TASK_STATE_UNACCEPTED);

						// 增加任务到任务列表
						if (!taskList.contains(task)) {
							taskList.add(task);
							if (task.getState() != null) {
								/*
								 * if ((!task.getState().equals("Completed")) &&
								 * logger.isInfoEnabled()) logger.info("ADD
								 * PERSON TASK LIST----" + task);
								 */
							}
						} else if (allocatedNumber == maximalNumber) {
							// 如果AllocatedNumber=MaximalNumber，返回2.2.1
							continue;
						}
					}
				}
			}
			return taskList;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	public List getHistoryTaskList() {
		try {
			List taskList = new ArrayList();

			int personID = processManager.getCurrentUser().getActorID();
			// 1.读ProcessActivityPerson表,以PersonID查找到State为"Accepted"的任务,这些任务已经为该人员所接受,自然是分配给他了
			sql = "SELECT PAP.*, PAI.ActivityName, PAI.State as activityState, PI.ProcessName FROM ProcessActivityPerson PAP LEFT JOIN ProcessActivityInformation PAI ON PAI.ProcessID=PAP.ProcessID AND PAI.ActivityID=PAP.ActivityID LEFT JOIN ProcessInformation PI ON PI.ProcessID=PAP.ProcessID WHERE PAP.PersonID=? AND PAP.State=?";
			params = new Object[] { new Integer(personID),
					Constants.TASK_STATE_SUBMITTED };
			types = new int[] { Types.INTEGER, Types.VARCHAR };
			List processActivityPersonList = (List) executeQuery(new MapListHandler());

			Iterator processActivityPersonIterator = processActivityPersonList
					.iterator();
			while (processActivityPersonIterator.hasNext()) {
				Map processActivityPersonMap = (Map) processActivityPersonIterator
						.next();
				Task task = new Task();
				if (processActivityPersonMap.get("ProcessID") != null) {
					task.setProcessID((String) processActivityPersonMap
							.get("ProcessID"));
				}
				if (processActivityPersonMap.get("ProcessName") != null) {
					task.setProcessName((String) processActivityPersonMap
							.get("ProcessName"));
				}
				if (processActivityPersonMap.get("ActivityID") != null) {
					task.setActivityID(((Integer) processActivityPersonMap
							.get("ActivityID")).intValue());
				}
				if (processActivityPersonMap.get("ActivityName") != null) {
					task.setActivityName((String) processActivityPersonMap
							.get("ActivityName"));
				}
				if (processActivityPersonMap.get("roleID") != null) {
					task.setRoleID((Integer) processActivityPersonMap
							.get("roleID"));
				}
				if (processActivityPersonMap.get("DefinedStartDate") != null) {
					task.setDefinedStartDate(new java.sql.Date(
							((Date) processActivityPersonMap
									.get("DefinedStartDate")).getTime()));
				}
				if (processActivityPersonMap.get("DefinedEndDate") != null) {
					task.setDefinedEndDate(new java.sql.Date(
							((Date) processActivityPersonMap
									.get("DefinedEndDate")).getTime()));
				}
				if (processActivityPersonMap.get("ActivityState") != null) {
					task.setState((String) processActivityPersonMap
							.get("ActivityState"));
				}
				if (processActivityPersonMap.get("State") != null) {
					task.setAllocateState((String) processActivityPersonMap
							.get("State"));
				}
				// task.setisBind(true);

				// 增加任务到任务列表
				if (!taskList.contains(task)) {
					taskList.add(task);
					/*
					 * if ((!task.getState().equals("Completed")) &&
					 * logger.isInfoEnabled()) logger.info("ADD PERSON TASK
					 * LIST----" + task);
					 */
				}
			}
			return taskList;
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return null;
		}
	}

	/**
	 * to delete the completed task the method's algorthim is: step 1: select
	 * all items in processActivityPerson step 2: select the role items except
	 * that the task has been contained in step 1 step 3: delete the items which
	 * state is completed in step 1 added by Dy 2006.4.13
	 */
	private void deleteCompletedTask(List taskList) {
		Iterator taskListIterator = taskList.iterator();
		while (taskListIterator.hasNext()) {
			Task task = (Task) taskListIterator.next();
			if (task.getState().equals(Constants.ACTIVITY_STATE_COMPLETED)) {
				taskListIterator.remove();
			}
		}

	}

	/**
	 * 功能：获得本人已经打开的任务
	 * 
	 * 步骤： 1.直接查询ProcessActivityPerson表，取得属于这个人的任务集 2.把个人的打开的任务集加入任务列表taskList中
	 */
	/*
	 * private void addPersonActivityToTaskList(List taskList) throws
	 * SQLException { sql = "SELECT PAP.*, PAI.ActivityName, PI.ProcessName FROM
	 * ProcessActivityPerson PAP LEFT JOIN ProcessActivityInformation PAI ON
	 * PAI.ProcessID=PAP.ProcessID AND PAI.ActivityID=PAP.ActivityID LEFT JOIN
	 * ProcessInformation PI ON PI.ProcessID=PAP.ProcessID WHERE PAP.PersonID=?
	 * AND PAP.State<>?"; // params = new Object[]{new
	 * Integer(processManager.getCurrentUser().getActorID()),
	 * Constants.ACTIVITY_STATE_COMPLETED}; //edited by Dy params = new
	 * Object[]{new Integer(processManager.getCurrentUser().getActorID()),
	 * Constants.ACTIVITY_STATE_READY}; //end types = new int[] {Types.INTEGER,
	 * Types.VARCHAR}; List processActivityPersonList = (List) executeQuery(new
	 * MapListHandler());
	 * 
	 * Iterator processActivityPersonIterator =
	 * processActivityPersonList.iterator();
	 * 
	 * while (processActivityPersonIterator.hasNext()) {
	 * 
	 * Map processActivityPersonMap = (Map)
	 * processActivityPersonIterator.next();
	 * 
	 * Task task = new Task(); task.setProcessID((String)
	 * processActivityPersonMap.get("ProcessID")); task.setProcessName((String)
	 * processActivityPersonMap.get("ProcessName"));
	 * task.setActivityID(((Integer)
	 * processActivityPersonMap.get("ActivityID")).intValue());
	 * task.setActivityName((String)
	 * processActivityPersonMap.get("ActivityName"));
	 * task.setDefinedStartDate(new java.sql.Date(((Date)
	 * processActivityPersonMap.get("DefinedStartDate")).getTime()));
	 * task.setDefinedEndDate(new java.sql.Date(((Date)
	 * processActivityPersonMap.get("DefinedEndDate")).getTime()));
	 * task.setState((String) processActivityPersonMap.get("State"));
	 * 
	 * //added by dy if((task.getState()).equals("Waiting")){
	 * task.setisBind(true); } else{ task.setisBind(false); }
	 * 
	 * //增加任务到任务列表 if (!taskList.contains(task)) { taskList.add(task); if
	 * ((!task.getState().equals("Completed")) && logger.isInfoEnabled())
	 * logger.info("ADD PERSON TASK LIST----" + task); } } }
	 */
	// sxh modified 2007.10
	private void addPersonActivityToTaskList(List taskList) throws SQLException {
		int personID = processManager.getCurrentUser().getActorID();
		// 1.读ProcessActivityPerson表,以PersonID查找到State为"Accepted"的任务,这些任务已经为该人员所接受,自然是分配给他了
		sql = "SELECT PAP.*, PAI.ActivityName, PAI.State as activityState, PI.ProcessName FROM ProcessActivityPerson PAP LEFT JOIN ProcessActivityInformation PAI ON PAI.ProcessID=PAP.ProcessID AND PAI.ActivityID=PAP.ActivityID LEFT JOIN ProcessInformation PI ON PI.ProcessID=PAP.ProcessID WHERE PAP.PersonID=? AND PAP.State=?";
		params = new Object[] { new Integer(personID),
				Constants.TASK_STATE_ACCEPTED };
		types = new int[] { Types.INTEGER, Types.VARCHAR };
		List processActivityPersonList = (List) executeQuery(new MapListHandler());

		if (processActivityPersonList.size() > 0) {
			Iterator processActivityPersonIterator = processActivityPersonList
					.iterator();
			while (processActivityPersonIterator.hasNext()) {
				Map processActivityPersonMap = (Map) processActivityPersonIterator
						.next();
				Task task = new Task();
				task.setProcessID((String) processActivityPersonMap
						.get("ProcessID"));
				task.setProcessName((String) processActivityPersonMap
						.get("ProcessName"));
				task.setActivityID(((Integer) processActivityPersonMap
						.get("ActivityID")).intValue());
				task.setActivityName((String) processActivityPersonMap
						.get("ActivityName"));
				task
						.setRoleID((Integer) processActivityPersonMap
								.get("roleID"));
				if (processActivityPersonMap.get("DefinedStartDate") != null) {
					task.setDefinedStartDate(new java.sql.Date(
							((Date) processActivityPersonMap
									.get("DefinedStartDate")).getTime()));
				}
				if (processActivityPersonMap.get("DefinedEndDate") != null) {
					task.setDefinedEndDate(new java.sql.Date(
							((Date) processActivityPersonMap
									.get("DefinedEndDate")).getTime()));
				}
				task.setState((String) processActivityPersonMap
						.get("ActivityState"));
				task.setAllocateState((String) processActivityPersonMap
						.get("State"));
				task.setisBind(true);

				// 增加任务到任务列表
				if (!taskList.contains(task)) {
					taskList.add(task);
					if ((!task.getState().equals("Completed"))
							&& logger.isInfoEnabled())
						logger.info("ADD PERSON TASK LIST----" + task);
				}
			}
		}

		// 2.读出该人员的RoleID
		sql = "SELECT roleID FROM personrole where personid=?";
		params = new Object[] { new Integer(personID) };
		types = new int[] { Types.INTEGER };
		List roleList = (List) executeQuery(new MapListHandler());
		if (roleList != null) {
			Iterator roleIterator = roleList.iterator();
			while (roleIterator.hasNext()) {
				// 2.1选择下一RoleID
				Map roleMap = (Map) roleIterator.next();
				int roleID = (Integer) roleMap.get("roleID");
				// 2.2以RoleID去查ProcessActivityRole表，读出记录集
				sql = "SELECT PAR.* FROM ProcessActivityRole PAR where PAR.RoleID=?";
				params = new Object[] { new Integer(roleID) };
				types = new int[] { Types.INTEGER };
				List processActivityRoleList = (List) executeQuery(new MapListHandler());
				if (processActivityRoleList == null) {
					continue;
				}
				Iterator processActivityRoleIterator = processActivityRoleList
						.iterator();

				while (processActivityRoleIterator.hasNext()) {
					// 2.2.1取下一记录，得到某一任务
					Map processActivityRoleMap = (Map) processActivityRoleIterator
							.next();
					// 2.2.2判断是否该任务已经分配给他（以ActivityID和PersonID查ProcessActivityPerson表）,如果是,则返回2.2.1;
					String processID = (String) processActivityRoleMap
							.get("processID");
					int activityID = (Integer) processActivityRoleMap
							.get("activityID");
					sql = "SELECT PAP.State FROM ProcessActivityPerson PAP where processid=? and activityid=? and personid=?";
					params = new Object[] { processID, new Integer(activityID),
							new Integer(personID) };
					types = new int[] { Types.VARCHAR, Types.INTEGER,
							Types.INTEGER };

					List allocateStateList = (List) executeQuery(new MapListHandler());
					if (allocateStateList.size() > 0) {
						Map allocateStateMap = (Map) allocateStateList.get(0);
						String allocateState = (String) allocateStateMap
								.get("State");
						if (allocateState.equals(Constants.TASK_STATE_ACCEPTED)) {
							continue;
						}
					}

					// 2.2.3判断针对该Process是否有ProcessPerson的定义
					sql = "SELECT * from ProcessPerson where processid=?";
					params = new Object[] { processID };
					types = new int[] { Types.VARCHAR };
					List processPersonList = (List) executeQuery(new MapListHandler());

					// 如果有,判断是否定义了该人员可以以该角色参与该过程
					if (processPersonList.size() > 0) {
						sql = "SELECT * from ProcessPerson where processid=? and personid=? and roleid=?";
						params = new Object[] { processID,
								new Integer(personID), new Integer(roleID) };
						types = new int[] { Types.VARCHAR, Types.INTEGER,
								Types.INTEGER };
						processPersonList = (List) executeQuery(new MapListHandler());
						// 如果没有,则返回2.2.1
						if (processPersonList.size() == 0) {
							continue;
						}// 如果有,进入2.2.4
					}// 如果没有,进入2.2.4

					// 2.2.4判断是否针对其父活动有ProcessActivityTeamMember的定义(注意这是要逐步向上查的,直到查到顶层)
					sql = "select parentID from processActivityInformation where processID = ? and activityID = ?";
					params = new Object[] { processID, new Integer(activityID) };
					types = new int[] { Types.VARCHAR, Types.INTEGER };
					List processActivityInformationList = (List) executeQuery(new MapListHandler());
					int parentID = -1;
					if (processActivityInformationList.size() > 0) {
						Map processActivityInformationMap = (Map) processActivityInformationList
								.get(0);
						parentID = (Integer) processActivityInformationMap
								.get("parentID");
					}

					boolean isTeamFound = false;
					while (parentID != -1) {
						sql = "select * from processActivityTeamMember where processID = ? and activityID = ?";
						params = new Object[] { processID,
								new Integer(activityID) };
						types = new int[] { Types.VARCHAR, Types.INTEGER };
						List processActivityTeamMemberList = (List) executeQuery(new MapListHandler());

						if (processActivityTeamMemberList.size() > 0) {
							isTeamFound = true;
							break;

						} else {
							sql = "select parentID from processActivityInformation where processID = ? and activityID = ?";
							params = new Object[] { processID,
									new Integer(activityID) };
							types = new int[] { Types.VARCHAR, Types.INTEGER };
							processActivityInformationList = (List) executeQuery(new MapListHandler());
							if (processActivityInformationList.size() > 0) {
								Map processActivityInformationMap = (Map) processActivityInformationList
										.get(0);
								parentID = (Integer) processActivityInformationMap
										.get("parentID");
							}
							continue;
						}

					}

					if (isTeamFound == true) {
						// 如果有,判断是否定义了该人员可以以该角色参与活动:
						sql = "select * from processActivityTeamMember where processID = ? and activityID = ? and personID = ? and roleID = ?";
						params = new Object[] { processID,
								new Integer(activityID), new Integer(personID),
								new Integer(roleID) };
						types = new int[] { Types.VARCHAR, Types.INTEGER,
								Types.INTEGER, Types.INTEGER };
						List processActivityTeamMemberList = (List) executeQuery(new MapListHandler());

						if (processActivityTeamMemberList.size() == 0) {
							// 如果没有,返回2.2.1
							continue;
						} else {
							// 如果有,进入2.2.5
						}
					}

					// 2.2.5比较该任务针对该RoleID的MaximalNumber和AllocatedNumber
					sql = "SELECT * FROM ProcessActivityRole where processid=? and activityid = ? and roleid = ?";
					params = new Object[] { processID, new Integer(activityID),
							new Integer(roleID) };
					types = new int[] { Types.VARCHAR, Types.INTEGER,
							Types.INTEGER };
					processActivityRoleList = (List) executeQuery(new MapListHandler());
					if (processActivityRoleList.size() > 0) {
						processActivityRoleMap = (Map) processActivityRoleList
								.get(0);
						int allocatedNumber = (Integer) processActivityRoleMap
								.get("AllocatedNumber");
						int maximalNumber = (Integer) processActivityRoleMap
								.get("MaximalNumber");

						if (allocatedNumber < maximalNumber) {
							// 如果AllocatedNumber<MaximalNumber,
							// 那么用户可以获得该任务信息，该任务项的分配状态为“UnAccepted”
							sql = "SELECT PAP.*, PAI.ActivityName, PAI.State as activityState, PI.ProcessName FROM ProcessActivityPerson PAP LEFT JOIN ProcessActivityInformation PAI ON PAI.ProcessID=PAP.ProcessID AND PAI.ActivityID=PAP.ActivityID LEFT JOIN ProcessInformation PI ON PI.ProcessID=PAP.ProcessID WHERE PAP.processID = ? and PAP.activityID = ? and PAP.roleID = ? and PAP.PersonID = ?";
							params = new Object[] { processID,
									new Integer(activityID),
									new Integer(roleID), new Integer(personID) };
							types = new int[] { Types.VARCHAR, Types.INTEGER,
									Types.INTEGER, Types.INTEGER };
							processActivityInformationList = (List) executeQuery(new MapListHandler());
							Map processActivityInformationMap = null;
							if (processActivityInformationList.size() > 0) {
								processActivityInformationMap = (Map) processActivityInformationList
										.get(0);
								String activityName = (String) processActivityInformationMap
										.get("activityName");

								Task task = new Task();
								task.setProcessID(processID);

								task
										.setProcessName((String) processActivityInformationMap
												.get("ProcessName"));

								task.setActivityID(activityID);
								task.setActivityName(activityName);
								task.setRoleID(roleID);
								task.setDefinedStartDate(new java.sql.Date(
										((Date) processActivityInformationMap
												.get("DefinedStartDate"))
												.getTime()));
								task.setDefinedEndDate(new java.sql.Date(
										((Date) processActivityInformationMap
												.get("DefinedEndDate"))
												.getTime()));
								task
										.setState((String) processActivityInformationMap
												.get("ActivityState"));
								task
										.setAllocateState(Constants.TASK_STATE_UNACCEPTED);
								task.setisBind(false);

								// 增加任务到任务列表
								if (!taskList.contains(task)) {
									taskList.add(task);
									if ((!task.getState().equals("Completed"))
											&& logger.isInfoEnabled())
										logger.info("ADD PERSON TASK LIST----"
												+ task);
								}
							}

						} else if (allocatedNumber == maximalNumber) {
							// 如果AllocatedNumber=MaximalNumber，返回2.2.1
							continue;
						}
					}
				}
				// String allocateState =
				// (String)processActivityRoleMap.get("state");

			}
		}

	}

	// sxh modified 2007.10 end

	/**
	 * 功能：在分配给角色的活动中查找此人可以打开的活动
	 * 
	 * @param taskList
	 * @throws SQLException
	 * 
	 * 步骤：
	 * 1.选出这样的活动:此活动所属流程状态为Running，活动状态为Ready或者Running并且此活动没有被此人打开也就是说次活动不在ProcessActivityPerson表里
	 * 2.把这样的活动加入任务列表里
	 */
	private void addRoleActivityToTaskList(List taskList) throws SQLException {

		StringBuffer sqlBuffer = new StringBuffer("");
		sqlBuffer
				.append(
						"SELECT DISTINCT PI.ProcessName, PAI.*, PTI.DefinedStartDate, PTI.DefinedEndDate  FROM ProcessActivityInformation PAI ")
				.append(
						"LEFT JOIN ProcessActivityRole PAR ON PAI.ProcessID = PAR.ProcessID AND PAI.ActivityID = PAR.ActivityID ")
				// .append("LEFT JOIN ProcessActivityRole PAR ON PAI.ActivityID
				// = PAR.ActivityID ")
				.append("LEFT JOIN PersonRole PR ON PR.RoleID = PAR.RoleID ")
				.append(
						"LEFT JOIN ProcessTWCInformation PTI ON PTI.ProcessID = PAI.ProcessID AND PTI.TWCID = PAI.TWCID ")
				.append(
						"LEFT JOIN ProcessInformation PI ON PI.ProcessID = PAI.ProcessID ")
				.append(
						"WHERE PI.State=? AND (PAI.State IN (?,?)) AND (PR.PersonID=?)");
		sql = new String(sqlBuffer);
		params = new Object[] { Constants.PROCESS_STATE_RUNNING,
				Constants.ACTIVITY_STATE_READY,
				Constants.ACTIVITY_STATE_RUNNING,
				new Integer(processManager.getCurrentUser().getActorID()) };
		types = new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
				Types.INTEGER };
		List processActivityInformationList = (List) executeQuery(new MapListHandler());

		Iterator processActivityInformationIterator = processActivityInformationList
				.iterator();
		while (processActivityInformationIterator.hasNext()) {

			Map processActivityInformationMap = (Map) processActivityInformationIterator
					.next();

			Task task = new Task();
			task.setProcessID((String) processActivityInformationMap
					.get("ProcessID"));
			task.setProcessName((String) processActivityInformationMap
					.get("ProcessName"));
			task.setActivityID(((Integer) processActivityInformationMap
					.get("ActivityID")).intValue());
			task.setActivityName((String) processActivityInformationMap
					.get("ActivityName"));
			task.setDefinedStartDate(new java.sql.Date(new Date().getTime()));
			task.setDefinedEndDate(new java.sql.Date(new Date().getTime()));
			task.setState(Constants.ACTIVITY_STATE_READY);
			// added by dy
			task.setisBind(false);

			/**
			 * 对于这个角色的任务，如果其所分配的人的已经满足了 那么属于这个角色的其他人就应该看不到该任务了
			 */
			if (isAvailable(task.getProcessID(), task.getActivityID())) {

				// 增加任务到任务列表
				if (!taskList.contains(task)) {
					taskList.add(task);
					if (logger.isInfoEnabled())
						logger.info("ADD ROLE TASK LIST----" + task);
				}
			}
		}

		/*
		 * StringBuffer sqlBuffer = new StringBuffer("");
		 * sqlBuffer.append("SELECT DISTINCT PI.ProcessName, PAI.*,
		 * PTI.DefinedStartDate, PTI.DefinedEndDate FROM
		 * ProcessActivityInformation PAI ") .append("LEFT JOIN
		 * ProcessActivityRole PAR ON PAI.ProcessID = PAR.ProcessID AND
		 * PAI.ActivityID = PAR.ActivityID ") //.append("LEFT JOIN
		 * ProcessActivityRole PAR ON PAI.ActivityID = PAR.ActivityID ")
		 * .append("LEFT JOIN PersonRole PR ON PR.RoleID = PAR.RoleID ")
		 * .append("LEFT JOIN ProcessTWCInformation PTI ON PTI.ProcessID =
		 * PAI.ProcessID AND PTI.TWCID = PAI.TWCID ") .append("LEFT JOIN
		 * ProcessInformation PI ON PI.ProcessID = PAI.ProcessID ")
		 * .append("WHERE PI.State=? AND (PAI.State IN (?,?)) AND
		 * (PR.PersonID=?)"); sql = new String(sqlBuffer); params = new
		 * Object[]{Constants.PROCESS_STATE_RUNNING,
		 * Constants.ACTIVITY_STATE_READY, Constants.ACTIVITY_STATE_RUNNING, new
		 * Integer(processManager.getCurrentUser().getActorID())}; types = new
		 * int[] {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
		 * List processActivityInformationList = (List) executeQuery(new
		 * MapListHandler());
		 * 
		 * Iterator processActivityInformationIterator =
		 * processActivityInformationList.iterator(); while
		 * (processActivityInformationIterator.hasNext()) {
		 * 
		 * Map processActivityInformationMap = (Map)
		 * processActivityInformationIterator.next();
		 * 
		 * Task task = new Task(); task.setProcessID((String)
		 * processActivityInformationMap.get("ProcessID"));
		 * task.setProcessName((String)
		 * processActivityInformationMap.get("ProcessName"));
		 * task.setActivityID(((Integer)
		 * processActivityInformationMap.get("ActivityID")).intValue());
		 * task.setActivityName((String)
		 * processActivityInformationMap.get("ActivityName"));
		 * task.setDefinedStartDate(new java.sql.Date(((Date)
		 * processActivityInformationMap.get("DefinedStartDate")).getTime()));
		 * task.setDefinedEndDate(new java.sql.Date(((Date)
		 * processActivityInformationMap.get("DefinedEndDate")).getTime()));
		 * task.setState(Constants.ACTIVITY_STATE_READY);
		 * 
		 * //增加任务到任务列表 if (!taskList.contains(task)) taskList.add(task);
		 * 
		 * if (logger.isInfoEnabled()) logger.info("ADD ROLE TASK LIST----" +
		 * task); }
		 */
	}

	/**
	 * 功能:判断该活动是否已经被足够多的人接受了
	 * 
	 * @return
	 * 
	 * 步骤: 1.查找该用户所属角色对于该活动所需的最多人数
	 */
	private boolean isAvailable(String processID, int activityID)
			throws SQLException {
		sql = "SELECT PAR.ActivityID FROM ProcessActivityRole PAR LEFT JOIN PersonRole PR ON PR.RoleID=PAR.RoleID WHERE ProcessID=? AND ActivityID=? AND PR.PersonID=? AND MaximalNumber > AllocatedNumber";
		params = new Object[] { processID, new Integer(activityID),
				new Integer(processManager.getCurrentUser().getActorID()) };
		types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER };
		Map processActivityRoleMap = (Map) executeQuery(new MapHandler());
		return processActivityRoleMap == null ? false : true;
	}

}
