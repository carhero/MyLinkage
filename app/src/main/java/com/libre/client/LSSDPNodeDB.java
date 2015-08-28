package com.libre.client;
/*********************************************************************************************
 * 
 * Copyright (C) 2014 Libre Wireless Technology
 *
 * "Junk Yard Lab" Project
 * 
 * Libre Sync Android App
 * Author: Subhajeet Roy
 *  
***********************************************************************************************/
import java.util.ArrayList;

public class LSSDPNodeDB {

	   private static LSSDPNodeDB LSSDPDB = new LSSDPNodeDB( );
	   private static ArrayList <LSSDPNodes> nodelist = new ArrayList<LSSDPNodes>();
	   /* A private Constructor prevents any other
		* class from instantiating.
		*/
	   private LSSDPNodeDB(){ }

	   /* Static 'instance' method */
	   public static LSSDPNodeDB getInstance( ) {

		   return LSSDPDB;

	   }
	   public  ArrayList<LSSDPNodes> GetDB( ) {
		 return nodelist;
	   }
	   protected  boolean AddtoDB(LSSDPNodes node) {
			 return nodelist.add(node);
	   }
}

