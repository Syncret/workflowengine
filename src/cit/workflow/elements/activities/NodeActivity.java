/*
 * Created on 2004-11-3
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.elements.activities;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;



import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import cit.workflow.Constants;
import cit.workflow.eca.ProcessManager;
import cit.workflow.elements.Process;
import cit.workflow.elements.factories.ElementFactory;
import cit.workflow.exception.WorkflowTransactionException;
import cit.workflow.exception.WorkflowSubmitException;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NodeActivity extends AbstractActivity implements Executable {
	
	public NodeActivity(Connection conn, Process process, int activityID, ProcessManager processManager) {
		super(conn, process, activityID, processManager);
	}
	
	/**
	 * 功能：改变活动的状态之后可能需要进行的一些操作
	 * 
	 * @param fromState
	 * @param toState
	 * @return
	 * @see cit.workflow.elements.AbstractActivity#afterStateChanged()
	 * 
	 * 步骤：
	 * 		1.更改活动TWC信息
	 * 		2.更改相关资源使用信息
	 * 		3.计算相关对象的值，并复位相关数据流
	 * 		4.由于活动状态改变可能会引发新的事件，所以调用ECAParser来解析由于活动状态改变而引起的事件
	 * 		5.如果活动变为状态变为Ready，那么可能需要激活子过程
	 */
	protected void afterStateChanged(String fromState, String toState) throws Exception {
		
		/* 激发事件监听器执行相应的事件处理 */
		super.afterStateChanged(fromState, toState);		
		
		if( toState.equals(Constants.ACTIVITY_STATE_RUNNING) || toState.equals(Constants.ACTIVITY_STATE_COMPLETED) ) {			
					
			int activityTWCID = ((Integer) getAttributeValue("TWCID")).intValue();
					
			/* 1.更改活动TWC信息 */
			if( toState.equals(Constants.ACTIVITY_STATE_RUNNING) ) {
				sql = "UPDATE ProcessTWCInformation SET ActualStartDate=? WHERE ProcessID=? AND TWCID=?";
			} else if( toState.equals(Constants.ACTIVITY_STATE_COMPLETED) ) {
				sql = "UPDATE ProcessTWCInformation SET ActualEndDate=? WHERE ProcessID=? AND TWCID=?";						
			}
			
			params = new Object[] {new Date(processManager.getCurrentDate().getTime()), this.process.getProcessID(), new Integer(activityTWCID)};
			types = new int[] {Types.TIMESTAMP, Types.VARCHAR, Types.INTEGER};
			executeUpdate();

			/* 2.更改相关资源使用信息 */
			if( toState.equals(Constants.ACTIVITY_STATE_RUNNING) ) {
				sql = "UPDATE ProcessActivityResource SET ActualStartDate=? WHERE ProcessID=? AND ActivityID=?";
			} else if( toState.equals(Constants.ACTIVITY_STATE_COMPLETED) ) {
				sql = "UPDATE ProcessActivityResource SET ActualEndDate=? WHERE ProcessID=? AND ActivityID=?";						
			}
			params = new Object[] {processManager.getCurrentDate(), this.process.getProcessID(), new Integer(this.activityID)};
			types = new int[] {Types.DATE, Types.VARCHAR, Types.INTEGER};
			executeUpdate();
		}	
		
		/* 执行此活动,活动执行可能会引起活动状态的改变 */
		if ( toState.equals(Constants.ACTIVITY_STATE_READY) ) {
			execute();
		}

		/* 由于以上的活动执行可能会引起状态的改变, 因此需要判断此时的活动状态而不是toState */			
		if( this.getActivityState().equals(Constants.ACTIVITY_STATE_READY) ) {
			invokeChildActivity();	
		}
	}

	/**
	 * 功能：判断是否需要改变整个活动的状态
	 * 
	 * @param fromState
	 * @param toState
	 * @return 
	 * @see cit.workflow.elements.AbstractActivity#canChangeState()
	 * 
	 * 步骤：
	 * 		1.如果改变后的状态toState为Ready,则需要改变.
	 * 		2.如果改变后的状态toState为Running,并且当前活动状态不为Running状态,则说明第一次打开活动需要改变状态.
	 * 		3.如果改变后的状态toState为Completed并且活动状态不为Completed,需要根据活动提交模式来决定是否需要改变活动状态.
	 */
protected boolean canChangeState(String fromState, String toState) throws Exception {
		
		boolean needChangeActivityState = true;
		
		/* 1.如果改变后的状态toState为Ready,则需要改变 */
		if (processManager.getCurrentUser() == null || toState.equals(Constants.ACTIVITY_STATE_READY))
			return needChangeActivityState;
		
		
		/* 查找活动状态为Waiting，Ready，Running的活动，如果活动状态不为这些的话就不需要修改活动状态 */
		sql = "SELECT * FROM ProcessActivityInformation WHERE ProcessID=? AND ActivityID=? AND State IN (?,?,?)";
		params = new Object[] {this.process.getProcessID(), new Integer(this.activityID), Constants.ACTIVITY_STATE_WAITING, Constants.ACTIVITY_STATE_READY, Constants.ACTIVITY_STATE_RUNNING};
		types = new int[] {Types.VARCHAR, Types.INTEGER, Types.VARCHAR, Types.VARCHAR};
		Map processActivityInformationMap = (Map) executeQuery(new MapHandler());
		if (processActivityInformationMap != null) {
				
			/* 2.如果改变后的状态toState为Running,并且当前活动状态不为Running状态, 则说明第一次打开活动需要改变状态. */
			if( toState.equals(Constants.ACTIVITY_STATE_RUNNING) && toState.equals((String) processActivityInformationMap.get("State")) ) {
				needChangeActivityState = false; 
			} else if( toState.equals(Constants.ACTIVITY_STATE_COMPLETED) ) {
				
				int multiPersonMode = ((Integer) processActivityInformationMap.get("MultiPersonMode")).intValue();
				
				/*注4：MultiPersonMode是多人承担任务时的完成方式
				 *		1：表示有1人提交后任务就算完成
				 *		2：表示所有人均提交后任务才算完成
				 *		3：表示依赖于SubmitPersonNumber中指定的人数
				 *		4：表示每一个角色提交的人数必须多于它的最少人数，各个角色的最少人数在
				 */
				switch (multiPersonMode) {
					case 1:
						
						break;
					case 2: {
						//sxh modified 2007.10
						sql = "SELECT COUNT(ProcessID) AS NUM FROM ProcessActivityPerson WHERE ProcessID=? AND ActivityID=? AND State<>?";
						params = new Object[] {this.process.getProcessID(), new Integer(this.activityID), Constants.TASK_STATE_SUBMITTED};
						//sxh modified 2007.10 end
						types = new int[] {Types.VARCHAR, Types.INTEGER, Types.VARCHAR};
						Map processActivityPersonMap = (Map) executeQuery(new MapHandler());
						
						//sxh modified 2007
						int num = 0;
						if (processActivityPersonMap.get("NUM") instanceof Integer) {
							num = ((Integer) processActivityPersonMap.get("NUM")).intValue();
						} else if (processActivityPersonMap.get("NUM") instanceof Long) {
							num = ((Long) processActivityPersonMap.get("NUM")).intValue();
						}
						
						
						
						if (num != 0) 
							needChangeActivityState = false;
						break;
						//sxh modified 2007 end
					}
					case 3: {
						//sxh modified 2007.10
						sql = "SELECT COUNT(ProcessID) AS Num FROM ProcessActivityPerson WHERE ProcessID=? AND ActivityID=? AND State=?";
						params = new Object[] {this.process.getProcessID(), new Integer(this.activityID), Constants.TASK_STATE_SUBMITTED};
						//sxh modified 2007.10 end
						types = new int[] {Types.VARCHAR, Types.INTEGER, Types.VARCHAR};
						
						Map processActivityPersonMap = (Map) executeQuery(new MapHandler());
//						sxh modified 2007
						int num = 0;
						if (processActivityPersonMap.get("NUM") instanceof Integer) {
							num = ((Integer) processActivityPersonMap.get("NUM")).intValue();
						} else if (processActivityPersonMap.get("NUM") instanceof Long) {
							num = ((Long) processActivityPersonMap.get("NUM")).intValue();
						}
						
						
						
						if (num != ((Integer) processActivityInformationMap.get("SubmitPersonNumber")).intValue()) 
							needChangeActivityState = false;
						break;
						//sxh modified 2007 end
						
					}
					case 4: {
						sql = "SELECT RoleID, MinimalSubmittedPerson FROM ProcessActivityRole WHERE ProcessID=? AND ActivityID=?";
						params = new Object[] {this.process.getProcessID(), new Integer(this.activityID)};
						types = new int[] {Types.VARCHAR, Types.INTEGER};
						List processActivityRoleList = (List) executeQuery(new MapListHandler());
						
						/* 循环检查是否每个角色所需要的最少人数都达到了 */
						for (int i = 0, length = processActivityRoleList.size(); i < length; i++) {
							Map processActivityRoleMap = (Map) processActivityRoleList.get(i);
							int minimalSubmittedPerson = ((Integer) processActivityRoleMap.get("MinimalSubmittedPerson")).intValue();
							
							sql = "SELECT COUNT(PersonID) AS Num FROM ProcessActivityPerson WHERE ProcessID=? AND ActivityID=? AND RoleID=? AND State=?";
							//sxh modified 2007.10
							params = new Object[] {this.process.getProcessID(), new Integer(this.activityID), processActivityRoleMap.get("RoleID"), Constants.TASK_STATE_SUBMITTED};
							//sxh modified 2007.10 end
							types = new int[] {Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.DATE};
							Map processActivityPersonMap = (Map) executeQuery(new MapHandler());
							
//							//sxh modified 2007
							int submitNum = 0;
							if (processActivityPersonMap.get("NUM") instanceof Integer) {
								submitNum = ((Integer) processActivityPersonMap.get("NUM")).intValue();
							} else if (processActivityPersonMap.get("NUM") instanceof Long) {
								submitNum = ((Long) processActivityPersonMap.get("NUM")).intValue();
							}
							
							
							
							if (submitNum < minimalSubmittedPerson) {
								needChangeActivityState = false;
							break;
							//sxh modified 2007 end
							}
						}
						break;
					}
					default:
						needChangeActivityState = true;
						break;
				}
//				if (needChangeActivityState == false){
//					//servercomment System.out.println("exception");
//					throw new WorkflowSubmitException("your action has been submitted. Due to the multiPersonMode of this activity("
//							+multiPersonMode+"),the activity is not finished yet.Please contact your manager for more inforamtion. ");
//				}
			}
		} else {
			needChangeActivityState = false;
		}
		return needChangeActivityState;
	}

	/* (non-Javadoc)
	 * @see cit.workflow.elements.Executable#execute()
	 */
	public void execute() throws Exception {
		
	}
	 
	/**
	 * 功能：在改变活动状态之前做些准备工作
	 * 
	 * @param fromState
	 * @param toState
	 * @throws WorkflowTransactionException
	 * @see cit.workflow.elements.AbstractActivity#beforeStateChanged()
	 * 
	 * 步骤：
	 * 		1.当活动状态即将变化为Ready并且活动重复执行时
	 * 			1.1.需要把重复执行信息记录下来
	 * 			1.2.重置活动的状态
	 */
	protected void beforeStateChanged(String fromState, String toState) throws Exception {

		sql = "SELECT PAI.*, PTI.ActualStartDate, PTI.ActualEndDate FROM ProcessActivityInformation PAI LEFT JOIN ProcessTWCInformation PTI ON PTI.ProcessID=PAI.ProcessID AND PTI.TWCID=PAI.TWCID WHERE PAI.ProcessID=? AND PAI.ActivityID=?";
		params = new Object[]{this.process.getProcessID(), new Integer(this.activityID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		Map processActivityInformationMap = (Map) executeQuery(new MapHandler());
		if ( processActivityInformationMap != null ) {	
			
			int intRepeatedTime = ((Integer)processActivityInformationMap.get("RepeatedTime")).intValue();
					
			//1.如果活动状态为Ready并且重复执行
			if( toState.equals(Constants.ACTIVITY_STATE_READY) && intRepeatedTime > 0 ) {
					
				//1.1.记录重复执行信息
				//RepeatedType---1代表Activity
				sql = "INSERT INTO ProcessRepeatedInformation(ProcessID, RepeatedType, RepeatedID1, RepeatedID2, RepeatedTime, StartTime, EndTime) VALUES(?,?,?,?,?,?,?)";
				params = new Object[] { this.process.getProcessID(), new Integer(1), 
										new Integer(this.activityID), null,
										processActivityInformationMap.get("RepeatedTime"), processActivityInformationMap.get("ActualStartDate"), 
										processActivityInformationMap.get("ActualEndDate")};
					
				types = new int[] {Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.DATE, Types.DATE};
				executeUpdate();
				
				//1.2重置活动的状态
				resetActivity();
			}
			/*		
			//同时，添加资源重复使用信息
			*/
		} else {
			throw new WorkflowTransactionException("There is no right data for handle!");
		}
	}
	
	protected void invokeChildActivity() throws Exception {

		//改变子活动的开始节点状态(ActivityType=1)
		sql = "SELECT * FROM ProcessActivityInformation WHERE ProcessID=? AND ParentID=? AND ActivityType=1";
		params = new Object[]{this.process.getProcessID(), new Integer(this.activityID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		Map processActivityInformationMap = (Map) executeQuery(new MapHandler());
		if ( processActivityInformationMap != null ) {
				
			AbstractActivity activity = ElementFactory.createActivity(conn, this.process, ((Integer)processActivityInformationMap.get("ActivityID")).intValue(), processManager);
			activity.setParentActivity((NodeActivity) this);
			//activity.setListenerList(this.getListenerList());
			
			//更新子流程中的开始结点的状态
			activity.changeState(Constants.ACTIVITY_STATE_WAITING, Constants.ACTIVITY_STATE_READY);		
		}
	}

}
