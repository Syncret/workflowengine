package cit.workflow.engine.droolsruleengine;

// lrj add begin 07-12-7
public class FactsClass
{
	FactsClass(int input_num){
		this.int_instance = input_num;
		this.input_instance_class = this.INTEGER_CLASS;
	}
	
	FactsClass(float input_float){
		this.float_instance = input_float;
		this.input_instance_class = this.FLOAT_CLASS;
	}
	
	FactsClass(double input_double){
		this.double_instance = input_double;
		this.input_instance_class = this.DOUBLE_CLASS;
	}
	
	FactsClass(String input_string){
		this.string_instance = input_string;
		this.input_instance_class = this.STRING_CLASS;
	}
	
	FactsClass(boolean input_boolean){
		this.boolean_instance = input_boolean;
		this.input_instance_class = this.BOOLEAN_CLASS;
	}
	
	public int getInput_instance_class(){
		return this.input_instance_class;
	}
	
	public int getIntegerValue(){
		return this.int_instance;
	}
	
	public float getFloatValue(){
		return this.float_instance;
	}
	
	public double getDoubleValue(){
		return this.double_instance;
	}
	
	public String getStringValue(){
		return this.string_instance;
	}
	
	public boolean getBooleanValue(){
		return this.boolean_instance;
	}
	
	public static final int INTEGER_CLASS = 1;
	public static final int FLOAT_CLASS = 2;
	public static final int DOUBLE_CLASS = 3;
	public static final int STRING_CLASS = 4;
	public static final int BOOLEAN_CLASS = 5; 
	public int input_instance_class;
	
	private int int_instance;
	private float float_instance;
	private double double_instance;
	private String string_instance;
	private boolean boolean_instance;
}

// lrj add end 07-12-7