/*
 * Created on 2004-3-13
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package cit.workflow.elements;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.log4j.Logger;

import cit.workflow.elements.factories.ElementFactory;
import cit.workflow.elements.variables.ProcessObject;
import cit.workflow.utils.DBUtility;

/**
 * @author weiwei
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Expression extends DBUtility {

	private static Logger logger = Logger.getLogger(Expression.class);

	private String processID;

	private int conditionID;

	public Expression(Connection conn, String processID) {
		super(conn);
		this.processID = processID;
	}

	public Expression(Connection conn, String processID, int conditionID) {
		super(conn);
		this.processID = processID;
		this.conditionID = conditionID;
	}

	/**
	 * 功能：分析表达式的值为真还是为假
	 * 
	 * @return
	 * 
	 * 步骤： 1.查找出该条件的表达式 2.把表达式中的变量替换为其值 3.把表达式里的boolean值True替换为1,False替换为0
	 * 4.计算替换后的表达式的值,这时候的表达式不包含变量和True,False
	 * 
	 * 例子： ({1} + {2} = 300 AND {3} = True) OR {4} > 'EE' 经过第一步后变为： (100 + 150 =
	 * 250 AND False = True) OR 'FE' > 'EE' 经过第二步后变为： (100 + 150 = 250 AND 0 =
	 * 1) OR 'FE' > 'EE' 计算后表达式为：true
	 */
	public boolean analyzeBoolExpression() throws Exception {

		boolean bConditionResult = false;

		sql = "SELECT * FROM ProcessCondition WHERE ProcessID=? AND ConditionID=?";
		params = new Object[] { this.processID, new Integer(this.conditionID) };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		Map processConditionMap = (Map) executeQuery(new MapHandler());
		if (processConditionMap != null) {
			// 把表达式里的变量替换为它的值，返回新的表达式
			String condition = replaceVariablesForCondition((String) processConditionMap
					.get("ConditionRepresentation"));

			// 把表达式里的boolean值True替换为1,False替换为0
			condition = replaceBoolean(condition);

			bConditionResult = computeBoolExpression(condition);

		}
		return bConditionResult;
	}

	private String replaceVariablesForCondition(String expression)
			throws Exception {
		StringBuffer conditionForHandle = new StringBuffer(expression);
		int startPos = conditionForHandle.indexOf("{");
		int endPos = conditionForHandle.indexOf("}");
		while (startPos != -1) {
			int objectID = Integer.parseInt(conditionForHandle.substring(
					startPos + 1, endPos));
			ProcessObject decisionObject = ElementFactory.createProcessObject(
					conn, this.processID, objectID);
			// ProcessObject decisionObject = new ProcessObject(conn,
			// this.processID, objectID);
			String objectValue = "'" + (String) decisionObject.getValue() + "'";

			conditionForHandle = conditionForHandle
					.delete(startPos, endPos + 1);
			conditionForHandle = conditionForHandle.insert(startPos,
					objectValue);

			startPos = conditionForHandle.indexOf("{");
			endPos = conditionForHandle.indexOf("}");
		}
		return conditionForHandle.toString();
	}

	public String replaceVariables(String expression) throws Exception {
		StringBuffer conditionForHandle = new StringBuffer(expression);
		int startPos = conditionForHandle.indexOf("{");
		int endPos = conditionForHandle.indexOf("}");
		while (startPos != -1) {
			int objectID = Integer.parseInt(conditionForHandle.substring(
					startPos + 1, endPos));
			// String objectValue = getObjectValue(this.processID, objectID);
			ProcessObject decisionObject = ElementFactory.createProcessObject(
					conn, this.processID, objectID);
			// ProcessObject decisionObject = new ProcessObject(conn,
			// this.processID, objectID);
			String objectValue = (String) decisionObject.getValue();

			conditionForHandle = conditionForHandle
					.delete(startPos, endPos + 1);
			conditionForHandle = conditionForHandle.insert(startPos,
					objectValue);

			startPos = conditionForHandle.indexOf("{");
			endPos = conditionForHandle.indexOf("}");
		}
		return conditionForHandle.toString();
	}

	private String replaceBoolean(String expression) {
		expression = expression.replaceAll("True", "1");
		expression = expression.replaceAll("False", "0");
		expression = expression.replaceAll("true", "1");
		expression = expression.replaceAll("false", "0");
		return expression;
	}

	private boolean computeBoolExpression(String expression)
			throws SQLException {
		/*sql = "SELECT ProcessID FROM ProcessCondition WHERE " + expression;
		params = new Object[] {};
		types = new int[] {};
		Map processConditionMap = (Map) executeQuery(new MapHandler());
		return processConditionMap == null ? false : true;*/
		StringBuffer leftStr;
		String rightStr;
		int index;
		index = expression.indexOf('=');
		leftStr = new StringBuffer(expression.substring(0, index));
		rightStr = expression.substring(index + 1);
		int startPos = leftStr.indexOf("'");
		leftStr.deleteCharAt(startPos);
		int endPos = leftStr.indexOf("'");
		leftStr.deleteCharAt(endPos);
		return leftStr.toString().equals(rightStr)?true : false;
	}

	
	// sxh add 2007.11

	public double computeMathExpression(String s) {
		LinkedList<Token> oper = new LinkedList<Token>();
		oper.addFirst(new Token('#', -1));
		LinkedList<Double> num = new LinkedList<Double>();
		String t = "";
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '(') {
				if (t.equals("") != true) {
					num.addFirst(new Double(t));
					t = "";
				}
				oper.addFirst(new Token('(', 0));
			}
			if (s.charAt(i) == ')') {
				if (t.equals("") != true) {
					num.addFirst(new Double(t));
					t = "";
				}
				while (true) {
					Token cur = oper.removeFirst();
					if (cur.c == '(')
						break;
					double d2 = num.removeFirst();
					double d1 = num.removeFirst();
					if (cur.c == '+')
						num.addFirst(d1 + d2);
					if (cur.c == '-')
						num.addFirst(d1 - d2);
					if (cur.c == '*')
						num.addFirst(d1 * d2);
					if (cur.c == '/') {
						if (d2 == 0) {
							//servercomment System.out.println("除数为0");
							System.exit(1);
						}
						num.addFirst(d1 / d2);
					}
				}
			}
			if (s.charAt(i) == '+') {
				if (t.equals("") != true) {
					num.addFirst(new Double(t));
					t = "";
				}
				Token tnew = new Token('+', 1);
				while (true) {
					Token cur = oper.removeFirst();
					if (tnew.level > cur.level) {
						oper.addFirst(cur);
						oper.addFirst(tnew);
						break;
					} else {
						double d2 = num.removeFirst();
						double d1 = num.removeFirst();
						if (cur.c == '+')
							num.addFirst(d1 + d2);
						if (cur.c == '-')
							num.addFirst(d1 - d2);
						if (cur.c == '*')
							num.addFirst(d1 * d2);
						if (cur.c == '/') {
							if (d2 == 0) {
								//servercomment System.out.println("除数为0");
								System.exit(1);
							}
							num.addFirst(d1 / d2);
						}
					}
				}
			}
			if (s.charAt(i) == '-') {
				if (t.equals("") != true) {
					num.addFirst(new Double(t));
					t = "";
				}
				Token tnew = new Token('-', 1);
				while (true) {
					Token cur = oper.removeFirst();
					if (tnew.level > cur.level) {
						oper.addFirst(cur);
						oper.addFirst(tnew);
						break;
					} else {
						double d2 = num.removeFirst();
						double d1 = num.removeFirst();
						if (cur.c == '+')
							num.addFirst(d1 + d2);
						if (cur.c == '-')
							num.addFirst(d1 - d2);
						if (cur.c == '*')
							num.addFirst(d1 * d2);
						if (cur.c == '/') {
							if (d2 == 0) {
								//servercomment System.out.println("除数为0");
								System.exit(1);
							}
							num.addFirst(d1 / d2);
						}
					}
				}

			}
			if (s.charAt(i) == '*') {
				if (t.equals("") != true) {
					num.addFirst(new Double(t));
					t = "";
				}
				Token tnew = new Token('*', 2);
				while (true) {
					Token cur = oper.removeFirst();
					if (tnew.level > cur.level) {
						oper.addFirst(cur);
						oper.addFirst(tnew);
						break;
					} else {
						double d2 = num.removeFirst();
						double d1 = num.removeFirst();
						if (cur.c == '+')
							num.addFirst(d1 + d2);
						if (cur.c == '-')
							num.addFirst(d1 - d2);
						if (cur.c == '*')
							num.addFirst(d1 * d2);
						if (cur.c == '/') {
							if (d2 == 0) {
								//servercomment System.out.println("除数为0");
								System.exit(1);
							}
							num.addFirst(d1 / d2);
						}
					}
				}

			}
			if (s.charAt(i) == '/') {
				if (t.equals("") != true) {
					num.addFirst(new Double(t));
					t = "";
				}
				Token tnew = new Token('/', 2);
				while (true) {
					Token cur = oper.removeFirst();
					if (tnew.level > cur.level) {
						oper.addFirst(cur);
						oper.addFirst(tnew);
						break;
					} else {
						double d2 = num.removeFirst();
						double d1 = num.removeFirst();
						if (cur.c == '+')
							num.addFirst(d1 + d2);
						if (cur.c == '-')
							num.addFirst(d1 - d2);
						if (cur.c == '*')
							num.addFirst(d1 * d2);
						if (cur.c == '/') {
							if (d2 == 0) {
								//servercomment System.out.println("除数为0");
								System.exit(1);
							}
							num.addFirst(d1 / d2);
						}
					}
				}

			}
			if (s.charAt(i) >= '0' && s.charAt(i) <= '9' || s.charAt(i) == '.')
				t += s.charAt(i);

		}
		if (t.equals("") != true)
			num.addFirst(new Double(t));
		while (oper.size() > 1) {
			Token cur = oper.removeFirst();
			double d2 = num.removeFirst();
			double d1 = num.removeFirst();
			if (cur.c == '+') {
				num.addFirst(d1 + d2);
			}
			if (cur.c == '-')
				num.addFirst(d1 - d2);
			if (cur.c == '*')
				num.addFirst(d1 * d2);
			if (cur.c == '/') {
				if (d2 == 0) {
					//servercomment System.out.println("除数为0");
					System.exit(1);
				}
				num.addFirst(d1 / d2);
			}

		}

		return num.getFirst();

	}

	class Token {
		public char c;

		public int level;// 运算优先级 ：(:0 +:1 -:1 *:2 /:2 ):3

		public Token(char c, int level) {
			this.c = c;
			this.level = level;
		}

		public String toString() {
			return "" + c + "   " + level;
		}
	}
	// sxh add 2007.11 end
}