/**
 * <copyright>
 * Copyright (c) 2010-2014 Henshin developers. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 which 
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 */
package org.eclipse.emf.henshin.examples.bank;

import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

/**
 * Bank example for the Henshin interpreter. Shows the usage of the interpreter.
 * 
 * @author Christian Krause
 */
public class BankExample {

	/** 
	 * Relative path to the bank model files.
	 */
	
	/**
	 * Run the bank example.
	 * @param path Relative path to the model files.
	 * @param saveResult Whether the result should be saved.
	 */
	final static String PATH = "src/org/eclipse/emf/henshin/examples/bank"; 
	
	public static void run(String path, boolean saveResult) {
		path = "src/org/eclipse/emf/henshin/examples/bank2"; 
		// Create a resource set with a base directory:
		HenshinResourceSet r2 = new HenshinResourceSet(path); 
		HenshinResourceSet r1= new HenshinResourceSet("src/org/eclipse/emf/henshin/examples/bank"); 
		
		// Load the module:
		String re = "bank.henshin";
		Module m1 = r2.getModule("bank.henshins", false);
		
		
		Module m2 = r1.getModule(re, false); 

		// Load the example model into an EGraph:
		String s= "example-bank.xmi";
		EGraph g1 = new EGraphImpl(r1.getResource(s)); 
		EGraph g2 = new EGraphImpl(r1.getResource("example-bank.xmi")); 
		
		// Create an engine and a rule application: 
		Engine engine = new EngineImpl();
		UnitApplication createAccountApp = new UnitApplicationImpl(engine); 
		createAccountApp.setEGraph(g1);
		UnitApplication createAccountApp2 = new UnitApplicationImpl(engine); 
		createAccountApp2.setEGraph(g2);
	
		// Creating a new account for Alice...
		String client = "clients";
		createAccountApp2.setUnit(m2.getUnit("createAccount"));
		createAccountApp2.setParameterValue(client, "Alice");
		createAccountApp.setUnit(m1.getUnit("createAccount"));
		createAccountApp.setParameterValue("client", "Alice");
		createAccountApp.setParameterValue("clients", "Alice");
		createAccountApp.setParameterValue("accountId", 5);
		createAccountApp.setParameterValue("accountId", "5");
		if (!createAccountApp.execute(null)) {
			throw new RuntimeException("Error creating account for Alice");
		}
		createAccountApp.getResultParameterValue("client");   
		createAccountApp.getResultParameterValue("accountId");
		
		UnitApplication transferMoneyApp = new UnitApplicationImpl(engine);
		transferMoneyApp.setEGraph(g1);
		// Transferring some money:
		transferMoneyApp.setUnit(m1.getUnit("transferMoney"));
		transferMoneyApp.setParameterValue("client", "Alice");
		transferMoneyApp.setParameterValue("fromId", 1);
		transferMoneyApp.setParameterValue("toId", 2);
		transferMoneyApp.setParameterValue("amount", 50.0d); // double
		if (!transferMoneyApp.execute(null)) { // parameters x and y will be matched by the engine
			throw new RuntimeException("Error transferring money");
		}
		
		// Deleting all accounts of Charles:
		UnitApplication deleteAccountsApp = new UnitApplicationImpl(engine);
		deleteAccountsApp.setEGraph(g1); 
		deleteAccountsApp.setUnit(m1.getUnit("deleteAllAccounts"));
		deleteAccountsApp.setParameterValue("client", "Charles");
		if (!deleteAccountsApp.execute(null)) {
			throw new RuntimeException("Error deleting Charles' accounts");
		}
		
		// Saving the result:
		if (saveResult) {
			r1.saveEObject(g1.getRoots().get(0), "example-result.xmi");
		}
	}
	
	public static void main(String[] args) {
		run(PATH, true); // we assume the working directory is the root of the examples plug-in
	}
	
}
