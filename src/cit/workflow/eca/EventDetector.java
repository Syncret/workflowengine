/*
 * Created on 2004-3-13
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package cit.workflow.eca;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.log4j.Logger;

import cit.workflow.Constants;
import cit.workflow.elements.Event;
import cit.workflow.elements.EventDiscoveryListener;
import cit.workflow.elements.factories.EventFactory;
import cit.workflow.utils.DBUtility;

/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EventDetector extends DBUtility {
	
	private static Logger logger = Logger.getLogger(Process.class);
	
	protected List listenerList = new ArrayList();
	
	public EventDetector(Connection conn) {	
		super(conn);
	}
	
	/**
	 * 作用:探测事件
	 * 假设:无
	 * 效果:无
	 * 输入:
	 *  eventItem：事件项
	 * 返回:无
	 * 算法:一个事件发生，与之相关的所有事件表达式的探测接口
	 * 				STEP1: Search the EventRelationforParse to get the 
	 * 						FatherEventIDs, if haven't, exit
	 * 				STEP2: Get first FatherID
	 * 				STEP3: Search the Event table to get the ExpressionforParse, then call RewriteExpression 
	 * 						function to Rewrite the expression. if the expression is satisfied, 
	 * 						add the complex EventRepresentation to the  event stack(AddEvent)
	 * 				STEP4: Get next FatherID, if have goto STEP 3,
	 * 				else exit
	 */	
	public void parseEvent(Event event) throws SQLException {
		
		sql = "SELECT * FROM ProcessEventRelationforParse WHERE ProcessID=? AND SonEventID=?";
		params = new Object[] {event.getProcess().getProcessID(), new Integer(event.getEventID())};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		List processEventRelationforParseList = (List) executeQuery(new MapListHandler());

		int intFatherEventID = -1;
		Iterator processEventRelationforParseIterator = processEventRelationforParseList.iterator();
		while( processEventRelationforParseIterator.hasNext() ) {	
			Map processEventRelationforParseMap = (Map) processEventRelationforParseIterator.next();
				
			intFatherEventID = ((Integer)processEventRelationforParseMap.get("FatherEventID")).intValue();
			parseFatherEvent(event, intFatherEventID);
		}	  
	}
	
	/**
	 * 作用: 事件表达式的解析
	 * 假设:无
	 * 效果:无
	 * 输入:
	 *  sonEventItem：事件项
	 *  intFatherEventID: 相关的事件表达式
	 * 返回:无
	 * 算法: 一个事件表达式的解析
	 * 				1. 重写事件表达式,并解析表达式的值	
	 *				2. 更新事件表达式
	 */		
	private void parseFatherEvent(Event sonEventItem, int intFatherEventID) throws SQLException {
		
		sql = "SELECT * FROM ProcessEvent WHERE ProcessID=? AND EventID=?";
		params = new Object[] {sonEventItem.getProcess().getProcessID(), new Integer(intFatherEventID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		Map processEventMap = (Map) executeQuery(new MapHandler());
		if( processEventMap != null ) {
			String[] strExpressionforParse = new String[1];
			strExpressionforParse[0] = (String) processEventMap.get("ExpressionforParse");
			
			//重写事件表达式，并判断之			
			if( rewriteAndEvalExpression(strExpressionforParse, sonEventItem.getEventRepresentation()) ) {
		
				/* 调用事件监听者，把添加事件到事件队列 */
				Iterator iterator = listenerList.iterator();
				while (iterator.hasNext()) {
					EventDiscoveryListener listener = (EventDiscoveryListener) iterator.next();
					if (listener != null) {
						listener.discoveryEvent(EventFactory.create(sonEventItem.getProcess(), intFatherEventID, (String) processEventMap.get("EventRepresentation")));
					}
				}
			}
			//更新条件表达式的值
			sql = "UPDATE ProcessEvent SET ExpressionforParse=? WHERE ProcessID=? AND EventID=?";
			params = new Object[] {strExpressionforParse[0], sonEventItem.getProcess().getProcessID(), new Integer(intFatherEventID)};
			types = new int[] {Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
			query.update(conn, sql, params);
		}				  
	}
	
	/**
	 * 作用: 事件表达式的重写与计算
	 * 假设:无
	 * 效果:无
	 * 输入:
	 * 	strExpressionforParse: 事件表达式
	 * 	strEvent: 发生的事件
	 * 返回:事件表达式真为True,反之亦然
	 * 算法:	 After the event(E1) has happened, the ExpressionforParse should be rewrite according to the following list:
	 * 			1.(E1 AND E2)/ E1=E2
	 * 			2.(E1 OR E2)/ E1=True
	 * 			3.(E1 PRE E2)/ E1=E2
	 * 			4.(E2 PRE E1)/ E1=False
	 * 			5.REP E1 n/E1=REP E1 (n-1)
	 * 			6.ANY(m，E1，E2，…，En)/E1 = ANY(m-1，E2，…，En)
	 * 			7.NOT E1/E1=false
	 */
	private boolean rewriteAndEvalExpression(String[] strExpressionforParse, String strEvent) {
		
		//事件表达式的值
		boolean bValue = false;

		//表达式是否是中缀形式，如果是按下面方式存储
		boolean bMidFormat = false;
		
		String[] strLeft = new String[1];
		String[] strOperator = new String[1];
		String[] strRight = new String[1];
		//左边子表达式的值
		boolean bLeftValue = false;
		//右边子表达式的值
		boolean bRightValue = false;
    
		//表达式是否是前缀形式，去除前缀的子表达式存储在strRight
		boolean bPreFormat = false;
	
		strExpressionforParse[0].trim();   
		if( strExpressionforParse[0].equals("0") ) {
			bValue = false;
		} else if (strExpressionforParse[0].equals("1") ) {
			bValue = true;
		} else {
			bMidFormat = expressionToMidFormat(strExpressionforParse, strLeft, strOperator, strRight);
    
			//表达式是否是中缀形式，如果是按下面方式存储
			if( bMidFormat ) {
				bLeftValue = evalAtomicExpr(strLeft, strEvent);
				bRightValue = rewriteAndEvalExpression(strRight, strEvent);
				bValue = rewriteAndEvalMidFormat(bLeftValue, strOperator, bRightValue, strExpressionforParse, strLeft, strRight);			
			} else {
				bPreFormat = expressionToPreFormat(strExpressionforParse, strOperator, strRight);
				//如果表达式是前缀方式
				if( bPreFormat ) {

				}
				else {             //表达式只包含原子事件，如EndOf(1)
					bValue = evalAtomicExpr(strExpressionforParse, strEvent);
				}
			}        
		}
		return bValue;
	}
	
	/**
	 * 作用: 表达式到中缀形式
	 * 假设:无
	 * 效果:无
	 * 输入:
	 * 	strExpression: 事件表达式
	 *  strLeft:中缀左操作数
	 * 	strOperator:中缀操作符
	 *  strRight:中缀右操作数
	 * 返回:事件表达式真为True,反之亦然
	 * 算法:	 
	 * 			If the expression has the mid operator such as "AND", Divide it into three parts and return true
	 * 			else return false
	 * 注意：该函数有问题，中缀情况复杂，涉及到操作符优先级
	 */
	private boolean expressionToMidFormat(String[] strExpression, String[] strLeft, String[] strOperator, String[] strRight) {
		
		//是否式中缀表达式
		boolean bMidFormat = false;
		//记录中缀操作符的位置
		int intOperatorPos = -1;

		int intOperatorIndex = -1;    
    
		//first divide the expression according to the mid operators
		intOperatorPos = -1;	
		for(int i = 0; i < Constants.MID_OP_NUMBER; i++ ) {
			intOperatorPos = strExpression[0].indexOf(Constants.aryMidOperators[i]);
			if( intOperatorPos > 0 ) {			
				intOperatorIndex = i;
				break;
			}
		}
    
		if( intOperatorIndex != -1 )
		{
			bMidFormat = true;        
			strLeft[0] = strExpression[0].substring(0, intOperatorPos - 1);
			strOperator[0] = Constants.aryMidOperators[intOperatorIndex];   //字符串操作有待检查---??????---
			strRight[0] = strExpression[0].substring(intOperatorPos + strOperator[0].length()); 
		}
		return bMidFormat;
	}
	
	/**
	 * 作用:计算原子事件，如EndOf(1)
	 * 假设:无
	 * 效果:无
	 * 输入:
	 * 	strExpression: 事件表达式
	 *  strEvent:事件表达式
	 * 返回:计算原子事件发生为True,反之亦然
	 * 算法:	alcuate the atomic expression which havn't any operators, eg.Endof(1) 
	 */
	private boolean evalAtomicExpr(String[] strExpression, String strEvent) {
		
		boolean bValue = false;

		if( strExpression[0].indexOf( strEvent ) != -1 ) {
			bValue = true;
		}
		return bValue;
	}
	
	/**
	 * 作用: 计算并重写中缀表达式
	 * 假设:无
	 * 效果:无
	 * 输入:
	 * 	strExpression: 事件表达式
	 *  strLeft:中缀左操作数
	 * 	strOperator:中缀操作符
	 *  strRight:中缀右操作数
	 * 返回:事件表达式真为True,反之亦然
	 * 算法:	 
	 *      calcuate the mid-format expression and get the left expression if the expression value is false
	 * 		eg. E1 AND E2 if E1 has happened and E2 not, then the expression is E2 and the return value is false
	 * 		if value is true, just return true
	 */
	private boolean rewriteAndEvalMidFormat(boolean bLeft, String[] strOperator, boolean bRight, String[] strExpression, String[] strLeft, String[] strRight) {
		
		boolean bValue = false;    
    
		//如果是AND表达式
		if( strOperator[0].equals("AND") ) {
			//计算值
			bValue = bLeft && bRight;
			//如果值不为真
			if( !bValue ) {
				//如果是左边为真，表达式简化为右边子表达式
				if( bLeft ) {
					strExpression[0] = strRight[0];
				} else if( bRight ) {
					strExpression[0] = strLeft[0];
				}
			}
		}
		//如果是OR表达式
		else if( strOperator[0].equals("OR") ) {
			bValue = bLeft || bRight;
			if( bValue ) {
				strExpression[0] = " 1 ";
			}
		} else if( strOperator[0].equals("PRE") ) {
			//
		}
		return bValue;
	}
	
	/**
	 * 作用:表达式到前缀形式
	 * 假设:无
	 * 效果:无
	 * 输入:
	 * 	strExpression: 事件表达式
	 * 	strOperator:中缀操作符
	 *  strRight:中缀右操作数
	 * 返回:事件表达式真为True,反之亦然
	 * 算法:	 
	 * 		If the expression has the pre operator such as "ANY", Divide it into two parts and return true
	 * 		else return false
	 */
	private boolean expressionToPreFormat(String[] strExpression, String[] strOperator, String[] strRight) {
		return false;
	}
	
	public void addEventDiscoveryListener(EventDiscoveryListener listener) {
		listenerList.add(listener);
	}
	
	public boolean removeEventDiscoveryListener(EventDiscoveryListener listener) {
		return listenerList.remove(listener);
	}
	
	public void clearEventDiscoveryListener() {
		listenerList.clear();
	}

}
