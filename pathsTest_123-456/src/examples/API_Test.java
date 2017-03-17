package examples;

import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.RuleApplication;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.RuleApplicationImpl;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

public class API_Test {
	public static final String staticPathDirectoryWRONG1 = "files/bAAAnk";

	public static final String staticPathDirectory = "files/bank";
	public static final String staticPathPart1 = "files/";
	public static final String staticPathPart2 = "bank"; 
	public static final String staticNameModule = "bank.henshin";
	public static final String staticNameInstance = "example-bank.xmi";
	public static final String staticCreateAccount = "createAccount";
	public static final String staticClient = "client";
	public static final int staticFive = 5; 
	public static final String staticNewAccount = "newAccount";

	
	
	public static final String staticPathDirectoryWRONG = "files/bAAAnk";
	public static final String staticPathPart1WRONG = "fiLLLes/";
	public static final String staticPathPart2WRONG = "bAAAnk";
	public static final String staticNameModuleWRONG1 = "bAAAnk.henshin";
	public static final String staticNameModuleWRONG2 = "bank.hEEEnshin";
	public static final String staticNameInstanceWRONG1 = "exAAAAmple-bank.xmi";
	public static final String staticNameInstanceWRONG2 = "example-bank.xMMMi";
	public static final String staticCreateAccountWRONG = "createAccOOOunt";
	public static final String staticClientWRONG = "cliEEEEnt";
	public static final String staticFiveWRONG = "five";
	public static final String staticNewAccountWrong = "newAccOOOOunt"; 
	
	// local before global check
	String localPathDirectory = "files/bAAAAnk";
	String localPathPart1 = "filEEEEEs/";
	String localPathPart2 = "bAAAAnk";
	String localNameModule = "bAAAAAnk.henshin";
	String localNameInstance = "exAAAAAAmple-bank.xmi";
	String localCreateAccount = "createAccOOOOOunt";
	String localClient = "cliEEEEEnt";
	String localNewAccount = "newAccOOOOunt";
	
	
	
	public static void run(String path) {
		
		
		String localPathDirectory = "files/bank";
		String localPathPart1 = "files/";
		String localPathPart2 = "bank";
		String localNameModule = "bank.henshin";
		String localNameInstance = "example-bank.xmi";
		String localCreateAccount = "createAccount";
		String localClient = "client";
		int localFive = 5;
		String localNewAccount = "newAccount";
		
		String localPathDirectoryWRONG = "files/bAAAnk";
		String localPathPart1WRONG = "fiLLLes/";
		String localPathPart2WRONG = "bAAAnk";
		String localNameModuleWRONG1 = "bAAAnk.henshin";
		String localNameModuleWRONG2 = "bank.hEEEnshin";
		String localNameInstanceWRONG1 = "exAAAAmple-bank.xmi";
		String localNameInstanceWRONG2 = "example-bank.xMMMi";
		String localCreateAccountWRONG = "createAccOOOunt";
		String localClientWRONG = "cliEEEEnt";
		String localFiveWRONG = "five";
		String localNewAccountWrong = "newAccOOOOunt";
 
		HenshinResourceSet resourceSetDirectPathDirectory = new HenshinResourceSet("files/bank");
		
		HenshinResourceSet resourceSetDirectPathParts = new HenshinResourceSet("files/" +"bank");
		// should have an error marker!
		HenshinResourceSet resourceSetDirectPathDirectoryWRONG = new HenshinResourceSet("files/bAAAAnk"); 
		// should have an error marker!
		HenshinResourceSet resourceSetDirectPathPartsWRONG1 = new HenshinResourceSet("fiLLLes/"  + "bank");
		// should have an error marker!
		HenshinResourceSet resourceSetDirectPathPartsWRONG2 = new HenshinResourceSet("files/"+"bAAAnk");
		

		HenshinResourceSet resourceSetStaticPathDirectory = new HenshinResourceSet(staticPathDirectory);
		HenshinResourceSet resourceSetStaticPathParts = new HenshinResourceSet(staticPathPart1+staticPathPart2);
		// should have an error marker!
		HenshinResourceSet resourceSetStaticPathDirectoryWRONG = new HenshinResourceSet(staticPathDirectoryWRONG);
		// should have an error marker!
		HenshinResourceSet resourceSetStaticPathPartsWRONG1 = new HenshinResourceSet(staticPathPart1WRONG+staticPathPart2);
		// should have an error marker!
		HenshinResourceSet resourceSetStaticPathPartsWRONG2 = new HenshinResourceSet(staticPathPart1+staticPathPart2WRONG);
		
		
		HenshinResourceSet resourceSetLocalPathDirectory = new HenshinResourceSet(localPathDirectory);
		HenshinResourceSet resourceSetLocalPathParts = new HenshinResourceSet(localPathPart1+localPathPart2);
		// should have an error marker!
		HenshinResourceSet resourceSetLocalPathDirectoryWRONG = new HenshinResourceSet(localPathDirectoryWRONG);
		// should have an error marker!
		HenshinResourceSet resourceSetLocalPathPartsWRONG1 = new HenshinResourceSet(localPathPart1WRONG+localPathPart2);
		// should have an error marker!
		HenshinResourceSet resourceSetLocalPathPartsWRONG2 = new HenshinResourceSet(localPathPart1+localPathPart2WRONG); 
		
		
		
		
		  
		 
		// Load the module: 
		Module moduleStaticPathDirectory = resourceSetStaticPathDirectory.getModule(staticNameModule, false);
		Module moduleStaticPathParts = resourceSetStaticPathParts.getModule(staticNameModule, false);
		// should have an error marker!
		Module moduleStaticPathDirectoryWRONG1 = resourceSetStaticPathDirectory.getModule(staticNameModuleWRONG1, false);
		// should have an error marker!
		Module moduleStaticPathPartsWRONG1 = resourceSetStaticPathParts.getModule(staticNameModuleWRONG1, false);
		// should have an error marker!
		Module moduleStaticPathDirectoryWRONG2 = resourceSetStaticPathDirectory.getModule(staticNameModuleWRONG2, false);
		// should have an error marker!
		Module moduleStaticPathPartsWRONG2 = resourceSetStaticPathParts.getModule(staticNameModuleWRONG2, false);

		Module moduleLocalPathDirectory = resourceSetLocalPathDirectory.getModule(localNameModule, false);
		Module moduleLocalPathParts = resourceSetLocalPathParts.getModule(localNameModule, false);
		// should have an error marker!
		Module moduleLocalPathDirectoryWRONG1 = resourceSetLocalPathDirectory.getModule(localNameModuleWRONG1, false);
		// should have an error marker!
		Module moduleLocalPathPartsWRONG1 = resourceSetLocalPathParts.getModule(localNameModuleWRONG1, false);
		// should have an error marker!
		Module moduleLocalPathDirectoryWRONG2 = resourceSetLocalPathDirectory.getModule(localNameModuleWRONG2, false);
		// should have an error marker!
		Module moduleLocalPathPartsWRONG2 = resourceSetLocalPathParts.getModule(localNameModuleWRONG2, false);
		
		Module moduleDirectPathDirectory = resourceSetDirectPathDirectory.getModule("bank.henshin", false);
		
		Module moduleDirectPathParts = resourceSetDirectPathParts.getModule("bank.henshin", false);
		// should have an error marker!
		Module moduleDirectPathDirectoryWRONG1 = resourceSetDirectPathDirectory.getModule("bAAAnk.henshin", false);
		// should have an error marker!
		Module moduleDirectPathPartsWRONG1 = resourceSetDirectPathParts.getModule("bAAAnk.henshin", false);
		// should have an error marker!
		Module moduleDirectPathDirectoryWRONG2 = resourceSetDirectPathDirectory.getModule("bank.hEEEnshin", false);
		// should have an error marker!
		Module moduleDirectPathPartsWRONG2 = resourceSetDirectPathParts.getModule("bank.hEEEnshin", false);
		
 
		// Load the example model into an EGraph:
		EGraph graphStaticPathDirectory = new EGraphImpl(resourceSetStaticPathDirectory.getResource(staticNameInstance));
		// should have an error marker!
		EGraph graphStaticPathDirectoryWRONG1 = new EGraphImpl(resourceSetStaticPathDirectory.getResource(staticNameInstanceWRONG1)); 
		// should have an error marker!
		EGraph graphStaticPathDirectoryWRONG2 = new EGraphImpl(resourceSetStaticPathDirectory.getResource(staticNameInstanceWRONG2));
		
		EGraph graphLocalPathDirectory = new EGraphImpl(resourceSetLocalPathDirectory.getResource(localNameInstance));
		// should have an error marker!
		EGraph graphLocalPathDirectoryWRONG1 = new EGraphImpl(resourceSetLocalPathDirectory.getResource(localNameInstanceWRONG1));
		// should have an error marker!
		EGraph graphLocalPathDirectoryWRONG2 = new EGraphImpl(resourceSetLocalPathDirectory.getResource(localNameInstanceWRONG2));
		
		EGraph graphDirectPathDirectory = new EGraphImpl(resourceSetDirectPathDirectory.getResource("example-bank" + ".xmi"));
		// should have an error marker!
		EGraph graphDirectPathDirectoryWRONG1 = new EGraphImpl(resourceSetDirectPathDirectory.getResource("exAAAAmple-bank.xmi"));
		// should have an error marker!
		EGraph graphDirectPathDirectoryWRONG2 = new EGraphImpl(resourceSetDirectPathDirectory.getResource("example-bank.xMMMi"));
		
		
		
		// Create an engine and a rule application:
		Engine engine = new EngineImpl();
		
		RuleApplication staticRuleApp = new RuleApplicationImpl(engine);
		staticRuleApp.setEGraph(graphStaticPathDirectory);
		
		Unit staticCreateAccountUnit = moduleStaticPathDirectory.getUnit(staticCreateAccount);
		staticRuleApp.setUnit(staticCreateAccountUnit); 
		staticRuleApp.setParameterValue(staticClient, "Alice");  
		
		// should have an error marker! 
		staticRuleApp.setParameterValue(staticClientWRONG, "Alice");
		staticRuleApp.setParameterValue("accountId", staticFive);
		// should have an error marker!
		staticRuleApp.setParameterValue("accountId", staticFiveWRONG);
		staticRuleApp.getResultParameterValue(staticNewAccount);
		// should have an error marker!
		staticRuleApp.getResultParameterValue(staticNewAccountWrong);
		// should have an error marker!
		Unit staticCreateAccountUnitWRONG = moduleStaticPathDirectory.getUnit(staticCreateAccountWRONG);
		
		RuleApplication localRuleApp = new RuleApplicationImpl(engine);
		localRuleApp.setEGraph(graphLocalPathDirectory);
		Unit localCreateAccountUnit = moduleLocalPathDirectory.getUnit(localCreateAccount);
		localRuleApp.setUnit(staticCreateAccountUnit);		
		localRuleApp.setParameterValue(localClient, "Alice");
		// should have an error marker!
		localRuleApp.setParameterValue(localClientWRONG, "Alice");
		localRuleApp.setParameterValue("accountId", localFive);
		// should have an error marker!
		localRuleApp.setParameterValue("accountId", localFiveWRONG);
		localRuleApp.getResultParameterValue(localNewAccount);
		// should have an error marker!
		localRuleApp.getResultParameterValue(localNewAccountWrong);
		// should have an error marker!
		Unit localCreateAccountUnitWRONG = moduleLocalPathDirectory.getUnit(localCreateAccountWRONG);
		
		
		RuleApplication directRuleApp = new RuleApplicationImpl(engine);
		directRuleApp.setEGraph(graphDirectPathDirectory);
		Unit directCreateAccountUnit = moduleDirectPathDirectory.getUnit("createAccount"); 
		directRuleApp.setUnit(staticCreateAccountUnit);		
		directRuleApp.setParameterValue("client", "Alice");
		// should have an error marker!
		directRuleApp.setParameterValue("cliEEEEnt", "Alice");
		directRuleApp.setParameterValue("accountId", 5);
		// should have an error marker!
		directRuleApp.setParameterValue("accountId", "five"); 
		directRuleApp.getResultParameterValue("newAccount");
		// should have an error marker!
		directRuleApp.getResultParameterValue("newAccOOOunt");
		// should have an error marker!
		Unit directCreateAccountUnitWRONG = moduleDirectPathDirectory.getUnit("createAccOOOunt");
		
	}
}
